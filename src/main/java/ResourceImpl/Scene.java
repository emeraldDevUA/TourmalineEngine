package ResourceImpl;

import Interfaces.DrawableContainer;
import Rendering.SkyBox;
import ResourceImpl.MeshTree;

import java.util.ArrayList;
import java.util.List;

public class Scene implements DrawableContainer<MeshTree> {
    private List<MeshTree> drawables;
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

}
