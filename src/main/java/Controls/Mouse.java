package Controls;

import Interfaces.MouseEventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

@NoArgsConstructor
public class Mouse {
    public double[] X, Y;
    private Stack<key_state> actions;
    @Setter
    private int window_pointer = -1;
    private Set<Integer> keys;


    public void processEvents(MouseEventHandler handler){

        glfwGetCursorPos(window_pointer, X,Y);

        keys.forEach(
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
            handler.processMouseEvent(actions.peek().key, actions.peek().state);
            actions.pop();
        }

        handler.processMouseMovement(X[0],Y[0]);
    }

    public void init(){
        actions = new Stack<>();
        keys = new HashSet<>();

        keys.add(GLFW_MOUSE_BUTTON_1);
        keys.add(GLFW_MOUSE_BUTTON_2);
        keys.add(GLFW_MOUSE_BUTTON_3);
        keys.add(GLFW_MOUSE_BUTTON_4);
        keys.add(GLFW_MOUSE_BUTTON_5);


        X = new double[1];
        Y = new double[1];
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
