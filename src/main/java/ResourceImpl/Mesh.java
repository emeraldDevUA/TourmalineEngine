package ResourceImpl;

import Interfaces.Drawable;

import Interfaces.Loadable;
import lombok.Getter;
import lombok.Setter;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;


class VBO implements Drawable, Closeable {

    // size constants
    private static final int vertex_size = 3 * Float.BYTES;
    private static final int tex_size = 2 * Float.BYTES;
    private static final int normal_size = vertex_size;
    private static final int color_size = vertex_size;

    private FloatBuffer vertices, normals, uv;
    private int vao, ubo;

    private int verticesBuffer, normalsBuffer, uvsBuffer;

    private int numVertices, numNormals, numUvs;
    private int numFaces;

    public VBO() {

    }

    public VBO(List<Vector3f> vertices, List<Vector3f> normals, List<Vector2f> textureCoordinates) {
        this.vertices = BufferUtils.createFloatBuffer(vertices.size() * 3);
        this.normals = BufferUtils.createFloatBuffer(vertices.size() * 3);
        this.uv = BufferUtils.createFloatBuffer(vertices.size() * 2);

        for (int i = 0; i < vertices.size(); i++) {
            this.vertices.put(vertices.get(i).x);
            this.vertices.put(vertices.get(i).y);
            this.vertices.put(vertices.get(i).z);

            this.normals.put(normals.get(i).x);
            this.normals.put(normals.get(i).y);
            this.normals.put(normals.get(i).z);

            this.uv.put(textureCoordinates.get(i).x);
            this.uv.put(textureCoordinates.get(i).y);
        }
        numFaces = vertices.size() / 3;

        numVertices = vertices.size();
        numNormals = normals.size();
        numUvs = textureCoordinates.size();


    }


    @Override
    public void draw() {

        GL30.glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, numVertices, GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);

    }


    @Override
    public void compile() {
//        System.err.println(STR."v:\{this.numVertices}\nn:\{this.numNormals}\nuv:\{this.numUvs}");
        ubo = glGenBuffers();
        glBindBuffer(GL31.GL_UNIFORM_BUFFER, ubo);
        glBufferData(GL31.GL_UNIFORM_BUFFER, 64, GL_STATIC_DRAW);
        glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);

        IntBuffer indices = BufferUtils.createIntBuffer(numVertices);

        for (int i = 0; i < numVertices; i++) {
            indices.put(i);
        }

        if (numVertices > 0) {
            vao = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vao);
            this.vertices.rewind();
            verticesBuffer = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, verticesBuffer);
            glBufferData(GL_ARRAY_BUFFER, this.vertices, GL_STATIC_DRAW);

            glEnableVertexAttribArray(Shader.POSITION_LOCATION);
            glVertexAttribPointer(Shader.POSITION_LOCATION, 3, GL_FLOAT, false, 0, 0);

        }

        if (numNormals > 0) {
            this.normals.rewind();
            normalsBuffer = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, normalsBuffer);
            glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);

            glEnableVertexAttribArray(Shader.NORMAL_LOCATION);
            glVertexAttribPointer(Shader.NORMAL_LOCATION, 3, GL_FLOAT, false, 0, 0);
        }
        if (numUvs > 0) {
            this.uv.rewind();
            uvsBuffer = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, uvsBuffer);
            glBufferData(GL_ARRAY_BUFFER, this.uv, GL_STATIC_DRAW);

            glEnableVertexAttribArray(Shader.UVS_LOCATION);
            glVertexAttribPointer(Shader.UVS_LOCATION, 2, GL_FLOAT, false, 0, 0);
        }
        indices.flip();
        int elementsBuffer = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementsBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glBindVertexArray(0);

    }


    @Override
    public void close() {
        glDeleteBuffers(ubo);
//        loadedInstances.put(mapName, loadedInstances.get(mapName) - 1);
//
//        if (loadedInstances.get(mapName) != 0)
//            return;

        glDeleteVertexArrays(vao);
        glDeleteBuffers(verticesBuffer);
        glDeleteBuffers(normalsBuffer);
        glDeleteBuffers(uvsBuffer);
//
//        loadedMeshes.remove(mapName);
//        loadedInstances.remove(mapName);
    }
}

public class Mesh implements Loadable, Drawable, Closeable {
    @Getter
    private final Map<String, VBO> map;
    //private final Map<VBO, Material> materialMap;
    private final Vector3f position;
    private final Quaternionf rotQuaternion;
    @Setter
    private Material material;
    @Setter
    private Shader shader;

