package gdraw.graph.util.action;

import gdraw.graph.util.MIandButtonPair;

import java.util.Stack;

public class ActionHelper {
    private MIandButtonPair FXML;
    private Stack<Action> stack;

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
}
