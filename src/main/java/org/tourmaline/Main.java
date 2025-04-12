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
import imgui.*;
import imgui.flag.ImGuiCol;

import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;



@OpenGLWindow(windowName = "Complex Example", defaultDimensions = {1920,1018},
        windowHints = {GLFW_DECORATED}, windowHintsValues={GLFW_TRUE}, shadowMapResolution = 8192)

public class Main extends BasicWindow {


    public static void main(String[] args) throws IOException {

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


        transparentShader = new Shader("src/main/glsl/vertex_test.vert",
                "src/main/glsl/fragment_test.frag");
        Texture.setVerticalFlip(true);
        Mesh.setUseAssimp(true);

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

        MeshTree F16 = autoLoader.getDrawables().get("Rafale");


        MeshTree S300 = autoLoader.getDrawables().get("S300");
        MeshTree Island = autoLoader.getDrawables().get("Map");

        F16.compile();
        S300.compile();
        Island.compile();


        F16.setPosition(new Vector3f(50,10,10));

        Vector3f temp = F16.getNodeValue().getPosition();

        camera = new Camera(
                temp.add(new Vector3f(3,1,0), new Vector3f()),
                temp.add(new Vector3f(0,0,0), new Vector3f()));

        shadowCamera = new Camera(
                temp.add(new Vector3f(-90,120,20), new Vector3f()),
                temp.add(new Vector3f(0,0,0), new Vector3f()));

        camera.loadViewMatrix();
        shadowCamera.loadViewMatrix();

        camera.loadPerspectiveProjection((float)Math.PI/3,1.8f, 2000,0.1f);
        shadowCamera.loadOrthographicProjection
                (-1500,1500, -1500, 1500, -800, 800);
        camera.setViewProjectionMatrix(skyBoxShader);


        Material mainMat = F16.getNodeValue().getMaterial();
        mainMat.addProperty(Material.ROUGHNESS_MAP, 0.8);
//        F16.findNode("F16_Aileron1.obj").getMaterial().close();
//        F16.findNode("F16_Aileron2.obj").getMaterial().close();
//        F16.findNode("F16_Elevator.obj").getMaterial().close();
//        F16.findNode("F16_Rudder.obj").getMaterial().close();
//
//
//        F16.findNode("F16_Aileron1.obj").setMaterial(mainMat);
//        F16.findNode("F16_Aileron2.obj").setMaterial(mainMat);
//        F16.findNode("F16_Elevator.obj").setMaterial(mainMat);
//        F16.findNode("F16_Rudder.obj").setMaterial(mainMat);
//
//        F16.findNode("F16_glass.obj").setEnableBlending(true);

        Island.getNodeValue().getMaterial().addProperty(Material.ROUGHNESS_MAP, 0.6);
        Island.getNodeValue().getMaterial().addProperty(Material.METALNESS_MAP, 0.3);

        Material mat = new Material();
        mat.addColor(Material.ALBEDO_MAP, new Vector3f(6f/255f, 64f/255f, 43f/255f));
        mat.addProperty(Material.METALNESS, 0.0);
        S300.getNodeValue().setMaterial(mat);

        //F16.getNodeValue().setShadowScale(new Vector3f(10));
        F16.forwardTransform();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_CULL_FACE);

        SkyBox skyBox = new SkyBox(
                new CubeMap("src/main/resources/skybox/skybox", ".hdr", false),
                new CubeMap("src/main/resources/skybox/radiance", ".hdr", false),
                new CubeMap("src/main/resources/skybox/irradiance", ".hdr", false)
        );

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
                        .add(new Vector3f(-20,3,0)
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

            @Override
            public void processScrolling(double offSet) {

            }
        };

        ImFont smallFont = ImGui.getIO().getFonts()
                .addFontFromFileTTF("src/main/resources/miscellaneous/Iceland-Regular.ttf", 16);
        ImFont largeFont = ImGui.getIO().getFonts()
                .addFontFromFileTTF("src/main/resources/miscellaneous/Iceland-Regular.ttf", 36);
        ImGui.getIO().getFonts().build();

        String os = System.getProperty("os.name");
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();

        long mb = systemInfo.getHardware().getMemory().getTotal()/1_000_000_000;
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println(mb);

        String cpu_name = processor.getProcessorIdentifier().getName().substring(0, 24).concat("...");
        String gpu = systemInfo.getHardware().getGraphicsCards().get(1).getName()
                .substring(0, 24).concat("...");

