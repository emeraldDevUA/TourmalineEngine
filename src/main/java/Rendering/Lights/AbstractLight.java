package Rendering.Lights;

import lombok.Getter;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.glGenBuffers;

public abstract class AbstractLight {

    @Getter
    protected ByteBuffer LightBuffer;
    protected float lightIntensity;
    protected Vector3f lightColor;

    public AbstractLight(){
        lightColor = new Vector3f(1,0,0);
        lightIntensity = 10;

    }

    public abstract ByteBuffer formLight();

}
