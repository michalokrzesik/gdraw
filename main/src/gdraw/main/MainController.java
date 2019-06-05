package gdraw.main;

import gdraw.graph.util.Selectable;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipFile;

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

    public void select(Selectable item, boolean ctrlPressed){ activeProject.select(item, ctrlPressed); }

    public void newProject(ActionEvent event) {
        String array[] = newProjectAlert(); //zebranie informacji od użytkownika
        String projectName = array[0];
        double projectWidth = Double.parseDouble(array[1]);
        double projectHeight = Double.parseDouble(array[2]);

        Canvas canvas = new Canvas(projectWidth, projectHeight);
        Group group = new Group();


        //żeby canvas był na środku
        BorderPane borderPane = new BorderPane();
//        borderPane.setPrefHeight(TAB_PANE_H);
//        borderPane.setPrefWidth(TAB_PANE_W);
        borderPane.setCenter(group);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(borderPane);


        Tab tab = new Tab(projectName);
        tab.setContent(scrollPane);

        tabPane.getTabs().add(tab);

        //Stworzenie nowego projektu i dodanie go do listy obiektów
        Project project = new Project(this, projectName, tab, group, canvas, properties);
        projects.add(project);
        setProject(project);
    }

    /**
     * Funkcja odpowiedzialna za utworzenie nowego projektu (wykorzystywana przy odczycie).
     * @param project projekt z którego ma być utworzona nowa zakładka
     */
    private void newProject(Project project) {
        Canvas canvas = project.getBackground().getCanvas();
        Group group = new Group();


        //żeby canvas był na środku
        BorderPane borderPane = new BorderPane();
//        borderPane.setPrefHeight(TAB_PANE_H);
//        borderPane.setPrefWidth(TAB_PANE_W);
        borderPane.setCenter(group);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(borderPane);


        Tab tab = new Tab(project.getName());
        tab.setContent(scrollPane);
        project.refresh(this, tab, group, canvas, properties);

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
}
