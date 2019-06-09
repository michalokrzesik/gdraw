package gdraw.graph.util.action;

import gdraw.graph.node.Node;
import gdraw.graph.vertex.Vertex;

import java.util.Stack;

public abstract class Action {
    protected ActionHelper from;
    protected ActionHelper to;

    public abstract void action();

    protected void changeStacks(){
        ActionHelper tmp = from;
        to.push(this);
        from = to;
        to = tmp;
    }

}
