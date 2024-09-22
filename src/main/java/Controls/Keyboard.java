package Controls;

import Interfaces.KeyboardEventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.HashMap;
import java.util.Stack;

import static org.lwjgl.glfw.GLFW.*;

@NoArgsConstructor
public class Keyboard {
    // Do I really have to do that?
    private static HashMap<String, Integer> keys;

    private Stack<key_state> actions;

    @Setter
    private int window_pointer = -1;

    private void processEvents(KeyboardEventHandler handler){
        keys.values().forEach(
                l->{
                   if( glfwGetKey(window_pointer, l) == GLFW_PRESS ) {
                        actions.push(new key_state(l, GLFW_PRESS));
                   }

                    if( glfwGetKey(window_pointer, l) == GLFW_REPEAT) {
                        actions.push(new key_state(l, GLFW_REPEAT));
                    }

                    if( glfwGetKey(window_pointer, l) == GLFW_RELEASE ) {
                        actions.push(new key_state(l, GLFW_RELEASE));
                    }

                    if( glfwGetKey(window_pointer, l) == GLFW_KEY_UNKNOWN ) {
                        actions.push(new key_state(l, GLFW_KEY_UNKNOWN));
                    }

                }
        );
        while(!actions.empty()) {
            handler.processKey(actions.peek().key, actions.peek().state);
            actions.pop();
        }


    }

    public void init(){
        keys = new HashMap<>();
        actions = new Stack<>();

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


    }

    @AllArgsConstructor
    @Getter
    @Setter
    private static class key_state{
       // String s_key;
        Integer key;
        Integer state;
    }
}
