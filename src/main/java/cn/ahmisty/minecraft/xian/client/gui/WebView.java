package cn.ahmisty.minecraft.xian.client.gui;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.foreign.Arena;

public class WebView implements Renderable {
    private static final Marker LOGGERMARKER = MarkerFactory.getMarker("WebView");
    private static final Logger LOGGER = LoggerFactory.getLogger("仙");

    public final View view;
    public final int texture_id;
    public final Identifier texture_location;

    public int x;
    public int y;

    public int width;
    public int height;

    public WebView(int x, int y, int width, int height, float hidpi_scale_factor, String initial_url) throws Throwable {
        RenderSystem.assertOnRenderThread();

        try (Arena arena = Arena.ofConfined()) {
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

        this.registerExternalTexture();
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void resize(int width, int height) {
        RenderSystem.assertOnRenderThread();

        if (this.width == width && this.height == height) {
            return;
        }

        try {
            this.view.resize(width, height);
        } catch (Throwable t) {
            LOGGER.error(LOGGERMARKER, "Failed to resize", t);
            return;
        }

        this.width = width;
        this.height = height;

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

        try {
            this.view.paint();
        } catch (Throwable t) {
            LOGGER.error(LOGGERMARKER, "Failed to paint (texture_id={})", this.texture_id, t);
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

    private static final class ExternalTexture extends AbstractTexture {
        ExternalTexture(GpuTexture texture, GpuTextureView textureView, GpuSampler sampler) {
            this.texture = texture;
            this.textureView = textureView;
            this.sampler = sampler;
        }
    }
}

