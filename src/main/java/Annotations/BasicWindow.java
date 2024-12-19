package Annotations;


import Interfaces.InterfaceRenderer;
import Rendering.Camera;
import ResourceImpl.CubeMap;
import Rendering.Scene;
import ResourceImpl.Shader;
import ResourceImpl.Texture;
import imgui.ImGui;
import imgui.ImGuiIO;

import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import org.lwjgl.glfw.*;


import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.tourmaline.Main;


import java.io.Closeable;
import java.nio.IntBuffer;


import static java.lang.StringTemplate.STR;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;


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


@Getter
@SuppressWarnings({"Duplicates"})

public abstract class BasicWindow implements Closeable {

    protected static ImGuiImplGlfw imGuiGlfw;
    protected static ImGuiImplGl3 imGuiGl3;

    private static String glslVersion = null;

    private static int windowWidth, windowHeight;
    private static int shadowMapSize;
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
    protected static int deferredShadowPositionBuffer;

    // Forward/Postprocessing pass data
    protected static int frameBuffer;
    protected static int colorBuffer;
    protected static int bloomBuffer;


    protected static int shadowMap;

    // Depth buffer is shared between different framebuffers
    protected static int sharedDepthBuffer;
    protected static int shadowDepthBuffer;

    protected static Shader deferredShader;
    protected static Shader combineShader;
    protected static Shader postprocessingShader;
    protected static Shader skyBoxShader;
    protected static Shader shadowMappingShader;
    protected static Texture BRDFLookUp;

    protected static Camera camera;
    protected static Camera shadowCamera;

    protected BasicWindow() {
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
            windowWidth = dims[0];
            windowHeight = dims[1];
            shadowMapSize = annotation.shadowMapResolution();
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
        for (int hint : windowHints) {
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
                glViewport(0, 0, width, height);
                resizeWindow(new int[]{width, height});
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


        BRDFLookUp = new Texture("src/main/resources/miscellaneous/BDRF.png", 4);

        generateDepthBuffer();
        generateDeferredFramebuffer();
        generateFrameBuffer();
        generateRenderQuad();
        generateShadowBuffer();

        glslVersion = "#version 460 core";


        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 6);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();

        ImGui.createContext();

        imGuiGlfw.init(window_handle, true);
        imGuiGl3.init(glslVersion);
        ImGuiIO io = ImGui.getIO();

        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);

