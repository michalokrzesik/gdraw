package gdraw.graph.util.action;

import gdraw.graph.node.Node;
import gdraw.graph.util.MIandButtonPair;
import gdraw.graph.util.Selectable;
import gdraw.graph.vertex.Vertex;
import gdraw.main.Project;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MultiAction extends Action {
    private ActionHelper multiFrom;
    private ActionHelper multiTo;

    @Override
    public void action() {
        while(!from.isEmpty()) from.pop();
        changeMultiStacks();
        changeStacks();
    }

    protected void changeMultiStacks() {
        ActionHelper tmp = multiFrom;
        multiFrom = multiTo;
        multiTo = tmp;
    }

    protected MultiAction(ActionHelper from, ActionHelper to){
        this.from = from;
        this.to = to;
        multiFrom = new ActionHelper(new MIandButtonPair(new MenuItem(), new Button()));
        multiTo = new ActionHelper(new MIandButtonPair(new MenuItem(), new Button()));
    }

    public static void applyNodeChangeSize(ActionHelper undo, List<Selectable> objects, double w, double h, ActionHelper redo){
        MultiAction ma = new MultiAction(redo, undo);
        ActionHelper multiFrom = ma.multiFrom;
        ActionHelper multiTo = ma.multiTo;
        objects.forEach(o -> {
            if(o.isNode()) NodeChangeSize.apply(multiFrom, (Node) o, w, h, multiTo);
        });
        if(!multiTo.isEmpty()) ma.action();                         //Pierwsze action tylko wymieni miejscami stacki
                                                                    //Jeśli w objects nie było node'ów, nic się nie dzieje
    }

    public static void applyTranslate(Project project, double dx, double dy) {
        MultiAction ma = new MultiAction(project.getRedo(), project.getUndo());
        ActionHelper multiFrom = ma.multiFrom;
        ActionHelper multiTo = ma.multiTo;
        project.getSelected().forEach(o -> Translate.applyTranslate(multiFrom, o, dx, dy, multiTo));
        if(!multiTo.isEmpty()) ma.action();                         //Pierwsze action tylko wymieni miejscami stacki
        //Jeśli w objects nie było node'ów, nic się nie dzieje
    }

    public static void applyNodeCircleMove(Project project, Node node, int i, double dx, double dy) {
        MultiAction ma = new MultiAction(project.getRedo(), project.getUndo());
        ActionHelper multiFrom = ma.multiFrom;
        ActionHelper multiTo = ma.multiTo;

        double x = 0, y = 0;

        if(i < 3) x = -dx;
        else if(i > 3 && i < 7) x = dx;
        //else x = 0

        if(i > 1 && i < 5) y = dy;
        else if(i == 0 || i > 5) y = -dy;
        //else y = 0

        NodeChangeSize.apply(multiFrom, node, node.getWidth() + x, node.getHeight() + y, multiTo);
        Translate.applyTranslate(multiFrom, node, (x == 0 ? x : dx), (y == 0 ? y : dy), multiTo);

        if(!multiTo.isEmpty()) ma.action();                         //Pierwsze action tylko wymieni miejscami stacki
        //Jeśli w objects nie było node'ów, nic się nie dzieje
    }

    public static void applyDelete(ActionHelper undo, ArrayList<Selectable> selected, ActionHelper redo) {
        MultiAction ma = new MultiAction(redo, undo);
        ActionHelper multiFrom = ma.multiFrom;
        ActionHelper multiTo = ma.multiTo;
        selected.forEach(o -> {
            if(o.isNode()) NodeCreation.applyDelete(multiFrom, (Node) o, multiTo);
            else VertexCreation.applyDelete(multiFrom, (Vertex) o, multiTo);
        });
        if(!multiTo.isEmpty()) ma.action();                         //Pierwsze action tylko wymieni miejscami stacki
        //Jeśli w objects nie było node'ów, nic się nie dzieje
    }
}
