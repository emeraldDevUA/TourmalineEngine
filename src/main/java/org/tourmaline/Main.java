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
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import org.lwjgl.BufferUtils;
import org.tourmaline.Collision.BoundingBox;
import org.tourmaline.PlanePhysics.Airfoil.Airfoil;
import org.tourmaline.PlanePhysics.Airfoil.Constants;
import org.tourmaline.PlanePhysics.Engine;

import org.tourmaline.Processing.PhysicsProcessor;
import org.tourmaline.RigidBody.RigidBody;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.joml.Math.*;

import static org.lwjgl.glfw.GLFW.*;


import static org.lwjgl.opengl.ARBUniformBufferObject.GL_INVALID_INDEX;
import static org.lwjgl.opengl.ARBUniformBufferObject.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.tourmaline.PlanePhysics.Airfoil.Airfoil.arrayToList;


@OpenGLWindow(windowName = "Complex Example", defaultDimensions = {1920,1018},
        windowHints = {GLFW_DECORATED}, windowHintsValues={GLFW_TRUE}, shadowMapResolution = 8192)

public class Main extends BasicWindow {

    static float scale = 1f/10f;
    static float aileron =0;
    static float elevator = 0;
    static float rudder = 0f;
    static float dt = 0.01f;
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


        resourceLoadScheduler.addResource(albedo,"src/main/resources/3D_Models/Eurofighter/Eurofighter_albedo.png");
        resourceLoadScheduler.addResource(normal,"src/main/resources/3D_Models/Eurofighter/Eurofighter_normal.png");
        resourceLoadScheduler.addResource(roughness_g,"src/main/resources/3D_Models/F16/F16_roughness_G.png");
        resourceLoadScheduler.addResource(roughness_b,"src/main/resources/3D_Models/F16/F16_roughness_B.png");
        resourceLoadScheduler.addResource(metalness,"src/main/resources/3D_Models/F16/F16_metalness.png");


        resourceLoadScheduler.addResource(euroFighter,"src/main/resources/3D_Models/F16/F16.obj");
        resourceLoadScheduler.addResource(fightingFalcon, "src/main/resources/3D_Models/Eurofighter/Eurofighter.obj");
        resourceLoadScheduler.addResource(mig29, "src/main/resources/3D_Models/MIG29/MIG29.obj");

        resourceLoadScheduler.addResource(land, "src/main/resources/3D_Models/Map/Map.obj");
        resourceLoadScheduler.addResource(land_alb, "src/main/resources/3D_Models/Map/Map_Albedo.jpeg");
        resourceLoadScheduler.addResource(land_normal, "src/main/resources/3D_Models/Map/Map_Normal.jpg");


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
        fightingFalcon.getPosition().add(new Vector3f(0,8000,0));

        Vector3f temp = fightingFalcon.getPosition();

        camera = new Camera(
                temp.add(new Vector3f(-3,1,0), new Vector3f()),
                temp.add(new Vector3f(0,0,0), new Vector3f()));

        shadowCamera = new Camera(
                temp.add(new Vector3f(-90,120,20), new Vector3f()),
                temp.add(new Vector3f(0,0,0), new Vector3f()));


        camera.loadViewMatrix();
        shadowCamera.loadViewMatrix();
        camera.loadPerspectiveProjection((float)Math.PI/3,1.8f, 4000,0.1f);
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



        float wing_offset = -1.0f;
        float tail_offset = -6.6f;
        Matrix3f inertia = new Matrix3f(
                46311.668f, -660.000f, -0.000f,
                -660.000f,188713.797f,-0.000f,
                -0.000f, -0.000f,147367.125f);

        Engine jetEngine = new Engine(13000);
//        ArrayList<Wing> wings = new ArrayList<>();
//
//        Airfoil airfoil0012 = new Airfoil(arrayToList(Constants.NACA_0012));
//
//        Airfoil airfoil2412 = new Airfoil(arrayToList(Constants.NACA_2412));
//
//
//        wings.add(new Wing(new Vector3f(wing_offset,0, -2.7f), 6.96f, 2.50f,
//                airfoil2412, new Vector3f(0,1,0), 0.2f));
//
//        wings.add(new Wing(new Vector3f(wing_offset,0, 2.7f), 6.96f, 2.50f,
//                airfoil2412, new Vector3f(0,1,0), 0.2f));
//
//        wings.add(new Wing(new Vector3f(tail_offset,-0.1f, 0.0f), 6.54f, 2.70f,
//                airfoil0012, new Vector3f(0,1,0), 1f));
//
//        wings.add(new Wing(new Vector3f(tail_offset,0.0f, 0.0f), 5.31f, 3.1f,
//                airfoil0012, new Vector3f(0,0,1), 0.15f));


