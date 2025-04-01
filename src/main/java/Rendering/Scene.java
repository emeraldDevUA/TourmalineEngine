package Rendering;


import Effects.BaseEffect;
import Interfaces.DrawableContainer;
import Liquids.LiquidBody;
import Rendering.Lights.AbstractLight;
import Rendering.Lights.DirectionalLight;
import Rendering.Lights.PointLight;

import ResourceImpl.Mesh;
import ResourceImpl.MeshTree;
import ResourceImpl.Shader;
import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Scene implements DrawableContainer<MeshTree, BaseEffect, LiquidBody> {

    private final List<BaseEffect> effects;
    private final List<LiquidBody> liquidBodies;
    private final List<MeshTree> drawables;
    @Getter
    private final List<AbstractLight> lights;
    @Setter
    @Getter
    private SkyBox skyBox;
    @Getter
    private final List<Mesh> transparentDrawables;

    public Scene(){
        lights = new ArrayList<>();
        effects = new ArrayList<>();
        drawables = new ArrayList<>();
        liquidBodies = new ArrayList<>();
        transparentDrawables = new ArrayList<>();
    }
    @Override
    public void drawSkyBox() {
        skyBox.draw();
    }

    @Override
    public void drawItems() {
        for(MeshTree drawable: drawables){
            drawable.traverse(mesh -> {
                if(!mesh.isEnableBlending()){
                    mesh.draw();
                }
            });
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

        item.traverse(mesh -> {
            if(mesh.isEnableBlending()){
                transparentDrawables.add(mesh);
            }
        });
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
            tree.setShader(shader);
        }
    }

    public void addLightSources(AbstractLight light, boolean addModel) {


        if(light instanceof DirectionalLight){
            lights.add(light);
            return;
        }

        if (!(light instanceof PointLight pointLight)) {
            throw new IllegalArgumentException("Unsupported light type: " + light.getClass().getName());
        }

        String pntLights = "PointLights";
        MeshTree targetTree = null;

        // Find an existing "PointLights" tree
        for (MeshTree tree : drawables) {
            if (pntLights.equals(tree.getNodeName())) {
                targetTree = tree;
                break;
            }
        }
        if(addModel) {
            if (targetTree == null) {
                // Create a new "PointLights" tree if not found
                targetTree = new MeshTree(new ArrayList<>(), pointLight.getLightMesh(), pntLights);
                drawables.add(targetTree);
            } else {
                // Add new PointLight node
                targetTree.addNode(new MeshTree(new ArrayList<>(), pointLight.getLightMesh(), UUID.randomUUID().toString()));
            }
        }
            lights.add(light);
    }
}
