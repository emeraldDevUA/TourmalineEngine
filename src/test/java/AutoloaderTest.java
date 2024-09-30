import Annotations.BasicWindow;
import Annotations.OpenGLWindow;
import Interfaces.Drawable;
import ResourceImpl.MeshTree;
import ResourceLoading.AutoLoader;
import ResourceLoading.ResourceLoadScheduler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;

@OpenGLWindow(windowName = "Complex Example", defaultDimensions = {1920,1080},
        windowHints = {GLFW_DECORATED}, windowHintsValues={GLFW_TRUE})
public class AutoloaderTest extends BasicWindow {
    public static final String rootPath = "src/main/resources/3D_Models";
    public static AutoLoader loader;
    @Test
    public void testMeshLoading(){
        init(AutoloaderTest.class);
        List<MeshTree> meshes = new ArrayList<>();
        long t1 = System.currentTimeMillis();
        loader = new AutoLoader(rootPath, meshes, new ResourceLoadScheduler());
        loader.loadTrees();
        loader.asyncLoad();

        while (loader.getReadiness() < 1);

        long t2 = System.currentTimeMillis();

        System.out.println(STR."Elapsed time: \{t2 - t1} ms");

    }


    @Override
    public void close() throws IOException {

    }
}
