package cn.ahmisty.minecraft.xian.services.early;

import com.google.auto.service.AutoService;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import net.neoforged.neoforgespi.locating.IOrderedProvider;

@AutoService({GraphicsBootstrapper.class})
public class GLFW implements GraphicsBootstrapper, IOrderedProvider {
    public static final String NAME = "Xian/GLFW";

    @Override
    public String name () {return NAME;}

    @Override
    public int getPriority() {return Integer.MAX_VALUE;}

    @Override
    public void bootstrap(String[] arguments) {
        ByteBuddyAgent.install();
        new ByteBuddy()
                .redefine(org.lwjgl.glfw.GLFW.class)
                .visit(Advice.to(glfwWindowHintStringAdvice.class).on(
                        ElementMatchers.named("glfwSetWindowTitle")
                                .and(ElementMatchers.takesArgument(1, CharSequence.class))
                ))
                .visit(Advice.to(GlfwCreateWindowAdvice.class).on(
                        ElementMatchers.named("glfwCreateWindow")
                                .and(ElementMatchers.takesArgument(2, CharSequence.class))
                ))
                .visit(Advice.to(GlfwSetTitleWindowAdvice.class).on(
                        ElementMatchers.named("glfwSetWindowTitle")
                                .and(ElementMatchers.takesArgument(1, CharSequence.class))
                ))
                .visit(Advice.to(glfwSwapBuffersAdvice.class).on(
                        ElementMatchers.named("glfwSwapBuffers")
                ))
                .make()
                .load(org.lwjgl.glfw.GLFW.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    }
    public static class glfwWindowHintStringAdvice {
        @Advice.OnMethodEnter
        public static void enter(@Advice.Argument(value = 1, readOnly = false) CharSequence value) {
            value = "Xian: " + value;
        }
    }
    public static class GlfwCreateWindowAdvice {
        @Advice.OnMethodEnter
        public static void enter(@Advice.Argument(value = 2, readOnly = false) CharSequence title) {
            title = "Xian: " + title;
        }
    }
    public static class GlfwSetTitleWindowAdvice {
        @Advice.OnMethodEnter
        public static void enter(@Advice.Argument(value = 1, readOnly = false) CharSequence title) {
            String prefix = "Xian: ";
            if (!title.toString().startsWith(prefix)) {
                title = prefix + title;
            }
        }
    }
    public static class glfwSwapBuffersAdvice {
        @Advice.OnMethodEnter
        public static void enter(@Advice.Argument(0) long window) {
        }
    }
}
