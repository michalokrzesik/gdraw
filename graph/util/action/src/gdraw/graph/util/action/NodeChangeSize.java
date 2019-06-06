package gdraw.graph.util.action;

import gdraw.graph.node.Node;

public class NodeChangeSize extends Action {
    private Node node;
    private double dw, dh;

    private NodeChangeSize(ActionHelper from, Node node, double dw, double dh, ActionHelper to) {
        this.from = from;
        this.to = to;
        this.node = node;
        this.dw = dw;
        this.dh = dh;
    }

    public static void apply(ActionHelper undo, Node node, double w, double h, ActionHelper redo){
        new NodeChangeSize(redo, node, w - node.getWidth(), h - node.getHeight(), undo).action();
    }

    @Override
    public void action() {
        double w = node.getWidth() + dw, h = node.getHeight() + dh;
        dw *= -1;
        dh *= -1;
        node.setWidth(w);
        node.setHeight(h);
        changeStacks();
    }
}
