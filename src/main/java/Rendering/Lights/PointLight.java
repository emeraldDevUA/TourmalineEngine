package Rendering.Lights;

import ResourceImpl.Material;
import ResourceImpl.Mesh;
import lombok.Getter;

import org.joml.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;



public class PointLight extends AbstractLight {
    private final Vector3f position;
    @Getter
    private Mesh lightMesh;
    @Getter
    private static  ByteBuffer emptyBuffer;

    private static final int posLightSize = 32 ;


    static {
        emptyBuffer = ByteBuffer.allocateDirect(posLightSize)
                .order(ByteOrder.nativeOrder());

        for (int i = 0; i < posLightSize / 4; i++) {
            emptyBuffer.putFloat(0.0f);
        }
        emptyBuffer.flip();
    }


    public PointLight(Vector3f position){
        super();
        this.position = position;
        this.LightBuffer = ByteBuffer.allocateDirect(posLightSize)
                .order(ByteOrder.nativeOrder());

        emptyBuffer.flip();
        lightMesh = new Mesh();
        lightMesh.setPosition(position);
    }



    public Float[] getFloatArray(){
        return  new Float[]{
                lightColor.x, lightColor.y, lightColor.z,
                0f,
                position.x, position.y, position.z, lightIntensity};
    }

    public void generatePrimitive(){
        try {
            lightMesh.load("src/main/resources/miscellaneous/Sphere32.obj");
            lightMesh.setScale(new Vector3f(lightIntensity/50f));
            lightMesh.compile();
            lightMesh.getMaterial().addColor(Material.ALBEDO_MAP, lightColor);
            lightMesh.setNoCull(true);
        }
        catch (IOException e){
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void setLightColor(Vector3f lightColor) {
        super.setLightColor(lightColor);
    }
}
