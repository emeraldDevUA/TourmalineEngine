package ResourceImpl;

import Interfaces.Drawable;

import Interfaces.Loadable;
import lombok.Getter;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBVertexBufferObject;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.ARBUniformBufferObject.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.ARBUniformBufferObject.glBindBufferBase;
import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.*;
@SuppressWarnings("unused")
class VBO implements Drawable {
    private static final int vertex_size = 3*Float.BYTES;
    private static final int tex_size = 2*Float.BYTES;
    private static final int normal_size = vertex_size;
    private static final int color_size = vertex_size;
    private ByteBuffer modelBuffer;
    private FloatBuffer vertices, normals, uv;
    private int vbo, vao;
    private int ubo;

    private int numFaces;
    public VBO(){

    }
    public VBO(List<Vector3f> vertices, List<Vector3f> normals, List<Vector2f> textureCoordinates){
        this.vertices = BufferUtils.createFloatBuffer(vertices.size()*3);
        this.normals = BufferUtils.createFloatBuffer(vertices.size()*3);
        this.uv = BufferUtils.createFloatBuffer(vertices.size()*2);

        for(int i = 0; i < vertices.size(); i ++){
            this.vertices.put(vertices.get(i).x);
            this.vertices.put(vertices.get(i).y);
            this.vertices.put(vertices.get(i).z);


            this.normals.put(normals.get(i).x);
            this.normals.put(normals.get(i).y);
            this.normals.put(normals.get(i).z);

            this.uv.put(textureCoordinates.get(i).x);
            this.uv.put(textureCoordinates.get(i).y);
        }


        allocate(vertices.size()*(vertex_size+tex_size+normal_size));
        modelBuffer.order(ByteOrder.nativeOrder());

        for (int i = 0; i < vertices.size(); i++) {
            modelBuffer.putInt(i);
        }
        numFaces = vertices.size()/3;
    }



    @Override
    public void draw() {
        glBindBufferBase(GL_UNIFORM_BUFFER, Shader.MODEL_BLOCK, ubo);
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, numFaces, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    @Override
    public void compile() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        vbo = ARBVertexBufferObject.glGenBuffersARB();


    }
    private void allocate(int size) {
        modelBuffer = ByteBuffer.allocateDirect(size);
        modelBuffer.order(ByteOrder.nativeOrder()    );
    }
}

public class Mesh implements Loadable, Drawable {
    @Getter
    private final Map<String, VBO> map;
    private final Vector3f position;
    private final Quaternionf rotQuaternion;

    public Mesh(){
        map = new ConcurrentHashMap<>();
        position = new Vector3f(0,0,0);
        rotQuaternion = new Quaternionf(0,0,0,1);
    }

    @Override
    public void load(String path) throws FileNotFoundException {
        File file = new File(path);
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            Scanner sc = new Scanner(bis);

            String line;

            String name = null;
            ArrayList<Vector3f> vertices = new ArrayList<>();
            ArrayList<Vector3f> vectors = new ArrayList<>();
            ArrayList<Vector2f> textureCoords = new ArrayList<>();
            Float[] vals;

            mainLoop:while (sc.hasNextLine()){
                line = sc.nextLine();
                line= line.replace("  "," ");
                //skipping the comment line

                if(line.isBlank()){ continue ;}
                if(line.charAt(0) == '#' && line.length()<=2){
                    continue ;
                }
                if (line.contains("object")||line.contains("o")) {
                    name = line.split(" ")[1];
                }
                if(line.substring(0,2).compareTo("v ")==0){
                    vals = getFloatValues(line);
                    vertices.add(new Vector3f(vals[0],vals[1],vals[2]));
                }

                else if(line.substring(0,2).compareTo("vn")==0){
                    vals = getFloatValues(line);
                    if (vals == null) break;
                    vectors.add(new Vector3f(vals[0],vals[1],vals[2]));
                }

                else if(line.substring(0,2).compareTo("vt")==0){
                    vals = getFloatValues(line);
                    if(vals==null) break;
                    textureCoords.add(new Vector2f(vals[0],vals[1]));
                }

                else if(line.substring(0,2).compareTo("f ") == 0){
                    ArrayList<Vector3f> finalVertices = new ArrayList<>();
                    ArrayList<Vector3f> finalNormals = new ArrayList<>();
                    ArrayList<Vector2f> finalUVs = new ArrayList<>();

                    while (!(line).contains("faces")) {
                        if(line.charAt(0)=='s'){
                            line = sc.nextLine();
                            continue ;
                        }
                        String[] temp = line.split(" ");
                        for (int c = 1; c < temp.length; c++) {
                            try {
                                Integer[] array = getFaces(temp[c]);
                                finalVertices.add(vertices.get(array[0] - 1 ));
                                finalUVs.add(textureCoords.get((array[1] -1)));
                                finalNormals.add(vectors.get((array[2]-1 )));

                            } catch (NullPointerException e){
                                System.err.println(STR."\{vertices.size()}\n\{vectors.size()}\n\{textureCoords.size()}");
                                break ;
                            }
                        }

                        if(!sc.hasNext()){break mainLoop; }

                        line = sc.nextLine();
                    }


                    //setting bias values because of how OBJ format is structured
                    // [ every new vertex is enumerated sequentially, not from 1 to N for every separate object]
                    map.put(name, new VBO(finalVertices, finalNormals,finalUVs));
                }

            }

            vertices.clear();
            vectors.clear();
            textureCoords.clear();

            fis.close();
            bis.close();
            sc.close();

        }catch (IOException e){
            System.err.println(e.getMessage());

        }

    }

    private Integer[] getFaces(String line){


        String[] arr = line.split("/");
        Integer[] result = new Integer[arr.length];
        int i = 0;
        for (String str: arr){
            try {
                result[i++] = Integer.parseInt(str);
            }catch (NumberFormatException e){
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
            } catch (NumberFormatException e){
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
}
