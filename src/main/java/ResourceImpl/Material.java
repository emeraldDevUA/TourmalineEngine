package ResourceImpl;

import lombok.Getter;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;


import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

@SuppressWarnings("unused")
public class Material implements Closeable {
    public static final String ALBEDO_MAP = "Albedo";
    public static final String NORMAL_MAP = "Normal";
    public static final String ROUGHNESS_MAP = "Roughness";
    public static final String METALNESS_MAP = "Metalness";
    public static final String EMISSION_MAP = "Emission";
    public static final String AO_MAP = "AmbientOcclusion";
    public static final String OPACITY = "Opacity";
    public static final String METALNESS = "Metalness";
    public static final String ROUGHNESS = "Roughness";
    private final Map<String, Double> physicalProperties;
    @Getter
    private final Map<String, Texture> pbrMaps;
    @Getter
    private final Map<String, Vector3f> colors;

    private boolean bufferUpdated;
    @Getter
    private final int buffer;
    private final ByteBuffer materialBuffer;

    public  Material(){

        materialBuffer = BufferUtils.createByteBuffer(64);
        pbrMaps = new ConcurrentHashMap<>();
        physicalProperties = new ConcurrentHashMap<>();
        colors = new ConcurrentHashMap<>();
        bufferUpdated = false;

        buffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, buffer);
        glBufferData(GL_UNIFORM_BUFFER, 80, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);


        addColor(ALBEDO_MAP, new Vector3f(1,1,1));
        addColor(EMISSION_MAP, new Vector3f(1,1,1));

        addProperty(OPACITY, 1.0);
        addProperty(METALNESS, 0.3);
        addProperty(ROUGHNESS,1.0);

        updateBuffer();
    }

    public void addProperty(String name, Double value){
        bufferUpdated = false;
        physicalProperties.put(name, value);

    }
    public void addMap(String name, Texture texture){
        bufferUpdated = false;
        pbrMaps.put(name, texture);
    }
    public void addColor(String name, Vector3f color){
        bufferUpdated = false;
        colors.put(name, color);
    }

    @Override
    public void close() {
        physicalProperties.clear();
        pbrMaps.clear();
        colors.clear();
        materialBuffer.clear();
        glDeleteBuffers(buffer);
    }

    public void use() {
        if(!bufferUpdated) {
            updateBuffer();
        }
        pbrMaps.keySet().forEach(map->{

            boolean isPresent = true;
            int binding = 0;
            switch (map) {
                case ALBEDO_MAP:
                    binding = GL_TEXTURE0 + Shader.ALBEDO_MAP_BINDING;
                    break;
                case NORMAL_MAP:
                    binding = GL_TEXTURE0 + Shader.NORMAL_MAP_BINDING;
                    break;
                case ROUGHNESS_MAP:
                    binding = GL_TEXTURE0 + Shader.ROUGHNESS_MAP_BINDING;
                    break;
                case METALNESS_MAP:
                    binding  = GL_TEXTURE0 + Shader.METALNESS_MAP_BINDING;
                    break;
                case EMISSION_MAP:
                    binding  = GL_TEXTURE0 + Shader.EMISSION_MAP_BINDING;
                    break;
                case AO_MAP:
                    binding  = GL_TEXTURE0 + Shader.AMBIENT_OCCLUSION_MAP_BINDING;
                    break;


                default:
                    isPresent = false;
                    break;

            }
            if (isPresent) {
                glActiveTexture(binding);
                pbrMaps.get(map).use();
            }

        });


        glBindBufferBase(GL_UNIFORM_BUFFER, Shader.MATERIAL_BLOCK, buffer);


    }

    public void compile(){
        for(String string: pbrMaps.keySet()){
            try {
                pbrMaps.get(string).assemble();
            }catch (Exception e){
                System.err.println(string);
            }

        }
    }

    private void updateBuffer() {

        try {

            Vector3f albedo = colors.get(ALBEDO_MAP);
            Vector3f emission = colors.get(EMISSION_MAP);

            float opacity = physicalProperties.get(OPACITY).floatValue();
            float metalness = physicalProperties.get(METALNESS).floatValue();
            float roughness = physicalProperties.get(ROUGHNESS).floatValue();

            Texture albedoMap = pbrMaps.get(ALBEDO_MAP);
            Texture normalMap = pbrMaps.get(NORMAL_MAP);
            Texture roughnessMap = pbrMaps.get(ROUGHNESS_MAP);
            Texture metalnessMap = pbrMaps.get(METALNESS_MAP);
            Texture ambientOcclusionMap = pbrMaps.get(AO_MAP);
            Texture emissionMap = pbrMaps.get(EMISSION_MAP);

            materialBuffer.clear();

            materialBuffer.putFloat(0, albedo.x());
            materialBuffer.putFloat(4, albedo.y());
            materialBuffer.putFloat(8, albedo.z());
            materialBuffer.putFloat(12, opacity);
            materialBuffer.putInt(16, albedoMap == null ? 0 : 1);
            materialBuffer.putInt(20, normalMap == null ? 0 : 1);
            materialBuffer.putFloat(24, metalness);
            materialBuffer.putInt(28, metalnessMap == null ? 0 : 1);
            materialBuffer.putFloat(32, roughness);
            materialBuffer.putInt(36, roughnessMap == null ? 0 : 1);
            materialBuffer.putInt(40, ambientOcclusionMap == null ? 0 : 1);
            materialBuffer.putInt(44, emissionMap == null ? 0 : 1);
            materialBuffer.putFloat(48, emission.x());
            materialBuffer.putFloat(52, emission.y());
            materialBuffer.putFloat(56, emission.z());

            // Ensure buffer size is a multiple of 16 by adding padding
            materialBuffer.position(60);
            materialBuffer.putFloat(0.0f);  // Padding to align `vec3` size
            materialBuffer.flip();

            glBindBuffer(GL_UNIFORM_BUFFER, buffer);
            glBufferData(GL_UNIFORM_BUFFER, materialBuffer, GL_STATIC_DRAW);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);


        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        bufferUpdated = true;


    }


    public void loadMatLib(EnhancedMTL mtl){
        addProperty(ROUGHNESS, (double) mtl.roughness);
        addProperty(METALNESS, (double) mtl.metallic);
        addProperty(OPACITY,   (double) mtl.opacity);
        addProperty(ROUGHNESS,   (double) mtl.roughness);
        addProperty(METALNESS,   (double) mtl.metallic);

    }
}
