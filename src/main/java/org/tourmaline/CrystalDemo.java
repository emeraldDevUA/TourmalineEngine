package org.tourmaline;
import Annotations.BasicWindow;
import Controls.Keyboard;
import Controls.Mouse;
import Effects.BoundingBoxEffect;
import Effects.ExplosionEffect;
import Effects.JetEffect;
import Interfaces.InterfaceRenderer;
import Interfaces.KeyboardEventHandler;
import Interfaces.MouseEventHandler;
import Liquids.LiquidBody;
import Rendering.Camera;
import Rendering.Lights.DirectionalLight;
import Rendering.Lights.LightingConfigurator;
import Rendering.Lights.PointLight;
import Rendering.Scene;
import Rendering.SkyBox;
import ResourceImpl.*;

import ResourceLoading.AutoLoader;
import ResourceLoading.ResourceLoadScheduler;

import Annotations.OpenGLWindow;
import org.joml.Vector2f;
import org.joml.Vector3f;


import static org.joml.Math.cos;
import static org.joml.Math.sin;
import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

@OpenGLWindow(windowName = "Complex Example", defaultDimensions = {1920,1018},windowHints = {GLFW_DECORATED}, windowHintsValues={GLFW_TRUE}, shadowMapResolution = 8192)

public class CrystalDemo extends BasicWindow {

    private static final ResourceLoadScheduler resourceLoadScheduler = new ResourceLoadScheduler();
    private static final AutoLoader autoLoader =
            new AutoLoader("src/main/resources/3D_Models", resourceLoadScheduler);

    public static void main(String[] args) throws Exception {

        init(CrystalDemo.class);

        scene = new Scene();

        Texture.setVerticalFlip(true);
        Mesh.setUseAssimp(true);

        autoLoader.loadTrees();
        autoLoader.asyncLoad();
        while (autoLoader.getReadiness() < 1){
            Thread.onSpinWait();
        }
        resourceLoadScheduler.reset();

        MeshTree crstalTree = autoLoader.getDrawables().get("crystal");
        crstalTree.compile();
        crstalTree.traverse(mesh -> mesh.setEnableReflection(false));
        crstalTree.setPosition(new Vector3f(0,-20,0));

        deferredShader = new Shader(
                "src/main/glsl/deferred_shaders/deferred_vertex.glsl",
                "src/main/glsl/deferred_shaders/deferred_fragment.glsl");

        postprocessingShader =
                new Shader(
                        "src/main/glsl/postprocessing_shaders/postprocessing_vertex.glsl",
                        "src/main/glsl/postprocessing_shaders/postprocessing_fragment.glsl");
        combineShader =
                new Shader("src/main/glsl/combine_shaders/combine_vertex.glsl",
                        "src/main/glsl/combine_shaders/combine_fragment.glsl");
        shadowMappingShader =
                new Shader("src/main/glsl/shadow_shaders/shadow_vertex.glsl",
                        "src/main/glsl/shadow_shaders/shadow_fragment.glsl");
        visualEffectsShader =
                new Shader("src/main/glsl/visualeffects_shaders/visual_effects_vertex.glsl",
                        "src/main/glsl/visualeffects_shaders/visual_effects_fragment.glsl");
        skyBoxShader =
                new Shader("src/main/glsl/skybox_shaders/skybox_vertex.glsl",
                        "src/main/glsl/skybox_shaders/skybox_frag.glsl");

        SkyBox skyBox = new SkyBox(
                new CubeMap("src/main/resources/skybox/skybox", ".hdr", false),
                new CubeMap("src/main/resources/skybox/radiance", ".hdr", false),
                new CubeMap("src/main/resources/skybox/irradiance", ".hdr", false)
        );

        skyBox.compile();

        camera = new Camera(
                new Vector3f(5,1,3).normalize().mul(100),
                new Vector3f(0,0,0));

        shadowCamera = new Camera(
                new Vector3f(0,200,0),
                new Vector3f(0,0,0));

        camera.loadViewMatrix();
        shadowCamera.loadViewMatrix();

        camera.loadPerspectiveProjection((float)Math.PI/3,1.8f, 2000,0.1f);
        shadowCamera.loadOrthographicProjection(-1500,1500, -1500,
                1500, -800, 800);

        camera.setViewProjectionMatrix(skyBoxShader);


        // add draw items
        scene.setSkyBox(skyBox);
        scene.addDrawItem(crstalTree);

        DirectionalLight directionalLight = new DirectionalLight(
                new Vector3f(-90,120,20).negate().normalize());
        directionalLight.setLightColor(new Vector3f(2.8f, 2.8f, 2.8f).div(10));
        directionalLight.setLightIntensity(15);
        scene.addLightSources(directionalLight, true);

        Keyboard keyboard = new Keyboard();
        keyboard.setWindow_pointer(window_handle);
        keyboard.init();


        float phi = 0;
        postprocessingShader.setUniform("uViewportSize",
                new Vector2f(windowWidth, windowHeight));

        postprocessingShader.setUniform("enableFXAA", true);
        postprocessingShader.setUniform("gamma", 2.0f);

        while (!glfwWindowShouldClose(window_handle)) {

            Vector3f cameraPos = camera.getPosition();
            phi+=0.001f;

            cameraPos.x = 100 * cos(phi);
            cameraPos.z = 100 * sin(phi);
            camera.loadViewMatrix();

            glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

            shadowPass();
            deferredPass();
            skyBoxPass();

            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            glDisable(GL_DEPTH_TEST);
            postprocessingPass();
            glEnable(GL_DEPTH_TEST);

            glfwPollEvents();
            glfwSwapBuffers(window_handle);
            measureTime();

            keyboard.processEvents((key, state) -> {
                if(key == GLFW_KEY_C && state == GLFW_PRESS){
                    saveFramebufferAsImage(windowWidth, windowHeight, "Screenshot.png");
                }
            });
        }

    }

}
