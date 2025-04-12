import Annotations.BasicWindow;
import Annotations.OpenGLWindow;
import Liquids.LiquidBody;
import ResourceImpl.Mesh;
import ResourceImpl.MeshTree;
import ResourceImpl.Shader;
import ResourceImpl.Texture;
import ResourceLoading.AutoLoader;
import ResourceLoading.ResourceLoadScheduler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;


@OpenGLWindow(windowName = "TourmalineTests", defaultDimensions = {640,480},
        windowHints = { GLFW_VISIBLE	}, windowHintsValues={GLFW_FALSE})
@RunWith(JUnit4.class)
public class AutoloaderTest extends BasicWindow {
    public static final String modelRootPath = "src/main/resources/3D_Models";

    public static final String shaderRootPath = "src/main/glsl";
    public static AutoLoader loader;
    @Test
    public void testMeshLoading(){
        init(AutoloaderTest.class);

        long t1 = System.currentTimeMillis();
        loader = new AutoLoader(modelRootPath, new ResourceLoadScheduler());
        loader.loadTrees();
        loader.asyncLoad();

        while (loader.getReadiness() < 1){
            System.out.println();
        }

        long t2 = System.currentTimeMillis();

        System.out.println(STR."Elapsed time: \{t2 - t1} ms");
        loader.getDrawables().values().forEach(Assert::assertNotNull);
        loader.getDrawables().values().forEach(MeshTree::compile);

        loader.getDrawables().keySet().forEach(System.out::println);

        Assert.assertNotNull(loader.get("F16"));
        System.out.println("F16 SubModels Loaded:");
        loader.getDrawables().get("F16").getChildNodes().forEach(Item->{
            System.out.println("\t" + Item.getNodeName());
        });
    }

    @Test
    public void testShaders() {
        System.out.println("===== Starting Shader Initialization Tests =====");

        testShader("Deferred Shader",
                shaderRootPath + "/deferred_shaders/deferred_vertex.glsl",
                shaderRootPath + "/deferred_shaders/deferred_fragment.glsl");

        testShader("Combined Shader",
                shaderRootPath + "/combine_shaders/combine_vertex.glsl",
                shaderRootPath + "/combine_shaders/combine_fragment.glsl");

        testShader("Shadow Shader",
                shaderRootPath + "/shadow_shaders/shadow_vertex.glsl",
                shaderRootPath + "/shadow_shaders/shadow_fragment.glsl");

        testShader("PostProcessing Shader",
                shaderRootPath + "/postprocessing_shaders/postprocessing_vertex.glsl",
                shaderRootPath + "/postprocessing_shaders/postprocessing_fragment.glsl");

        testShader("Skybox Shader",
                shaderRootPath + "/skybox_shaders/skybox_vertex.glsl",
                shaderRootPath + "/skybox_shaders/skybox_frag.glsl");

        testShader("Visual Effects Shader",
                shaderRootPath + "/visualeffects_shaders/visual_effects_vertex.glsl",
                shaderRootPath + "/visualeffects_shaders/visual_effects_fragment.glsl");

        testShader("Transparent Shader",
                shaderRootPath + "/vertex_test.vert",
                shaderRootPath + "/fragment_test.frag");

        System.out.println("===== Shader Initialization Tests Completed =====");
    }

    private void testShader(String name, String vertexPath, String fragmentPath) {
        System.out.printf("Initializing %s (%s, %s)...%n", name, vertexPath, fragmentPath);
        Shader shader = new Shader(vertexPath, fragmentPath);
        int programId = shader.getProgram();

        if (programId == -1) {
            System.err.printf("❌ %s failed to initialize (program ID: %d)%n", name, programId);
        } else {
            System.out.printf("✅ %s initialized successfully (program ID: %d)%n", name, programId);
        }

        Assert.assertNotEquals("Shader failed to initialize: " + name, programId, -1);
    }



    @Test
    public void liquidBodyTest(){
        LiquidBody liquidBody = new LiquidBody();
        Map<String, List<?>> list = liquidBody.generateWater(100, 400,800);

        Mesh water = new Mesh("Water", list);
        water.compile();
        liquidBody.getWaterMeshes().put(4, water);

       // Texture tex = liquidBody.generateCoefficients();

    }



}