        ArrayList<Boolean> selected = new ArrayList<>(4);
        List<Boolean> selectedMissile = new ArrayList<>(9);
        for(int i = 0; i < 4; i++){selected.add(false);}
        for (int i = 0; i < 9; i++) {selectedMissile.add(false);}


        InterfaceRenderer combinedRenderer = () -> {
            // System Info Window
            ImGui.setNextWindowPos(new ImVec2(0, 0));
            ImGui.setNextWindowSize(new ImVec2(215, 190));
            ImGui.setNextWindowSizeConstraints(new ImVec2(215, 190),
                    new ImVec2(215, 190));
            ImGui.pushFont(smallFont);
            if (ImGui.begin("System Info")) {
                ImGui.text(os);
                ImGui.text(STR."\{mb} GB");
                ImGui.text(STR."\{processors} Cores");
                ImGui.text(STR."\{cpu_name}");
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
            ImGui.setNextWindowSize(new ImVec2(500, 470)); // Increased height to accommodate checkboxes
            if (ImGui.begin("Item List")) {
                // Table headers
                if (ImGui.beginTable("Table", 5, ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg)) {
                    ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WidthStretch);
                    ImGui.tableSetupColumn("Distance", ImGuiTableColumnFlags.WidthFixed);
                    ImGui.tableSetupColumn("Type", ImGuiTableColumnFlags.WidthStretch);
                    ImGui.tableSetupColumn("State", ImGuiTableColumnFlags.WidthFixed);
                    ImGui.tableSetupColumn("Selected", ImGuiTableColumnFlags.WidthFixed);
                    ImGui.tableHeadersRow();

                    // Example items for the table
                    String[][] items = {
                            {"S-300", "10 km", "Type 1", "Alive"},
                            {"Mi-8", "20 km", "Type 2", "Alive"},
                            {"Item C", "5 km", "Type 3", "Destroyed"},
                            {"Item D", "15 km", "Type 1", "Destroyed"},
                    };

                    // Table rows
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
                            ImGui.textColored(new ImVec4(0.0f, 1.0f, 0.0f, 1.0f), item[3]);
                        } else {
                            ImGui.textColored(new ImVec4(1.0f, 0.0f, 0.0f, 1.0f), item[3]);
                        }

                        // Checkbox column with single selection logic
                        ImGui.tableSetColumnIndex(4);
                        if (ImGui.checkbox(STR."##checkbox\{i}", selected.get(i))) {
                            // Deselect all checkboxes
                            Collections.fill(selected, false);
                            // Select the clicked checkbox
                            selected.set(i, true);
                        }
                    }
                    ImGui.endTable();
                }


                if (ImGui.beginTable("MissileTable", 2, ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg)) {
                    ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WidthStretch);
                    ImGui.tableSetupColumn("Selected", ImGuiTableColumnFlags.WidthFixed);

                    ImGui.tableHeadersRow();

                    // Missile points for the table
                    String[] missilePoints = {
                            "1", "2", "3",
                            "4", "5", "6",
                            "7", "8", "9"
                    };


                    // Render table rows
                    for (int i = 0; i < missilePoints.length; i++) {
                        ImGui.tableNextRow();

                        // Column 0: Display the missile point
                        ImGui.tableSetColumnIndex(0);
                        ImGui.text(missilePoints[i]);

                        // Column 1: Checkbox for selection
                        ImGui.tableSetColumnIndex(1);
                        boolean isSelected = selectedMissile.get(i); // Get current state
                        if (ImGui.checkbox(STR."##checkbox\{i}", isSelected)) {
                            Collections.fill(selectedMissile, false);
                            // Select the clicked checkbox
                            selectedMissile.set(i, true);
                        }
                    }

                    ImGui.endTable();

