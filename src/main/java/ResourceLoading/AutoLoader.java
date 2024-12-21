package ResourceLoading;

import Interfaces.TreeNode;
import ResourceImpl.Material;
import ResourceImpl.Mesh;
import ResourceImpl.MeshTree;
import ResourceImpl.Texture;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor

public class AutoLoader {
    private final String rootFolder;
    @Getter
    private Map<String,MeshTree> drawables;
    private final ResourceLoadScheduler resourceLoadScheduler;
    public void loadTrees() {

        drawables = new ConcurrentHashMap<String, MeshTree>();

        File folder = new File(rootFolder);

        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                drawables.put( fileEntry.getName() ,(MeshTree) loadMesh(fileEntry, null));
            }
            else{
                System.err.println(STR."Only folders are expected to be in the root dir.\n\{fileEntry.getName()}");
            }
        }


    }


    private TreeNode<Mesh>loadMesh(File dir, TreeNode<Mesh> meshTree) {

        final String models_formats = ".obj .fbx .glb .stl";
        final String texture_formats = ".png .jpg";

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

                    if(f.getName().toUpperCase().contains("ALBEDO")){
                        material.addMap(Material.ALBEDO_MAP, texture);
                    }
                    if(f.getName().toUpperCase().contains("NORMAL")){
                        material.addMap(Material.NORMAL_MAP, texture);
                    }
                    if(f.getName().toUpperCase().contains("ROUGHNESS")){
                        material.addMap(Material.ROUGHNESS_MAP, texture);
                    }
                    if(f.getName().toUpperCase().contains("AMBIENT_OCCLUSION")){
                        material.addMap(Material.ROUGHNESS_MAP, texture);
                    }


                    // add them to the material
                }
            }


            if (mesh != null) {

                mesh.setMaterial(material);
                TreeNode<Mesh> node = new MeshTree(new ArrayList<>(), mesh, f.getName());
                if(meshTree == null){
                    meshTree = node;
                }
                else{
                    meshTree.addNode(node);
                }
            }
        }


        //resourceLoadScheduler.getResources();
        return meshTree;
    }


    public void asyncLoad(){
        resourceLoadScheduler.loadResources();
        while(resourceLoadScheduler.getReadiness() < 1){
            Thread.onSpinWait();
        }
        resourceLoadScheduler.reset();

    }
    public float getReadiness(){
       return resourceLoadScheduler.getReadiness();
    }

}
