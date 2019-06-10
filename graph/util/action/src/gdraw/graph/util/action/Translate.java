package gdraw.graph.util.action;

import gdraw.graph.node.Node;
import gdraw.graph.util.Selectable;
import gdraw.graph.vertex.Vertex;

public class Translate extends Action {
    private SelectableCreationListener listener;
    private double dx, dy;

    private Translate(ActionHelper from, Selectable object, double dx, double dy, ActionHelper to) {
        this.from = to;
        this.to = from;
        listener = object.getCreationListener();
        this.dx = -dx;
        this.dy = -dy;
        to.push(this);
    }

    public static void applyTranslate(ActionHelper undo, Selectable object, double dx, double dy, ActionHelper redo) {
        new Translate(redo, object, dx, dy, undo).action();
    }

    @Override
    public void action() {
        listener.getObject().translate(dx, dy);
        dx = -dx;
        dy = -dy;
        changeStacks();
    }

}
