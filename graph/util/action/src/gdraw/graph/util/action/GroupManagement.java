package gdraw.graph.util.action;

import gdraw.graph.node.Node;
import gdraw.graph.util.Selectable;
import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.Vertex;
import javafx.geometry.Point2D;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class GroupManagement extends MultiAction {



    private enum ActionType{
        Multi,
        Group,
        ToNode,
        ToGroup
    }

    private ActionType type;
    private SelectableCreationListener node, parent;

    private GroupManagement(ActionHelper from, Node node, ActionHelper to) {
        super(from, to);
        this.node = node.getCreationListener();
        type = ActionType.ToGroup;
    }

    private GroupManagement(ActionHelper from, ActionHelper to){
        super(from, to);
        type = ActionType.Multi;
    }

    private GroupManagement(ActionHelper from, Node node, Node newParent, ActionHelper to) {
        super(from, to);
        this.node = node.getCreationListener();
        this.parent = newParent.getCreationListener();
        type = ActionType.Group;
    }

    public static Action applyUngroup(ActionHelper undo, ArrayList<Selectable> selected, ActionHelper redo) {
        GroupManagement ma = new GroupManagement(redo, undo);
        ActionHelper multiFrom = ma.multiFrom;
        ActionHelper multiTo = ma.multiTo;
        if(!selected.isEmpty()) selected.forEach(o -> {
            if(o.isNode()) ma.actionHolder.add(GroupManagement.applyUngroup(multiFrom, (Node) o, multiTo));
        });

        if(!multiTo.isEmpty()) ma.action();                         //Pierwsze action tylko wymieni miejscami stacki
        //Jeśli w objects nie było node'ów, nic się nie dzieje
        return ma;
    }

    public static Action applyUngroup(ActionHelper undo, Node node, ActionHelper redo) {
        GroupManagement action = new GroupManagement(redo, node, node.getTreeItem().getParent().getParent().getValue(), undo);
        action.action();
        return action;
    }


    public static Action applyGroup(ActionHelper undo, Node node, TreeItem<Node> parentOfParent, ArrayList<Node> nodes, ActionHelper redo) {
        GroupManagement ma = new GroupManagement(redo, undo);
        ActionHelper multiFrom = ma.multiFrom;
        ActionHelper multiTo = ma.multiTo;

        SelectableCreationListener listener = node.getCreationListener();

        ma.actionHolder.add(NodeCreation.applyCreate(multiFrom, node, multiTo));


        Node parent = (Node) listener.getObject();

        parentOfParent.getValue().getSubNodes().add(parent);


        if(!nodes.isEmpty()) nodes.forEach(o -> ma.actionHolder.add(GroupManagement.applyGroup(multiFrom, o, parent, multiTo)));

        if(!multiTo.isEmpty()) ma.action();                         //Pierwsze action tylko wymieni miejscami stacki
        //Jeśli w objects nie było node'ów, nic się nie dzieje
        return ma;
    }

    public static Action applyGroup(ActionHelper undo, Node node, Node parent, ActionHelper redo) {
        Action action = new GroupManagement(redo, node, parent, undo);
        action.action();
        return action;
    }

    public static Action applyGroup(ActionHelper undo, Node node, ArrayList<Selectable> selected, ActionHelper redo) {
        GroupManagement ma = new GroupManagement(redo, undo);
        ActionHelper multiFrom = ma.multiFrom;
        ActionHelper multiTo = ma.multiTo;

        if(!selected.isEmpty()) selected.forEach(o -> {
            if(o.isNode())
                ma.actionHolder.add(GroupManagement.applyGroup(multiFrom, (Node) o, node, multiTo));
        });

        if(!multiTo.isEmpty()) ma.action();                         //Pierwsze action tylko wymieni miejscami stacki
        //Jeśli w objects nie było node'ów, nic się nie dzieje
        return ma;
    }

    public static Action applyToNode(ActionHelper undo, ArrayList<Selectable> selected, ActionHelper redo) {
        GroupManagement ma = new GroupManagement(redo, undo);
        ActionHelper multiFrom = ma.multiFrom;
        ActionHelper multiTo = ma.multiTo;

        if(!selected.isEmpty()) selected.forEach(o -> {
            if(o.isNode()){
                ArrayList<Selectable> subNodes = new ArrayList<>();
                if(!((Node) o).getSubNodes().isEmpty()) subNodes.addAll(((Node) o).getSubNodes());
                ma.actionHolder.add(GroupManagement.applyUngroup(multiFrom, subNodes, multiTo));
                ((Node) o).changeGroupToNode();                                 //Dodanie czegoś do grupy od razu zmienia node w grupę, ergo - nie trzeba robić osobnej akcji
            }
        });

        if(!multiTo.isEmpty()) ma.action();                         //Pierwsze action tylko wymieni miejscami stacki
        //Jeśli w objects nie było node'ów, nic się nie dzieje
        return ma;
    }

    public static Action applyToGroup(ActionHelper undo, ArrayList<Selectable> selected, ActionHelper redo) {
        GroupManagement ma = new GroupManagement(redo, undo);
        ActionHelper multiFrom = ma.multiFrom;
        ActionHelper multiTo = ma.multiTo;

        if(!selected.isEmpty()) selected.forEach(o -> {
            if(o.isNode())
                ma.actionHolder.add(GroupManagement.applyToGroup(multiFrom, (Node) o, multiTo));
        });

        if(!multiTo.isEmpty()) ma.action();                         //Pierwsze action tylko wymieni miejscami stacki
        //Jeśli w objects nie było node'ów, nic się nie dzieje
        return ma;
    }

    public static Action applyToGroup(ActionHelper undo, Node node, ActionHelper redo){
        Action action = new GroupManagement(redo, node, undo);
        action.action();
        return action;
    }

    @Override
    public void action(){
        switch(type){
            case Multi: {
                super.action();
                break;
            }
            case Group: {
                SelectableCreationListener oldParent = ((Node) node.getObject()).getTreeItem().getParent().getValue().getCreationListener();
                ((Node) parent.getObject()).groupNodes((Node) node.getObject());
                parent = oldParent;
                break;
            }
            case ToGroup: {
                ((Node) node.getObject()).groupNodes();
                type = ActionType.ToNode;
                break;
            }
            case ToNode: {
                ((Node) node.getObject()).changeGroupToNode();
                type = ActionType.ToGroup;
                break;
            }
        }
    }
}
