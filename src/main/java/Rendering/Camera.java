package Rendering;

import ResourceImpl.Shader;
import lombok.Getter;

import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.*;

@SuppressWarnings("unused")
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
    this.viewMatrix = new Matrix4f().identity();
    this.projectionMatrix = new Matrix4f().identity();
  }

  public void loadPerspectiveProjection(float fov, float aspect, float far, float near){
        projectionMatrix = projectionMatrix.setPerspective(fov,aspect, near, far);
      //projectionMatrix = projectionMatrix.ortho(-100,100,-100,100,100,0);
  }

  public void loadViewMatrix(){
        viewMatrix = viewMatrix.lookAt(position, focus, new Vector3f(0,1,0));

  }
  public void setMVP(Shader shader){
      shader.use();
      float[] array = new float[16];
      int shader_pointer = shader.getProgram();
      array = projectionMatrix.get(array);
      glUniformMatrix4fv(glGetUniformLocation(shader_pointer, "projection_matrix"), false,array);
      array = viewMatrix.get(array);
      glUniformMatrix4fv(glGetUniformLocation(shader_pointer, "view_matrix"), false,array);
      shader.unbind();
  }

}
