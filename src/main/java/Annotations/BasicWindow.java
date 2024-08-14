package Annotations;


import Interfaces.Drawable;
import ResourceImpl.Texture;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.Closeable;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;

import static org.lwjgl.system.MemoryStack.stackPush;

@Getter
public abstract class BasicWindow implements Closeable {

    protected static final long NULL = 0L;
    public static long window_handle = 0L;
    private static String window_name;

    protected List<Drawable> drawList;
    protected static void init(@NotNull Class<?> className) {


        int[] dims = new int[2];
        if (className.isAnnotationPresent(OpenGLWindow.class)) {

            OpenGLWindow annotation = className.getAnnotation(OpenGLWindow.class);
            window_name = annotation.windowName();
            dims = annotation.defaultDimensions();

        } else {
            System.err.println("Your annotation is faulty");
            System.exit(1);
        }


        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new RuntimeException();
        }

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);
        window_handle = glfwCreateWindow(dims[0], dims[1], window_name, NULL, NULL);

        if (window_handle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        try (MemoryStack stack = stackPush()) {

            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window_handle, pWidth, pHeight);
            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Center the window
            assert vidmode != null;
            glfwSetWindowPos(
                    window_handle,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }
        glfwMakeContextCurrent(window_handle);

        glfwSwapInterval(0);
        glfwShowWindow(window_handle);

        GL.createCapabilities();

        glfwSetWindowSizeCallback(window_handle, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long l, int width, int height) {
                //  glViewport(0,0, width,height);
            }
        });
        try {

            Texture icon =
            new Texture("src/main/resources/miscellaneous/default_icon.png", 4);
            GLFWImage image = GLFWImage.malloc();
            GLFWImage.Buffer imagebf = GLFWImage.malloc(1);

            image
                    .set(icon.getTextureWidth() .get(0),
                    icon.getTextureHeight().get(0),
                    icon.getTextureData());

            imagebf.put(0, image);
            glfwSetWindowIcon(window_handle, imagebf);

        } catch (NullPointerException e) {
            System.err.println(e.getMessage());
        }
    }

    protected abstract void drawElements();


}
