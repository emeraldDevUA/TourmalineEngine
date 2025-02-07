package ResourceImpl;

import Interfaces.TreeNode;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Calendar;
import java.util.List;

public class MeshTree extends TreeNode<Mesh> {
    public MeshTree(List<TreeNode<Mesh>> childNodes, Mesh nodeValue, String nodeName) {
        super(childNodes, nodeValue, nodeName);
    }

     public void forwardTransform(){


    }


    public void draw() {
        MeshTree node = this;
        List<TreeNode<Mesh>> list = node.getChildNodes();
        if(node.getNodeValue()!=null) {
            node.getNodeValue().draw();
            if(list!=null){
                for(TreeNode<Mesh> meshTreeNode: list){
                    ((MeshTree)(meshTreeNode)).draw();
                }
            }
        }
    }


    public void setShader(Shader shader) {
        MeshTree node = this;
        List<TreeNode<Mesh>> list = node.getChildNodes();
        if(node.getNodeValue()!=null) {
            node.getNodeValue().setShader(shader);
            if(list!=null){
                for(TreeNode<Mesh> meshTreeNode: list){
                    ((MeshTree)(meshTreeNode)).setShader(shader);
                }
            }
        }
    }


    public void compile() {

        MeshTree node = this;
        Mesh nodeValue;
        if( (nodeValue = node.getNodeValue()) == null){
            return;
        }
        nodeValue.compile();
        if(nodeValue.getMaterial() != null){
            nodeValue.getMaterial().compile();
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
