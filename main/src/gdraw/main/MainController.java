package gdraw.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipFile;

import gdraw.graph.node.NodeLibrary;

public class MainController {

    @FXML
    private Accordion nodeLibraryAccordion;

    @FXML
    private TabPane tabPane;

    private ArrayList<Project> projects;
    private Project activeProject;

    public void initialize() throws URISyntaxException {
        nodeLibraryAccordion.getPanes().addAll(
                new NodeLibrary("./libraries/Ostatnie.zip", nodeLibraryAccordion, this)
        );
        //TODO
    }

    public void addNodeToActiveLibrary(ActionEvent actionEvent) {
        //TODO
    }

    public void addNodeLibrary(String path) throws IOException, URISyntaxException {
        URL zipUrl = Main.class.getResource(path);
        File zipFile = new File(zipUrl.toURI());
        ZipFile zip = new ZipFile(zipFile);
        InputStream is = zip.getInputStream(zip.getEntry("test.txt"));
        //TODO
    }

    public void addNodeLibrary(ActionEvent actionEvent) throws URISyntaxException {
        String path = (new FileChooser().showOpenDialog(null).getAbsolutePath());

        TitledPane titledPane = new NodeLibrary(path, nodeLibraryAccordion, this);
        nodeLibraryAccordion.getChildrenUnmodifiable().add(titledPane);
        nodeLibraryAccordion.setExpandedPane(titledPane);

    }

    public void addImageToCanvas(Image image) {
        //TODO
    }

    public void clearSelected() {
        activeProject.clearSelected();
    }

    public void select(double x1, double y1, double x2, double y2) {
        activeProject.checkSelect(x1, y1, x2, y2);
    }
}
