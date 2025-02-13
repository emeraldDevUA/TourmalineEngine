package Effects;

import ResourceImpl.Material;
import ResourceImpl.Mesh;
import ResourceImpl.Shader;
import ResourceImpl.Texture;
import lombok.Getter;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;

@Getter
public class BoundingBoxEffect extends BaseEffect{
private Mesh mesh;

    public BoundingBoxEffect(Shader shader, Vector3f position, Vector3f scale, Quaternionf rotation) throws IOException {
        mesh = new Mesh();
        mesh.setEnableBlending(true);
        mesh.load("src/main/resources/miscellaneous/cube.obj");
        mesh.setPosition(position);
        mesh.setScale(scale);
        mesh.setShadowScale(new Vector3f(0));
        mesh.setPosition(position);
        mesh.setRotQuaternion(rotation);
        mesh.setNoCull(true);
        mesh.setShader(shader);
    }


    @Override
    public void draw(){
        mesh.getShader().setUniform("effectType", -1);
        mesh.draw();
    }

    @Override
    public void compile(){
        mesh.compile();
        mesh.getMaterial().addMap(Material.ALBEDO_MAP,
                new Texture("src/main/resources/miscellaneous/BoundingBoxTexture.png", 4));
    }

}
