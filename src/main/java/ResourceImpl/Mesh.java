package ResourceImpl;

import Interfaces.Drawable;

import Interfaces.Loadable;
import lombok.Getter;
import lombok.Setter;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.opengl.GL30;



import java.io.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.opengl.GL30.*;



class VBO implements Drawable, Closeable {

    // size constants
    private static final int vertex_size = 3 * Float.BYTES;
    private static final int tex_size = 2 * Float.BYTES;
    private static final int normal_size = vertex_size;
    private static final int color_size = vertex_size;

    private FloatBuffer vertices, normals, uv;
    private int vao;

    private int verticesBuffer, normalsBuffer, uvsBuffer;

    private int numVertices, numNormals, numUvs;

    private IntBuffer indices;
    public VBO() {

    }

    public VBO(List<Vector3f> vertices, List<Vector3f> normals, List<Vector2f> textureCoordinates) {
        this.vertices = BufferUtils.createFloatBuffer(vertices.size() * 3);
        this.normals = BufferUtils.createFloatBuffer(normals.size() * 3);
        this.uv = BufferUtils.createFloatBuffer(textureCoordinates.size() * 2);

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


        numVertices = vertices.size();
        numNormals = normals.size();
        numUvs = textureCoordinates.size();

        indices = BufferUtils.createIntBuffer(numVertices);
        for (int i = 0; i < numVertices; i++) {
            indices.put(i);
        }
    }

    public VBO(List<Integer> indices, List<Vector3f> vertices, List<Vector3f> normals, List<Vector2f> textureCoordinates) {
        this.vertices = BufferUtils.createFloatBuffer(indices.size() * 3);
        this.normals = BufferUtils.createFloatBuffer(indices.size() * 3);
        this.uv = BufferUtils.createFloatBuffer(indices.size() * 2);

        for (int i = 0; i < vertices.size(); i++) {
            this.vertices.put(vertices.get(i).x);
            this.vertices.put(vertices.get(i).y);
            this.vertices.put(vertices.get(i).z);
        }
        for(int i = 0; i < normals.size(); i ++) {
            this.normals.put(normals.get(i).x);
            this.normals.put(normals.get(i).y);
            this.normals.put(normals.get(i).z);
        }

        for(int i = 0 ; i < textureCoordinates.size(); i ++) {
            this.uv.put(textureCoordinates.get(i).x);
            this.uv.put(textureCoordinates.get(i).y);
        }


        numVertices = vertices.size();
        numNormals = normals.size();
        numUvs = textureCoordinates.size();

        this.indices = BufferUtils.createIntBuffer(indices.size());

        indices.forEach(index ->{this.indices.put(index);});
    }


    @Override
    public void draw() {
        //glBindBufferBase(GL_UNIFORM_BUFFER, Shader.MODEL_BLOCK, ubo);
        GL30.glBindVertexArray(vao);
        GL30.glDrawElements(GL_TRIANGLES, numVertices, GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }


    @Override
    public void compile() {
//        System.err.println(STR."v:\{this.numVertices}\nn:\{this.numNormals}\nuv:\{this.numUvs}");
//        ubo = glGenBuffers();
//        glBindBuffer(GL31.GL_UNIFORM_BUFFER, ubo);
//        glBufferData(GL31.GL_UNIFORM_BUFFER, 64, GL_STATIC_DRAW);
//        glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);
        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);





        if (numVertices > 0) {

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
        glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, elementsBuffer);
        glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices, GL30.GL_STATIC_DRAW);

        glBindVertexArray(0);

    }


    @Override
    public void close() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(verticesBuffer);
        glDeleteBuffers(normalsBuffer);
        glDeleteBuffers(uvsBuffer);
    }
}
@Getter
@Setter
public class Mesh implements Loadable, Drawable, Closeable {
    private  Map<String, VBO> map;

    private Vector3f position;
    private Vector3f scale;
    private Vector3f shadowScale;

    private Quaternionf rotQuaternion;

    private Material material;
    private Shader shader;

    private boolean updated;
    private boolean noCull;


    private float[] model_matrix;

    public Mesh() {
        map = new ConcurrentHashMap<>();
        position = new Vector3f(0, 0, 0);
        scale = new Vector3f(1,1,1);
        shadowScale = new Vector3f(1,1,1);
        rotQuaternion = new Quaternionf(0, 0, 0, 1);
        material = new Material();
        updated = true;
        noCull = false;
        model_matrix = new float[16];
    }

