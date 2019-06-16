package gdraw.graph.node;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ImageViewWithName extends ImageView {
    private String name;
    private NodeLibrary parent;

    public ImageViewWithName(NodeLibrary parent, String name, Image image) {
        super(image);
        setFitHeight(100);
        setFitWidth(100);
        this.name = name;
        this.parent = parent;

        this.setOnMouseClicked((e) -> select());

        this.setOnContextMenuRequested(event -> {
            select();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem toGraph = new MenuItem("Dodaj do grafu");
            toGraph.setOnAction(e -> addToGraph());

            MenuItem addParent = new MenuItem("Dodaj do kategorii");
            addParent.setOnAction(e -> selectParentAndAdd());

            MenuItem toParent = new MenuItem("Przenieś do kategorii");
            toParent.setOnAction(e -> {
                selectParentAndAdd();
                parent.remove(name);
            });

            MenuItem newParent = new MenuItem("Utwórz kategorię z...");
            newParent.setOnAction(e -> {
                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("Podaj nazwę kategorii");

                TextField libraryName = new TextField();
                Button button = new Button("Zatwierdź");
                button.setOnAction(actionEvent -> {
                    String lib = libraryName.getText();
                    if(lib.isEmpty() || lib.contains(" ")) lib = "Nowa Kategoria";
                    try {
                        parent.getLibraryList().add(parent.newLibraryWithNode(lib, name));
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                    window.close();
                });

                FlowPane pane = new FlowPane();
                pane.getChildren().addAll(libraryName, button);

                window.setScene(new Scene(pane));
                window.showAndWait();
            });

            MenuItem delete = new MenuItem("Usuń z kategorii");
            delete.setOnAction(e -> parent.remove(name));

            contextMenu.getItems().addAll(toGraph, addParent, toParent, newParent, delete);
            contextMenu.show(this, event.getScreenX(), event.getScreenY());
        });

    }

    public void addToGraph() {
        parent.toGraph(getImage());
        for(TitledPane titledPane : parent.getLibraryList()) {
            NodeLibrary library = (NodeLibrary) titledPane;
            if (library.getName().equals("OSTATNIE")) {
                try {
                    library.addNode(parent.getPath(name));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Wybór nowej kategorii
     */
    private void selectParentAndAdd(){
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Wybierz kategorie");

        ListView<NodeLibraryRef> libraryListView = new ListView<>(parent.getLibraryListRef());
        libraryListView.setOnMouseClicked(event1 -> {
            File path = parent.getPath(name);

            NodeLibrary library = libraryListView.getSelectionModel().getSelectedItem().getLibrary();
            try {
                library.addNode(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            window.close();
        });


        Pane pane = new Pane(libraryListView);
        window.setScene(new Scene(pane));
        window.showAndWait();
    }

    private void select() {
        parent.unselect();
        parent.setSelected(this);
        this.setEffect(new InnerShadow());
    }

    public void unselect(){
        this.setEffect(null);
    }

    public String getName(){
        return name;
    }
}
