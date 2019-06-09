package gdraw.graph.util.action;

import gdraw.graph.util.MIandButtonPair;

import java.util.Stack;

public class ActionHelper {
    private MIandButtonPair FXML;
    private Stack<Action> stack;

    public void clear() {
        stack.clear();
    }

    public interface Helper {
        void apply(Action e);
    }

    public ActionHelper(MIandButtonPair item){
        FXML = item;
        stack = new Stack<>();
    }

    public void pop(){
        if(!stack.empty()){
            stack.pop().action();
        }
        if(stack.empty()) FXML.setDisable(true);
    }

    public void push(Action action){
        stack.push(action);
        FXML.setDisable(false);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public void forEach(Helper h){
        stack.forEach(e -> h.apply(e));
    }
}
