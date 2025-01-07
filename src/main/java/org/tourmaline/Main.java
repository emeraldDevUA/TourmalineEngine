package org.tourmaline;

import Annotations.BasicWindow;
import Controls.Keyboard;
import Controls.Mouse;
import Effects.JetEffect;
import Interfaces.InterfaceRenderer;
import Interfaces.KeyboardEventHandler;
import Interfaces.MouseEventHandler;
import Interfaces.TreeNode;
import Liquids.LiquidBody;
import Rendering.Camera;
import Rendering.Scene;
import Rendering.SkyBox;
import ResourceImpl.*;
import ResourceLoading.AutoLoader;
import ResourceLoading.ResourceLoadScheduler;

import Annotations.OpenGLWindow;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import org.joml.Quaternionf;
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
import java.util.Map;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.*;

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
        AutoLoader autoLoader = new AutoLoader("src/main/resources/3D_Models", resourceLoadScheduler);
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

        visualEffectsShader = new Shader("src/main/glsl/visualeffects_shaders/visual_effects_vertex.glsl",
                "src/main/glsl/visualeffects_shaders/visual_effects_fragment.glsl");

        skyBoxShader = new Shader("src/main/glsl/skybox_shaders/skybox_vertex.glsl",
                "src/main/glsl/skybox_shaders/skybox_frag.glsl");

        Texture.setVerticalFlip(true);

        t1 = System.currentTimeMillis();
        autoLoader.loadTrees();
        t2 = System.currentTimeMillis();
        autoLoader.asyncLoad();

        while (autoLoader.getReadiness() < 1){
            System.out.println();
        }

        t3 = System.currentTimeMillis();//
         resourceLoadScheduler.reset();

       System.out.printf("Async load took %d ms, Resource init took %d ms", t2-t1, t3-t2);

        MeshTree F16 = autoLoader.getDrawables().get("F16");
        MeshTree S300 = autoLoader.getDrawables().get("S300");
        MeshTree Island = autoLoader.getDrawables().get("Map");

        F16.compile();
        S300.compile();
        Island.compile();

        F16.getNodeValue().getPosition().add(new Vector3f(10,10,10));
        Vector3f temp = F16.getNodeValue().getPosition();

        camera = new Camera(
                temp.add(new Vector3f(-3,1,0), new Vector3f()),
                temp.add(new Vector3f(0,0,0), new Vector3f()));

        shadowCamera = new Camera(
                temp.add(new Vector3f(-90,120,20), new Vector3f()),
                temp.add(new Vector3f(0,0,0), new Vector3f()));

        camera.loadViewMatrix();
        shadowCamera.loadViewMatrix();

        camera.loadPerspectiveProjection((float)Math.PI/3,1.8f, 1000,0.1f);
        shadowCamera.loadOrthographicProjection
                (-1500,1500, -1500, 1500, -800, 800);
        camera.setViewProjectionMatrix(skyBoxShader);


        F16.getNodeValue().getMaterial().addProperty(Material.ROUGHNESS_MAP, 0.8);
        Island.getNodeValue().getMaterial().addProperty(Material.ROUGHNESS_MAP, 0.9);
        Island.getNodeValue().getMaterial().addProperty(Material.METALNESS_MAP, 0.2);

        Material mat = new Material();
        mat.addColor(Material.ALBEDO_MAP, new Vector3f(6f/255f, 64f/255f, 43f/255f));
        S300.getNodeValue().setMaterial(mat);

        F16.getNodeValue().setShadowScale(new Vector3f(10));


        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_CULL_FACE);

        SkyBox skyBox = new SkyBox();

        CubeMap cm =
                new CubeMap("src/main/resources/skybox/skybox", ".hdr", false);
        CubeMap radiance_cm =
                new CubeMap("src/main/resources/skybox/radiance", ".hdr", false);
        CubeMap irradiance_cm =
                new CubeMap("src/main/resources/skybox/irradiance", ".hdr", false);

        skyBox.setCubeMap(cm);
        skyBox.setRadiance(radiance_cm);
        skyBox.setIrradiance(irradiance_cm);

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
                    F16.getRotQuaternion().rotateY(0.01f).normalize();
                    camera.getQuaternionf().rotateY(0.01f).normalize();

                } else if (key == GLFW_KEY_D) {
                    System.out.println("D");
                    F16.getRotQuaternion().rotateY(-0.01f).normalize();
                    camera.getQuaternionf().rotateY(-0.01f).normalize();

                } else if (key == GLFW_KEY_W) {
                    System.out.println("W");
                    F16.getPosition().add(F16.getRotQuaternion()
                            .transform(new Vector3f(1,0,0)));
                } else if (key == GLFW_KEY_S) {
                    System.out.println("S");
                    F16.getRotQuaternion().rotateX(0.01f).normalize();
                    camera.getQuaternionf().rotateX(0.01f).normalize();

                } else if (key == GLFW_KEY_C) {
                    F16.getRotQuaternion().rotateZ(0.01f).normalize();
                    camera.getQuaternionf().rotateZ(0.01f).normalize();

                } else if (key == GLFW_KEY_V) {
                    F16.getRotQuaternion().rotateZ(-0.01f).normalize();
                    camera.getQuaternionf().rotateZ(-0.01f).normalize();

                }

                F16.setUpdated(true);
                camera.setFocus(F16.getPosition());
                camera.setPosition(F16.getPosition()
                        .add(new Vector3f(-3,1,0)
                                .rotate(F16.getRotQuaternion()), new Vector3f()));
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
                if(action == GLFW_PRESS){
                    if(key == GLFW_MOUSE_BUTTON_LEFT){
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


                    }

                }


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
        String gpu = systemInfo.getHardware().getGraphicsCards().get(1).getName();


        InterfaceRenderer combinedRenderer = () -> {
            // System Info Window
            ImGui.setNextWindowPos(new ImVec2(0, 0));
            ImGui.setNextWindowSize(new ImVec2(215, 190));
            if (ImGui.begin("System Info")) {
                ImGui.text(os);
                ImGui.text(STR."\{mb} MB");
                ImGui.text(STR."\{processors} Cores");
                ImGui.text(STR."\{name}");
                ImGui.text(STR."\{gpu}");
                ImGui.pushID(1);
                ImGui.pushStyleColor(ImGuiCol.Button, ImGui.getColorU32(1.0f, 0.08f, 0.58f, 1.0f)); // Hot pink
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, ImGui.getColorU32(1.0f, 0.4f, 0.7f, 1.0f)); // Brighter hot pink for hover
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, ImGui.getColorU32(1.0f, 0.2f, 0.6f, 1.0f)); // Slightly darker pink for active

                if (ImGui.button("Text", new ImVec2(200, 50))) {
                    System.out.println("TEXT");
                }

                ImGui.popStyleColor(3); // Pop all three colors
                ImGui.popID();
                ImGui.end();
            }

            // Item List Window
            ImGui.setNextWindowPos(new ImVec2(1920 - 500, 0));
            ImGui.setNextWindowSize(new ImVec2(500, 150)); // Increased height to accommodate checkboxes
            if (ImGui.begin("Item List")) {
                // Table headers
                if (ImGui.beginTable("Table", 5, ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg)) {
                    ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WidthStretch);
                    ImGui.tableSetupColumn("Distance", ImGuiTableColumnFlags.WidthFixed);
                    ImGui.tableSetupColumn("Type", ImGuiTableColumnFlags.WidthStretch);
                    ImGui.tableSetupColumn("State", ImGuiTableColumnFlags.WidthFixed);
                    ImGui.tableSetupColumn("Selected", ImGuiTableColumnFlags.WidthFixed);
                    ImGui.tableHeadersRow();

                    // Example list of items
                    String[][] items = {
                            {"S-300", "10 km", "Type 1", "Alive"},
                            {"Mi-8", "20 km", "Type 2", "Alive"},
                            {"Item C", "5 km", "Type 3", "Destroyed"},
                            {"Item D", "15 km", "Type 1", "Destroyed"},
                    };

                    // Checkbox states stored in an ArrayList
                    ArrayList<Boolean> selected = new ArrayList<>();
                    for (int i = 0; i < items.length; i++) {
                        selected.add(i%2==0); // Default state is unchecked
                    }

                    // Loop through and display each item
                    for (int i = 0; i < items.length; i++) {
                        String[] item = items[i];
                        ImGui.tableNextRow();

                        // Name column
                        ImGui.tableSetColumnIndex(0);
                        ImGui.text(item[0]);

                        // Distance column
                        ImGui.tableSetColumnIndex(1);
                        ImGui.text(item[1]);

                        // Type column
                        ImGui.tableSetColumnIndex(2);
                        ImGui.text(item[2]);

                        // State column
                        ImGui.tableSetColumnIndex(3);
                        if ("Alive".equals(item[3])) {
                            ImGui.textColored(new ImVec4(0.0f, 1.0f, 0.0f, 1.0f), item[3]); // Green for Alive
                        } else {
                            ImGui.textColored(new ImVec4(1.0f, 0.0f, 0.0f, 1.0f), item[3]); // Red for Destroyed
                        }

                        // Checkbox column
                        ImGui.tableSetColumnIndex(4);
                        boolean isSelected = selected.get(i);
                        if (ImGui.checkbox(STR."##checkbox\{i}", isSelected)) {
                            selected.set(i, !isSelected); // Toggle the state
                        }
                    }

                    ImGui.endTable();
                }
                ImGui.end();
            }
        };

        scene.setSkyBox(skyBox);


        camera.setViewProjectionMatrix(deferredShader);
        camera.setViewProjectionMatrix(combineShader);
        scene.setActiveProgram(deferredShader);


        deferredShader.setUniform("isWater", false);
        deferredShader.setUniform("waveNumber", 3);

        LiquidBody liquidBody = new LiquidBody("src/main/resources/miscellaneous/water.jpg");
        Map<String, List<?>> list = liquidBody.generateWater(768, 240, 2200);

        Mesh water = new Mesh("Water", list);
        water.compile();
        water.setShader(deferredShader);
        water.getPosition().add(new Vector3f(-140,-33,-325));
        liquidBody.getWaterMeshes().put(4, water);

        waterBodies.add(liquidBody);

        JetEffect jetStream = new JetEffect();
        jetStream.setMainPosition(F16.getPosition());
        jetStream.setMainRotation(F16.getRotQuaternion());
        jetStream.compile();

        //scene.addDrawItem(new MeshTree(null, jetStream.getMesh(), "th"));
        jetStream.getMesh().setShader(visualEffectsShader);
        scene.addEffect(jetStream);

        S300.getNodeValue().setPosition(new Vector3f(150,-49,-280));
        S300.getNodeValue().setScale(new Vector3f(2,2,2));

        scene.addDrawItem(S300);
        scene.addDrawItem(Island);
        scene.addDrawItem(F16);

        while (!glfwWindowShouldClose(window_handle)){

            jetStream.getMesh().setPosition(new Vector3f(F16.getPosition()).sub(F16.getRotQuaternion().transform(
                    new Vector3f(2.2f,0.1f,-0.015f)))
            );
            visualEffectsShader.setUniform("rocketPos", F16.getPosition());
           //glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);

           glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

                shadowPass();

                deferredPass();


            skyBoxShader.use();
                glActiveTexture(GL_TEXTURE10);
                skyBox.draw();
            skyBoxShader.unbind();


            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            glDisable(GL_DEPTH_TEST);
            postprocessingPass();
            glEnable(GL_DEPTH_TEST);

            keyboard.processEvents(keyboard_handler);
            mouse.processEvents(mouse_handler);
            renderUI(combinedRenderer);


            glfwPollEvents();
            glfwSwapBuffers(window_handle);
            measureTime();
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

