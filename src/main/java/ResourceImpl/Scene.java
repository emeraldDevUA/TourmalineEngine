package ResourceImpl;

import Interfaces.DrawableContainer;
import Rendering.SkyBox;
import lombok.Setter;


import java.util.ArrayList;
import java.util.List;

public class Scene implements DrawableContainer<MeshTree> {
    private List<MeshTree> drawables;
    @Setter
    private SkyBox skyBox;

    public Scene(){
        drawables = new ArrayList<>();
    }
    @Override
    public void drawSkyBox() {
        skyBox.draw();
    }

    @Override
    public void drawItems() {
        drawables.forEach(MeshTree::draw);
    }

    @Override
    public void clear() {

        drawables.clear();
    }

    @Override
    public void clearSkyBox() {

    }

    @Override
    public void addDrawItem(MeshTree item) {

        drawables.add(item);
    }

    @Override
    public Boolean deleteItem(MeshTree item) {

        return drawables.remove(item);
    }

    public void setActiveProgram(Shader shader) {
        for(MeshTree tree: drawables){
            tree.getNodeValue().setShader(shader);
        }
    }
}
