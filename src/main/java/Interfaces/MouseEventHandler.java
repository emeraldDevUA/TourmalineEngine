package Interfaces;

public interface MouseEventHandler {

    void processMouseEvent(int key, int action);

    /**
     * This method is to be used for handling mouse movements.
     */
    void processMouseMovement(double X, double Y);

   // void processScrolling();
}
