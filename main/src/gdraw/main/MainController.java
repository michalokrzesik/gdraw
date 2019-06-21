package gdraw.main;

import gdraw.graph.node.Node;
import gdraw.graph.util.*;
import gdraw.graph.vertex.ArrowType;
import gdraw.graph.vertex.LineType;
import gdraw.graph.vertex.Vertex;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;

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

    private MIandButtonPair undo, redo;

    private ArrayList<Project> projects;
    private Project activeProject;

    private ArrayList<Selectable> clipboard;
    private ArrayList<Request> requestedNodes;
    private ArrayList<Request> requestedVertices;
    private boolean toSnapshot;

    public void initialize() {
        nodeLibraryAccordion.setOnContextMenuRequested(this::libraryContextMenu);

        NodeLibrary recent = new NodeLibrary(
                new File("./libraries/OSTATNIE"), nodeLibraryAccordion, this, new LibraryPane());
        nodeLibraryAccordion.getPanes().add(recent);

        nodeLibraryAccordion.setExpandedPane(recent);

        for(File library : Objects.requireNonNull(new File("./libraries").listFiles(File::isDirectory)))
            if(!library.getName().equals("OSTATNIE"))
                nodeLibraryAccordion.getPanes().add(
                        new NodeLibrary(library, nodeLibraryAccordion, this, new LibraryPane())
                );

        clipboard = new ArrayList<>();
        projects = new ArrayList<>();
        requestedNodes = new ArrayList<>();
        requestedVertices = new ArrayList<>();
        undo = new MIandButtonPair(undoMI, undoB, this);
        redo = new MIandButtonPair(redoMI, redoB, this);
        undo.setDisable(true);
        redo.setDisable(true);

        lineType.getItems().addAll(LineType.values());
        lineType.setValue(LineType.Straight);
        arrowType.getItems().addAll(ArrowType.values());
        arrowType.setValue(ArrowType.None);
        vertexColor.setValue(Color.BLACK);
    }

    public void addNodeToActiveLibrary(ActionEvent actionEvent) throws IOException {
        File path = (ImageFileChooser("Wybierz obraz", null).showOpenDialog(null));
        if(path == null) return;
        ((NodeLibrary) nodeLibraryAccordion.getExpandedPane()).addNode(path);
    }

    private FileChooser ImageFileChooser(String title, String name) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        String[] formatNames = ImageIO.getWriterFormatNames();
        for(String formatName : formatNames)
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(formatName + " format","*." + formatName));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Wszystkie pliki", "*"));
        fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));
        if(name != null)
            fileChooser.setInitialFileName(name + fileChooser.getSelectedExtensionFilter().getExtensions().get(0).substring(1));
        return fileChooser;
    }


    public void addNodeLibrary(ActionEvent actionEvent) throws URISyntaxException {
        File path = (new FileChooser().showOpenDialog(null));
        if(path == null) return;

        TitledPane titledPane = new NodeLibrary(path, nodeLibraryAccordion, this, new LibraryPane());
        nodeLibraryAccordion.getChildrenUnmodifiable().add(titledPane);
        nodeLibraryAccordion.setExpandedPane(titledPane);

    }

    public void addNode(ActionEvent actionEvent){
        ((NodeLibrary)nodeLibraryAccordion.getExpandedPane()).toGraph();
    }



    public void addNode(Image image) {
        if(activeProject != null) activeProject.addNode(image);
    }

    public void clearSelected() {
        if(activeProject != null) activeProject.clearSelected(null);
    }

    public void select(Rectangle selection) {
        if(activeProject != null) activeProject.checkSelect(selection);
    }

    public void select(Selectable item, boolean ctrlPressed){
        if(activeProject != null) activeProject.select(item, ctrlPressed);
    }

    public Tab tabForProject(Project project){

        //żeby canvas był na środku
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(project.getCanvas());

        ScrollPane scrollPane = new ScrollPane(borderPane);
        scrollPane.setStyle("-fx-background-color: #FDF5E6; " +
                "-fx-border-color: #FDF5E6;");
        project.getCanvas().setStyle("-fx-background-color: #FDF5E6; " +
                "-fx-border-color: #FDF5E6;");



        Tab tab = new Tab(project.getName());
        tab.setContent(scrollPane);
        tab.setStyle("-fx-background-color: #FDF5E6; " +
                "-fx-border-color: #FDF5E6;");

        tabPane.getTabs().add(tab);
        tab.setOnSelectionChanged(e -> {
            if(tab.isSelected()) setProject(project);
        });

        return tab;
    }

    public void newProject(ActionEvent event) {
        String[] array = newProjectAlert(); //zebranie informacji od użytkownika
        String projectName = array[0];
        double projectWidth = Double.parseDouble(array[1]);
        double projectHeight = Double.parseDouble(array[2]);

        Project project = new Project(this, projectName, projectWidth, projectHeight,
                hierarchy, properties, undo, redo);

        project.setTab(tabForProject(project));

        projects.add(project);
        setProject(project);
    }

    /**
     * Funkcja odpowiedzialna za utworzenie nowego projektu (wykorzystywana przy odczycie).
     * @param project projekt z którego ma być utworzona nowa zakładka
     */
    private void newProject(Project project, File file) {
        project.refresh(file, this, hierarchy, properties, undo, redo);

        Tab tab = tabForProject(project);

        project.setTab(tab);//zmiana zakładki
        projects.add(project);
        tabPane.getTabs().add(projects.get(projects.size() - 1).getTab());
        tabPane.getSelectionModel().select(projects.get(projects.size() - 1).getTab());
        setProject(project);
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
                newProject(project, selectedFile);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Funkcja zapisuje projekt.
     * @param event
     */
    public void saveProject(ActionEvent event) {
        if(activeProject != null)
            saveProject(activeProject);
    }

    /**
     * Funkcja zamyka projekt.
     * @param event
     */
    public void closeProject(ActionEvent event) {
        if(activeProject != null)
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
        project.setParents();

        //zapis
        if(file != null) {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file))) {
                outputStream.writeObject(project);
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
        saveProject(project);
    }

    private void closeProject(Project project) {
        tabPane.getTabs().remove(project.getTab()); //usuwa z panelu
        projects.remove(project); //usuwa z listy
    }

    public void saveProjectAs(ActionEvent actionEvent) {
        if(activeProject != null)
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


    public void exportToImage(ActionEvent actionEvent) {
        if(activeProject == null) return;;
        File file = ImageFileChooser("Eksportuj graf", activeProject.getName()).showSaveDialog(null);
        if(file != null) {
            String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            System.out.println("Exporting to file " + file.getName() + " with extension " + extension);
            toSnapshot = true;
            activeProject.draw();
            WritableImage snapshot = activeProject.snapshot();
            toSnapshot = false;
            try {
                BufferedImage bufferedImage = new BufferedImage((int) snapshot.getWidth(), (int) snapshot.getHeight(),
                        (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg") ?
                                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB));
                bufferedImage.getGraphics().drawImage(SwingFXUtils.fromFXImage(snapshot, null), 0, 0, null);

                ImageIO.write(bufferedImage, extension, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void exportToObject(ActionEvent actionEvent){
        if(activeProject == null) return;;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Eksportuj graf");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Plik JSON", "*.json"),
                new FileChooser.ExtensionFilter("Plik dedykowanego XML", "*.gdml")
        );
        fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));
        fileChooser.setInitialFileName(activeProject.getName());
        File file = fileChooser.showSaveDialog(null);
        if(file != null) {
            String extension = file.getName().substring(file.getName().lastIndexOf("."));
            activeProject.exportToFile(file, extension);
        }
    }

    public void onMousePressed(MouseEvent e, Selectable item) {
        if(activeProject != null) activeProject.onMousePressed(e, item);
    }

    public void onMouseDragged(MouseEvent e, Selectable item) {
        if(activeProject != null) activeProject.onMouseDragged(e, item);
    }

    public void addObject(Selectable object) {
        if(activeProject != null) activeProject.newObject(object);
    }

    public void removeObject(Selectable object){
        if(activeProject != null) activeProject.removeObject(object);
    }

    public void undo(ActionEvent actionEvent) {
        if(activeProject != null) activeProject.undo();
    }


    public void redo(ActionEvent actionEvent) {
        if(activeProject != null) activeProject.redo();
    }

    public void copySelected(ActionEvent actionEvent) {
        if(activeProject == null) return;
        clipboard.clear();
        ArrayList<Selectable> selected = activeProject.getSelected();
        if(!selected.isEmpty()) selected.forEach(object -> clipboard.add(object.copy()));
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
        if(activeProject != null)
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
        if(activeProject == null) return;
        requestedVertices.clear();
/*        requestedNodes.forEach(request -> {
            if(request.checkProject(activeProject))
                request.request();
        }); */
        requestedNodes.clear();
        activeProject.paste(clipboard);
    }

    public void duplicateSelected(ActionEvent actionEvent) {
        if(activeProject == null) return;
        copySelected(actionEvent);
        paste(actionEvent);
    }

    public void addVertex(ActionEvent actionEvent) {
        if(activeProject != null)
            activeProject.addVertex(
                lineType.getSelectionModel().getSelectedItem(),
                arrowType.getSelectionModel().getSelectedItem(),
                vertexColor.getValue(),
                duplex.isSelected(),
                curved.isSelected(),
                width.getText());
    }

    public void onMouseReleased(MouseEvent e, Selectable item) {
        if(activeProject != null) activeProject.onMouseReleased(e, item);
    }

    public void groupSelected(ActionEvent actionEvent) {
        if(activeProject != null) activeProject.groupSelected();
    }

    public void ungroupSelected(ActionEvent actionEvent) {
        if(activeProject != null) activeProject.ungroupSelected();
    }

    public void nodesToGroups(ActionEvent actionEvent) {
        if(activeProject != null) activeProject.nodesToGroups();
    }

    public void groupsToNodes(ActionEvent actionEvent) {
        if(activeProject != null) activeProject.groupsToNodes();
    }

    public void moveToGroup(ActionEvent actionEvent) {
        if(activeProject != null) activeProject.moveToGroup();
    }

    public void changeBackground(ActionEvent actionEvent) {
        if(activeProject == null) return;
        File file = ImageFileChooser("Wybierz obraz", null).showOpenDialog(null);
        if(file != null) {
            try {
                activeProject.getBackground().setImage(SwingFXUtils.toFXImage(ImageIO.read(file), null));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void contextMenu(ContextMenuEvent contextMenuEvent) {
        if(activeProject == null) return;
        activeProject.setDragModel(DragModel.Standard);

        ContextMenu contextMenu = new ContextMenu();

        MenuItem toGraph = new MenuItem("Dodaj węzeł do grafu");
        toGraph.setOnAction(this::addNode);

        SeparatorMenuItem s1 = new SeparatorMenuItem();

        contextMenu.getItems().addAll(toGraph, s1);

        if(!activeProject.getSelected().isEmpty()) {
            activeProject.getSelected().get(0).contextMenu(contextMenu);
            contextMenu.getItems().add(new SeparatorMenuItem());
        }


        MenuItem changeBackground = new MenuItem("Zmień obraz w tle");
        changeBackground.setOnAction(this::changeBackground);

        contextMenu.getItems().add(changeBackground);

        contextMenu.show((javafx.scene.Node) contextMenuEvent.getTarget(), contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
    }

    public void collapse(ActionEvent actionEvent) {
        if(activeProject != null)
            activeProject.changeCollapsed(true);
    }

    public void extend(ActionEvent actionEvent) {
        if(activeProject != null)
            activeProject.changeCollapsed(false);
    }

    public void forceDraw() {
        if(activeProject != null) activeProject.draw();
    }

    public void libraryContextMenu(ContextMenuEvent event) {
        ContextMenu contextMenu = new ContextMenu();

        NodeLibrary active = (NodeLibrary) nodeLibraryAccordion.getExpandedPane();
        if(active != null) active.contextMenu(contextMenu);

        MenuItem addNodeToActiveLibrary = new MenuItem("Dodaj obraz węzła");
        addNodeToActiveLibrary.setOnAction(e -> {
            try {
                addNodeToActiveLibrary(e);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        MenuItem addNodeLibrary = new MenuItem("Dodaj bibliotekę węzłów");
        addNodeLibrary.setOnAction(actionEvent -> {
            try {
                addNodeLibrary(actionEvent);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });

        contextMenu.getItems().addAll(addNodeToActiveLibrary, addNodeLibrary);

        contextMenu.show(nodeLibraryAccordion, event.getScreenX(), event.getScreenY());
    }

    public void setDragModel(DragModel model) {
        if(activeProject != null) activeProject.setDragModel(model);
    }

    public boolean isToSnapshot() {
        return toSnapshot;
    }
}