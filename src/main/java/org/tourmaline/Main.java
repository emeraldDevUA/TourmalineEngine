package org.tourmaline;

import Annotations.BasicWindow;
import Controls.Keyboard;
import Controls.Mouse;
import Interfaces.InterfaceRenderer;
import Interfaces.KeyboardEventHandler;
import Interfaces.MouseEventHandler;
import Interfaces.TreeNode;
import Rendering.Camera;
import Rendering.Scene;
import Rendering.SkyBox;
import ResourceImpl.*;
import ResourceLoading.ResourceLoadScheduler;

import Annotations.OpenGLWindow;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.*;


import static org.lwjgl.opengl.ARBUniformBufferObject.GL_INVALID_INDEX;
import static org.lwjgl.opengl.ARBUniformBufferObject.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;


@OpenGLWindow(windowName = "Complex Example", defaultDimensions = {1920,1018},
        windowHints = {GLFW_DECORATED}, windowHintsValues={GLFW_TRUE}, shadowMapResolution = 8192)

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

         shadowMappingShader =
                new Shader("src/main/glsl/shadow_shaders/shadow_vertex.glsl",
                        "src/main/glsl/shadow_shaders/shadow_fragment.glsl");

         
        Texture albedo = new Texture();
        Texture normal = new Texture();
        Texture roughness_g = new Texture();
        Texture roughness_b = new Texture();
        Texture land_alb = new Texture();
        Texture land_normal = new Texture();

        Texture metalness = new Texture();
        Mesh fightingFalcon = new Mesh();
        Mesh euroFighter = new Mesh();
        Mesh mig29 = new Mesh();

        Mesh land = new Mesh();
        Material material = new Material();
        Material land_mat = new Material();


        resourceLoadScheduler.addResource(albedo,"src/main/resources/3D_Models/F16/F16_albedo.png");
        resourceLoadScheduler.addResource(normal,"src/main/resources/3D_Models/F16/F16_normal.png");
        resourceLoadScheduler.addResource(roughness_g,"src/main/resources/3D_Models/F16/F16_roughness_G.png");
        resourceLoadScheduler.addResource(roughness_b,"src/main/resources/3D_Models/F16/F16_roughness_B.png");
        resourceLoadScheduler.addResource(metalness,"src/main/resources/3D_Models/F16/F16_metalness.png");


        resourceLoadScheduler.addResource(fightingFalcon,"src/main/resources/3D_Models/F16/F16.obj");
        resourceLoadScheduler.addResource(euroFighter, "src/main/resources/3D_Models/Eurofighter/Eurofighter.obj");
        resourceLoadScheduler.addResource(mig29, "src/main/resources/3D_Models/MIG29/MIG29.obj");

        resourceLoadScheduler.addResource(land, "src/main/resources/3D_Models/Map/etopo10_2.obj");
        resourceLoadScheduler.addResource(land_alb, "src/main/resources/3D_Models/Map/gltf_embedded_0.jpeg");
        resourceLoadScheduler.addResource(land_normal, "src/main/resources/3D_Models/Map/gltf_embedded_0_normal.jpg");


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
        land.compile();
        land_alb.assemble();
        land_normal.assemble();
        metalness.assemble();

        t3 = System.currentTimeMillis();
        resourceLoadScheduler.reset();

        System.out.printf("Async load took %d ms, Resource init took %d ms", t2-t1, t3-t2);
        fightingFalcon.getPosition().add(new Vector3f(10,10,10));

        Vector3f temp = fightingFalcon.getPosition();

        camera = new Camera(
                temp.add(new Vector3f(-3,1,0), new Vector3f()),
                temp.add(new Vector3f(0,0,0), new Vector3f()));


        shadowCamera = new Camera(
                temp.add(new Vector3f(-90,120,20), new Vector3f()),
                temp.add(new Vector3f(0,0,0), new Vector3f()));

        camera.loadViewMatrix();
        shadowCamera.loadViewMatrix();
        camera.loadPerspectiveProjection((float)Math.PI/3,1.8f, 1000,0.1f);
        shadowCamera.loadOrthographicProjection(-1500,1500, -1500, 1500, -800, 800);

        material.addMap(Material.ALBEDO_MAP, albedo);
        material.addMap(Material.NORMAL_MAP, normal);
        material.addMap(Material.ROUGHNESS_MAP, roughness_g);
        material.addMap(Material.METALNESS_MAP, metalness);
        material.addProperty(Material.ROUGHNESS_MAP, 0.8);

        land_mat.addMap(Material.ALBEDO_MAP, land_alb);
        land_mat.addMap(Material.NORMAL_MAP, land_normal);

        fightingFalcon.setMaterial(material);
        land.setMaterial(land_mat);

        List<TreeNode<Mesh>> arrayList = new ArrayList<>();


        Shader test_shader = new Shader("src/main/glsl/vertex_test.vert", "src/main/glsl/fragment_test.frag");
        test_shader.use();
        camera.setViewProjectionMatrix(test_shader);
        fightingFalcon.setShader(test_shader);
        fightingFalcon.setShadowScale(new Vector3f(4));
        land.setShader(test_shader);
        MeshTree F16 = new MeshTree(arrayList, fightingFalcon,"F16");

        scene.addDrawItem(F16);
        scene.addDrawItem(new MeshTree(null, land, "land"));

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_CULL_FACE);

        SkyBox skyBox = new SkyBox();
        CubeMap cm = new CubeMap("src/main/resources/skybox/skybox", ".hdr", false);
        CubeMap radiance_cm =
                new CubeMap("src/main/resources/skybox/radiance", ".hdr", false);

        CubeMap irradiance_cm =
                new CubeMap("src/main/resources/skybox/irradiance", ".hdr", false);

        skyBox.setCubeMap(cm);
        skyBox.setRadiance(radiance_cm);
        skyBox.setIrradiance(irradiance_cm);

        skyBoxShader = new Shader("src/main/glsl/skybox_shaders/skybox_vertex.glsl",
                "src/main/glsl/skybox_shaders/skybox_frag.glsl");

        camera.setViewProjectionMatrix(skyBoxShader);
        skyBox.compile();

        Keyboard keyboard = new Keyboard();
        keyboard.setWindow_pointer(window_handle);
        keyboard.init();

        Mouse mouse = new Mouse();
        mouse.setWindow_pointer(window_handle);
        mouse.init();

        KeyboardEventHandler keyboard_handler = (key, state) -> {
            if (state == GLFW_PRESS) {
                if (key == GLFW_KEY_A) {
                    System.out.println("A");
                    fightingFalcon.getRotQuaternion().rotateY(0.01f).normalize();
                    camera.getQuaternionf().rotateY(0.01f).normalize();

                } else if (key == GLFW_KEY_D) {
                    System.out.println("D");
                    fightingFalcon.getRotQuaternion().rotateY(-0.01f).normalize();
                    camera.getQuaternionf().rotateY(-0.01f).normalize();

                } else if (key == GLFW_KEY_W) {
                    System.out.println("W");
                    fightingFalcon.getPosition().add(fightingFalcon.getRotQuaternion()
                            .transform(new Vector3f(1,0,0)));
                } else if (key == GLFW_KEY_S) {
                    System.out.println("S");
                    fightingFalcon.getRotQuaternion().rotateX(0.01f).normalize();
                    camera.getQuaternionf().rotateX(0.01f).normalize();

                } else if (key == GLFW_KEY_C) {
                    fightingFalcon.getRotQuaternion().rotateZ(0.01f).normalize();
                    camera.getQuaternionf().rotateZ(0.01f).normalize();

                } else if (key == GLFW_KEY_V) {
                    fightingFalcon.getRotQuaternion().rotateZ(-0.01f).normalize();
                    camera.getQuaternionf().rotateZ(-0.01f).normalize();

                }


                fightingFalcon.setUpdated(true);
                camera.setFocus(fightingFalcon.getPosition());
                camera.setPosition(fightingFalcon.getPosition()
                        .add(new Vector3f(-3,1,0)
                                .rotate(fightingFalcon.getRotQuaternion()), new Vector3f()));
                //camera.setPosition(camera.getQuaternionf(), new Vector3f(-3, 1, 0));
                camera.loadViewMatrix();
//                shadowCamera.setFocus(fightingFalcon.getPosition());
//                shadowCamera.setPosition(fightingFalcon.getPosition()
//                        .add(new Vector3f(-30,40,30),new Vector3f()));
//                shadowCamera.loadViewMatrix();
//                shadowCamera.setShadowViewProjectionMatrix(deferredShader);
                camera.setViewProjectionMatrix(skyBoxShader);
            }
        };

        MouseEventHandler mouse_handler = new MouseEventHandler() {
            @Override
            public void processMouseEvent(int key, int action) {
//                if(action == GLFW_PRESS){
//                    if(key == GLFW_MOUSE_BUTTON_LEFT){
//                        fightingFalcon.getRotQuaternion().x = 0;
//                        fightingFalcon.getRotQuaternion().y = 0;
//                        fightingFalcon.getRotQuaternion().z = 0;
//                        fightingFalcon.getRotQuaternion().w = 1;
//                        //fightingFalcon.setUpdated(true);
//
//                        camera.getQuaternionf().x = 0;
//                        camera.getQuaternionf().y = 0;
//                        camera.getQuaternionf().z = 0;
//                        camera.getQuaternionf().w = 1;
//
//                        //camera.setPosition(fightingFalcon.getRotQuaternion(), new Vector3f(-3,1,0));
//                        camera.loadViewMatrix();
//                    }
//                }
            }
            @Override
            public void processMouseMovement(double X, double Y) {
//                System.out.println(STR."(X,Y)= {\{X} \{Y}}");
                ImGuiIO io = ImGui.getIO();
                io.setMousePos((float) X, (float) Y);
            }
        };


        String os = System.getProperty("os.name");
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();

        long mb = systemInfo.getHardware().getMemory().getTotal();
        int processors = Runtime.getRuntime().availableProcessors();

        String name = processor.getProcessorIdentifier().getName();

        InterfaceRenderer ioRenderer = () -> {
            ImGui.setNextWindowSize(new ImVec2(215, 160));
            if (ImGui.begin("System Info")) {
                ImGui.text(os);
                ImGui.text(STR."\{mb} MB");
                ImGui.text(STR."\{processors} Cores");
                ImGui.text(STR."\{name}");
                ImGui.pushID(1);
                if (ImGui.button("Text", new ImVec2(200, 50))) {
                    System.out.println("TEXT");
                }
                ImGui.popID();
                ImGui.end();
            }
        };

        scene.setSkyBox(skyBox);


        camera.setViewProjectionMatrix(deferredShader);
        camera.setViewProjectionMatrix(combineShader);
        scene.setActiveProgram(deferredShader);


        while (!glfwWindowShouldClose(window_handle)){
           glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
           glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

                shadowPass();

           camera.setViewProjectionMatrix(deferredShader);

                deferredPass();


            skyBoxShader.use();
                glActiveTexture(GL_TEXTURE10);
                skyBox.draw();
            skyBoxShader.unbind();



            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            glDisable(GL_DEPTH_TEST);
            postprocessingPass();
            glEnable(GL_DEPTH_TEST);

            renderUI(ioRenderer);
            keyboard.processEvents(keyboard_handler);
            mouse.processEvents(mouse_handler);


            glfwPollEvents();
            glfwSwapBuffers(window_handle);
        }

    }

    @Override
    public void close() {
        glfwDestroyWindow(window_handle);
    }



    public static void saveFramebufferAsImage(int width, int height, String filePath) {
        // Allocate a buffer to store the pixel data (RGBA, 8-bit per channel)
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        // Read the pixels from the currently bound framebuffer
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        // Create a BufferedImage to store the pixel data
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Flip Y-axis because OpenGL's origin is at the bottom-left
                int index = (x + (height - y - 1) * width) * 4;

                int r = buffer.get(index) & 0xFF;
                int g = buffer.get(index + 1) & 0xFF;
                int b = buffer.get(index + 2) & 0xFF;

                int a = buffer.get(index + 3) & 0xFF;

                // Create a pixel with ARGB format (Java uses ARGB)
                int pixel = (a << 24) | (r << 16) | (g << 8) | b;

                image.setRGB(x, y, pixel);
            }
        }

        // Save the BufferedImage as a PNG file
        try {
            ImageIO.write(image, "PNG", new File(filePath));
            System.out.println(STR."Image saved: \{filePath}");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}

