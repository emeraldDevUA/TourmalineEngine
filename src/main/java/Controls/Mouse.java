package Controls;

import Interfaces.MouseEventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lwjgl.glfw.GLFWMouseButtonCallback;


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
    private long window_pointer = -1;
    private Set<Integer> keys;


    public void processEvents(MouseEventHandler handler){

        glfwGetCursorPos(window_pointer, X,Y);


        while(!actions.empty()) {
            handler.processMouseEvent(actions.peek().key, actions.peek().state);
            actions.pop();
        }

        handler.processMouseMovement(X[0],Y[0]);
    }

    public void init(){
        actions = new Stack<>();
        keys = new HashSet<>();
        keys.add(GLFW_MOUSE_BUTTON_LEFT);
        keys.add(GLFW_MOUSE_BUTTON_RIGHT);
        keys.add(GLFW_MOUSE_BUTTON_3);
        keys.add(GLFW_MOUSE_BUTTON_4);
        keys.add(GLFW_MOUSE_BUTTON_5);

        X = new double[1];
        Y = new double[1];
        glfwSetMouseButtonCallback(window_pointer, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window,  int button, int action, int mods) {
                keys.forEach(
                        l->{
                            if( action == GLFW_PRESS && l == button) {
                                actions.push(new key_state(l, GLFW_PRESS));
                            }
                            if( action == GLFW_REPEAT && l == button) {
                                actions.push(new key_state(l, GLFW_REPEAT));
                            }
                            if( action == GLFW_RELEASE && l == button) {
                                actions.push(new key_state(l, GLFW_RELEASE));
                            }
                            if( action == GLFW_KEY_UNKNOWN && l == button) {
                                actions.push(new key_state(l, GLFW_KEY_UNKNOWN));
                            }

                        }
                );

            }
        });

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
