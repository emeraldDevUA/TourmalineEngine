package Rendering;

import ResourceImpl.Shader;
import lombok.Getter;

import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform4fv;

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
    this.viewMatrix = new Matrix4f();
    this.projectionMatrix = new Matrix4f();
  }

  public void loadPerspectiveProjection(float fov, float aspect, float far, float near){
        projectionMatrix = projectionMatrix.perspective(fov,aspect, near, far);
  }

  public void loadViewMatrix(){
        viewMatrix = viewMatrix.lookAt(position, focus, new Vector3f(1,0,0));

  }
  public void setMVP(Shader shader){

      float[] array = new float[16];
      int shader_pointer = shader.getProgram();
      array = projectionMatrix.get(array);
      glUniform4fv(glGetUniformLocation(shader_pointer, "projectionMatrix"), array);
      array = viewMatrix.get(array);
      glUniform4fv(glGetUniformLocation(shader_pointer, "viewMatrix"), array);

  }

}
