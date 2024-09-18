package Annotations;


import ResourceImpl.Scene;
import ResourceImpl.Shader;
import ResourceImpl.Texture;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkUserFont;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.nuklear.*;
import java.io.Closeable;
import java.nio.IntBuffer;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.GL_RGBA8_SNORM;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.nmemAllocChecked;
import static org.lwjgl.system.MemoryUtil.nmemFree;

@Getter
@SuppressWarnings({"unused", "Duplicates"})

public abstract class BasicWindow implements Closeable {

    private static NkContext ctx;

    private static int windowWidth, windowHeight;
    // for time measurement
    private static long t1, t2;

    protected static final long NULL = 0L;
    public static long window_handle = 0L;


    protected static String window_name;
    protected static Scene scene;


    protected static int renderQuadArray;
    protected static int verticesBuffer;
    protected static int uvsBuffer;

    // Deferred pass data
    protected static int deferredframeBuffer;
    protected static int deferredPositionBuffer;
    protected static int deferredAlbedoMetalnessBuffer;
    protected static int deferredNormalRoughnessBuffer;
    protected static int deferredEnvironmentEmissionBuffer;

    // Forward/Postprocessing pass data
    protected static int frameBuffer;
    protected static int colorBuffer;

    // Depth buffer is shared between different framebuffers
    protected static int sharedDepthBuffer;
    protected static int shadowDepthBuffer;

    protected static Shader deferredShader;
    protected static Shader combineShader;
    protected static Shader postprocessingShader;
//    protected static Shader skyboxShader;
    protected static Shader shadowMappingShader;
    protected static Texture BRDFLookUp;


    protected BasicWindow(){
        t1 = System.currentTimeMillis();
        t2 = t1;
    }
    protected static void init(@NotNull Class<?> className) {

        int[] windowHints = new int[0];
        int[] windowHintsValues = new int[0];
        int[] dims = new int[2];
        if (className.isAnnotationPresent(OpenGLWindow.class)) {

            OpenGLWindow annotation = className.getAnnotation(OpenGLWindow.class);
            window_name = annotation.windowName();
            dims = annotation.defaultDimensions();
            windowWidth =  dims[0];
            windowHeight = dims[1];
            windowHintsValues = annotation.windowHintsValues();
            windowHints = annotation.windowHints();

        } else {
            System.err.println("Your annotation is faulty");
            System.exit(1);
        }


        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new RuntimeException();
        }

        //glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);
        int cnt = 0;
        for(int hint:windowHints){
            glfwWindowHint(hint, windowHintsValues[cnt]);
            cnt++;
        }
        window_handle = glfwCreateWindow(dims[0], dims[1], window_name, NULL, NULL);

        if (window_handle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        try (MemoryStack stack = stackPush()) {

            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            glfwGetWindowSize(window_handle, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Center the window
            assert videoMode != null;
            glfwSetWindowPos(
                    window_handle,
                    (videoMode.width() - pWidth.get(0)) / 2,
                    (videoMode.height() - pHeight.get(0)) / 2
            );
        }
        glfwMakeContextCurrent(window_handle);

        glfwSwapInterval(0);
        glfwShowWindow(window_handle);

        GL.createCapabilities();

        glfwSetWindowSizeCallback(window_handle, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long l, int width, int height) {
                glViewport(0,0, width,height);
                //glfwSetWindowSize(window_handle,width,height);
            }
        });
        try {

            Texture icon =
                    new Texture("src/main/resources/miscellaneous/secondary_icon.png", 4);
            GLFWImage image = GLFWImage.malloc();
            GLFWImage.Buffer imagebf = GLFWImage.malloc(1);
            image
                    .set(icon.getTextureWidth().get(0),
                            icon.getTextureHeight().get(0),
                            icon.getTextureData());

            imagebf.put(0, image);
            glfwSetWindowIcon(window_handle, imagebf);

        } catch (NullPointerException e) {
            System.err.println(e.getMessage());
        }


        BRDFLookUp = new Texture("src/main/resources/miscellaneous/BDRF.png",4);
        generateDepthBuffer();
        generateDeferredFramebuffer();
        generateFrameBuffer();
        generateRenderQuad();

