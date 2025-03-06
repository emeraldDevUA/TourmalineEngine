package Rendering;

import ResourceImpl.Shader;
import lombok.Getter;

import lombok.Setter;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;


import static org.lwjgl.opengl.GL20.*;

public class Camera {
  @Setter
  @Getter
  private Vector3f position;
  @Setter
  @Getter
  private Vector3f focus;
  private Matrix4f projectionMatrix;
  private Matrix4f viewMatrix, invertedViewMatrix;
  private Matrix4f previousViewMatrix;

  @Getter
  private Quaternionf quaternionf;
  private final Vector3f positionDifference;

  public Camera(Vector3f pos, Vector3f focus){
    this.position = pos;
    this.focus = focus;
    this.viewMatrix = new Matrix4f();
    this.invertedViewMatrix = new Matrix4f();
    this.previousViewMatrix = new Matrix4f();
    this.projectionMatrix = new Matrix4f();
    this.positionDifference = pos.sub(focus, new Vector3f());
    this.quaternionf = new Quaternionf(0,0,0,1);
  }

  public void loadPerspectiveProjection(float fov, float aspect, float far, float near){
       projectionMatrix = projectionMatrix.setPerspective(fov,aspect, near, far);
      // projectionMatrix = projectionMatrix.ortho(-100,100,-100,100,100,0);
  }
  public void loadOrthographicProjection(float left, float right, float bottom, float top, float zNear, float zFar){

      projectionMatrix = projectionMatrix.setOrtho(left,right,bottom,top,zNear,zFar);
  }


    public void loadViewMatrix() {
        // Ensure viewMatrix is initialized before using it
        if (viewMatrix == null) {
            viewMatrix = new Matrix4f(); // Initialize viewMatrix if it’s null
        } else {
            viewMatrix.identity();
        }
        previousViewMatrix = new Matrix4f(viewMatrix);
        viewMatrix.lookAt(position, focus, new Vector3f(0, 1, 0));
        //invertedViewMatrix = new Matrix4f(viewMatrix).invert();
    }

  public void setPosition(final Quaternionf quaternion, final Vector3f position){

      this.position = (position.rotate(quaternion));
  }

    public void setViewProjectionMatrix(Shader shader) {
        if (shader != null) {
            shader.use();
            int shaderPointer = shader.getProgram();

            float[] viewMatrixData = new float[16];
            float[] projectionMatrixData = new float[16];

            viewMatrix.get(viewMatrixData);
            projectionMatrix.get(projectionMatrixData);

            int viewMatrixLocation = glGetUniformLocation(shaderPointer, "view_matrix");
            if (viewMatrixLocation != -1) {
                glUniformMatrix4fv(viewMatrixLocation, false, viewMatrixData);
            }

            int projectionMatrixLocation = glGetUniformLocation(shaderPointer, "projection_matrix");
            if (projectionMatrixLocation != -1) {
                glUniformMatrix4fv(projectionMatrixLocation, false, projectionMatrixData);
            }


            int previousViewMatrixLocation = glGetUniformLocation(shaderPointer, "previous_view_matrix");
            if (previousViewMatrixLocation != -1) {
                previousViewMatrix.get(viewMatrixData);
                glUniformMatrix4fv(previousViewMatrixLocation, false, viewMatrixData);
            }


            int invViewMatrixLocation = glGetUniformLocation(shaderPointer, "inverted_view_matrix");
            if (viewMatrixLocation != -1) {
            //    invertedViewMatrix.get(viewMatrixData);
             //   glUniformMatrix4fv(invViewMatrixLocation, false, viewMatrixData);
            }


        }
    }





    public void setShadowViewProjectionMatrix(Shader shader) {

        if (shader != null) {
            shader.use();
            int shaderPointer = shader.getProgram();

            float[] viewMatrixData = new float[16];
            float[] projectionMatrixData = new float[16];

            // Get data from matrices
            viewMatrix.get(viewMatrixData);
            projectionMatrix.get(projectionMatrixData);

            int shadowViewMatrixLocation = glGetUniformLocation(shaderPointer, "shadow_view_matrix");
            if (shadowViewMatrixLocation != -1) {
                glUniformMatrix4fv(shadowViewMatrixLocation, false, viewMatrixData);
            }

            int shadowProjectionMatrixLocation = glGetUniformLocation(shaderPointer, "shadow_projection_matrix");
            if (shadowProjectionMatrixLocation != -1) {
                glUniformMatrix4fv(shadowProjectionMatrixLocation, false, projectionMatrixData);
            }
        }
    }
  public void update(){
      position = focus.add(positionDifference);
      loadViewMatrix();
  }

}
