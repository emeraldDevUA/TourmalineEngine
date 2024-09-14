package Controls;

import Interfaces.KeyboardEventHandler;
import lombok.NoArgsConstructor;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Stack;

import static org.lwjgl.glfw.GLFW.*;

@NoArgsConstructor
public class Keyboard {
    private static HashMap<String, Integer> keys;
    private static HashMap<String, Integer> state;
    private Stack<Integer> actions;

    private void processEvents(KeyboardEventHandler handler){

    }

    public void init(){
        keys.put("W", GLFW_KEY_W);
        keys.put("A", GLFW_KEY_A);
        keys.put("S", GLFW_KEY_S);
        keys.put("D", GLFW_KEY_D);


        keys.put("C", GLFW_KEY_C);
        keys.put("V", GLFW_KEY_V);
        keys.put("M", GLFW_KEY_M);
        keys.put("N", GLFW_KEY_N);

        keys.put("R_SHIFT", GLFW_KEY_RIGHT_SHIFT);
        keys.put("L_SHIFT", GLFW_KEY_LEFT_SHIFT);

        keys.put("ENTER", GLFW_KEY_ENTER);
        keys.put("TAB", GLFW_KEY_TAB);
        keys.put("BACKSPACE", GLFW_KEY_BACKSPACE);


        state.put("PRESSED",        GLFW_PRESS);
        state.put("RELEASED",    GLFW_RELEASE );
        state.put("REPEATED",     GLFW_REPEAT );
        state.put("UNKNOWN",  GLFW_KEY_UNKNOWN);
    }


}
