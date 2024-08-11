package Annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OpenGLWindow {
    String windowName();
    int[] defaultDimensions();
    long[] hints() default { };

}
