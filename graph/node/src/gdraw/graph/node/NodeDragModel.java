package gdraw.graph.node;

import gdraw.graph.util.Selectable;
import gdraw.graph.util.action.GroupManagement;
import gdraw.graph.util.action.MultiAction;
import gdraw.graph.util.action.VertexCreation;
import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.VertexPoint;
import gdraw.main.Project;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public enum NodeDragModel implements Serializable {
    Standard{
        private double x, y;
        private double dx, dy;

        @Override
        public void pressed(Project project, MouseEvent e, Selectable item) {
            if(project.getSelected().contains(item)){
                x = e.getX();
                y = e.getY();
                dx = 0;
                dy = 0;
            }
        }

        @Override
        public void dragged(Project project, MouseEvent e, Selectable item) {
            List<Selectable> selected = project.getSelected();
            if(selected.contains(item)){
                double nx = e.getX(), ny = e.getY();
                double ndx = nx - x, ndy = ny - y;
                dx += ndx;
                dy += ndy;
                for (Selectable selectedItem : selected) {
                    selectedItem.translate(ndx, ndy);
                }
                x = nx;
                y = ny;
            }
        }

        @Override
        public void released(Project project, MouseEvent e, Selectable item) {
            MultiAction.applyTranslate(project, dx, dy);
        }


    },
    Vertex{

        @Override
        public void pressed(Project project, MouseEvent e, Selectable item) {
            from = (Node) item;
            start = new VertexPoint(e.getX(), e.getY());
            start.setPointBounded(start.getPoint(), from);
            stop = new VertexPoint(e.getX(), e.getY());
            to = from;
            pane = project.getPane();
            refresh();
            if(!pane.getChildren().contains(path))
                pane.getChildren().add(path);
            path.toFront();
        }

        private void refresh() {
            path.setStroke(color);
            lineType.set(path);
            path.getElements().clear();
            path.getElements().addAll(new MoveTo(start.getX(), start.getY()), new LineTo(stop.getX(), stop.getY()));
            pane.getChildren().removeAll(arrows);
            arrowType.draw(arrows, color, start, stop);
            if(duplex) arrowType.draw(arrows, color, stop, start);
            if(!arrows.isEmpty()) {
                pane.getChildren().addAll(arrows);
                arrows.forEach(javafx.scene.Node::toFront);
            }
        }

        @Override
        public void dragged(Project project, MouseEvent e, Selectable item) {
            if(item.isNode()) stop.setPointBounded(new Point2D(e.getX(), e.getY()), (Node) item);
            else stop.setPoint(new Point2D(e.getX(), e.getY()));
            refresh();
        }

        @Override
        public void released(Project project, MouseEvent e, Selectable item) {
            if(item.isNode()){
                to = (Node) item;
                VertexCreation.applyCreate(
                        project.getUndo(),
                        from, start.getPoint(), stop.getPoint(), to, arrowType, lineType, duplex, curved, width, 1, color,
                        project.getRedo());
            }
            arrows.add(path);
            pane.getChildren().removeAll(arrows);
        }
    },
    Grouping{
        @Override
        public void pressed(Project project, MouseEvent e, Selectable item) {
            NodeDragModel.Vertex.pressed(project, e, item);
        }

        @Override
        public void dragged(Project project, MouseEvent e, Selectable item) {
            NodeDragModel.Vertex.dragged(project, e, item);
        }

        @Override
        public void released(Project project, MouseEvent e, Selectable item) {
            if(item.isNode())
                GroupManagement.applyGroup(project.getUndo(), from, (Node) item, project.getRedo());
            arrows.add(path);
            pane.getChildren().removeAll(arrows);
        }
    };

    Path path = new Path();
    ArrowType arrowType = ArrowType.None;
    LineType lineType = LineType.Straight;
    Color color = Color.BLACK;
    boolean duplex = false, curved = false;
    double width = 1;


    VertexPoint start, stop;
    Node from, to;
    ArrayList<Shape> arrows = new ArrayList<>();
    Pane pane;

    public abstract void pressed(Project project, MouseEvent e, Selectable item);
    public abstract void dragged(Project project, MouseEvent e, Selectable item);
    public abstract void released(Project project, MouseEvent e, Selectable item);


    public void set(LineType lineType, ArrowType arrowType, Color color, boolean duplex, boolean curved, String width) {
        this.lineType = lineType; this.arrowType = arrowType; this.color = color; this.duplex = duplex; this.curved = curved;
        try {
            this.width = Double.parseDouble(width);
        } catch (Exception e) {
            this.width = 1;
        }
    }
}
