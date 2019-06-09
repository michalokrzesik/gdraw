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

    private Point2D fromPoint, toPoint;
    private ArrowType arrowType;
    private LineType lineType;
    private boolean duplex, curved;
    private double width, value;
    private Paint color;
    private SelectableCreationListener vertex, fromNode, toNode;

    private ActionType type;

    private VertexCreation(ActionHelper from,
                     Node fromNode, Point2D fromPoint, Point2D toPoint, Node toNode,
                     ArrowType arrowType, LineType lineType, boolean duplex, boolean curved, double width, double value, Color color,
                     ActionHelper to) {
        this.from = from; this.to = to;
        this.fromNode = fromNode.getCreationListener(); this.fromPoint = fromPoint;
        this.toNode = toNode.getCreationListener(); this.toPoint = toPoint;
        this.arrowType = arrowType; this.lineType = lineType; this.duplex = duplex; this.curved = curved;
        this.width = width; this.value = value; this.color = color;
        this.type = ActionType.Create;
        vertex = null;
    }

    private VertexCreation(ActionHelper from, Vertex vertex, ActionHelper to){
        this.from = from; this.to = to;
        this.vertex = vertex.getCreationListener();
        fromNode = vertex.getFromNode().getCreationListener();
        fromPoint = vertex.getFromPoint();
        toPoint = vertex.getToPoint();
        toNode = vertex.getToNode().getCreationListener();
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
                Vertex object = ((Node) fromNode.getObject()).newVertex(fromPoint, toPoint, (Node) toNode.getObject(), arrowType, lineType, duplex, curved, width, value, color);
                if(vertex == null) vertex = new SelectableCreationListener(object);
                else vertex.setObject(object);
                type = ActionType.Delete;
                break;
            case Delete:
                ((Vertex) vertex.getObject()).finishDelete();
                vertex.setObject(null);
                type = ActionType.Create;
                break;
        }
        changeStacks();
    }


}
