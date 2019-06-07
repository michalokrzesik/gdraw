package gdraw.graph.util.action;

import gdraw.graph.node.Node;
import gdraw.graph.util.MIandButtonPair;
import gdraw.graph.util.Selectable;
import gdraw.graph.vertex.Vertex;
import gdraw.main.Project;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

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

    @Override
    public void refresh(Node oldNode, Node newNode) {
        multiFrom.forEach(a -> a.refresh(oldNode, newNode));
    }

    @Override
    public void refresh(Vertex oldVertex, Vertex newVertex) {
        multiFrom.forEach(a -> a.refresh(oldVertex, newVertex));
    }

    private void changeMultiStacks() {
        ActionHelper tmp = multiFrom;
        multiFrom = multiTo;
        multiTo = tmp;
    }

    private MultiAction(ActionHelper from, ActionHelper to){
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

}
