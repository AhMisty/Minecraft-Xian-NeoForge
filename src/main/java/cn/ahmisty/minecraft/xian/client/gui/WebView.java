package cn.ahmisty.minecraft.xian.client.gui;

import cn.ahmisty.minecraft.xian.ffi.web.Abi;
import cn.ahmisty.minecraft.xian.ffi.web.View;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.foreign.Arena;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class WebView implements Renderable {
    private static final Marker LOGGERMARKER = MarkerFactory.getMarker("WebView");
    private static final Logger LOGGER = LoggerFactory.getLogger("仙");

    public final View view;
    public final int texture_id;
    public final Identifier texture_location;

    public int x;
    public int y;

    // Logical size in Minecraft GUI coordinates (scaled resolution).
    public int width;
    public int height;

    // Physical size in device pixels for the native Servo WebView + OpenGL texture.
    private int deviceWidth;
    private int deviceHeight;

    // Tracks the last GUI scale we applied to the native view (used as hidpi_scale_factor).
    private int lastGuiScale = -1;

    // Whether we asked the native Servo WebView to run in throttled (background) mode.
    private boolean throttled;

    // WebRender uses raw OpenGL calls and can touch multiple texture units. Minecraft tracks its own
    // texture bindings and may skip redundant binds, so we must restore *all* units we might disturb.
    private static int TEXTURE_UNITS_TO_BACKUP = -1;

    // Debug helpers (glGetError/glReadPixels) are extremely expensive and can stall the GPU.
    // Opt-in via JVM system property: -Dxian.web.debug_gl=true
    private static final boolean DEBUG_GL = Boolean.getBoolean("xian.web.debug_gl");

    private static int textureUnitsToBackup() {
        if (TEXTURE_UNITS_TO_BACKUP > 0) {
            return TEXTURE_UNITS_TO_BACKUP;
        }
        int max = 1;
        try {
            max = GL11.glGetInteger(0x8B4D /* GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS */);
        } catch (Throwable ignored) {
        }
        if (max <= 0) {
            max = 1;
        }
        TEXTURE_UNITS_TO_BACKUP = Math.min(max, 16);
        return TEXTURE_UNITS_TO_BACKUP;
    }

    private boolean loggedFirstPaint;
    // Debug: sample a pixel for a few paints so we can see whether content ever changes from the clear color.
    private int debugReadbackPaintsRemaining = DEBUG_GL ? 30 : 0;
    private int debugReadbackFbo;
    // Debug: track load state transitions reported by the native webview.
    private int debugLoadProbeFramesRemaining = DEBUG_GL ? 300 : 0;
    private int debugLastLoadStatus = Integer.MIN_VALUE;
    private String debugLastUrl;

    // Reuse NIO buffers/arrays to avoid per-frame allocations in render().
    private final IntBuffer tmpViewport = BufferUtils.createIntBuffer(4);
    private final IntBuffer tmpScissorBox = BufferUtils.createIntBuffer(4);
    private final ByteBuffer tmpColorMask = BufferUtils.createByteBuffer(4);
    private final ByteBuffer tmpDepthMask = BufferUtils.createByteBuffer(1);
    private final FloatBuffer tmpBlendColor = BufferUtils.createFloatBuffer(4);
    private final ByteBuffer tmpReadbackRgba = BufferUtils.createByteBuffer(4);
    private final int textureUnitsToBackupCount;
    private final int[] tmpTexture2dByUnit;

    public WebView(int x, int y, int width, int height, float hidpi_scale_factor, String initial_url) throws Throwable {
        RenderSystem.assertOnRenderThread();

        int logicalW = Math.max(1, width);
        int logicalH = Math.max(1, height);
        int guiScale = queryGuiScale();
        int deviceW = scaleToDevicePixels(logicalW, guiScale);
        int deviceH = scaleToDevicePixels(logicalH, guiScale);

        try (Arena arena = Arena.ofConfined()) {
            this.view = new View(
                    new View.Config(arena)
                            // Servo expects sizes in device pixels.
                            .set_width(deviceW)
                            .set_height(deviceH)
                            // Match Minecraft GUI scale so 1 CSS px ~= 1 GUI px, while rendering at higher device resolution.
                            .set_hidpi_scale_factor((float) guiScale)
                            // Important: set the initial URL here so native creates the WebView with
                            // the correct initial navigation (avoids the "LoadUrl ignored, stuck at about:blank" race).
                            .set_initial_url(initial_url)
            );
        }

        this.texture_id = this.view.texture_id();
        this.texture_location = Identifier.fromNamespaceAndPath("xian", "webview/" + this.texture_id);

        this.textureUnitsToBackupCount = textureUnitsToBackup();
        this.tmpTexture2dByUnit = new int[this.textureUnitsToBackupCount];

        this.x = x;
        this.y = y;
        this.width = logicalW;
        this.height = logicalH;
        this.deviceWidth = deviceW;
        this.deviceHeight = deviceH;
        this.lastGuiScale = guiScale;

        LOGGER.info(
                LOGGERMARKER,
                "Java WebView created: logical={}x{}, device={}x{}, gui_scale={} (texture_id={})",
                this.width,
                this.height,
                this.deviceWidth,
                this.deviceHeight,
                guiScale,
                this.texture_id
        );

        this.registerExternalTexture();
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setThrottled(boolean throttled) {
        RenderSystem.assertOnRenderThread();
        if (this.throttled == throttled) {
            return;
        }
        try {
            this.view.set_throttled(throttled);
            this.throttled = throttled;
            LOGGER.info(LOGGERMARKER, "Java WebView set throttled={} (texture_id={})", throttled, this.texture_id);
        } catch (Throwable t) {
            LOGGER.warn(LOGGERMARKER, "Failed to set throttled={} (texture_id={})", throttled, this.texture_id, t);
        }
    }

    public void resize(int width, int height) {
        RenderSystem.assertOnRenderThread();

        int logicalW = Math.max(1, width);
        int logicalH = Math.max(1, height);
        int guiScale = queryGuiScale();
        int deviceW = scaleToDevicePixels(logicalW, guiScale);
        int deviceH = scaleToDevicePixels(logicalH, guiScale);

        if (this.width == logicalW
                && this.height == logicalH
                && this.deviceWidth == deviceW
                && this.deviceHeight == deviceH
                && this.lastGuiScale == guiScale) {
            return;
        }

        try {
            if (this.lastGuiScale != guiScale) {
                // Keep CSS pixel size stable (logicalW/H) while rendering at guiScale times the device resolution.
                this.view.set_hidpi_scale_factor((float) guiScale);
            }
            if (this.deviceWidth != deviceW || this.deviceHeight != deviceH) {
                this.view.resize(deviceW, deviceH);
            }
        } catch (Throwable t) {
            LOGGER.error(LOGGERMARKER, "Failed to resize", t);
            return;
        }

        this.width = logicalW;
        this.height = logicalH;
        this.deviceWidth = deviceW;
        this.deviceHeight = deviceH;
        this.lastGuiScale = guiScale;

        LOGGER.info(
                LOGGERMARKER,
                "Java WebView resized: logical_requested={}x{}, logical_applied={}x{}, gui_scale={}, device_applied={}x{} (texture_id={})",
                width,
                height,
                this.width,
                this.height,
                guiScale,
                this.deviceWidth,
                this.deviceHeight,
                this.texture_id
        );

        this.registerExternalTexture();
    }

    public void destroy() {
        RenderSystem.assertOnRenderThread();

        try {
            Minecraft.getInstance().getTextureManager().release(this.texture_location);
        } catch (Throwable t) {
            LOGGER.error(LOGGERMARKER, "WebView texture release failed (texture_id={})", this.texture_id, t);
        }

        try {
            this.view.destroy();
        } catch (Throwable t) {
            LOGGER.error(LOGGERMARKER, "WebView view destroy failed (texture_id={})", this.texture_id, t);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.render(guiGraphics, this.x, this.y, this.width, this.height, mouseX, mouseY, partialTick);
    }

    public void render(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
        RenderSystem.assertOnRenderThread();

        // Keep the native view/texture sized to the actual draw area *and* current GUI scale.
        // This makes the page crisp when GUI scale > 1 by rendering at higher device resolution.
        this.resize(width, height);

        // Servo/WebRender uses raw OpenGL calls and may change GL state (notably framebuffer binding + viewport).
        // Minecraft caches some of that state; if we don't restore it, subsequent GUI drawing may silently render
        // into the wrong framebuffer.
        int prevDrawFbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int prevReadFbo = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        IntBuffer prevViewport = this.tmpViewport;
        prevViewport.clear();
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevViewport);
        boolean prevCullFaceEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean prevDepthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean prevStencilTestEnabled = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
        boolean prevScissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        IntBuffer prevScissorBox = this.tmpScissorBox;
        prevScissorBox.clear();
        GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, prevScissorBox);
        var prevColorMask = this.tmpColorMask;
        prevColorMask.clear();
        GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, prevColorMask);
        boolean prevColorMaskR = prevColorMask.get(0) != 0;
        boolean prevColorMaskG = prevColorMask.get(1) != 0;
        boolean prevColorMaskB = prevColorMask.get(2) != 0;
        boolean prevColorMaskA = prevColorMask.get(3) != 0;
        int prevProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int prevVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int prevArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        int prevElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        int prevActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        int unitsToBackup = this.textureUnitsToBackupCount;
        int[] prevTexture2dByUnit = this.tmpTexture2dByUnit;
        for (int i = 0; i < unitsToBackup; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            prevTexture2dByUnit[i] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        }
        GL13.glActiveTexture(prevActiveTexture);
        var prevDepthMaskBuf = this.tmpDepthMask;
        prevDepthMaskBuf.clear();
        GL11.glGetBooleanv(GL11.GL_DEPTH_WRITEMASK, prevDepthMaskBuf);
        boolean prevDepthMask = prevDepthMaskBuf.get(0) != 0;
        int prevStencilMask = GL11.glGetInteger(GL11.GL_STENCIL_WRITEMASK);
        int prevStencilBackMask = GL11.glGetInteger(0x8CA5 /* GL_STENCIL_BACK_WRITEMASK */);

        boolean prevBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        int prevBlendSrcRgb = GL11.glGetInteger(0x80C9 /* GL_BLEND_SRC_RGB */);
        int prevBlendDstRgb = GL11.glGetInteger(0x80C8 /* GL_BLEND_DST_RGB */);
        int prevBlendSrcAlpha = GL11.glGetInteger(0x80CB /* GL_BLEND_SRC_ALPHA */);
        int prevBlendDstAlpha = GL11.glGetInteger(0x80CA /* GL_BLEND_DST_ALPHA */);
        int prevBlendEqRgb = GL11.glGetInteger(0x8009 /* GL_BLEND_EQUATION_RGB */);
        int prevBlendEqAlpha = GL11.glGetInteger(0x883D /* GL_BLEND_EQUATION_ALPHA */);
        var prevBlendColor = this.tmpBlendColor;
        prevBlendColor.clear();
        // Some LWJGL variants used by Minecraft do not expose GL14.GL_BLEND_COLOR, so use the raw enum.
        GL11.glGetFloatv(0x8005 /* GL_BLEND_COLOR */, prevBlendColor);
        float prevBlendColorR = prevBlendColor.get(0);
        float prevBlendColorG = prevBlendColor.get(1);
        float prevBlendColorB = prevBlendColor.get(2);
        float prevBlendColorA = prevBlendColor.get(3);

        // Ensure WebRender isn't accidentally clipped by Minecraft GUI scissor state and can write alpha.
        // GUI shaders discard when alpha==0, so if alpha writes are masked off, the result will look transparent.
        // Also disable cull face: WebRender is a 2D renderer and should not rely on the host's culling state.
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL20.glStencilMaskSeparate(GL11.GL_FRONT_AND_BACK, 0xFF);
        // Some WebRender paths assume a sane viewport for the current render target.
        GL11.glViewport(0, 0, Math.max(1, this.deviceWidth), Math.max(1, this.deviceHeight));
        // If WebRender enables scissor without setting a new box (it caches state internally),
        // a stale Minecraft GUI scissor box (in screen coordinates) can clip everything away.
        GL11.glScissor(0, 0, Math.max(1, this.deviceWidth), Math.max(1, this.deviceHeight));

        // Debug: observe whether the native side considers the page "loading" at all.
        if (DEBUG_GL) {
            try {
                if (this.debugLoadProbeFramesRemaining > 0) {
                    this.debugLoadProbeFramesRemaining--;

                    int status = this.view.load_status();
                    if (status != this.debugLastLoadStatus) {
                        this.debugLastLoadStatus = status;
                        String name = switch (status) {
                            case Abi.XIAN_WEB_ENGINE_LOAD_STATUS_STARTED -> "Started";
                            case Abi.XIAN_WEB_ENGINE_LOAD_STATUS_HEAD_PARSED -> "HeadParsed";
                            case Abi.XIAN_WEB_ENGINE_LOAD_STATUS_COMPLETE -> "Complete";
                            case Abi.XIAN_WEB_ENGINE_LOAD_STATUS_INVALID -> "Invalid";
                            default -> "Unknown(" + status + ")";
                        };
                        LOGGER.info(LOGGERMARKER, "load_status={} ({}) (texture_id={})", status, name, this.texture_id);
                    }

                    String url = this.view.url();
                    if (url != null && !url.equals(this.debugLastUrl)) {
                        this.debugLastUrl = url;
                        LOGGER.info(LOGGERMARKER, "current_url={} (texture_id={})", url, this.texture_id);
                    }

                    if (status == Abi.XIAN_WEB_ENGINE_LOAD_STATUS_COMPLETE && this.debugLastUrl != null) {
                        this.debugLoadProbeFramesRemaining = 0;
                    }
                }
            } catch (Throwable t) {
                LOGGER.warn(LOGGERMARKER, "Failed to query load status/url (texture_id={})", this.texture_id, t);
            }
        }

        // Clear any pre-existing GL error so we can attribute errors to WebRender more confidently.
        if (DEBUG_GL) {
            while (GL11.glGetError() != GL11.GL_NO_ERROR) {
                // drain
            }
        }

        boolean painted = false;
        try {
            painted = this.view.paint();
            if (painted && this.debugReadbackPaintsRemaining > 0) {
                int packed = this.debugLogCenterPixel();
                this.debugReadbackPaintsRemaining--;
                // Stop early once the sampled pixel is no longer pure white (likely indicates page content drew).
                if ((packed & 0x00FFFFFF) != 0x00FFFFFF) {
                    this.debugReadbackPaintsRemaining = 0;
                }
            }

            if (DEBUG_GL) {
                // Log GL errors (often indicates state/attachment mismatches that make WebRender draw nothing).
                int err;
                boolean anyErr = false;
                while ((err = GL11.glGetError()) != GL11.GL_NO_ERROR) {
                    anyErr = true;
                    LOGGER.warn(LOGGERMARKER, "GL error after paint: 0x{} (texture_id={})", Integer.toHexString(err), this.texture_id);
                }
                if (anyErr) {
                    // Avoid spamming forever.
                    this.debugReadbackPaintsRemaining = 0;
                }
            }
        } catch (Throwable t) {
            LOGGER.error(LOGGERMARKER, "Failed to paint (texture_id={})", this.texture_id, t);
        } finally {
            // Restore critical GL state for Minecraft rendering.
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, prevDrawFbo);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevReadFbo);
            GL11.glViewport(prevViewport.get(0), prevViewport.get(1), prevViewport.get(2), prevViewport.get(3));
            if (prevCullFaceEnabled) {
                GL11.glEnable(GL11.GL_CULL_FACE);
            } else {
                GL11.glDisable(GL11.GL_CULL_FACE);
            }
            if (prevDepthTestEnabled) {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            } else {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }
            if (prevStencilTestEnabled) {
                GL11.glEnable(GL11.GL_STENCIL_TEST);
            } else {
                GL11.glDisable(GL11.GL_STENCIL_TEST);
            }
            if (prevScissorEnabled) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            } else {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
            GL11.glScissor(prevScissorBox.get(0), prevScissorBox.get(1), prevScissorBox.get(2), prevScissorBox.get(3));
            GL11.glColorMask(prevColorMaskR, prevColorMaskG, prevColorMaskB, prevColorMaskA);
            GL11.glDepthMask(prevDepthMask);
            GL20.glStencilMaskSeparate(GL11.GL_FRONT, prevStencilMask);
            GL20.glStencilMaskSeparate(GL11.GL_BACK, prevStencilBackMask);

            if (prevBlendEnabled) {
                GL11.glEnable(GL11.GL_BLEND);
            } else {
                GL11.glDisable(GL11.GL_BLEND);
            }
            GL14.glBlendFuncSeparate(prevBlendSrcRgb, prevBlendDstRgb, prevBlendSrcAlpha, prevBlendDstAlpha);
            GL20.glBlendEquationSeparate(prevBlendEqRgb, prevBlendEqAlpha);
            GL14.glBlendColor(prevBlendColorR, prevBlendColorG, prevBlendColorB, prevBlendColorA);

            GL20.glUseProgram(prevProgram);
            GL30.glBindVertexArray(prevVao);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevElementArrayBuffer);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevArrayBuffer);
            // Restore per-unit bindings first, then restore the active unit to what Minecraft expects.
            for (int i = 0; i < unitsToBackup; i++) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTexture2dByUnit[i]);
            }
            GL13.glActiveTexture(prevActiveTexture);
        }
        if (!this.loggedFirstPaint) {
            this.loggedFirstPaint = true;
            LOGGER.info(LOGGERMARKER, "paint() returned {} (texture_id={})", painted, this.texture_id);
        }

        guiGraphics.enableScissor(x, y, x + width, y + height);
        try {
            // WebRender renders into an OpenGL texture (origin at bottom-left). Minecraft GUI blits assume a
            // top-left origin, so we flip V here to display the page upright.
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    this.texture_location,
                    x,
                    y,
                    0.0F,
                    (float) this.deviceHeight,
                    width,
                    height,
                    this.deviceWidth,
                    -this.deviceHeight,
                    this.deviceWidth,
                    this.deviceHeight
            );
        } finally {
            guiGraphics.disableScissor();
        }
    }

    private int debugLogCenterPixel() {
        if (!DEBUG_GL) {
            return 0;
        }
        // Debug helper: read back a single pixel from the center of the texture to verify that
        // Servo/WebRender actually wrote something (helps distinguish "paint OK but draw path broken").
        try {
            if (this.debugReadbackFbo == 0) {
                this.debugReadbackFbo = GL30.glGenFramebuffers();
            }

            // Query the actual GL texture dimensions (may differ from our logical size if the native side clamps).
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture_id);
            int texW = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            int texH = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            if (texW <= 0 || texH <= 0) {
                LOGGER.warn(LOGGERMARKER, "Invalid texture size ({}x{}) (texture_id={})", texW, texH, this.texture_id);
                return 0;
            }
            if (texW != this.deviceWidth || texH != this.deviceHeight) {
                LOGGER.info(
                        LOGGERMARKER,
                        "Texture size mismatch: gl={}x{}, expected_device={}x{}, logical={}x{} (texture_id={})",
                        texW,
                        texH,
                        this.deviceWidth,
                        this.deviceHeight,
                        this.width,
                        this.height,
                        this.texture_id
                );
            }

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.debugReadbackFbo);
            GL30.glFramebufferTexture2D(
                    GL30.GL_FRAMEBUFFER,
                    GL30.GL_COLOR_ATTACHMENT0,
                    GL11.GL_TEXTURE_2D,
                    this.texture_id,
                    0
            );
            int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
            if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
                LOGGER.warn(LOGGERMARKER, "Debug readback FBO incomplete (status=0x{}, texture_id={})", Integer.toHexString(status), this.texture_id);
                return 0;
            }

            int cx = texW / 2;
            int cy = texH / 2;
            var buf = this.tmpReadbackRgba;
            buf.clear();
            GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
            GL11.glReadPixels(cx, cy, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
            int r = buf.get(0) & 0xFF;
            int g = buf.get(1) & 0xFF;
            int b = buf.get(2) & 0xFF;
            int a = buf.get(3) & 0xFF;
            LOGGER.info(LOGGERMARKER, "Center pixel RGBA=({}, {}, {}, {}) (texture_id={})", r, g, b, a, this.texture_id);
            return (r << 24) | (g << 16) | (b << 8) | a;
        } catch (Throwable t) {
            LOGGER.warn(LOGGERMARKER, "Debug readback failed (texture_id={})", this.texture_id, t);
            return 0;
        } finally {
            try {
                // Detach to avoid keeping a dangling attachment around.
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, 0, 0);
            } catch (Throwable ignored) {
            }
        }
    }

    private void registerExternalTexture() {
        var device = RenderSystem.getDevice();
        while (device instanceof ValidationGpuDevice validation) {
            device = validation.getRealDevice();
        }

        if (!(device instanceof GlDevice glDevice)) {
            String error = "Unsupported GpuDevice backend: " + device.getBackendName();
            LOGGER.error(LOGGERMARKER, error);
            throw new IllegalStateException(error);
        }

        GpuTexture gpuTexture = glDevice.createExternalTexture(
                "xian_webview_" + this.texture_id,
                GpuTexture.USAGE_TEXTURE_BINDING,
                this.texture_id
        );
        GpuTextureView gpuTextureView = glDevice.createTextureView(gpuTexture);
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);

        AbstractTexture texture = new ExternalTexture(gpuTexture, gpuTextureView, sampler);
        Minecraft.getInstance().getTextureManager().register(this.texture_location, texture);
    }

    private static int queryGuiScale() {
        try {
            int scale = Minecraft.getInstance().getWindow().getGuiScale();
            return Math.max(1, scale);
        } catch (Throwable ignored) {
            return 1;
        }
    }

    private static int scaleToDevicePixels(int logicalPixels, int guiScale) {
        long scaled = (long) Math.max(1, logicalPixels) * (long) Math.max(1, guiScale);
        if (scaled > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) scaled;
    }

    private static final class ExternalTexture extends AbstractTexture {
        ExternalTexture(GpuTexture texture, GpuTextureView textureView, GpuSampler sampler) {
            this.texture = texture;
            this.textureView = textureView;
            this.sampler = sampler;
        }
    }
}

