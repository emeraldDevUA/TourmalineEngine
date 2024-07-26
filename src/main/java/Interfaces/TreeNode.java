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

    void addNode(TreeNode<T> treeNode){
        childNodes.add(treeNode);
    }

    T findNode(String name){
        TreeNode<T> node = this;
        if(node.getNodeName().compareTo(name) == 0){
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
