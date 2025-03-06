package ResourceImpl;


import Interfaces.Drawable;

import lombok.Getter;

import java.io.*;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL30.*;


public class VBO implements Drawable, Closeable {

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

    @Getter
    private List<Vector3f> finalVertices;
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
        finalVertices = vertices;
    }

    public VBO(List<Integer> indices, List<Vector3f> vertices, List<Vector3f> normals, List<Vector2f> textureCoordinates) {
        this.vertices = BufferUtils.createFloatBuffer(vertices.size() * 3);
        this.normals = BufferUtils.createFloatBuffer(normals.size() * 3);
        this.uv = BufferUtils.createFloatBuffer(textureCoordinates.size() * 2);

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

        for (int i = 0; i < indices.size(); i++) {
            this.indices.put(indices.get(i));
        }
        finalVertices = vertices;
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
