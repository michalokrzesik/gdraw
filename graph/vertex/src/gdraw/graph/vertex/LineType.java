package gdraw.graph.vertex;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Path;

public enum LineType {
    Straight {
        public void set(Path path){
            path.getStrokeDashArray().clear();
        }
    },
    ThreeLength{
        public void set(Path path){
            path.getStrokeDashArray().clear();
            path.getStrokeDashArray().addAll(3 * path.getStrokeWidth());
        }
    },
    TwoLength{
        public void set(Path path){
            path.getStrokeDashArray().clear();
            path.getStrokeDashArray().addAll(2 * path.getStrokeWidth());
        }
    },
    Dot{
        public void set(Path path){
            path.getStrokeDashArray().clear();
            path.getStrokeDashArray().addAll(path.getStrokeWidth());
        }
    },
    DotDash{
        public void set(Path path){
            path.getStrokeDashArray().clear();
            path.getStrokeDashArray().addAll(path.getStrokeWidth(), 2 * path.getStrokeWidth());
        }
    };

    abstract public void set(Path path);

}
