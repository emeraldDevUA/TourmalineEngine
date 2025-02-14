package Effects;

import ResourceImpl.Mesh;
import org.joml.Vector3f;

import java.io.IOException;

public class ExplosionEffect  extends BaseEffect{
    private Mesh explosionMesh;

    @Override
    public void setMainPosition(Vector3f position){
        super.setMainPosition(position);
        if(explosionMesh != null)
            explosionMesh.setPosition(position);
    }

    @Override
    public void draw(){
        explosionMesh.getShader()
                .setUniform("effectType", 2);
        explosionMesh.getShader().setUniform("rocketPos", getMainPosition());
        explosionMesh.draw();
    }
    @Override
    public void compile(){
            explosionMesh = new Mesh();
        try {
            explosionMesh.load("src/main/resources/miscellaneous/Sphere128.obj");
            explosionMesh.compile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        explosionMesh.setPosition(getMainPosition());
    }

    public Mesh getMesh() {
        return explosionMesh;
    }
}
