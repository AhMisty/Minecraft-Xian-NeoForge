package cn.ahmisty.minecraft.xian;

import cn.ahmisty.minecraft.xian.ffi.web.Engine;
import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
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

import java.nio.IntBuffer;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class WebEngineTicker {
    private static final Marker LOGGERMARKER = MarkerFactory.getMarker("WebEngineTicker");
    private static final Logger LOGGER = LoggerFactory.getLogger("仙");

    @SubscribeEvent
    public static void onTick(RenderFrameEvent.Pre e) {
        // The native engine is thread-local and must be driven on the render thread.
        if (!RenderSystem.isOnRenderThread()) {
            return;
        }

        // Servo/WebRender uses raw OpenGL calls and may change GL state. If we don't restore it,
        // Minecraft will render the rest of the frame with the wrong blend/framebuffer/etc state.
        int prevDrawFbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int prevReadFbo = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        IntBuffer prevViewport = BufferUtils.createIntBuffer(4);
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevViewport);
        boolean prevDepthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean prevStencilTestEnabled = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
        boolean prevScissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        IntBuffer prevScissorBox = BufferUtils.createIntBuffer(4);
        GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, prevScissorBox);
        var prevColorMask = BufferUtils.createByteBuffer(4);
        GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, prevColorMask);
        boolean prevColorMaskR = prevColorMask.get(0) != 0;
        boolean prevColorMaskG = prevColorMask.get(1) != 0;
        boolean prevColorMaskB = prevColorMask.get(2) != 0;
        boolean prevColorMaskA = prevColorMask.get(3) != 0;
        var prevDepthMaskBuf = BufferUtils.createByteBuffer(1);
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
        var prevBlendColor = BufferUtils.createFloatBuffer(4);
        // Some LWJGL variants used by Minecraft do not expose GL14.GL_BLEND_COLOR, so use the raw enum.
        GL11.glGetFloatv(0x8005 /* GL_BLEND_COLOR */, prevBlendColor);
        float prevBlendColorR = prevBlendColor.get(0);
        float prevBlendColorG = prevBlendColor.get(1);
        float prevBlendColorB = prevBlendColor.get(2);
        float prevBlendColorA = prevBlendColor.get(3);

        int prevProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int prevVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int prevArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        int prevElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        int prevActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        int unitsToBackup = 1;
        try {
            unitsToBackup = GL11.glGetInteger(0x8B4D /* GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS */);
        } catch (Throwable ignored) {
        }
        if (unitsToBackup <= 0) {
            unitsToBackup = 1;
        }
        unitsToBackup = Math.min(unitsToBackup, 16);
        int[] prevTexture2dByUnit = new int[unitsToBackup];
        for (int i = 0; i < unitsToBackup; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            prevTexture2dByUnit[i] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        }
        GL13.glActiveTexture(prevActiveTexture);

        try {
            Engine.tick();
        } catch (Throwable t) {
            LOGGER.error(LOGGERMARKER, "Failed to tick the WebEngine", t);
        } finally {
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, prevDrawFbo);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevReadFbo);
            GL11.glViewport(prevViewport.get(0), prevViewport.get(1), prevViewport.get(2), prevViewport.get(3));
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
            for (int i = 0; i < unitsToBackup; i++) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTexture2dByUnit[i]);
            }
            GL13.glActiveTexture(prevActiveTexture);
        }
    }
}
