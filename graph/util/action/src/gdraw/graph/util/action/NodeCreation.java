package gdraw.graph.util.action;

import gdraw.graph.node.Node;
import gdraw.graph.util.Selectable;
import gdraw.main.MainController;
import gdraw.main.Project;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;

import java.util.ArrayList;

public class NodeCreation extends MultiAction {


    private NodeCreation(ActionHelper from, Node object, ActionHelper to) {
        super(from, to);
        node = object.getCreationListener();
        type = ActionType.Delete;
    }

    private NodeCreation(ActionHelper from, Node object, MultiAction requestManager, ActionHelper to) {
        super(from, to);
        getInfoFromNode(object);
        type = ActionType.Create;
    }

    private NodeCreation(ActionHelper from, Project project, Image image, ActionHelper to) {
        super(from, to);
        width = image.getWidth(); widthCollapsed = width;
        height = image.getHeight(); heightCollapsed = height;
        center = new Point2D(width/2, height/2);
        this.image = image;
        group = project.getGroup();
        isCollapsed = false; isGroupNodes = false;
        label = "";
        parent = project.getBackground().getCreationListener();
        controller = project.getController();
        type = ActionType.Create;
    }

    private enum ActionType{
        Create,
        Delete
    }

    private SelectableCreationListener node;
    private Point2D center;
    private Image image;
    private Group group;
    private boolean isGroupNodes, isCollapsed;
    private double width, height, widthCollapsed, heightCollapsed;
    private String label;
    private SelectableCreationListener parent;
    private MainController controller;

    private ActionType type;

    public static void applyDelete(ActionHelper undo, Node o, ActionHelper redo) {
        new NodeCreation(redo, o, undo).action();
    }

    public static void applyCreate(ActionHelper undo, Project project, Image image, ActionHelper redo) {
        new NodeCreation(redo, project, image, undo).action();
    }

    public static void applyCreate(ActionHelper undo, Node node, ActionHelper redo) {
        NodeCreation creator = new NodeCreation(undo, node, redo);
        creator.action();
        creator.action();
    }

    public static void applyCopy(ActionHelper undo, Node node, MultiAction requestManager, ActionHelper redo){
        NodeCreation creator = new NodeCreation(redo, node, requestManager, undo);
        creator.action();
        node.getVertices().forEach(vertex -> requestManager.request(vertex.getFromNode() == node, creator.getNode(), vertex, node));
    }

    private Node getNode() {
        return (Node) node.getObject();
    }

    @Override
    public void action() {
        switch (type){
            case Create:
                Node object = new Node(center, image, group, isGroupNodes, ((Node) parent.getObject()).getTreeItem(), controller);
                object.setWH(width, height, widthCollapsed, heightCollapsed);
                object.setCollapsed(isCollapsed);
                object.setLabel(label);
                controller.getProject().newObject(object);
                ((Node) parent.getObject()).groupNodes(object);

                if(node == null) node = new SelectableCreationListener(object);
                else node.setObject(object);

                super.action();

                type = ActionType.Delete;
                break;
            case Delete:
                Node objectToDelete = (Node) node.getObject();

                ArrayList<Selectable> toDelete = new ArrayList<>();
                toDelete.addAll(objectToDelete.getVertices());
                toDelete.addAll(objectToDelete.getSubNodes());

                multiFrom.clear();
                multiTo.clear();

                applyDelete(multiFrom, toDelete, multiTo);

                getInfoFromNode(objectToDelete);

                node.getObject().delete();
                node.setObject(null);

                type = ActionType.Create;
                break;
        }
        changeStacks();
    }

    private void getInfoFromNode(Node object) {
        center = object.getCenter();
        image = object.getImage();
        group = object.getProjectGroup();
        isGroupNodes = object.isGroupNodes(); isCollapsed = object.isCollapsed();
        object.setCollapsed(false); width = object.getWidth(); height = object.getHeight();
        object.setCollapsed(true); widthCollapsed = object.getWidth(); heightCollapsed = object.getHeight();
        object.setCollapsed(isCollapsed);
        label = object.getLabel();
        parent = object.getTreeItem().getParent().getValue().getCreationListener();
        controller = object.getController();
    }

}