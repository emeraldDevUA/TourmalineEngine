package Interfaces;

public interface KeyboardEventHandler {
    /**
     *
     * @param key refers to GLFW_KEY variables
     * @param state refers to GLFW_STATE variables
     *  This interface should be implemented by the developer for convenient keyboard action management.
     */
    void processKey(Integer key, Integer state);

}
