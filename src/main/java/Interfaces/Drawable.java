package Interfaces;

public interface Drawable {
    /**
     * This method is used to render any object. It must be implemented by any drawable entity.
     */
    void draw();
    /**
     * Legacy method, partially overlaps with EnhancedLoadable ( void assemble(); ).
     */
    void compile();

}