    public Mesh() {
        map = new ConcurrentHashMap<>();
        position = new Vector3f(0, 0, 0);
        rotQuaternion = new Quaternionf(0, 0, 0, 1);
        material = new Material();
    }

    @Override
    public void load(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        Scanner sc = new Scanner(bis);

        String line;

        String name = null;
        ArrayList<Vector3f> vertices = new ArrayList<>();
        ArrayList<Vector3f> vectors = new ArrayList<>();
        ArrayList<Vector2f> textureCoords = new ArrayList<>();
        Float[] vals;

        mainLoop:
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            line = line.replace("  ", " ");
            //skipping the comment line

            if (line.isBlank()) {
                continue;
            }
            if (line.charAt(0) == '#' && line.length() <= 2) {
                continue;
            }
            if (line.contains("object") || line.contains("o")) {
                name = line.split(" ")[1];
            }
            if (line.substring(0, 2).compareTo("v ") == 0) {
                vals = getFloatValues(line);
                vertices.add(new Vector3f(vals[0], vals[1], vals[2]));
            } else if (line.substring(0, 2).compareTo("vn") == 0) {
                vals = getFloatValues(line);
                if (vals == null) break;
                vectors.add(new Vector3f(vals[0], vals[1], vals[2]));
            } else if (line.substring(0, 2).compareTo("vt") == 0) {
                vals = getFloatValues(line);
                if (vals == null) break;
                textureCoords.add(new Vector2f(vals[0], vals[1]));
            } else if (line.substring(0, 2).compareTo("f ") == 0) {
                ArrayList<Vector3f> finalVertices = new ArrayList<>();
                ArrayList<Vector3f> finalNormals = new ArrayList<>();
                ArrayList<Vector2f> finalUVs = new ArrayList<>();

                while (!(line).contains("faces")) {
                    if (line.charAt(0) == 's') {
                        line = sc.nextLine();
                        continue;
                    }
                    String[] temp = line.split(" ");
                    for (int c = 1; c < temp.length; c++) {
                        try {
                            Integer[] array = getFaces(temp[c]);
                            finalVertices.add(vertices.get(array[0] - 1));
                            finalUVs.add(textureCoords.get((array[1] - 1)));
                            finalNormals.add(vectors.get((array[2] - 1)));

                        } catch (NullPointerException e) {
                            System.err.println(STR."\{vertices.size()}\n\{vectors.size()}\n\{textureCoords.size()}");
                            break;
                        }
                    }

                    if (!sc.hasNext()) {
                        break mainLoop;
                    }

                    line = sc.nextLine();
                }


                //setting bias values because of how OBJ format is structured
                // [ every new vertex is enumerated sequentially, not from 1 to N for every separate object]
                map.put(name, new VBO(finalVertices, finalNormals, finalUVs));
            }

        }

        vertices.clear();
        vectors.clear();
        textureCoords.clear();

        fis.close();
        bis.close();
        sc.close();


    }

    private Integer[] getFaces(String line) {


        String[] arr = line.split("/");
        Integer[] result = new Integer[arr.length];
        int i = 0;
        for (String str : arr) {
            try {
                result[i++] = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                System.err.println("Could read a vertex");

            }
        }


        return result;
    }

    private Float[] getFloatValues(String line) {
        String[] args;
        Float[] values;
        args = line.split(" ");
        values = new Float[3];
        int cnt = 0;
        for (int i = 1; i < args.length; i++) {

            try {
                values[cnt++] = Float.parseFloat(args[i]);
            } catch (NumberFormatException e) {
                return values;
            }
        }
        return values;
    }

    @Override
    public void draw() {

        Matrix4f matrix4f = new Matrix4f();
        matrix4f.translate(position).rotate(rotQuaternion);
        float[] model_matrix = new float[16];
        matrix4f.get(model_matrix);

        // not good, is going to deter performance.
        if (shader != null) {
            int shader_pointer = shader.getProgram();
            glUniformMatrix4fv(glGetUniformLocation(shader_pointer, "model_matrix"),false, model_matrix);
        }
        material.use();
        for (VBO vbo : map.values()) {
            vbo.draw();
        }

    }

    @Override
    public void compile() {
        for (VBO vbo : map.values()) {
            vbo.compile();
        }
    }

    @Override
    public void close(){
        map.clear();
        material.close();
    }


}
