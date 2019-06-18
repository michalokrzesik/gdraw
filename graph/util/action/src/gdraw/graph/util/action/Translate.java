package gdraw.graph.util.action;

import gdraw.graph.util.Selectable;

public class Translate extends Action {
    private SelectableCreationListener listener;
    private double dx, dy;

    private Translate(ActionHelper from, Selectable object, double dx, double dy, ActionHelper to) {
        this.from = to;
        this.to = from;
        listener = object.getCreationListener();
        this.dx = dx;
        this.dy = dy;
    }

    public static Action applyTranslate(ActionHelper undo, Selectable object, double dx, double dy, ActionHelper redo) {
        Action action = new Translate(redo, object, dx, dy, undo);
        action.action();
        return action;
    }

    @Override
    public void action() {
        listener.getObject().translate(dx, dy);
        dx = -dx;
        dy = -dy;
        changeStacks();
    }

}
