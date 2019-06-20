package gdraw.graph.vertex;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Path;

import java.io.Serializable;

public enum LineType implements Serializable {
    Straight {
        public void set(GraphicsContext gc, double width){
            gc.setLineWidth(width);
            gc.setLineDashes(null); // getStrokeDashArray().clear();
        }

        @Override
        public String toString() {
            return "———";
        }
    },
    ThreeLength{
        public void set(GraphicsContext gc, double width){
            //path.getStrokeDashArray().clear();
            //path.getStrokeDashArray().addAll(3 * path.getStrokeWidth());
            gc.setLineDashes(3 * width);
            gc.setLineWidth(width);
            gc.setLineDashOffset(width);
        }

        @Override
        public String toString() { return "––– –––"; }
    },
    TwoLength{
        public void set(GraphicsContext gc, double width){
//            path.getStrokeDashArray().clear();
//            path.getStrokeDashArray().addAll(2 * path.getStrokeWidth());
            gc.setLineWidth(width);
            gc.setLineDashOffset(width);
            gc.setLineDashes(2 * width);
        }

        @Override
        public String toString() { return "–– –– ––"; }
    },
    Dot{
        public void set(GraphicsContext gc, double width){
//            path.getStrokeDashArray().clear();
//            path.getStrokeDashArray().addAll(path.getStrokeWidth());
            gc.setLineWidth(width);
            gc.setLineDashes(width);
            gc.setLineDashOffset(width);
        }

        @Override
        public String toString() { return "• • • • •"; }
    },
    DotDash{
        public void set(GraphicsContext gc, double width){
//            path.getStrokeDashArray().clear();
//            path.getStrokeDashArray().addAll(path.getStrokeWidth(), 2 * path.getStrokeWidth());
            gc.setLineWidth(width);
            gc.setLineDashOffset(width);
            gc.setLineDashes(width, 2* width);
        }

        @Override
        public String toString() {
            return "• –– • –– •";
        }
    };

    abstract public void set(GraphicsContext gc, double width);

    public static LineType getValueOf(String name){
        switch(name){
            case "• –– • –– •": return DotDash;
            case "––– –––": return ThreeLength;
            case "–– –– ––": return TwoLength;
            case "• • • • •": return Dot;
            default: return Straight;
        }
    }

}
