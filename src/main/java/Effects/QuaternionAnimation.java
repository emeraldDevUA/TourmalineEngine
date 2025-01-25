package Effects;

import Interfaces.Animation;
import Interfaces.AnimationType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class QuaternionAnimation extends Animation<Quaternionf> {

    private Quaternionf state;

    public QuaternionAnimation(Quaternionf startPos,
                               Quaternionf finalPos,
                               AnimationType animationType) {
        super(startPos, finalPos, animationType);
        state = new Quaternionf(startPos);
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void restartAnimation() {

    }
}
