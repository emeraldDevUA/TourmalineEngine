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

        List<TreeNode<Mesh>> list = node.getChildNodes();
        //list.forEach(meshTreeNode -> { ((MeshTree)(meshTreeNode)).compile();});


    }
}