        RigidBody plane = new RigidBody(inertia, new Vector3f(0,200,0), 9000);
        plane.setEnableGravity(true);
        fightingFalcon.getPosition().set(plane.getPosition().mul(scale, new Vector3f()));

        KeyboardEventHandler keyboard_handler = (key, state) -> {
            Vector3f factor = new Vector3f(2.4f, 6f, 4.0f);
            if (state == GLFW_PRESS) {

                Quaternionf planeOrientation = plane.getOrientation();
                if(key == GLFW_KEY_W){
                    plane.applyForceAtPoint(
                            planeOrientation.transform((new Vector3f(0,factor.x*9000,0))),
                            planeOrientation.transform(new Vector3f(5,0,0)));

                    //aileron  = move(aileron, factor.x, dt);;
                    //   aileron =320f;
                }else if(key == GLFW_KEY_S) {
                    plane.applyForceAtPoint(    planeOrientation.transform(
                            new Vector3f(0,-factor.x*9000,0)),
                            planeOrientation.transform(new Vector3f(5,0,0)));

                    //aileron  = move(aileron, -factor.x, dt);;
                    //     aileron =- 320f;
                }
                else{



                }


                if(key == GLFW_KEY_A){
                    plane.applyForceAtPoint( planeOrientation.transform(
                            new Vector3f(0, factor.y*9000 ,0)),
                            planeOrientation.transform(new Vector3f(wing_offset,0,2.7f)));

                    rudder  = move(rudder, factor.y, dt);

                }else if(key == GLFW_KEY_D) {

                    plane.applyForceAtPoint(
                            planeOrientation.transform(new Vector3f(0,  factor.y*9000,0)),
                            planeOrientation.transform(
                                    new Vector3f(wing_offset,0,-2.7f)));

                    rudder  = move(rudder, -factor.y, dt);

                }
                if(key == GLFW_KEY_C){

                    plane.applyForceAtPoint(new Vector3f(0, 0,factor.z*9000),
                            new Vector3f(tail_offset,0,0f));
                    elevator = move(rudder, factor.z, dt);

                }else if(key == GLFW_KEY_V) {
                    plane.applyForceAtPoint(new Vector3f(0, 0,-factor.z*9000),
                            new Vector3f(tail_offset,0,0f));
                }

                else{
                   elevator = center(rudder, factor.z, dt);
                }

                if(key == GLFW_KEY_M) {
                    float thrust = 135000f;
                    plane.applyForceAtPoint(new Vector3f(thrust,0,0), new Vector3f(0));

                    Vector3f dir = fightingFalcon.getRotQuaternion()
                            .transform(new Vector3f(0,1,0));
                    dir.normalize();
                    float num = dir.dot(new Vector3f(0,1,0));
                    num/=abs(num);
                    plane.applyForceAtPoint(new Vector3f(0,num*thrust/2,0), new Vector3f(0));

            }   else if(key == GLFW_KEY_N) {
                    float thrust = 1000;
                    plane.applyForceAtPoint(new Vector3f(-thrust,0,0), new Vector3f(0));

                }


                Vector3f velocity = new Vector3f(plane.getVelocity()).normalize();
                
                Vector3f rotatedVec = planeOrientation
                        .transform(new Vector3f(1, 0, 0)).normalize();
                // Assuming (1, 0, 0) is the default direction
                float interpolationFactor = 0.1f;
                // Adjust this for how fast you want the interpolation
                Vector3f interpolatedDir = velocity
                        .lerp(rotatedVec, interpolationFactor).normalize();
                plane.setVelocity(interpolatedDir.mul(plane.getVelocity().length()));


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

        NumberFormat numberFormat = NumberFormat.getNumberInstance(
                new Locale("uk", "UA"));

        numberFormat.setMaximumFractionDigits(2);
        InterfaceRenderer ioRenderer = () -> {

            ImGui.setNextWindowSize(new ImVec2(245, 160));
            if (ImGui.begin("System Info")) {
                ImGui.text(STR."Pos:\{plane.getPosition().toString(numberFormat)}");
                ImGui.text(STR."Vel:\{STR."\{plane.getVelocity().toString(numberFormat)} \{plane.getVelocity().length()} m/s"}");
                ImGui.text(STR."XYZW: \{plane.getOrientation().toString(numberFormat)}");
                ImGui.text(STR."AngVel: \{plane.getAngularVelocity().toString(numberFormat)} \{plane.getAngularVelocity().length()} rad/s");

                ImGui.text(STR."a: \{plane.getAcceleration().toString(numberFormat)} \{plane.getAcceleration().length()} m/s^2");

                ImGui.end();
            }
        };

        scene.setSkyBox(skyBox);


        camera.setViewProjectionMatrix(deferredShader);
        camera.setViewProjectionMatrix(combineShader);

        scene.setActiveProgram(deferredShader);


        if(glGetUniformBlockIndex(deferredShader.getProgram(), "material_block") == GL_INVALID_INDEX){
            throw new RuntimeException("Fuck Life");
        }

        measureTime();



        RigidBody collisionObject = new RigidBody(new Matrix3f(inertia), mig29.getPosition(), 10000);


        collisionObject.setCollisionPrimitive(
                new BoundingBox(collisionObject.getPosition(),
                new Vector3f(10,10,10), collisionObject.getOrientation()));


        List<Float> frameTimes = new ArrayList<>();

        plane.setPosition(new Vector3f(-400,0,0));
        plane.setVelocity(new Vector3f(10,0,0));


        plane.setCollisionPrimitive(new BoundingBox(plane.getPosition(),
                new Vector3f(10,10,10), plane.getOrientation()));

        PhysicsProcessor physicsProcessor = new PhysicsProcessor(new ArrayList<>(), 0.005f);
        physicsProcessor.addRigidBody(plane);
        physicsProcessor.addRigidBody(collisionObject);
        physicsProcessor.start();
        mig29.setScale(new Vector3f(10,10, 10));
        plane.getCollisionPrimitive()
                .setCollisionLambda(() -> System.err.println("Intersection!"));

        scene.addDrawItem(new MeshTree(new ArrayList<>(), mig29, "name"));
        collisionObject.setEnableGravity(false);
        plane.setSurfaceArea(50);
        plane.setEnableAirResistance(true);
        while (!glfwWindowShouldClose(window_handle)){;


           fightingFalcon.getPosition().set(plane.getPosition());

           Vector3f dir = fightingFalcon.getRotQuaternion().transform(new Vector3f(0,1,0));
           dir.normalize();

           float num = dir.dot(new Vector3f(0,1,0));
           num/=abs(num);

            plane.applyForceAtPoint(
                   new Vector3f(0,0.1f*plane.getMass() * 9.8f* num,0),new Vector3f(0));
            plane.getNetForce().add(new Vector3f(0, 0.9f * plane.getMass() * 9.8f, 0));

            float thrust = 135000f;
            //plane.applyForceAtPoint(new Vector3f(thrust*0.01f,0,0), new Vector3f(0));

            //plane.applyForceAtPoint(new Vector3f(-thrust*0.01f,0,0), new Vector3f(0));


           glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
           glClear(GL_DEPTH_BUFFER_BIT|GL_COLOR_BUFFER_BIT);

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
            plane.getAngularVelocity().mul(0.99f);

//            plane.getAcceleration().mul(0.93f);
            collisionObject.getVelocity().mul(0.99f);
            plane.update(dt);
            collisionObject.update(dt);
            fightingFalcon.setUpdated(true);

            fightingFalcon.getRotQuaternion().set(plane.getOrientation());
            mig29.getRotQuaternion().set(collisionObject.getOrientation());


            camera.setFocus(fightingFalcon.getPosition());
            camera.setPosition(fightingFalcon.getPosition()
                    .add(new Vector3f(-3,1,0)
                            .rotate(fightingFalcon.getRotQuaternion()), new Vector3f()));
            //camera.setPosition(camera.getQuaternionf(), new Vector3f(-3, 1, 0));
            camera.loadViewMatrix();
            camera.setViewProjectionMatrix(skyBoxShader);
            measureTime();
            frameTimes.add(getCurrentFPS());
        }

        writeCsv("data.csv", frameTimes);
        physicsProcessor.setRunning(false);
    }
    public static void writeCsv(String fileName, List<Float> frameTimes) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < frameTimes.size(); i++) {
                writer.write(frameTimes.get(i).toString());
                if (i < frameTimes.size() - 1) {
                    writer.write(","); // Add a comma between values
                }
            }
            writer.newLine(); // Add a new line at the end of the row
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV file", e);
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
    public static float center(float value, float factor, float dt) {
        if (value >= 0) {
            return Math.max(0.0f, Math.min(1.0f, value - factor * dt));
        } else {
            return Math.max(-1.0f, Math.min(0.0f, value + factor * dt));
        }
    }
    public static float move(float value, float factor, float dt) {
        return clamp(value - factor * dt, -1.0f, 1.0f);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }


    Vector3f computeDrag(float ro, float area, float drag, Vector3f velocity){
        if(velocity.length() < 10E-8){}
        Vector3f dir = new Vector3f(velocity).normalize().negate();
        float magnitude = (float) (0.5 * ro * velocity.length()*area*drag);
        return dir.mul(magnitude);
    }
}

