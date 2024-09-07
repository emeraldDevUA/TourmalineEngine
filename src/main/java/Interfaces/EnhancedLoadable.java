package Interfaces;

public interface EnhancedLoadable  extends Loadable{
    /**
     * Compiles the loadable object. It is used for loading stages that require OpenGL context to be current.
     */
    void assemble();
}
