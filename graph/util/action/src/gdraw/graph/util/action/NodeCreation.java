package gdraw.graph.util.action;

import gdraw.graph.node.Node;
import gdraw.graph.vertex.Vertex;
import gdraw.main.Project;
import javafx.scene.image.Image;

//TODO
public class NodeCreation extends Action {

    private enum ActionType{
        Create,
        Delete
    }

    private ActionType type;

    public static void applyDelete(ActionHelper multiFrom, Node o, ActionHelper multiTo) {

    }

    public static void applyCreate(ActionHelper undo, Project project, Image image, ActionHelper redo) {

    }

    @Override
    public void action() {
        switch (type){
            case Create:
                Node object = new Node(center, image, group, isGroupNodes, parent, controller);
                object.setWidth();
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
