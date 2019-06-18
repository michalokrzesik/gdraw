package gdraw.graph.util.action;

import gdraw.graph.node.Node;
import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.Vertex;
import gdraw.graph.vertex.VertexType;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

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
    private Color color;
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
        toNode = vertex.getToNode().getCreationListener();
        getInfoFromVertex(vertex);
        type = ActionType.Delete;

    }

    private void getInfoFromVertex(Vertex vertex) {
        fromPoint = vertex.getFromPoint();
        toPoint = vertex.getToPoint();
        arrowType = vertex.getArrowType();
        lineType = vertex.getLineType();
        duplex = vertex.isDuplex();
        curved = vertex.getVertexType() == VertexType.Curved;
        value = vertex.getValue();
        width = vertex.getLineWidth();
        color = vertex.getColor();
    }


    private VertexCreation(ActionHelper from, Vertex vertex, MultiAction requestManager, ActionHelper to) {
        this.from = from; this.to = to;
        getInfoFromVertex(vertex);
        fromNode = requestManager.request(true, vertex).getCreationListener();
        toNode = requestManager.request(false, vertex).getCreationListener();
        type = ActionType.Create;
    }

    public static Action applyCreate(ActionHelper undo,
                             Node fromNode, Point2D fromPoint, Point2D toPoint, Node toNode,
                             ArrowType arrowType, LineType lineType, boolean duplex, boolean curved,
                             double width, double value, Color color,
                             ActionHelper redo) {
        Action action = new VertexCreation(redo,
                fromNode, fromPoint, toPoint, toNode,
                arrowType, lineType, duplex, curved, width, value, color,
                undo);
        action.action();
        return action;
    }

    public static Action applyDelete(ActionHelper undo, Vertex vertex, ActionHelper redo) {
        Action action = new VertexCreation(redo, vertex, undo);
        action.action();
        return action;
    }

    public static Action applyCopy(ActionHelper undo, Vertex vertex, MultiAction requestManager, ActionHelper redo){
        Action action = new VertexCreation(redo, vertex, requestManager, undo);
        action.action();
        return action;
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
