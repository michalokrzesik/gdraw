package gdraw.main;

import gdraw.graph.node.Node;
import gdraw.graph.util.MIandButtonPair;
import gdraw.graph.util.Selectable;
import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.Vertex;
import gdraw.graph.util.Request;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.awt.image.RenderedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;

import gdraw.graph.node.NodeLibrary;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

public class MainController {

    @FXML
    Accordion nodeLibraryAccordion;
    @FXML
    TabPane tabPane;
    @FXML
    ScrollPane hierarchy;
    @FXML
    ScrollPane properties;

    @FXML
    MenuItem undoMI;
    @FXML
    Button undoB;
    @FXML
    MenuItem redoMI;
    @FXML
    Button redoB;

    @FXML
    ChoiceBox<LineType> lineType;
    @FXML
    ChoiceBox<ArrowType> arrowType;
    @FXML
    ColorPicker vertexColor;
    @FXML
    CheckMenuItem duplex;
    @FXML
    CheckMenuItem curved;
    @FXML
    TextField width;

    private MIandButtonPair undo = new MIandButtonPair(undoMI, undoB), redo = new MIandButtonPair(redoMI, redoB);

    private ArrayList<Project> projects;
    private Project activeProject;

    private ArrayList<Selectable> clipboard;
    private ArrayList<Request> requestedNodes;
    private ArrayList<Request> requestedVertices;

    public void initialize() throws URISyntaxException {
        nodeLibraryAccordion.getPanes().addAll(
                new NodeLibrary("./libraries/Ostatnie.zip", nodeLibraryAccordion, this)
        );
        clipboard = new ArrayList<>();
        projects = new ArrayList<>();
        requestedNodes = new ArrayList<>();
        requestedVertices = new ArrayList<>();
        undo.setDisable(true);
        redo.setDisable(true);

        //TODO
    }

    public void addNodeToActiveLibrary(ActionEvent actionEvent) throws IOException, URISyntaxException {
        String path = (new FileChooser().showOpenDialog(null).getAbsolutePath());
        ((NodeLibrary) nodeLibraryAccordion.getExpandedPane()).addNode(path);
    }


    public void addNodeLibrary(ActionEvent actionEvent) throws URISyntaxException {
        String path = (new FileChooser().showOpenDialog(null).getAbsolutePath());

        TitledPane titledPane = new NodeLibrary(path, nodeLibraryAccordion, this);
        nodeLibraryAccordion.getChildrenUnmodifiable().add(titledPane);
        nodeLibraryAccordion.setExpandedPane(titledPane);

    }

    public void addNode(ActionEvent actionEvent){
        ((NodeLibrary)nodeLibraryAccordion.getExpandedPane()).toGraph();
    }



    public void addNode(Image image) {
        activeProject.addNode(image);
    }

    public void clearSelected() {
        activeProject.clearSelected();
    }

    public void select(double x1, double y1, double x2, double y2) {
        activeProject.checkSelect(x1, y1, x2, y2);
    }

    public void select(Selectable item, boolean ctrlPressed){ activeProject.select(item, ctrlPressed); }

    public Tab tabForProject(Project project){
        //żeby canvas był na środku
        BorderPane borderPane = new BorderPane();
//        borderPane.setPrefHeight(TAB_PANE_H);
//        borderPane.setPrefWidth(TAB_PANE_W);
        borderPane.setCenter(project.getGroup());

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(borderPane);


        Tab tab = new Tab(project.getName());
        tab.setContent(scrollPane);

        tabPane.getTabs().add(tab);
        tab.setOnSelectionChanged(e -> {
            if(tab.isSelected()) setProject(project);
        });

        return tab;
    }

    public void newProject(ActionEvent event) {
        String array[] = newProjectAlert(); //zebranie informacji od użytkownika
        String projectName = array[0];
        double projectWidth = Double.parseDouble(array[1]);
        double projectHeight = Double.parseDouble(array[2]);

        Canvas canvas = new Canvas(projectWidth, projectHeight);
        Group group = new Group();
        Project project = new Project(this, projectName, group, canvas, properties, undo, redo);

        projects.add(project);
        setProject(project);
    }

