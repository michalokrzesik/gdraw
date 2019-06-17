package gdraw.graph.node;


import gdraw.graph.util.action.Action;
import gdraw.graph.util.action.MultiAction;
import gdraw.main.Project;

public class CircleHelper {

    public static Action move(Project project, Node node, int i, double dx, double dy) {
        return MultiAction.applyNodeCircleMove(project, node, i, dx, dy);
    }
}
