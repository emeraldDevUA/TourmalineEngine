import Interfaces.Drawable;
import ResourceImpl.MeshTree;
import ResourceLoading.AutoLoader;
import ResourceLoading.ResourceLoadScheduler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class AutoloaderTest {
    public static final String rootPath = "src/main/resources/3D_Models";
    public static AutoLoader loader;
    @Test
    public void testMeshLoading(){
        List<MeshTree> meshes = new ArrayList<>();

        loader = new AutoLoader(rootPath, meshes, new ResourceLoadScheduler());
        loader.loadTrees();

    }


}
