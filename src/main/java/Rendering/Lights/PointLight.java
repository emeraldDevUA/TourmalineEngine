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

    private static final int posLightSize = 48 ;
    public PointLight(Vector3f position){
        super();
        this.position = position;
        this.LightBuffer = ByteBuffer.allocateDirect(posLightSize)
                .order(ByteOrder.nativeOrder());
        emptyBuffer =  ByteBuffer.allocateDirect(posLightSize)
                .order(ByteOrder.nativeOrder());
        for(int i = 0; i < 48/4; i ++){
            emptyBuffer.putFloat(1.0f);
        }
        emptyBuffer.flip();
        lightMesh = new Mesh();
        lightMesh.setPosition(position);
    }

    @Override
    public ByteBuffer formLight() {
        LightBuffer.clear();

        // Color (vec3) → Needs padding
        LightBuffer.putFloat(lightColor.x);
        LightBuffer.putFloat(lightColor.y);
        LightBuffer.putFloat(lightColor.z);
        LightBuffer.putFloat(0.0f);  // Padding to align to vec4 (std140 rule)

        // Position (vec3) → Needs padding
        LightBuffer.putFloat(position.x);
        LightBuffer.putFloat(position.y);
        LightBuffer.putFloat(position.z);
        LightBuffer.putFloat(0.0f);  // Padding to align to vec4 (std140 rule)

        // Intensity (float) → No extra padding needed
        LightBuffer.putFloat(lightIntensity);
        LightBuffer.putFloat(0.0f);  // Extra padding to make struct 48 bytes
        LightBuffer.putFloat(0.0f);
        LightBuffer.putFloat(0.0f);

        LightBuffer.flip(); // **Important! Ensures OpenGL reads correct data**

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
