package ResourceImpl;

import Interfaces.TreeNode;

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
}
