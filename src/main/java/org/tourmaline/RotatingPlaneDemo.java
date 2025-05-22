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

@OpenGLWindow(windowName = "Complex Example", defaultDimensions = {1920,1018},
        windowHints = {GLFW_DECORATED}, windowHintsValues={GLFW_TRUE}, shadowMapResolution = 8192)

public class RotatingPlaneDemo extends BasicWindow {

    private static final ResourceLoadScheduler resourceLoadScheduler = new ResourceLoadScheduler();
    private static final AutoLoader autoLoader =
            new AutoLoader("src/main/resources/3D_Models", resourceLoadScheduler);
    private static float ro = 200;
    private static float phi = 0;
    public static void main(String[] args) throws Exception {

        init(RotatingPlaneDemo.class);
        scene = new Scene();

        Texture.setVerticalFlip(true);
        Mesh.setUseAssimp(true);

        autoLoader.loadTrees();
        autoLoader.asyncLoad();

        while (autoLoader.getReadiness() < 1){
            Thread.onSpinWait();
        }
        resourceLoadScheduler.reset();

        MeshTree F16Tree = autoLoader.getDrawables().get("F16");
        F16Tree.compile();

        Vector3f scaleVector = new Vector3f(80);

        Material mainMat = F16Tree.getNodeValue().getMaterial();
        mainMat.addProperty(Material.METALNESS, 0.0);
        mainMat.addProperty(Material.ROUGHNESS, 0.8);

        F16Tree.getChildNodes().forEach(meshTreeNode -> {
            F16Tree.traverse(mesh -> {
                mesh.setScale(scaleVector);
                mesh.setEnableReflection(false);
            });
            if (!meshTreeNode.getNodeName().equals("%s_glass.obj".formatted("F16"))) {
                ((MeshTree) (meshTreeNode))
                        .traverse(mesh -> {
                            mesh.getMaterial().close();
                            mesh.setMaterial(mainMat);

                            mesh.setIncidenceAtZero(new Vector3f(0.04f));

                        });
            } else {
                meshTreeNode.getNodeValue().setEnableBlending(true);
            }
        });

        F16Tree.setPosition(new Vector3f(0,-20,0));

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

        transparentShader = new Shader("src/main/glsl/vertex_test.vert",
                "src/main/glsl/fragment_test.frag");

        SkyBox skyBox = new SkyBox(
                new CubeMap("src/main/resources/skybox/skybox", ".hdr", false),
                new CubeMap("src/main/resources/skybox/radiance", ".hdr", false),
                new CubeMap("src/main/resources/skybox/irradiance", ".hdr", false)
        );

        skyBox.compile();

        camera = new Camera(
                new Vector3f(5,1,3).normalize().mul(200),
                new Vector3f(0,0,0));

        shadowCamera = new Camera(
                new Vector3f(-90,120,20),
                new Vector3f(0,0,0));

        camera.loadPerspectiveProjection((float)Math.PI/3,1.8f, 2000,0.1f);
        shadowCamera.loadOrthographicProjection(-200,200, -200,
                200, -400, 400);

        camera.loadViewMatrix();
        shadowCamera.loadViewMatrix();

        camera.setViewProjectionMatrix(skyBoxShader);

        scene.setSkyBox(skyBox);
        scene.addDrawItem(F16Tree);

        DirectionalLight directionalLight = new DirectionalLight(
                new Vector3f(-90,120,20).negate().normalize());
        directionalLight.setLightColor(new Vector3f(2.8f, 2.8f, 2.8f).div(10));
        directionalLight.setLightIntensity(15);
        scene.addLightSources(directionalLight, true);
        LightingConfigurator.setLights(scene.getLights(), combineShader);

        Keyboard keyboard = new Keyboard();
        keyboard.setWindow_pointer(window_handle);
        keyboard.init();

        Vector2f screenDimensions = new Vector2f(windowWidth, windowHeight);


        postprocessingShader.setUniform("uViewportSize", screenDimensions);
        postprocessingShader.setUniform("enableFXAA", true);
        postprocessingShader.setUniform("gamma", 1.8f);

        transparentShader.setUniform("uViewportSize", screenDimensions);


        while (!glfwWindowShouldClose(window_handle)) {

            Vector3f cameraPos = camera.getPosition();
            phi+=0.001f;

            cameraPos.x = ro * cos(phi);
            cameraPos.z = ro * sin(phi);
            camera.loadViewMatrix();

            glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

            shadowPass();
            deferredPass();
            skyBoxPass();

            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            glDisable(GL_DEPTH_TEST);
            postprocessingPass();
            glEnable(GL_DEPTH_TEST);
            transparentPass();

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
