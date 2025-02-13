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





    public Float[] getFloatArray(){
        return  new Float[]{
                lightColor.x, lightColor.y, lightColor.z,
                0f,
                direction.x, direction.y, direction.z, lightIntensity};
    }

}
