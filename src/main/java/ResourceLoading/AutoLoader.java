package ResourceLoading;

import Interfaces.TreeNode;
import ResourceImpl.Material;
import ResourceImpl.Mesh;
import ResourceImpl.MeshTree;
import ResourceImpl.Texture;
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

        drawables = new ConcurrentHashMap<>();

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


    private TreeNode<Mesh> loadMesh(File dir, TreeNode<Mesh> meshTree) {
        final Set<String> modelFormats = Set.of("obj", "fbx", "glb", "stl");
        final Set<String> textureFormats = Set.of("png", "jpg", "jpeg");

        File[] files = dir.listFiles();
        if (files == null) return meshTree;  // or throw an exception, as needed

        // Option: Collect textures first.
        // This map could be used to assign textures to a material.
        Material sharedMaterial = new Material();
        for (File f : files) {
            if (!f.isDirectory()) {
                String fileName = f.getName();
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex == -1) continue; // Skip files without extension

                String extension = fileName.substring(dotIndex + 1).toLowerCase();
                if (textureFormats.contains(extension)) {
                    Texture texture = new Texture();
                    resourceLoadScheduler.addResource(texture, f.getPath());

                    String upperName = fileName.toUpperCase();
                    if (upperName.contains("ALBEDO")) {
                        sharedMaterial.addMap(Material.ALBEDO_MAP, texture);
                    }
                    if (upperName.contains("NORMAL")) {
                        sharedMaterial.addMap(Material.NORMAL_MAP, texture);
                    }
                    if (upperName.contains("ROUGHNESS")) {
                        sharedMaterial.addMap(Material.ROUGHNESS_MAP, texture);
                    }
                    if (upperName.contains("AMBIENT_OCCLUSION")) {
                        sharedMaterial.addMap(Material.AO_MAP, texture);
                    }
                    if (upperName.contains("EMISSIVE")) {
                        sharedMaterial.addMap(Material.EMISSION_MAP, texture);
                    }
                }
            }
        }

        // Then, process files (including subdirectories) to load meshes.
        for (File f : files) {
            if (f.isDirectory()) {
                // Process subdirectories. You might want to pass the same tree, or create a new node for the folder.
                TreeNode<Mesh> childNode = loadMesh(f, null);
                if (childNode != null) {
                    if (meshTree == null) {
                        meshTree = childNode;
                    } else {
                        meshTree.addNode(childNode);
                    }
                }
            } else {
                String fileName = f.getName();
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex == -1) continue;
                String extension = fileName.substring(dotIndex + 1).toLowerCase();

                if (modelFormats.contains(extension)) {
                    // Create a new mesh and assign the shared material.
                    Mesh mesh = new Mesh();
                    resourceLoadScheduler.addResource(mesh, f.getPath());

                    // Optionally, if each mesh should have its own material instance,
                    // clone the sharedMaterial or build a new one.
                    mesh.setMaterial(sharedMaterial);

                    TreeNode<Mesh> node = new MeshTree(new ArrayList<>(), mesh, fileName);
                    if (meshTree == null) {
                        meshTree = node;
                    } else {
                        meshTree.addNode(node);
                    }
                }
            }
        }
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

    public MeshTree get(String name){
        return drawables.get(name);
    }
}