    /**
     * Funkcja odpowiedzialna za utworzenie nowego projektu (wykorzystywana przy odczycie).
     * @param project projekt z którego ma być utworzona nowa zakładka
     */
    private void newProject(Project project) {

        Tab tab = tabForProject(project);

        project.refresh(this, tab, group, canvas, properties, undo, redo);

        project.setTab(tab);//zmiana zakładki
        projects.add(project);
        tabPane.getTabs().add(projects.get(projects.size() - 1).getTab());
        tabPane.getSelectionModel().select(projects.get(projects.size() - 1).getTab());
        activeProject = project;
        project.draw();
    }

    private String[] newProjectAlert() {
        String[] tab = new String[3];

        //nowe okno
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Nowy Projekt");

        //layout
        Label nameLabel = new Label("Nazwa: ");
        Label widthLabel = new Label("Szerokość[px]: ");
        Label heightLabel = new Label("Wysokość[px]: ");

        TextField nameText = new TextField();
        TextField widthText = new TextField();
        TextField heightText = new TextField();

        Button button = new Button("Zatwierdź");

        //Przypisuje wpisane dane do odpowiednich pól, które umożliwiają powstanie nowego projektu
        button.setOnAction(e -> {

            //ustawienie parametrów nowego projektu
            //projectName = nameText.getText();
            //projectWidth = Integer.parseInt(widthText.getText());
            //projectHeight = Integer.parseInt(heightText.getText());

            tab[0] = nameText.getText();
            tab[1] = widthText.getText();
            tab[2] = heightText.getText();

            //zamyka okno
            window.close();
        });

        //layout
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(10));

        gridPane.getChildren().addAll(nameLabel, nameText, widthLabel, widthText, heightLabel, heightText, button);

        GridPane.setConstraints(nameLabel, 0, 0);
        GridPane.setConstraints(nameText, 0, 1);
        GridPane.setConstraints(widthLabel, 1, 0);
        GridPane.setConstraints(widthText, 1, 1);
        GridPane.setConstraints(heightLabel, 2, 0);
        GridPane.setConstraints(heightText, 2, 1);
        GridPane.setConstraints(button, 3, 1);

        gridPane.setAlignment(Pos.CENTER);

