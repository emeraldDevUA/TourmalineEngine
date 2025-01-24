package Rendering.Lights;

import ResourceImpl.Shader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

public class LightingConfigurator {
    private static int bufferBase = -1;
    private static int COMPUTED_SIZE = 624;
    private static ByteBuffer BufferedLights = ByteBuffer.allocateDirect(COMPUTED_SIZE);


    public static void setLights(List<AbstractLight> lights, Shader shaderProgram){

        if(bufferBase == -1)
             bufferBase = glGenBuffers();


        // a lot honestly

        glBindBuffer(GL_UNIFORM_BUFFER, bufferBase);
        glBufferData(GL_UNIFORM_BUFFER, COMPUTED_SIZE, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);


        List<PointLight> pointLights = new ArrayList<>();
        List<DirectionalLight> dirLights = new ArrayList<>();
        lights.forEach(light->{
            if(light instanceof PointLight){
                pointLights.add((PointLight) light);
            }else if(light instanceof DirectionalLight){
                dirLights.add((DirectionalLight) light);
            }else{
                throw new ClassCastException(STR."Unsupported Light Type! \{light.getClass().getName()}");
            }

        });

        int bound = 5;
        for(int i =  0 ; i < bound; i ++){
            DirectionalLight dl = dirLights.get(i);
            if(dl!=null) {
                BufferedLights.put(dl.formLight());
            }else{
                BufferedLights.put(DirectionalLight.getEmptyBuffer());
            }
        }

        bound = 50;

        for(int i =  0 ; i < bound; i ++){
            PointLight pl = pointLights.get(i);
            if(pl!=null) {
                BufferedLights.put(pl.formLight());
            }else{
                BufferedLights.put(PointLight.getEmptyBuffer());
            }
        }

        BufferedLights.flip();

        shaderProgram.use();
        glBindBufferBase(GL_UNIFORM_BUFFER, 4, bufferBase);

        shaderProgram.setUniform("number_pointLights", pointLights.size());
        shaderProgram.setUniform("number_dirLights",   dirLights.size());

        shaderProgram.unbind();
    }



}
