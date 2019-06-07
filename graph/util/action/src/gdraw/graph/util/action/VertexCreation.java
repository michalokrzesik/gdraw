package gdraw.graph.util.action;

import gdraw.graph.node.Node;
import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.Vertex;
import gdraw.graph.vertex.VertexType;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class VertexCreation extends Action {



    private enum ActionType{
        Create,
        Delete
    }

    private Node fromNode, toNode;
    private Point2D fromPoint, toPoint;
    private ArrowType arrowType;
    private LineType lineType;
    private boolean duplex, curved;
    private double width, value;
    private Paint color;
    private Vertex vertex;

    private ActionType type;

    private VertexCreation(ActionHelper from,
                     Node fromNode, Point2D fromPoint, Point2D toPoint, Node toNode,
                     ArrowType arrowType, LineType lineType, boolean duplex, boolean curved, double width, double value, Color color,
                     ActionHelper to) {
        this.from = from; this.to = to;
        this.fromNode = fromNode; this.fromPoint = fromPoint;
        this.toNode = toNode; this.toPoint = toPoint;
        this.arrowType = arrowType; this.lineType = lineType; this.duplex = duplex; this.curved = curved;
        this.width = width; this.value = value; this.color = color;
        this.type = ActionType.Create;
    }

    private VertexCreation(ActionHelper from, Vertex vertex, ActionHelper to){
        this.from = from; this.to = to;
        this.vertex = vertex;
        fromNode = vertex.getFromNode();
        fromPoint = vertex.getFromPoint();
        toPoint = vertex.getToPoint();
        toNode = vertex.getToNode();
        arrowType = vertex.getArrowType();
        lineType = vertex.getLineType();
        duplex = vertex.isDuplex();
        curved = vertex.getVertexType() == VertexType.Curved;
        value = vertex.getValue();
        width = vertex.getLineWidth();
        color = vertex.getColor();
        type = ActionType.Delete;

    }

    public static void applyCreate(ActionHelper undo,
                             Node fromNode, Point2D fromPoint, Point2D toPoint, Node toNode,
                             ArrowType arrowType, LineType lineType, boolean duplex, boolean curved,
                             double width, double value, Color color,
                             ActionHelper redo) {
        (new VertexCreation(redo,
                fromNode, fromPoint, toPoint, toNode,
                arrowType, lineType, duplex, curved, width, value, color,
                undo)
        ).action();
    }

    public static void applyDelete(ActionHelper undo, Vertex vertex, ActionHelper redo) {
        (new VertexCreation(redo, vertex, undo)).action();
    }

    @Override
    public void action() {
        switch (type){
            case Create:
                vertex = fromNode.newVertex(fromPoint, toPoint, toNode, arrowType, lineType, duplex, curved, width, value, color);
                type = ActionType.Delete;
                break;
            case Delete:
                vertex.finishDelete();
                vertex = null;
                type = ActionType.Create;
                break;
        }
        changeStacks();
    }

    @Override
    public void refresh(Node oldNode, Node newNode) {
        if(oldNode == fromNode) fromNode = newNode;
        if(oldNode == toNode) toNode = newNode;
    }

    @Override
    public void refresh(Vertex oldVertex, Vertex newVertex) {
        if(oldVertex == vertex) vertex = newVertex;
    }


}
