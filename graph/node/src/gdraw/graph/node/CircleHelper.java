package gdraw.graph.node;



public class CircleHelper {

    public static void move(Node node, int i, double dx, double dy) {
        double x = 0, y = 0;

        if(i < 3) x = -dx;
        else if(i > 3 && i < 7) x = dx;
        //else x = 0

        if(i > 1 && i < 5) y = dy;
        else if(i == 0 || i > 5) y = -dy;
        //else y = 0

        node.setWidth(node.getWidth() + x);
        node.setHeight(node.getHeight() + y);
        node.translate((x == 0 ? x : dx), (y == 0 ? y : dy));
    }
}
