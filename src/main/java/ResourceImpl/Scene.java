package ResourceImpl;

import Interfaces.Drawable;
import Interfaces.DrawableContainer;
import Rendering.SkyBox;
import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.List;

public class Scene implements DrawableContainer<MeshTree> {
    private List<MeshTree> drawables;
    @Setter
    @Getter
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
        for(MeshTree drawable: drawables){
            drawable.getNodeValue().draw();
        }
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
