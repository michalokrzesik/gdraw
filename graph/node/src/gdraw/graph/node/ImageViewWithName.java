package gdraw.graph.node;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Bloom;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;

public class ImageViewWithName extends ImageView {
    private String name;
    private NodeLibrary parent;

    public ImageViewWithName(NodeLibrary parent, String name, Image image) {
        super(image);
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
                try {
                    parent.remove(name);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
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
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (URISyntaxException ex) {
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
            delete.setOnAction(e -> {
                try {
                    parent.remove(name);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
            });

            contextMenu.getItems().addAll(toGraph, addParent, toParent, newParent, delete);
            contextMenu.show(this, event.getScreenX(), event.getScreenY());
        });

    }

    public void addToGraph() {
        parent.toGraph(getImage());
        for(TitledPane titledPane : parent.getLibraryList()){
            NodeLibrary library = (NodeLibrary) titledPane;
            try {
                if(library.getName() == "Ostatnie.zip")
                    library.addNode(parent.getPath(name));
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
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

        ListView<TitledPane> libraryListView = new ListView<>(parent.getLibraryList());
        libraryListView.setOnMouseClicked(event1 -> {
            String path = null;
            try {
                path = parent.getPath(name);
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            TitledPane titledPane = libraryListView.getSelectionModel().getSelectedItem();
            NodeLibrary library = (NodeLibrary) titledPane;
            try {
                library.addNode(path);
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
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
        this.setEffect(new Bloom());
    }

    public void unselect(){
        this.setEffect(null);
    }

    public String getName(){
        return name;
    }
}
