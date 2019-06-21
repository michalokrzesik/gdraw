package gdraw.graph.util.action;

import gdraw.graph.node.Node;

public class NodeChangeSize extends Action {
    private SelectableCreationListener listener;
    private double dw, dh;

    private NodeChangeSize(ActionHelper from, Node node, double dw, double dh, ActionHelper to) {
        this.from = to;
        this.to = from;
        this.listener = node.getCreationListener();
        this.dw = dw;
        this.dh = dh;
    }

    public static Action apply(ActionHelper undo, Node node, double w, double h, ActionHelper redo){
        Action action = new NodeChangeSize(redo, node, w - node.getWidth(), h - node.getHeight(), undo);
        action.action();
        return action;
    }

    @Override
    public void action() {
        Node node = (Node) listener.getObject();
        double w = node.getWidth() + dw, h = node.getHeight() + dh;
        dw *= -1;
        dh *= -1;
        node.setWidth(w);
        node.setHeight(h);
        changeStacks();
    }

}
