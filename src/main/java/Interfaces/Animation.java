package Interfaces;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

;

@RequiredArgsConstructor
public abstract class Animation<T>{
    private final T startPos;
    private final T finalPos;
    private final AnimationType animationType;

    public abstract void update(float dt);
    public abstract void restartAnimation();


}