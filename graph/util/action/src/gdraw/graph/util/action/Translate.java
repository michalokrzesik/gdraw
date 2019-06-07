package gdraw.graph.util.action;

import gdraw.graph.node.Node;
import gdraw.graph.util.Selectable;
import gdraw.graph.vertex.Vertex;

public class Translate extends Action {
    Selectable object;
    double dx, dy;
    private Translate(ActionHelper from, Selectable object, double dx, double dy, ActionHelper to) {
        this.from = to;
        this.to = from;
        this.object = object;
        this.dx = -dx;
        this.dy = -dy;
        to.push(this);
    }

    public static void applyTranslate(ActionHelper from, Selectable object, double dx, double dy, ActionHelper to) {
        new Translate(from, object, dx, dy, to);
    }

    @Override
    public void action() {
        object.translate(dx, dy);
        dx = -dx;
        dy = -dy;
        changeStacks();
    }

    @Override
    public void refresh(Node oldNode, Node newNode) {
        if(object.isNode() && object == oldNode) object = newNode;
    }

    @Override
    public void refresh(Vertex oldVertex, Vertex newVertex) {
        if(!object.isNode() && object == oldVertex) object = newVertex;
    }
}
