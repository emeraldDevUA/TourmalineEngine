package Interfaces;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joml.Quaternionf;
import org.joml.Vector3f;



@RequiredArgsConstructor
public abstract class Animation<T>{
    protected final T startPos;
    protected final T finalPos;
    protected final AnimationType animationType;


    public abstract void update(float dt);
    public abstract void restartAnimation();
}

