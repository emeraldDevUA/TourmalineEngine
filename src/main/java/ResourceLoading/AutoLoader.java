package ResourceLoading;

import Interfaces.TreeNode;
import ResourceImpl.Material;
import ResourceImpl.Mesh;
import ResourceImpl.MeshTree;
import ResourceImpl.Texture;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class AutoLoader {
    private String rootFolder;
    @Getter
    private List<MeshTree> drawables;
    ResourceLoadScheduler resourceLoadScheduler = new ResourceLoadScheduler();
    public void loadTrees() {

        drawables = new ArrayList<>();

        File folder = new File(rootFolder);



        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                drawables.add((MeshTree) loadMesh(fileEntry, null));
            }
            else{
                System.err.println(STR."Only folders are expected to be in the root dir.\n\{fileEntry.getName()}");
            }
        }


    }


    TreeNode<Mesh>loadMesh(File dir, TreeNode<Mesh> meshTree) {
        final String models_formats = ".obj .fbx .glb .stl";
        final String texture_formats = ".png .jpg";
        final ArrayList<String> maps = new ArrayList<>();
        maps.add("normal");
        File[] files = dir.listFiles();
        assert files != null;

        Material material = new Material();
        Mesh mesh = null;

        for (File f : files) {
            if (f.isDirectory()) {
                loadMesh(f, meshTree);
            } else {
                String extension = f.toPath().toString().split("\\.")[1];
                if (models_formats.contains(extension)) {
                    mesh = new Mesh();
                    resourceLoadScheduler.addResource(mesh, f.getPath());
                } else if (texture_formats.contains(extension)) {
                    Texture texture = new Texture();
                    resourceLoadScheduler.addResource(texture, f.getPath());

                    // add them to the material
                }
            }
            resourceLoadScheduler.loadResources();
            while(resourceLoadScheduler.getReadiness() < 1);

            if (mesh != null) {
                material.getPbrMaps().values().forEach(Texture::assemble);
                mesh.compile();
                mesh.setMaterial(material);
                TreeNode<Mesh> node = new MeshTree(new ArrayList<>(), mesh, f.getName());
                if(meshTree == null){
                    meshTree = node;
                }
                else{
                    meshTree.addNode(node);
                }
            }
            resourceLoadScheduler.reset();
        }

        return meshTree;
    }
}
