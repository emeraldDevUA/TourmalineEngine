package Rendering;


import Effects.BaseEffect;
import Interfaces.DrawableContainer;
import Liquids.LiquidBody;
import ResourceImpl.Mesh;
import ResourceImpl.MeshTree;
import ResourceImpl.Shader;
import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.List;

public class Scene implements DrawableContainer<MeshTree, BaseEffect, LiquidBody> {

    private final List<BaseEffect> effects;
    private final List<LiquidBody> liquidBodies;
    private final List<MeshTree> drawables;
    @Setter
    @Getter
    private SkyBox skyBox;

    public Scene(){

        effects = new ArrayList<>();
        drawables = new ArrayList<>();
        liquidBodies = new ArrayList<>();
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
    public void drawWater() {
        liquidBodies.forEach(LiquidBody::draw);
    }

    @Override
    public void drawEffects() {
        effects.forEach(BaseEffect::draw);
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
    public void addEffect(BaseEffect item) {
        effects.add(item);
    }

    @Override
    public void addLiquidBody(LiquidBody item) {
        liquidBodies.add(item);
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
