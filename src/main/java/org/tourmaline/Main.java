package org.tourmaline;

import ResourceImpl.Mesh;
import ResourceImpl.Texture;
import ResourceLoading.ResourceLoadScheduler;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Main {
    public static long window_handle = 0L;
    public static void main(String[] args){
        long t1,t2,t3;
        ResourceLoadScheduler resourceLoadScheduler = new ResourceLoadScheduler();
        window_handle = init();

        Texture albedo = new Texture();
        Texture normal = new Texture();
        Texture roughness_g = new Texture();
        Texture roughness_b = new Texture();

        Mesh mesh = new Mesh();

        resourceLoadScheduler.addResource(albedo,"src/main/resources/F16/F16_albedo.png");
        resourceLoadScheduler.addResource(normal,"src/main/resources/F16_normal.png");
        resourceLoadScheduler.addResource(roughness_g,"src/main/resources/F16/F16_roughness_G.png");
        resourceLoadScheduler.addResource(roughness_b,"src/main/resources/F16/F16_roughness_B.png");

        resourceLoadScheduler.addResource(mesh,"src/main/resources/F16/F16.obj");


        t1 = System.currentTimeMillis();
        resourceLoadScheduler.loadResources();

        while (resourceLoadScheduler.getReadiness() < 1.0){
            System.out.println(resourceLoadScheduler.getReadiness());
        }
        t2 = System.currentTimeMillis();
        albedo.assemble();
        normal.assemble();
        roughness_g.assemble();
        roughness_b.assemble();
        t3 = System.currentTimeMillis();
        resourceLoadScheduler.reset();

        System.out.printf("Async load took %d ms, Resource init took %d ms", t2-t1, t3-t2);


        while (!glfwWindowShouldClose(window_handle)){
            glfwPollEvents();
            glfwSwapBuffers(window_handle);
        }

    }





    private static long init(){
        long window;
        long NULL = 0L;
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit()){
            throw new RuntimeException();
        }

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);
        window = glfwCreateWindow(1920,1080, "The War To End All Wars",NULL,NULL);

        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        try ( MemoryStack stack = stackPush() ) {

            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);
            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Center the window
            assert vidmode != null;
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(0);
        // Make the window visible
        glfwShowWindow(window);
        GL.createCapabilities();



        return window;

    }

}