         NkContext ctx = NkContext.create();
        try(MemoryStack stack = stackPush()){
            NkRect rect = NkRect.mallocStack(stack);
            rect.x(50).y(50).w(300).h(200);
            // Begin the window
            if(nk_begin(ctx, "Window Name", rect, NK_WINDOW_TITLE|NK_WINDOW_BORDER|NK_WINDOW_MINIMIZABLE)) {
                // Add rows here
                float rowHeight = 50;
                int itemsPerRow = 1;
                nk_layout_row_dynamic(ctx, rowHeight, itemsPerRow);
            }
            // End the window
            nk_end(ctx);

        }



    }

    protected static void drawElements(){
            glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
            deferredPass();
            postprocessingPass();
    }

    protected Vector2i getMousePosition() {
        return new Vector2i(0, 0);
    }

    protected float getCurrentFPS() {

        return 1.0f / (t2 - t1);
    }

    protected static void measureTime() {

        if (t2 != t1) {
            t1 = t2;
        }
        t2 = System.currentTimeMillis();
    }

    protected static void postprocessingPass()
    {
        glDisable(GL_DEPTH_TEST);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT);

        postprocessingShader.use();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, colorBuffer);

        drawRenderQuad();
    }

    private static void generateDepthBuffer()
    {
        sharedDepthBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, sharedDepthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, windowWidth, windowHeight);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
    }

    private static void wipeDepthBuffer()
    {

        glDeleteRenderbuffers(sharedDepthBuffer);
    }

    private static void generateDeferredFramebuffer()
    {
        deferredframeBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, deferredframeBuffer);

        deferredPositionBuffer = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, deferredPositionBuffer);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight,
                0, GL_RGB, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, deferredPositionBuffer, 0);

        deferredAlbedoMetalnessBuffer = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, deferredAlbedoMetalnessBuffer);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, windowWidth, windowHeight,
                0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, deferredAlbedoMetalnessBuffer, 0);

        deferredNormalRoughnessBuffer = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, deferredNormalRoughnessBuffer);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8_SNORM, windowWidth, windowHeight,
                0, GL_RGBA, GL_SHORT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, GL_TEXTURE_2D, deferredNormalRoughnessBuffer, 0);

        deferredEnvironmentEmissionBuffer = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, deferredEnvironmentEmissionBuffer);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight,
                0, GL_RGB, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT3, GL_TEXTURE_2D, deferredEnvironmentEmissionBuffer, 0);

        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, sharedDepthBuffer);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            System.err.println(STR."Deferred framebuffer is not ready: \{glCheckFramebufferStatus(GL_FRAMEBUFFER)}");


        int[] attachments = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2,
                GL_COLOR_ATTACHMENT3, GL_COLOR_ATTACHMENT4, GL_COLOR_ATTACHMENT5, GL_COLOR_ATTACHMENT6};

        glDrawBuffers(attachments);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private static void wipeDeferredFrameBuffer()
    {
        glDeleteFramebuffers(deferredframeBuffer);
        glDeleteTextures(deferredPositionBuffer);
        glDeleteTextures(deferredAlbedoMetalnessBuffer);
        glDeleteTextures(deferredNormalRoughnessBuffer);
        glDeleteTextures(deferredEnvironmentEmissionBuffer);
    }

    private static void generateFrameBuffer()
    {
        frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);

        colorBuffer = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, colorBuffer);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, windowWidth, windowHeight,
                0, GL_RGBA, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorBuffer, 0);

        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, sharedDepthBuffer);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            System.err.println("Forward framebuffer is not ready");

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private static void cleanupFramebuffer()
    {
        glDeleteFramebuffers(frameBuffer);
        glDeleteTextures(colorBuffer);
    }

    private static void generateRenderQuad()
    {
        float[] vertices = {
                -1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, -1.0f,
                1.0f, 1.0f,
        };

        float[] uvs = {
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
        };

        renderQuadArray = glGenVertexArrays();
        glBindVertexArray(renderQuadArray);

        verticesBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, verticesBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(Shader.POSITION_LOCATION);
        glVertexAttribPointer(Shader.POSITION_LOCATION, 2, GL_FLOAT, false, 0, 0);

        uvsBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, uvsBuffer);
        glBufferData(GL_ARRAY_BUFFER, uvs, GL_STATIC_DRAW);

        glEnableVertexAttribArray(Shader.UVS_LOCATION);
        glVertexAttribPointer(Shader.UVS_LOCATION, 2, GL_FLOAT, false, 0, 0);

        glBindVertexArray(0);
    }

    protected static void drawRenderQuad()
    {
        glBindVertexArray(renderQuadArray);
        glDrawArrays(GL_TRIANGLES, 0, 6);

    }

    private static void wipeRenderQuad()
    {
        glDeleteVertexArrays(renderQuadArray);
        glDeleteBuffers(verticesBuffer);
        glDeleteBuffers(uvsBuffer);
    }

    protected static void deferredPass()
    {
        glBindFramebuffer(GL_FRAMEBUFFER, deferredframeBuffer);
        glClear(GL_COLOR_BUFFER_BIT);
        deferredShader.use();
        scene.setActiveProgram(deferredShader);
        glActiveTexture(GL_TEXTURE10);
        //Scene.getSkyboxRadiance().use();
        glActiveTexture(GL_TEXTURE11);
        //Scene.getSkyboxIrradiance().use();
        glActiveTexture(GL_TEXTURE12);
        BRDFLookUp.use();
        //;

        scene.drawItems();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        combineShader.use();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, deferredPositionBuffer);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, deferredAlbedoMetalnessBuffer);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, deferredNormalRoughnessBuffer);
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, deferredEnvironmentEmissionBuffer);
        glActiveTexture(GL_TEXTURE9);
       // BRDFLookUp.use();

        glDepthMask(false);
        drawRenderQuad();
        glDepthMask(true);
        combineShader.unbind();
    }
}
