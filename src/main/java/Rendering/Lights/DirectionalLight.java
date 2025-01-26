package Rendering.Lights;

import lombok.Getter;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DirectionalLight extends AbstractLight{

    private Vector3f direction;
    private static final int dirLightSize = 32;

    @Getter
    private static  ByteBuffer emptyBuffer;
    public DirectionalLight(Vector3f direction){
        super();
        this.direction = direction;
        this.LightBuffer = ByteBuffer.allocateDirect(dirLightSize)
                .order(ByteOrder.nativeOrder());
        emptyBuffer =  ByteBuffer.allocateDirect(dirLightSize)
                .order(ByteOrder.nativeOrder());
    }



    public ByteBuffer formLight(){
        LightBuffer.clear();

        LightBuffer.putFloat(lightColor.x);
        LightBuffer.putFloat(lightColor.y);
        LightBuffer.putFloat(lightColor.z);

        LightBuffer.putFloat(direction.x);
        LightBuffer.putFloat(direction.y);
        LightBuffer.putFloat(direction.z);

        LightBuffer.putFloat(lightIntensity);


        return LightBuffer;
    }

}
