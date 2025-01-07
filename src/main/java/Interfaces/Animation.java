package Interfaces;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

enum AnimationTypes{
    Linear, Quadratic, Logarithmic
}


@RequiredArgsConstructor
public class Animation{
    private final Quaternionf animatedObject;
    private final Vector3f angularVelocity;
    private final long animationTime; // milliseconds

    private long currentTime; // milliseconds
    private AnimationTypes type;
    @Getter
    private boolean repeat = false;

    // float maybe?
    private void update(long time){
        if(currentTime >= animationTime){
            if(repeat){
                currentTime = 0;
            } else {
                return;
            }
        }
        // animate

        animatedObject.mul(new Quaternionf(angularVelocity.x,angularVelocity.y,angularVelocity.z, 0)
                .mul(0.5f*time));


        currentTime++;
    }

}
