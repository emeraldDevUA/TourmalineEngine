package Rendering;

import ResourceImpl.Shader;
import lombok.Getter;

import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;


import static org.lwjgl.opengl.GL20.*;

public class Camera {
  @Setter
  private Vector3f position;
  @Setter
  @Getter
  private Vector3f focus;
  private Matrix4f projectionMatrix;
  private Matrix4f viewMatrix;

  public Camera(Vector3f pos, Vector3f focus){
    this.position = pos;
    this.focus = focus;
    this.viewMatrix = new Matrix4f();
    this.projectionMatrix = new Matrix4f();
  }

  public void loadPerspectiveProjection(float fov, float aspect, float far, float near){
       projectionMatrix = projectionMatrix.setPerspective(fov,aspect, near, far);
      // projectionMatrix = projectionMatrix.ortho(-100,100,-100,100,100,0);
  }

  public void loadViewMatrix(){
        viewMatrix = viewMatrix.lookAt(position, focus, new Vector3f(0,1,0));

  }
  public void setMVP(Shader shader){

      if(shader != null){
          shader.use();
          int shader_pointer = shader.getProgram();
          float[] view_matrix = new float[16];
          float[] projection_matrix = new float[16];
          viewMatrix.get(view_matrix);
          glUniformMatrix4fv(glGetUniformLocation(shader_pointer, "view_matrix"),false, view_matrix);
          projectionMatrix.get(projection_matrix);
          glUniformMatrix4fv(glGetUniformLocation(shader_pointer, "projection_matrix"),false, projection_matrix);
      }
  }

}
