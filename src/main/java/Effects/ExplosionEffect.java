package Effects;

import ResourceImpl.Mesh;
import ResourceImpl.Shader;
import lombok.Setter;
import org.joml.Vector3f;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class ExplosionEffect  extends BaseEffect{
    private Mesh explosionMesh;
    @Setter
    private float existenceTime = 5;
    private float creationTime = -1;
    private float currentTime = - 1;


    @Override
    public void setMainPosition(Vector3f position){
        super.setMainPosition(position);
        if(explosionMesh != null)
            explosionMesh.setPosition(position);
    }

    @Override
    public void draw() {
        float now = (float) glfwGetTime();

        if (currentTime <= 0) {
            creationTime = now;
            currentTime = now;
        }

        currentTime = now;
        float elapsed = currentTime - creationTime;

        float totalLife = existenceTime + existenceTime/5f; // existenceTime + 2 seconds of pulsing

        if (elapsed <= totalLife) {
            Shader shader = explosionMesh.getShader();

            float t;
            int phase;

            if (elapsed <= existenceTime) {
                // Phase 1: explosion growth
                t = elapsed / existenceTime;
                phase = 0;
            } else {
                // Phase 2: pulsing after explosion
                t = (elapsed - existenceTime) /existenceTime/5;
                phase = 1;
            }

            shader.setUniform("time", t);             // normalized time for current phase
            shader.setUniform("phase", phase);        // 0 = explode, 1 = pulse
            shader.setUniform("effectType", 2);       // your custom effect
            shader.setUniform("rocketPos", getMainPosition());

            explosionMesh.draw();
        }else {
            obsolete = true;
        }
    }
    @Override
    public void compile(){
            explosionMesh = new Mesh();
            if(getScaleVector()!=null)
                explosionMesh.setScale(getScaleVector());
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
