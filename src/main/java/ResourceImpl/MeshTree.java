package ResourceImpl;

import Interfaces.TreeNode;
import org.joml.Quaternionf;
import org.joml.Vector3f;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class MeshTree extends TreeNode<Mesh> {
    public MeshTree(List<TreeNode<Mesh>> childNodes, Mesh nodeValue, String nodeName) {
        super(childNodes, nodeValue, nodeName);
    }

     public void forwardTransform(){
        MeshTree currentMesh = this;

         traverse(mesh->{
             mesh.setPosition(currentMesh.getPosition());
             mesh.setRotQuaternion(currentMesh.getRotQuaternion());
             mesh.setScale(currentMesh.getNodeValue().getScale());
             mesh.setShadowScale(currentMesh.getNodeValue().getShadowScale());//
         });

    }

    public void setPosition(Vector3f position){
        traverse(mesh -> {mesh.setPosition(position);});
    }

    public void draw() {
        traverse(Mesh::draw);
    }


    public void setShader(Shader shader) {
        traverse(mesh -> mesh.setShader(shader));
    }


    public void compile() {
        Set<Material> compiledMaterials = new HashSet<>();
        traverse(node -> {
            node.compile();
            Material material = node.getMaterial();
            if (material != null && !compiledMaterials.contains(material)) {
                material.compile();
                compiledMaterials.add(material);
            }
        });
    }


    public void traverse(Consumer<Mesh> action) {
        MeshTree node = this;
        List<TreeNode<Mesh>> list = node.getChildNodes();

        if (node.getNodeValue() != null) {
            action.accept(node.getNodeValue());

            if (list != null) {
                for (TreeNode<Mesh> meshTreeNode : list) {
                    ((MeshTree) meshTreeNode).traverse(action);
                }
            }
        }
    }
    public Vector3f getPosition() {

        return getNodeValue().getPosition();
    }
    public Quaternionf getRotQuaternion() {
        return getNodeValue().getRotQuaternion();
    }

    public void setUpdated(boolean updated) {

        getNodeValue().setUpdated(updated);
    }
}
