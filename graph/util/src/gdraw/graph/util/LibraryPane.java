package gdraw.graph.util;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

public class LibraryPane extends Pane {
    private FlowPane pane;
    public LibraryPane(){
        super();
        ScrollPane scroll = new ScrollPane();
        getChildren().add(scroll);
        pane = new FlowPane();
        scroll.setContent(pane);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(440);
        scroll.setStyle("-fx-background-color: #FDF5E6; " +
                "-fx-border-color: #FDF5E6; ");
        setStyle("-fx-background-color:  #FDF5E6; ");
        pane.maxWidth(310);
        pane.setVgap(2);
        pane.setHgap(5);
        pane.setStyle("-fx-background-color: #FDF5E6; " +
                "-fx-border-color: #FDF5E6; ");
    }

    public void add(Node node){
        pane.getChildren().add(node);
    }

    public void clear() {
        pane.getChildren().clear();
    }
}
