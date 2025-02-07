package Rendering.Lights;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.glGenBuffers;

@Getter
@Setter
public abstract class AbstractLight {

    protected ByteBuffer LightBuffer;
    protected float lightIntensity;
    protected Vector3f lightColor;

    public AbstractLight(){
        lightColor = new Vector3f(1,0,0);
        lightIntensity = 10;
    }
    public  AbstractLight(Vector3f lightColor){
        this.lightColor = lightColor;
    }


    public abstract ByteBuffer formLight();

}