                    ImGui.pushStyleColor(ImGuiCol.Button, ImGui.colorConvertFloat4ToU32(
                            new ImVec4(0.3f, 0.3f, 0.7f, 1.0f))); // Base color
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, ImGui.colorConvertFloat4ToU32(
                            new ImVec4(0.4f, 0.4f, 0.9f, 1.0f))); // Hover color
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, ImGui.colorConvertFloat4ToU32(
                            new ImVec4(0.2f, 0.2f, 0.6f, 1.0f))); // Active color

                    ImGui.popFont();
                    ImGui.pushFont(largeFont);


                    if (ImGui.button("Fire       ", new ImVec2(570f, 40f))) {
                        System.out.println("TEXT");
                    }

                    ImGui.popFont();
                    // Restore the default colors
                    ImGui.popStyleColor(3);

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
        liquidBody.wavelength = 20;
        liquidBody.amplitude = 1;
        liquidBody.steepness = 0.5f;
        Map<String, List<?>> list =
                liquidBody.generateWater(768, 240, 2200);

        Mesh water = new Mesh("Water", list);
        water.compile();
        water.setShader(deferredShader);
        water.getPosition().add(new Vector3f(-140,-33,-325));
        water.getMaterial().addMap(Material.NORMAL_MAP, new Texture("src/main/resources/miscellaneous/waternormals.jpg", 4));
        liquidBody.getWaterMeshes().put(4, water);


        waterBodies.add(liquidBody);

        JetEffect jetStream = new JetEffect();

        jetStream.setMainPosition(F16.getPosition());
        jetStream.setMainRotation(F16.getRotQuaternion());
        jetStream.compile();

        ExplosionEffect explosionEffect = new ExplosionEffect();
explosionEffect.setExistenceTime(20);
        explosionEffect.setScaleVector(new Vector3f(0.6f));
        explosionEffect.setMainPosition(S300.getPosition());
        explosionEffect.compile();

        explosionEffect.getMesh().setShader(visualEffectsShader);
        scene.addEffect(explosionEffect);
        //scene.addDrawItem(new MeshTree(null, jetStream.getMesh(), "th"));
        jetStream.getMesh().setShader(visualEffectsShader);
        scene.addEffect(jetStream);

        S300.setPosition(new Vector3f(150,-49,-280));
        S300.getNodeValue().setScale(new Vector3f(2,2,2));
        explosionEffect.setMainPosition(S300.getPosition());
        scene.addDrawItem(S300);
        scene.addDrawItem(Island);
        scene.addDrawItem(F16);
        F16.traverse(mesh -> mesh.setEnableReflection(false));
        Island.traverse(mesh -> mesh.setEnableReflection(false));

        PointLight pointLight = new PointLight(new Vector3f(10,10,10));
        pointLight.setLightColor(new Vector3f(1,0,0));  pointLight.generatePrimitive();

        scene.addLightSources(pointLight, true);



        PointLight pointLight2 = new PointLight(new Vector3f(10,50,10));
        pointLight2.setLightColor(new Vector3f(0,0,1));  pointLight2.generatePrimitive();

        scene.addLightSources(pointLight2, true);

        PointLight pointLight3 = new PointLight(new Vector3f(90,40,90));
        pointLight3.setLightColor(new Vector3f(1,1,0));  pointLight3.generatePrimitive();

        scene.addLightSources(pointLight3, true);

        DirectionalLight directionalLight = new DirectionalLight(
                new Vector3f(-90,120,20).negate().normalize());
        directionalLight.setLightColor(new Vector3f(2.8f, 2.8f, 2.8f).div(10));
        directionalLight.setLightIntensity(15);
        scene.addLightSources(directionalLight, true);

        LightingConfigurator.setLights(scene.getLights(), combineShader);

        BoundingBoxEffect BB = new BoundingBoxEffect(visualEffectsShader,
                new Vector3f(S300.getPosition()), new Vector3f(8,5f,20), S300.getRotQuaternion());
        BB.compile();

        scene.addEffect(BB);

        while (!glfwWindowShouldClose(window_handle)){
            camera.loadViewMatrix();

            jetStream.getMesh().setPosition(
                    new Vector3f(F16.getPosition()).sub(F16.getRotQuaternion()
                            .transform(new Vector3f(2.2f,0.1f,-0.015f)))
            );

           glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

           shadowPass();
           deferredPass();

            skyBoxShader.use();
            glActiveTexture(GL_TEXTURE10);
            scene.getSkyBox().draw();
            skyBoxShader.unbind();

//            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            glDisable(GL_DEPTH_TEST);
                postprocessingPass();
            glEnable(GL_DEPTH_TEST);
            transparentPass();



            keyboard.processEvents(keyboard_handler);
            mouse.processEvents(mouse_handler);
            renderUI(combinedRenderer);


            glfwPollEvents();
            glfwSwapBuffers(window_handle);
            measureTime();
        }

    }






}


