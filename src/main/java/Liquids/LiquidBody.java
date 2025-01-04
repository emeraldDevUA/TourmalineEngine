package Liquids;

import Interfaces.Drawable;
import ResourceImpl.Material;
import ResourceImpl.Mesh;
import ResourceImpl.Shader;
import ResourceImpl.Texture;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;


import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

record Wave(float steepness,
            float speed,
            float angle,
            float frequency,
            float amplitude) {}

public class LiquidBody implements Drawable {

    @Getter
    private final Map<Integer, Mesh> waterMeshes = new HashMap<>();
    private static final int MAX_LOD = 4;
    @Setter
    private int currentLod = MAX_LOD;

    private static final int WAVE_STRUCT_SIZE = 48  ;
    private static Texture coefficients;
    private final Texture waterSurface;
    private final Material material;
    private final ByteBuffer waterStructBuffer;

    @Getter
    private final int waterBuffer;

    private boolean bufferUpdated;
    public int waveCount = 3;

    public Vector2f direction = new Vector2f(0.5f,0.7f);
    public float wavelength = 3f;
    public float amplitude = 1.3F;
    public float steepness = 1.6f;
    public float speed = 0.5f;
    public float phase = 0.05f;


    public LiquidBody(String texturePath) {
        waterSurface = new Texture(texturePath, 3);
        material = initializeMaterial();
        System.out.println(material.getColors());
        if (coefficients == null) {
            coefficients = generateCoefficients();
        }

        waterBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, waterBuffer);
        glBufferData(GL_UNIFORM_BUFFER, WAVE_STRUCT_SIZE, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        waterStructBuffer = ByteBuffer.allocateDirect(WAVE_STRUCT_SIZE).order(ByteOrder.nativeOrder());
        bufferUpdated = false;

        if(waterBuffer == -1){
            throw new RuntimeException("Struct failed to create");
        }
    }

