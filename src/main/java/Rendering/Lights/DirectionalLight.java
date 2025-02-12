package Rendering.Lights;

import lombok.Getter;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DirectionalLight extends AbstractLight{

    private final Vector3f direction;
    private static final int dirLightSize = 32;

    @Getter
    private static  ByteBuffer emptyBuffer;

    static {
        emptyBuffer = ByteBuffer.allocateDirect(dirLightSize)
                .order(ByteOrder.nativeOrder());

        for (int i = 0; i < dirLightSize / 4; i++) {
            emptyBuffer.putFloat(0.0f);
        }
        emptyBuffer.flip();
    }

    public DirectionalLight(Vector3f direction){
        super();
        this.direction = direction;
        this.LightBuffer = ByteBuffer.allocateDirect(dirLightSize)
                .order(ByteOrder.nativeOrder());
    }



    public ByteBuffer formLight(){
        LightBuffer.clear();

        LightBuffer.putFloat(lightColor.x);
        LightBuffer.putFloat(lightColor.y);
        LightBuffer.putFloat(lightColor.z);

        LightBuffer.putFloat(lightIntensity);
        LightBuffer.putFloat(direction.x);
        LightBuffer.putFloat(direction.y);
        LightBuffer.putFloat(direction.z);
        LightBuffer.putFloat(0.0f);

        LightBuffer.flip();
        return LightBuffer;
    }

    public Float[] getFloatArray(){
        return  new Float[]{
                lightColor.x, lightColor.y, lightColor.z,
                lightIntensity,
                direction.x, direction.y, direction.z, 0f};
    }

}
