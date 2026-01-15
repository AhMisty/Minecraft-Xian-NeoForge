package cn.ahmisty.minecraft.xian.client.gui;

import cn.ahmisty.minecraft.xian.ffi.web.Engine;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;

public class WebView implements Renderable {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebView.class);
    static {
        Engine.safe_init();
    }

    public final View view;
    public final int texture_id;
    public final Identifier texture_location;
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    private AbstractTexture texture;

    public WebView(int x, int y, int width, int height, float hidpi_scale_factor, String initial_url) throws Throwable {
        try(Arena arena = Arena.ofConfined()) {
            this.view = new View(
                    new View.Config(arena)
                            .set_width(width)
                            .set_height(height)
                            .set_hidpi_scale_factor(hidpi_scale_factor)
                            .set_initial_url(initial_url)
            );
        }
        this.texture_id = this.view.texture_id();
        this.texture_location = Identifier.fromNamespaceAndPath("xian", "webview/" + this.texture_id);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.render(guiGraphics, this.x, this.y, this.width, this.height, mouseX, mouseY, partialTick);
    }

    public void render(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
        RenderSystem.assertOnRenderThread();

        try {
            // Embedder contract: tick Servo every frame. needs_tick is only a best-effort hint.
            Engine.tick();
            // paint() is cheap when clean (Rust side early-outs), and keeps working if AUTO_PAINT is disabled.
            this.view.paint();
        } catch (Throwable t) {
            LOGGER.warn("WebView tick/paint failed", t);
        }

        try {
            this.ensureTextureRegistered();
        } catch (Throwable t) {
            LOGGER.warn("WebView texture registration failed (texture_id={})", this.texture_id, t);
            return;
        }

        guiGraphics.enableScissor(x, y, x + width, y + height);
        try {
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    this.texture_location,
                    x,
                    y,
                    0.0F,
                    0.0F,
                    width,
                    height,
                    this.width,
                    this.height,
                    this.width,
                    this.height
            );
        } finally {
            guiGraphics.disableScissor();
        }
    }

    private void ensureTextureRegistered() {
        if (this.texture != null) {
            return;
        }

        var device = RenderSystem.getDevice();
        var realDevice = device;
        while (realDevice instanceof ValidationGpuDevice validation) {
            realDevice = validation.getRealDevice();
        }

        if (!(realDevice instanceof GlDevice glDevice)) {
            throw new IllegalStateException("Unsupported GpuDevice backend: " + device.getBackendName());
        }

        GpuTexture gpuTexture = glDevice.createExternalTexture(
                "xian_webview_" + Integer.toUnsignedString(this.texture_id),
                GpuTexture.USAGE_TEXTURE_BINDING,
                this.texture_id
        );
        GpuTextureView gpuTextureView = glDevice.createTextureView(gpuTexture);
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);

        this.texture = new ExternalTexture(gpuTexture, gpuTextureView, sampler);
        Minecraft.getInstance().getTextureManager().register(this.texture_location, this.texture);
    }

    private static final class ExternalTexture extends AbstractTexture {
        ExternalTexture(GpuTexture texture, GpuTextureView textureView, GpuSampler sampler) {
            this.texture = texture;
            this.textureView = textureView;
            this.sampler = sampler;
        }
    }
}
