package Rendering.Lights;

import ResourceImpl.Material;
import ResourceImpl.Mesh;
import lombok.Getter;

import org.joml.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;



public class PointLight extends AbstractLight {
    private Vector3f position;
    @Getter
    private Mesh lightMesh;
    @Getter
    private static  ByteBuffer emptyBuffer;

    private static final int posLightSize = 32 ;
    public PointLight(Vector3f position){
        super();
        this.position = position;
        this.LightBuffer = ByteBuffer.allocateDirect(posLightSize)
                .order(ByteOrder.nativeOrder());
        emptyBuffer =  ByteBuffer.allocateDirect(posLightSize)
                .order(ByteOrder.nativeOrder());
        lightMesh = new Mesh();
        lightMesh.setPosition(position);
    }

    @Override
    public ByteBuffer formLight(){
        LightBuffer.clear();

        LightBuffer.putFloat(lightColor.x);
        LightBuffer.putFloat(lightColor.y);
        LightBuffer.putFloat(lightColor.z);

        LightBuffer.putFloat(position.x);
        LightBuffer.putFloat(position.y);
        LightBuffer.putFloat(position.z);

        LightBuffer.putFloat(lightIntensity);


        return LightBuffer;
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