        io.addConfigFlags(ImGuiConfigFlags.NavEnableSetMousePos);
    }

    public static void renderUI(@NotNull InterfaceRenderer interfaceRenderer) {

        imGuiGl3.newFrame();
        imGuiGlfw.newFrame();
        ImGui.newFrame();


        interfaceRenderer.renderInterface();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupCurrentContext = glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            glfwMakeContextCurrent(backupCurrentContext);
        }

    }




    protected static void drawElements() {
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        deferredPass();
        postprocessingPass();
    }

    protected static void postprocessingPass() {
        glDisable(GL_DEPTH_TEST);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT);

        postprocessingShader.use();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, colorBuffer);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, bloomBuffer);

        drawRenderQuad();
    }

    private static void generateDepthBuffer() {

        sharedDepthBuffer = glGenRenderbuffers();

        glBindRenderbuffer(GL_RENDERBUFFER, sharedDepthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, windowWidth, windowHeight);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

    }


    private static void generateDeferredFramebuffer() {

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

        deferredShadowPositionBuffer = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, deferredShadowPositionBuffer);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight,
                0, GL_RGB, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT4, GL_TEXTURE_2D, deferredShadowPositionBuffer, 0);


        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, sharedDepthBuffer);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            System.err.println(STR."Deferred framebuffer is not ready: \{glCheckFramebufferStatus(GL_FRAMEBUFFER)}");


        int[] attachments = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2,
                GL_COLOR_ATTACHMENT3, GL_COLOR_ATTACHMENT4, GL_COLOR_ATTACHMENT5, GL_COLOR_ATTACHMENT6};

        glDrawBuffers(attachments);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    protected static void deferredPass() {
        camera.setViewProjectionMatrix(deferredShader);
        shadowCamera.setShadowViewProjectionMatrix(deferredShader);
        scene.setActiveProgram(deferredShader);

        glEnable(GL_DEPTH_TEST);
        glCullFace(GL_BACK);

        glBindFramebuffer(GL_FRAMEBUFFER, deferredframeBuffer);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        deferredShader.use();
        glActiveTexture(GL_TEXTURE10);

        CubeMap radiance = scene.getSkyBox().getRadiance();
        if (radiance != null) {
            radiance.use();
        }

        glActiveTexture(GL_TEXTURE11);

        CubeMap irradiance = scene.getSkyBox().getIrradiance();
        if (irradiance != null) {
            irradiance.use();
        }

        glActiveTexture(GL_TEXTURE12);
        BRDFLookUp.use();

        scene.drawItems();

        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);

        camera.setViewProjectionMatrix(combineShader);

        combineShader.use();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, deferredPositionBuffer);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, deferredAlbedoMetalnessBuffer);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, deferredNormalRoughnessBuffer);
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, deferredEnvironmentEmissionBuffer);
        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, deferredShadowPositionBuffer);
        glActiveTexture(GL_TEXTURE5);
        glBindTexture(GL_TEXTURE_2D, shadowMap);

        glActiveTexture(GL_TEXTURE9);
        BRDFLookUp.use();

        glDepthMask(false);
        drawRenderQuad();
        glDepthMask(true);
        combineShader.unbind();

        // it's already lost here
    }

    private static void generateFrameBuffer() {
        frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);

        colorBuffer = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, colorBuffer);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, windowWidth, windowHeight,
                0, GL_RGBA, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        bloomBuffer = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, bloomBuffer);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, windowWidth, windowHeight,
                0, GL_RGBA, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorBuffer, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, bloomBuffer, 0);

        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, sharedDepthBuffer);


        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            System.err.println("Forward framebuffer is not ready");

        int[] attachments = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1};

        glDrawBuffers(attachments);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        //System.out.println(STR."Color Buffer \{colorBuffer}\n Bloom Buffer \{bloomBuffer}\n Shared Depth buffer \{shadowDepthBuffer}");
    }


    protected static void shadowPass(){
        glBindFramebuffer(GL_FRAMEBUFFER, shadowDepthBuffer);


        shadowCamera.setViewProjectionMatrix(shadowMappingShader);
        scene.setActiveProgram(shadowMappingShader);
        shadowMappingShader.use();

        glEnable(GL_DEPTH_TEST);
        glCullFace(GL_FRONT);
        glDepthFunc(GL_LESS);

        glActiveTexture(GL_TEXTURE5);
        glBindTexture(GL_TEXTURE_2D, shadowMap);
        glViewport(0,0, shadowMapSize, shadowMapSize);
        glClear(GL_DEPTH_BUFFER_BIT);
        scene.drawItems();
        shadowMappingShader.unbind();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0,0, windowWidth, windowHeight);
        glCullFace(GL_BACK);
    }
    private static void generateShadowBuffer(){
        /*
        shadowDepthBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, shadowDepthBuffer);
        shadowMap = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, shadowMap);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, shadowMapSize, shadowMapSize,
                0, GL_RGBA, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        float[] borderColor = { 1.0f, 1.0f, 1.0f, 1.0f };
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);
        glBindTexture(GL_TEXTURE_2D, 0);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, shadowMap, 0);

        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, sharedDepthBuffer);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            System.err.println("Forward framebuffer is not ready");

        int[] attachments = {GL_COLOR_ATTACHMENT0};

        glDrawBuffers(attachments);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
*/
        shadowDepthBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, shadowDepthBuffer);

// Create a depth texture for the shadow map
        shadowMap = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, shadowMap);

// Configure the depth texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, shadowMapSize, shadowMapSize,
                0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

// Set border color for the texture
        float[] borderColor = { 1.0f, 1.0f, 1.0f, 1.0f };
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);

// Attach the depth texture to the framebuffer
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowMap, 0);

// Ensure no color buffer is attached (optional for shadow mapping)
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

// Check the framebuffer completeness
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Shadow framebuffer is not complete!");
        }

// Unbind the framebuffer to prevent unintended rendering
        glBindFramebuffer(GL_FRAMEBUFFER, 0);



    }

    private static void generateRenderQuad() {
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

    protected static void drawRenderQuad() {
        glBindVertexArray(renderQuadArray);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }


    private static void wipeDepthBuffer() {

        glDeleteRenderbuffers(sharedDepthBuffer);
    }

    private static void wipeRenderQuad() {
        glDeleteVertexArrays(renderQuadArray);
        glDeleteBuffers(verticesBuffer);
        glDeleteBuffers(uvsBuffer);
    }

    private static void wipeFramebuffer() {
        glDeleteFramebuffers(frameBuffer);
        glDeleteTextures(colorBuffer);
    }
    private static void wipeShadowFramebuffer() {
        glDeleteFramebuffers(sharedDepthBuffer);
        glDeleteTextures(shadowMap);
    }

    private static void wipeDeferredFrameBuffer() {
        glDeleteFramebuffers(deferredframeBuffer);
        glDeleteTextures(deferredPositionBuffer);
        glDeleteTextures(deferredAlbedoMetalnessBuffer);
        glDeleteTextures(deferredNormalRoughnessBuffer);
        glDeleteTextures(deferredEnvironmentEmissionBuffer);
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

    /**
     * @param dims
     * dims[0] - width
     * dims[1] - height
     */
    protected static void resizeWindow(int[] dims){
        wipeDepthBuffer();
        wipeFramebuffer();
        wipeDeferredFrameBuffer();

        windowWidth = dims[0];
        windowHeight = dims[1];

        generateDepthBuffer();
        generateDeferredFramebuffer();
        generateFrameBuffer();
        // regenerate FBs
    }

}
