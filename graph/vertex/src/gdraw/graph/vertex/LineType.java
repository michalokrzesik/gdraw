package gdraw.graph.vertex;

import javafx.scene.canvas.GraphicsContext;

public enum LineType {
    Straight {
        public void set(GraphicsContext gc, double lineWidth){
            gc.setLineDashes(null);
        }
    },
    ThreeLength{
        public void set(GraphicsContext gc, double lineWidth){
            gc.setLineDashes(3 * lineWidth);
        }
    },
    TwoLength{
        public void set(GraphicsContext gc, double lineWidth){
            gc.setLineDashes(2 * lineWidth);
        }
    },
    Dot{
        public void set(GraphicsContext gc, double lineWidth){
            gc.setLineDashes(lineWidth);
        }
    },
    DotDash{
        public void set(GraphicsContext gc, double lineWidth){
            gc.setLineDashes(lineWidth, 2 * lineWidth);
        }
    };

    abstract public void set(GraphicsContext gc, double lineWidth);

}
