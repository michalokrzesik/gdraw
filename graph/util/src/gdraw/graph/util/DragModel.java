package gdraw.graph.util;

import gdraw.graph.node.Node;
import gdraw.graph.util.action.Action;
import gdraw.graph.util.action.GroupManagement;
import gdraw.graph.util.action.MultiAction;
import gdraw.graph.util.action.VertexCreation;
import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.VertexPoint;
import gdraw.main.Project;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public enum DragModel implements Serializable {
    Standard{
        private double x, y;
        private double dx, dy;

        @Override
        public void pressed(Project project, MouseEvent e, Selectable item) {
            if(project.getSelected().contains(item)){
                actionHolder = new ArrayList<>();
                double ex = e.getX(), ey = e.getY();
                Point2D point = item.isNode() ? ((Node) item).getCenter() : new Point2D(ex, ey);
                x = point.getX();
                y = point.getY();
                dx = ex - x;
                dy = ey - y;
                canvas = project.getCanvas();
                draw(project);
            }
        }

        private void draw(Project project) {
            List<Selectable> selected = project.getSelected();
            if(!selected.isEmpty())
                selected.forEach(o -> {
                    if(o.isNode()){
                        Node node = (Node) o;
                        double x = node.getCenter().getX(), y = node.getCenter().getY();
                        project.getCanvas().getGraphicsContext2D().strokeLine(x, y, x + dx, y + dy);
                    }
                    else project.getCanvas().getGraphicsContext2D().strokeLine(x, y, x + dx, y + dy);
                });
        }

        @Override
        public void dragged(Project project, MouseEvent e, Selectable item) {
            double nx = e.getX(), ny = e.getY();
            double ndx = nx - x, ndy = ny - y;
            project.draw();
            dx += ndx;
            dy += ndy;
            x = nx;
            y = ny;
            draw(project);
        }

        @Override
        public void released(Project project, MouseEvent e, Selectable item) {
            List<Selectable> selected = project.getSelected();
            if(selected.isEmpty()) return;
            if(selected.size() > 1 || selected.get(0).isNode())
                actionHolder.add(MultiAction.applyTranslate(project, dx, dy));

            else ((gdraw.graph.vertex.Vertex) selected.get(0)).moveInteractedPoint(x, y, x + dx, y + dy);
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
            canvas = project.getCanvas();
        }

        @Override
        public void dragged(Project project, MouseEvent e, Selectable item) {
            project.draw();
            stop.setPoint(new Point2D(e.getX(), e.getY()));
            if(item.isNode()) {
                to = (Node) item;
                gdraw.graph.vertex.Vertex.draw(from, start, stop, to, canvas);
            }
            else
            canvas.getGraphicsContext2D().strokeLine(start.getX(), start.getY(), stop.getX(), stop.getY());
        }

        @Override
        public void released(Project project, MouseEvent e, Selectable item) {
            if(item.isNode()){
                to = (Node) item;
                actionHolder.add(VertexCreation.applyCreate(
                        project.getUndo(),
                        from, start.getPoint(), stop.getPoint(), to, arrowType, lineType, duplex, curved, width, 1, color,
                        project.getRedo()));
            }
        }
    },
    Grouping{
        @Override
        public void pressed(Project project, MouseEvent e, Selectable item) {
            DragModel.Vertex.pressed(project, e, item);
        }

        @Override
        public void dragged(Project project, MouseEvent e, Selectable item) {
            ArrayList<Selectable> selected = project.getSelected();
            if(!selected.isEmpty())
                selected.forEach(o -> {
                    if(o.isNode()) DragModel.Vertex.dragged(project, e, o);
                });
        }

        @Override
        public void released(Project project, MouseEvent e, Selectable item) {
            actionHolder.add(GroupManagement.applyGroup(project.getUndo(), from, project.getSelected(), project.getRedo()));
        }
    },
    Select{
        private double x, y;
        private Rectangle selection;
        @Override
        public void pressed(Project project, MouseEvent e, Selectable item) {
            item.setSelected(true);
            x = e.getX();
            y = e.getY();
        }

        @Override
        public void dragged(Project project, MouseEvent e, Selectable item) {
            project.draw();
            double xmin = Double.min(x, e.getX()), ymin = Double.min(y, e.getY()),
                    w = Math.abs(e.getX() - x), h = Math.abs(e.getY() - y);
            selection = new Rectangle(xmin, ymin, w, h);
            GraphicsContext gc = project.getCanvas().getGraphicsContext2D();
            gc.setStroke(Color.BLANCHEDALMOND);
            gc.strokeRect(xmin, ymin, w, h);
        }

        @Override
        public void released(Project project, MouseEvent e, Selectable item) {
            if(selection == null) return;
            project.checkSelect(selection);
            project.draw();
        }
    };

    ArrowType arrowType = ArrowType.None;
    LineType lineType = LineType.Straight;
    Color color = Color.BLACK;
    boolean duplex = false, curved = false;
    double width = 1;


    VertexPoint start, stop;
    Node from, to;
    Canvas canvas;
    ArrayList<Action> actionHolder;

    public abstract void pressed(Project project, MouseEvent e, Selectable item);
    public abstract void dragged(Project project, MouseEvent e, Selectable item);
    public abstract void released(Project project, MouseEvent e, Selectable item);


    public void set(LineType lineType, ArrowType arrowType, Color color, boolean duplex, boolean curved, String width) {
        actionHolder = new ArrayList<>();
        this.lineType = lineType; this.arrowType = arrowType; this.color = color; this.duplex = duplex; this.curved = curved;
        try {
            this.width = Double.parseDouble(width);
        } catch (Exception e) {
            this.width = 1;
        }
    }
}
