package Annotations;


import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.system.MemoryStack.stackPush;

@Getter
public class BasicWindow {
    public static long window_handle = 0L;
    private static String window_name;

    protected static void init(@NotNull Class<?> className){


        int[] dims = new int[2];
        if (className.isAnnotationPresent(OpenGLWindow.class)) {

            OpenGLWindow annotation = className.getAnnotation(OpenGLWindow.class);
            window_name = annotation.windowName();
            dims = annotation.defaultDimensions();

        }else {
            System.err.println("Your annotation is faulty");
            System.exit(1);
        }

        long NULL = 0L;
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit()){
            throw new RuntimeException();
        }

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);
        window_handle = glfwCreateWindow(dims[0],dims[1], window_name,NULL,NULL);

        if (window_handle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        try ( MemoryStack stack = stackPush() ) {

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

    }
}
