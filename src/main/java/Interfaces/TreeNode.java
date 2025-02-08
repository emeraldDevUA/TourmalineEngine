package Interfaces;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public abstract class TreeNode<T> {

    private List<TreeNode<T>> childNodes;
    private T nodeValue;
    private String nodeName;

    public void addNode(TreeNode<T> treeNode){

        childNodes.add(treeNode);
    }

    public TreeNode<T> deleteNode(String name){

        return null;
    }

    public T findNode(String name){
        TreeNode<T> node = this;
        if(node.getNodeName().equalsIgnoreCase(name)){
            return  nodeValue;
        }
        // Recursively check the child nodes
        for (TreeNode<T> child : childNodes) {
            T result = child.findNode(name);
            if (result != null) {
                return result;
            }
        }

        return null;
    }



}