        Scene scene = new Scene(gridPane);
        window.setScene(scene);
        window.showAndWait();
        return tab;
    }

    /**
     * Funkcja otwiera zapisany wcześniej projekt.
     * @param event
     */
    public void openProject(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otwórz projekt");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("graphDRAW format","*.gdf"));
        File selectedFile = fileChooser.showOpenDialog(null);

        //odczyt
        if(selectedFile != null) {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(selectedFile))) {
                Project project = (Project) inputStream.readObject();
                newProject(project);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Funkcja zapisuje projekt.
     * @param event
     */
    public void saveProject(ActionEvent event) {
        saveProject(activeProject);
    }

    /**
     * Funkcja zamyka projekt.
     * @param event
     */
    public void closeProject(ActionEvent event) {
        closeProjectAlert(activeProject);
    }

    private void closeProjectAlert(Project project) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Zamknij projekt");

        //layout
        Label label = new Label(String.format("Czy chcesz zapisać projekt %s przed zamknięciem?", project.getName()));

        Button cancel = new Button("Cofnij");
        Button close = new Button("Nie");
        Button saveAndClose = new Button("Tak");

        cancel.setOnAction(e -> window.close());
        close.setOnAction(e -> {
            closeProject(project);
            window.close();
        });
        saveAndClose.setOnAction(e -> {
            saveProject(project);
            closeProject(project);
            window.close();
        });

        //layout
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(10));

        gridPane.getChildren().addAll(label, cancel, close, saveAndClose);

        GridPane.setConstraints(label, 0, 0, 3, 1);
        GridPane.setConstraints(cancel, 0, 1);
        GridPane.setConstraints(close, 1, 1);
        GridPane.setConstraints(saveAndClose, 2, 1);

        gridPane.setAlignment(Pos.CENTER);

        Scene scene = new Scene(gridPane);
        window.setScene(scene);
        window.showAndWait();
    }

    private void saveProject(Project project) {
        File file = project.getFile();

        //zapis
        if(file != null) {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file))) {
                outputStream.writeObject(project);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else saveProjectAs(project);
    }

    private void saveProjectAs(Project project) {
        String name = project.getName();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz projekt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("graphDRAW format","*.gdf"));
        fileChooser.setInitialFileName(name + ".gdf");
        project.setFile(fileChooser.showSaveDialog(null));
    }

    private void closeProject(Project project) {
        tabPane.getTabs().remove(project); //usuwa z panelu
        projects.remove(project); //usuwa z listy
    }

    public void saveProjectAs(ActionEvent actionEvent) {
        saveProjectAs(activeProject);
    }

    public void setProject(Project project) {
        activeProject = project;
        hierarchy.setContent(project.getTreeView());
        project.setProperties();
    }

    public void closeProgram(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    public void exportGraph(ActionEvent actionEvent) {
        String name = activeProject.getName();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Eksportuj graf");
        String[] formatNames = ImageIO.getWriterFormatNames();
        for(String formatName : formatNames)
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(formatName + " format","*." + formatName));
        fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));
        fileChooser.setInitialFileName(name + fileChooser.getSelectedExtensionFilter().getExtensions().get(0));
        File file = fileChooser.showSaveDialog(null);
        if(file != null) {
            String extension = file.getName().split(".")[1];
            WritableImage snapshot = activeProject.snapshot();
            try {
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(snapshot,null);
                ImageIO.write(renderedImage, extension, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onMousePressed(MouseEvent e, Selectable item) {
        activeProject.onMousePressed(e, item);
    }

    public void onMouseDragged(MouseEvent e, Selectable item) {
        activeProject.onMouseDragged(e, item);
    }

    public void addObject(Selectable object) {
        activeProject.newObject(object);
    }

    public void removeObject(Selectable object){
        activeProject.removeObject(object);
    }

    public void undo(ActionEvent actionEvent) {
        activeProject.undo();
    }


    public void redo(ActionEvent actionEvent) {
        activeProject.redo();
    }

    public void copySelected(ActionEvent actionEvent) {
        clipboard.clear();
        activeProject.getSelected().forEach(object -> clipboard.add(object.copy()));
    }

    public void request(boolean isFrom, Node node, Vertex vertex, Node oldNode) {
        Request request = null;
        for(Request r : requestedNodes)
            if(r.checkNode(oldNode) && r.checkIsFrom(isFrom)) {
                request = r;
                break;
            }
        if(request == null) requestedVertices.add(new Request(activeProject, isFrom, node, vertex));
        else {
            request.request(node);
            requestedNodes.remove(request);
        }
    }

    public void request(boolean isFrom, Node node, Vertex vertex, Vertex oldVertex){
        Request request = null;
        for(Request r : requestedVertices)
            if(r.checkVertex(oldVertex) && r.checkIsFrom(isFrom)) {
                request = r;
                break;
            }
        if(request == null) requestedNodes.add(new Request(activeProject, isFrom, node, vertex));
        else {
            request.request(vertex);
            requestedVertices.remove(request);
        }
    }

    public void deleteSelected(ActionEvent actionEvent) {
        activeProject.deleteSelected();
    }

    public Project getProject() {
        return activeProject;
    }

    public void cutSelected(ActionEvent actionEvent) {
        copySelected(actionEvent);
        deleteSelected(actionEvent);
    }

    public void paste(ActionEvent actionEvent) {
        requestedVertices.clear();
/*        requestedNodes.forEach(request -> {
            if(request.checkProject(activeProject))
                request.request();
        }); */
        requestedNodes.clear();
        activeProject.paste(clipboard);
    }

    public void duplicateSelected(ActionEvent actionEvent) {
        copySelected(actionEvent);
        paste(actionEvent);
    }

    public void addVertex(ActionEvent actionEvent) {
        activeProject.addVertex(
                lineType.getSelectionModel().getSelectedItem(),
                arrowType.getSelectionModel().getSelectedItem(),
                vertexColor.getValue(),
                duplex.isSelected(),
                curved.isSelected(),
                width.getText());
    }

    public void onMouseReleased(MouseEvent e, Selectable item) {
        activeProject.onMouseReleased(e, item);
    }

    public void groupSelected(ActionEvent actionEvent) {
        activeProject.groupSelected();
    }

    public void ungroupSelected(ActionEvent actionEvent) {
        activeProject.ungroupSelected();
    }

    public void nodesToGroups(ActionEvent actionEvent) {
        activeProject.nodesToGroups();
    }

    public void groupsToNodes(ActionEvent actionEvent) {
        activeProject.groupsToNodes();
    }

    public void moveToGroup(ActionEvent actionEvent) {
        activeProject.moveToGroup();
    }
}
