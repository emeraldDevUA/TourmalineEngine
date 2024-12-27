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

        Assert.assertNotNull(loader.getDrawables().get("F16"));
    }

    @Test
    public void testShaders(){
        System.out.println("Deferred Shader initialization: ");
        Shader deferredShader =
                new Shader(shaderRootPath +"/deferred_shaders/deferred_vertex.glsl",
                        shaderRootPath +"/deferred_shaders/deferred_fragment.glsl");

        Assert.assertNotEquals(deferredShader.getProgram(), -1);
        System.out.println("Combined Shader initialization: ");
        Shader combineShader =
                new Shader(shaderRootPath +"/combine_shaders/combine_vertex.glsl",
                        shaderRootPath +"/combine_shaders/combine_fragment.glsl");

        Assert.assertNotEquals(combineShader.getProgram(), -1);
        System.out.println("Shadow Shader initialization: ");
        Shader shadowShader =
                new Shader(shaderRootPath +"/shadow_shaders/shadow_vertex.glsl",
                        shaderRootPath +"/shadow_shaders/shadow_fragment.glsl");

        Assert.assertNotEquals(shadowShader.getProgram(), -1);
        System.out.println("PostProcessing Shader initialization: ");
        Shader postProcessingShader =
                new Shader(shaderRootPath +"/postprocessing_shaders/postprocessing_vertex.glsl",
                        shaderRootPath +"/postprocessing_shaders/postprocessing_fragment.glsl");
        Assert.assertNotEquals(postProcessingShader.getProgram(), -1);
        System.out.println("Skybox Shader initialization: ");
        Shader skyBoxShader =
                new Shader(shaderRootPath +"/skybox_shaders/skybox_vertex.glsl",
                        shaderRootPath +"/skybox_shaders/skybox_frag.glsl");

        Assert.assertNotEquals(skyBoxShader.getProgram(), -1);
        System.out.println("VisualEffects Shader initialization: ");
        Shader visualEffects =
                new Shader(shaderRootPath +"/visualeffects_shaders/visual_effects_vertex.glsl",
                        shaderRootPath +"/visualeffects_shaders/visual_effects_fragment.glsl");
        Assert.assertNotEquals(visualEffects.getProgram(), -1);
    }


    @Test
    public void liquidBodyTest(){
        LiquidBody liquidBody = new LiquidBody();
        Map<String, List<?>> list = liquidBody.generateWater(100, 400);

        Mesh water = new Mesh("Water", list);
        water.compile();
        liquidBody.getWaterMeshes().put(4, water);

       // Texture tex = liquidBody.generateCoefficients();

    }


    @Override
    public void close() throws IOException {

    }
}