    public LiquidBody() {
        waterSurface = null; // Placeholder for uninitialized textures
        material = initializeMaterial();
        waterStructBuffer = ByteBuffer.allocateDirect(WAVE_STRUCT_SIZE);

        waterBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, waterBuffer);
        glBufferData(GL_UNIFORM_BUFFER, WAVE_STRUCT_SIZE, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);


    }

    private Material initializeMaterial() {
        Material mat = new Material();
        mat.addColor(Material.ALBEDO_MAP, new Vector3f(0f, 0.8f, 0.6f));
        mat.addProperty(Material.ROUGHNESS, 1.0);
        mat.addProperty(Material.METALNESS, .5);


        mat.addMap(Material.ALBEDO_MAP, new Texture("src/main/resources/miscellaneous/water.jpg", 3));
       // mat.addMap(Material.NORMAL_MAP, new Texture("src/main/resources/miscellaneous/waternormals.jpg", 3));


        return mat;
    }

    public Map<String, List<?>> generateWater(int gridSize, float scaleX, float scaleY) {
        return PlaneGenerator.generate(gridSize, scaleX, scaleY);
    }

    @Override
    public void draw() {


        if(waterSurface != null) {
            glActiveTexture(GL_TEXTURE14);
            waterSurface.use();
        }
        if(coefficients != null) {
            glActiveTexture(GL_TEXTURE13);
            coefficients.use();
        }

        updateBuffer();
        glBindBufferBase(GL_UNIFORM_BUFFER, Shader.WAVE_BLOCK, waterBuffer);


        if (waterMeshes.containsKey(currentLod)) {
            waterMeshes.get(currentLod).setMaterial(material);
            waterMeshes.get(currentLod).draw();
        }
    }

    @Override
    public void compile() {
        waterMeshes.values().forEach(Mesh::compile);
    }

    private Wave calculateWave(int waveIndex, int maxWaves) {
        float amplitude = (float) (this.amplitude / Math.pow(waveIndex, 2.0));
        float frequency = (float) ((speed/this.wavelength + ((waveIndex - 1) / (double) maxWaves) * 0.18) / 0.06);

        return new Wave(
                (float) ((waveIndex + 2) / (double) (maxWaves + 2)),
                (float) (2.0 * (waveIndex + 1) / (double) maxWaves),
                (float) (waveIndex * Math.PI * 2.0 / maxWaves),
                frequency,
                amplitude
        );
    }

    private Texture generateCoefficients() {
        int width = 8, height = 8;
        int totalWaves = width * height;
        ByteBuffer data = ByteBuffer.allocateDirect(3 * totalWaves * 4);

        for (int waveIndex = 0; waveIndex < totalWaves / 2; waveIndex++) {
            Wave wave = calculateWave(waveIndex, totalWaves);

            data.putInt((int) (255 * wave.steepness()));
            data.putInt((int) (255 * wave.speed()));
            data.putInt(0);

            data.putInt((int) (255 * wave.angle())/2);
            data.putInt((int) (255 * wave.frequency()));
            data.putInt((int) (255 * wave.amplitude()));
        }

        data.flip();
        Texture tex =  new Texture(data, 3, width, height);
        coefficients = tex;
        return tex;
    }


    /**
     * layout (std140, binding = 3) uniform waveBlock{
     *     vec2 direction;
     *     float wavelength;
     *     float amplitude;
     *     float steepness;
     *     float speed;
     *     float phase;
     * };
     */
    public void updateBuffer() {
    if (!bufferUpdated) {
        waterStructBuffer.clear();

        // vec2 direction
        waterStructBuffer.putFloat(direction.x);
        waterStructBuffer.putFloat(direction.y);
        // Padding for alignment
        waterStructBuffer.putFloat(wavelength);

        // float amplitude
        waterStructBuffer.putFloat(amplitude);
        // float steepness
        waterStructBuffer.putFloat(steepness);
        // float speed
        waterStructBuffer.putFloat(speed);
        // float phase
        waterStructBuffer.putFloat(phase);

        // Prepare the buffer for upload
        waterStructBuffer.flip();

        // Bind and upload the buffer
        glBindBuffer(GL_UNIFORM_BUFFER, waterBuffer);
        glBufferData(GL_UNIFORM_BUFFER, waterStructBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);


    }
        bufferUpdated = true;
    }


static class PlaneGenerator {

    static Map<String, List<?>> generate(int gridSize, float scaleX, float scaleZ) {
        // Calculate vertex and index counts
        int vertexCount = (gridSize + 1) * (gridSize + 1);
        int triangleCount = 2 * gridSize * gridSize;
        int indexCount = 3 * triangleCount;

        float delta = 2.0f / gridSize; // Step size in normalized [-1, 1] space
        List<Vector3f> vertices = new ArrayList<>(vertexCount);
        List<Vector2f> texcoords = new ArrayList<>(vertexCount);
        List<Integer> indices = new ArrayList<>(indexCount);

        // Generate vertices and texture coordinates
        for (int y = 0; y <= gridSize; y++) {
            for (int x = 0; x <= gridSize; x++) {
                float xPos = (x * delta) * scaleX;
                float zPos = (y * delta) * scaleZ;

                vertices.add(new Vector3f(xPos, 0.0f, zPos));
                texcoords.add(new Vector2f(x * delta, y * delta));
            }
        }

        // Generate indices for triangles
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                int baseIndex = y * (gridSize + 1) + x; // Corrected index calculation
                int rowOffset = gridSize + 1;

                // First triangle
                indices.add(baseIndex);
                indices.add(baseIndex + rowOffset + 1);
                indices.add(baseIndex + 1);

                // Second triangle
                indices.add(baseIndex);
                indices.add(baseIndex + rowOffset);
                indices.add(baseIndex + rowOffset + 1);
            }
        }

        // Pack results into the map
        Map<String, List<?>> result = new HashMap<>();
        result.put("Vertices", vertices);
        result.put("UVs", texcoords);
        result.put("Indices", indices);

        return result;
    }

    }

}

