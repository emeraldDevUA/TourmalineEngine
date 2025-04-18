package Effects;

import Interfaces.Drawable;
import lombok.Getter;
import lombok.Setter;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Getter
@Setter

public abstract class BaseEffect implements Drawable {
    @Setter
    private Vector3f mainPosition;
    private Quaternionf mainRotation;
    private Vector3f scaleVector;
    public boolean obsolete = false;

    @Override
    public void draw() {

    }

    @Override
    public void compile() {

    }
}
