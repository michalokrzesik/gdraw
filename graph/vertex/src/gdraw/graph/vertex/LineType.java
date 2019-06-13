package gdraw.graph.vertex;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Path;

import java.io.Serializable;

public enum LineType implements Serializable {
    Straight {
        public void set(Path path){
            path.getStrokeDashArray().clear();
        }

        @Override
        public String toString() {
            return "———";
        }
    },
    ThreeLength{
        public void set(Path path){
            path.getStrokeDashArray().clear();
            path.getStrokeDashArray().addAll(3 * path.getStrokeWidth());
        }

        @Override
        public String toString() { return "––– –––"; }
    },
    TwoLength{
        public void set(Path path){
            path.getStrokeDashArray().clear();
            path.getStrokeDashArray().addAll(2 * path.getStrokeWidth());
        }

        @Override
        public String toString() { return "–– –– ––"; }
    },
    Dot{
        public void set(Path path){
            path.getStrokeDashArray().clear();
            path.getStrokeDashArray().addAll(path.getStrokeWidth());
        }

        @Override
        public String toString() { return "• • • • •"; }
    },
    DotDash{
        public void set(Path path){
            path.getStrokeDashArray().clear();
            path.getStrokeDashArray().addAll(path.getStrokeWidth(), 2 * path.getStrokeWidth());
        }

        @Override
        public String toString() {
            return "• –– • –– •";
        }
    };

    abstract public void set(Path path);

}