    public Mesh(String name, Map<String, List<?>> params) {
        map = new ConcurrentHashMap<>();
        position = new Vector3f(0, 0, 0);
        scale = new Vector3f(1,1,1);
        shadowScale = new Vector3f(1,1,1);
        rotQuaternion = new Quaternionf(0, 0, 0, 1);
        material = new Material();
        updated = true;
        model_matrix = new float[16];

        map.put(name, new VBO(
                (List<Integer>) params.get("Indices"),
                (List<Vector3f>) params.get("Vertices"),
                (List<Vector3f>) params.get("Vertices"),
                (List<Vector2f>) params.get("UVs"))
        );

    }



    @Override
    public void load(String path) throws IOException {

        if(!path.contains(".obj")){
            lib_load(path);
            return;
        }
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        Scanner sc = new Scanner(bis);

        String line;

        String name = null;
        ArrayList<Vector3f> vertices = new ArrayList<>();
        ArrayList<Vector3f> vectors = new ArrayList<>();
        ArrayList<Vector2f> textureCoords = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        Float[] vals;

        mainLoop:
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            line = line.replace("  ", " ");
            //skipping the comment line

            if (line.isBlank()) {
                continue;
            }
            if (( line.charAt(0) == '#' && line.length() <= 2 ) || line.contains("mtlib")) {
                continue;
            }
            if (line.contains("object") || line.contains("o")) {
                name = line.split(" ")[1];
                VBO retrievedVBO = map.get(name);

                if(retrievedVBO != null){
                    map.put(STR."_\{name}", retrievedVBO);
                }
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
                            indices.add(array[0] - 1);
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
                // map.put(name, new VBO(indices, vertices, vectors, textureCoords));
                // indices.clear();
            }

        }

        vertices.clear();
        vectors.clear();
        textureCoords.clear();
        indices.clear();

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
                System.err.println("Couldn't read a vertex");

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
            } catch (NumberFormatException|ArrayIndexOutOfBoundsException e) {
                return values;
            }
        }
        return values;
    }

    @Override
    public void draw() {

        if(updated) {
            Matrix4f matrix4f = new Matrix4f().identity();
            matrix4f.translate(position)
                    .scale(scale)
                    .rotate(rotQuaternion);
            model_matrix = matrix4f.get(model_matrix);
        }
        if (shader != null) {
            int shader_pointer = shader.getProgram();
            int scaleVectorLocation = glGetUniformLocation(shader_pointer, "scale_vector");
            int modelMatrixLocation = glGetUniformLocation(shader_pointer, "model_matrix");

            glUniformMatrix4fv(modelMatrixLocation, false, model_matrix);
            glUniform3f(scaleVectorLocation, shadowScale.x,shadowScale.y, shadowScale.z);


        }

        material.use();

        if(noCull){
            glDisable(GL_CULL_FACE);
        }
        for (VBO vbo : map.values()) {
            vbo.draw();
        }
        if(noCull){
            glEnable(GL_CULL_FACE);
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

    private void lib_load(String path){

        ArrayList<Vector3f> vertices = new ArrayList<>();
        ArrayList<Vector3f> normals = new ArrayList<>();
        ArrayList<Vector2f> textureCoords = new ArrayList<>();
        ArrayList<Integer> indices_ = new ArrayList<>();

        int flags = aiProcess_Triangulate |  // Ensure all meshes are made of triangles
                aiProcess_GenNormals |   // Generate normals if missing
                aiProcess_OptimizeMeshes; // Optimize meshes for better performance

        // Load the model using Assimp
        AIScene scene = Assimp.aiImportFile(path, flags);
        assert scene != null;
        PointerBuffer meshes = scene.mMeshes();
        String meshName = scene.mName().dataString();
        for (int i = 0; i < scene.mNumMeshes(); i++) {
            AIMesh aiMesh = AIMesh.create(meshes.get(i));

            boolean hasNormals = aiMesh.mNormals() != null;
            boolean hasUVs = aiMesh.mTextureCoords(0) != null;

            for (int k = 0; k < aiMesh.mNumVertices(); k++) {
                vertices.add(convertVec(aiMesh.mVertices().get(k)));


                if (hasNormals) {
                    normals.add(convertVec(aiMesh.mNormals().get(k)));
                }


                if (hasUVs) {
                    textureCoords.add(new Vector2f(
                            aiMesh.mTextureCoords(0).get(k).x(),
                            aiMesh.mTextureCoords(0).get(k).y())
                    );
                }
            }



            for (int m = 0; m < aiMesh.mNumFaces(); m++) {
                indices_.add(aiMesh.mFaces().get(m).mIndices().get(0));
                indices_.add(aiMesh.mFaces().get(m).mIndices().get(1));
                indices_.add(aiMesh.mFaces().get(m).mIndices().get(2));
            }



        }
        map.put(meshName, new VBO(indices_, vertices, normals, textureCoords));

    }

    private Vector3f convertVec(AIVector3D vec){
        return  new Vector3f(vec.x(), vec.y(), vec.z());
    }
}
