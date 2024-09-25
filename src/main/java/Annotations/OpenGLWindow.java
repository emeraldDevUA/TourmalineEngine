package Annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OpenGLWindow {
    String windowName();
    /**
     * Accepts width and height IN THIS ORDER. Is used before the main class.
     */
    int[] defaultDimensions();
    /**
     * Accepts default GLFW parameters in an array and uses int[ ] windowHintsValues as values
     */
    int[] windowHints() default { };

    /**
     * Is used to pass values for GLFW parameters, address the official OGL documentation for further information.
     */
    int[] windowHintsValues() default { };
}
