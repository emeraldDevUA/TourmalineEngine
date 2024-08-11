package org.tourmaline;

import Annotations.BasicWindow;
import ResourceImpl.Mesh;
import ResourceImpl.Shader;
import ResourceImpl.Texture;
import ResourceLoading.ResourceLoadScheduler;

import Annotations.OpenGLWindow;


import static org.joml.Math.abs;
import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL11.*;


@OpenGLWindow(windowName = "Complex Example", defaultDimensions = {1920,1080})

public class Main extends BasicWindow {
    public static void main(String[] args){
        long t1,t2,t3;
        ResourceLoadScheduler resourceLoadScheduler = new ResourceLoadScheduler();

        init(Main.class);

        Shader shader = new Shader("src/main/glsl/vertex_test.vert",
                "src/main/glsl/fragment_test.frag");

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
        t3 = System.currentTimeMillis();
        resourceLoadScheduler.reset();

        System.out.printf("Async load took %d ms, Resource init took %d ms", t2-t1, t3-t2);

        while (!glfwWindowShouldClose(window_handle)){

            glBegin(GL_TRIANGLES);
                glColor3d(1,0,0);
                glVertex2d(0,1-0.1);
                glColor3d(0,1,0);
                glVertex2d(1-0.1,-1+0.1);
                glColor3d(0,0,1);
                glVertex2d(-1+0.1,-1+0.1);
            glEnd();

            glfwPollEvents();
            glfwSwapBuffers(window_handle);
        }

    }


    @Override
    protected void drawElements() {

    }
}

