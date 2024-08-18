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
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
@SuppressWarnings("unused")
public class Material implements Closeable {
    public static final String ALBEDO_MAP = "Albedo";
    public static final String NORMAL_MAP = "Normal";
    public static final String ROUGHNESS_MAP = "Roughness";
    private static final String METALNESS_MAP = "Metalness";
    private static final String EMISSION_MAP = "Emission";
    private static final String AO_MAP = "AmbientOcclusion";


    private static final String OPACITY = "Opacity";
    private static final String METALNESS = "Metalness";
    private static final String ROUGHNESS = "Roughness";
    private final Map<String, Double> physicalProperties;
    @Getter
    private final Map<String, Texture> pbrMaps;
    private final Map<String, Vector3f> colors;

    private boolean bufferUpdated;
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
        addProperty(METALNESS, 0.5);
        addProperty(ROUGHNESS,0.5);

    }

    public void addProperty(String name, Double value){

        physicalProperties.put(name, value);
    }
    public void addMap(String name, Texture texture){

        pbrMaps.put(name, texture);
    }
    public void addColor(String name, Vector3f color){

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
        if(!bufferUpdated)
            updateBuffer();
        
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

                default:
                    isPresent = false;
                    break;

            }
            if (isPresent) {
                glActiveTexture(binding);
                pbrMaps.get(map).use();
            }

        });

    }

    private void updateBuffer() {

        {
            materialBuffer.clear();
            Vector3f albedo = colors.get(ALBEDO_MAP);
            Vector3f emission = colors.get("Emission");
            materialBuffer.putFloat(0, albedo.x());
            materialBuffer.putFloat(4, albedo.y());
            materialBuffer.putFloat(8, albedo.z());
            materialBuffer.putFloat(12, physicalProperties.get(OPACITY).floatValue());
            materialBuffer.putInt(16, pbrMaps.get(ALBEDO_MAP) == null ? 0 : 1);
            materialBuffer.putInt(20, pbrMaps.get(NORMAL_MAP)  == null ? 0 : 1);
            materialBuffer.putFloat(24, physicalProperties.get(METALNESS).floatValue());
            materialBuffer.putInt(28, pbrMaps.get(METALNESS_MAP) == null ? 0 : 1);
            materialBuffer.putFloat(32, physicalProperties.get(ROUGHNESS).floatValue());
            materialBuffer.putInt(36, pbrMaps.get(ROUGHNESS_MAP) == null ? 0 : 1);
            materialBuffer.putInt(40, pbrMaps.get(AO_MAP) == null ? 0 : 1);
            materialBuffer.putInt(46, pbrMaps.get(EMISSION_MAP)  == null ? 0 : 1);
            materialBuffer.putFloat(52, emission.x());
            materialBuffer.putFloat(56, emission.y());
            materialBuffer.putFloat(60, emission.z());

            glBindBuffer(GL_UNIFORM_BUFFER, buffer);
            glBufferData(GL_UNIFORM_BUFFER, materialBuffer, GL_STATIC_DRAW);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);

            bufferUpdated = true;
        }
    }
}
