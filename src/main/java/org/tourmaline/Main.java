package org.tourmaline;

import Annotations.BasicWindow;
import Interfaces.TreeNode;
import Rendering.Camera;
import Rendering.SkyBox;
import ResourceImpl.*;
import ResourceLoading.AutoLoader;
import ResourceLoading.ResourceLoadScheduler;

import Annotations.OpenGLWindow;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.joml.Math.abs;
import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.system.MemoryStack.stackPush;


@OpenGLWindow(windowName = "Complex Example", defaultDimensions = {1920,1080},
        windowHints = {GLFW_DECORATED}, windowHintsValues={GLFW_TRUE})

public class Main extends BasicWindow {
    public static void main(String[] args){
        long t1,t2,t3;
        ResourceLoadScheduler resourceLoadScheduler = new ResourceLoadScheduler();
        scene = new Scene();
        init(Main.class);

        deferredShader = new Shader("src/main/glsl/deferred_shaders/deferred_vertex.glsl",
                "src/main/glsl/deferred_shaders/deferred_fragment.glsl");

        postprocessingShader = new Shader("src/main/glsl/postprocessing_shaders/postprocessing_vertex.glsl",
                "src/main/glsl/postprocessing_shaders/postprocessing_fragment.glsl");

        combineShader = new Shader("src/main/glsl/combine_shaders/combine_vertex.glsl",
                "src/main/glsl/combine_shaders/combine_fragment.glsl");
        Texture albedo = new Texture();
        Texture normal = new Texture();
        Texture roughness_g = new Texture();
        Texture roughness_b = new Texture();
        Mesh fightingFalcon = new Mesh();
        Mesh euroFighter = new Mesh();
        Mesh mig29 = new Mesh();
        Material material = new Material();


        resourceLoadScheduler.addResource(albedo,"src/main/resources/3D_Models/F16/F16_albedo.png");
        resourceLoadScheduler.addResource(normal,"src/main/resources/3D_Models/F16_normal.png");
        resourceLoadScheduler.addResource(roughness_g,"src/main/resources/3D_Models/F16/F16_roughness_G.png");
        resourceLoadScheduler.addResource(roughness_b,"src/main/resources/3D_Models/F16/F16_roughness_B.png");

        resourceLoadScheduler.addResource(fightingFalcon,"src/main/resources/3D_Models/F16/F16.obj");
        resourceLoadScheduler.addResource(euroFighter, "src/main/resources/3D_Models/Eurofighter/Eurofighter.obj");
        resourceLoadScheduler.addResource(mig29, "src/main/resources/3D_Models/MIG29/MIG29.obj");
        t1 = System.currentTimeMillis();

        Texture.setVerticalFlip(true);
        resourceLoadScheduler.loadResources();
        double load = 0;
        while (resourceLoadScheduler.getReadiness() < 1.0){
            if(abs(load-resourceLoadScheduler.getReadiness()) >= 0.1) {
                load = resourceLoadScheduler.getReadiness();
                System.out.println(STR."\{load * 100}%");
            }
        }
        t2 = System.currentTimeMillis();
        albedo.assemble();
        normal.assemble();
        roughness_g.assemble();
        roughness_b.assemble();
        fightingFalcon.compile();
        euroFighter.compile();
        mig29.compile();
        t3 = System.currentTimeMillis();
        resourceLoadScheduler.reset();

        System.out.printf("Async load took %d ms, Resource init took %d ms", t2-t1, t3-t2);

        Camera camera = new Camera(new Vector3f(1,2,2), new Vector3f(0,0,0));
        camera.loadViewMatrix();
        camera.loadPerspectiveProjection((float)Math.PI/3,1.8f, 100,0.1f);

//        camera.setMVP(deferredShader);
//        camera.setMVP(combineShader);
  //      fightingFalcon.setShader(deferredShader);

        material.addMap(Material.ALBEDO_MAP, albedo);
        material.addMap(Material.NORMAL_MAP, normal);
        material.addMap(Material.ROUGHNESS_MAP, roughness_g);

        fightingFalcon.setMaterial(material);
        List<TreeNode<Mesh>> arrayList = new ArrayList<>();


        Shader test_shader = new Shader("src/main/glsl/vertex_test.vert", "src/main/glsl/fragment_test.frag");
        test_shader.use();
        camera.setMVP(test_shader);
        fightingFalcon.setShader(test_shader);
        MeshTree F16 = new MeshTree(arrayList, fightingFalcon,"F16");
        scene.addDrawItem(F16);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glEnable(GL_CULL_FACE);



        SkyBox skyBox = new SkyBox();
        CubeMap cm = new CubeMap("src/main/resources/skybox/skybox", ".hdr", false);

        skyBox.setCubeMap(cm);
        Shader skyBoxShader = new Shader("src/main/glsl/skybox_shaders/skybox_vertex.glsl",
                "src/main/glsl/skybox_shaders/skybox_frag.glsl");

        camera.setMVP(skyBoxShader);
        skyBox.compile();


//        AutoLoader autoLoader = new AutoLoader("src/main/resources/3D_Models", new ArrayList<>(), new ResourceLoadScheduler());
//        autoLoader.loadTrees();

        while (!glfwWindowShouldClose(window_handle)){
//           glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
            glClear(GL_DEPTH_BUFFER_BIT);
//            deferredPass();
//            postprocessingPass();
            camera.setMVP(test_shader);

            test_shader.use();
                F16.draw();
            test_shader.unbind();

            skyBoxShader.use();
                glActiveTexture(GL_TEXTURE10);
                skyBox.draw();
            skyBoxShader.unbind();
            glfwPollEvents();
            glfwSwapBuffers(window_handle);

        }

    }




    @Override
    public void close() {

        glfwDestroyWindow(window_handle);
    }
}

