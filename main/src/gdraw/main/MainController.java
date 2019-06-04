package gdraw.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipFile;

import gdraw.graph.node.NodeLibrary;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    public void newProject(ActionEvent event) {
        String array[] = newProjectAlert(); //zebranie informacji od użytkownika
        String projectName = array[0];
        double projectWidth = Double.parseDouble(array[1]);
        double projectHeight = Double.parseDouble(array[2]);

        Canvas canvas = new Canvas(projectWidth, projectHeight);


        //żeby canvas był na środku
        BorderPane borderPane = new BorderPane();
//        borderPane.setPrefHeight(TAB_PANE_H);
//        borderPane.setPrefWidth(TAB_PANE_W);
        borderPane.setCenter(canvas);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(borderPane);


        Tab tab = new Tab(projectName);
        tab.setContent(scrollPane);

        tabPane.getTabs().add(tab);

        //Stworzenie nowego projektu i dodanie go do listy obiektów
        Project project = new Project(this, projectName, tab, canvas);
        projects.add(project);
    }

    /**
     * Funkcja odpowiedzialna za utworzenie nowego projektu (wykorzystywana przy odczycie).
     * @param project projekt z którego ma być utworzona nowa zakładka
     */
    private void newProject(Project project)
    {
        Canvas canvas = project.getBackgorund().getCanvas();

        //żeby canvas był na środku
        BorderPane borderPane = new BorderPane();
//        borderPane.setPrefHeight(TAB_PANE_H);
//        borderPane.setPrefWidth(TAB_PANE_W);
        borderPane.setCenter(canvas);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(borderPane);


        Tab tab = new Tab(project.getName());
        tab.setContent(scrollPane);

        project.setTab(tab);//zmiana zakładki
        projects.add(project);
        tabPane.getTabs().add(projects.get(projects.size() - 1).getTab());
        for(DrawObject drawObject : projects.get(projects.size() - 1).getDrawObjects())
            drawObject.refresh();
        tabPane.getSelectionModel().select(projects.get(projects.size() - 1).getTab());
        projects.get(projects.size()-1).redraw();
    }

    private String[] newProjectAlert()
    {
        String[] tab = new String[3];

        //nowe okno
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Nowy Projekt");

        //layout
        Label nameLabel = new Label("Nazwa: ");
        Label widthLabel = new Label("Szerokość[px]: ");
        Label heightLable = new Label("Wysokość[px]: ");

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

        gridPane.getChildren().addAll(nameLabel, nameText, widthLabel, widthText, heightLable, heightText, button);

        GridPane.setConstraints(nameLabel, 0, 0);
        GridPane.setConstraints(nameText, 0, 1);
        GridPane.setConstraints(widthLabel, 1, 0);
        GridPane.setConstraints(widthText, 1, 1);
        GridPane.setConstraints(heightLable, 2, 0);
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
        File file = activeProject.getFile();

        //zapis
        if(file != null) {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file))) {
                outputStream.writeObject(activeProject);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else saveProjectAs(event);
    }

    /**
     * Funkcja zamyka projekt.
     * @param event
     */
    public void closeProject(ActionEvent event) {
        tabPane.getTabs().remove(activeProject.getTab()); //usuwa z panelu
        projects.remove(activeProject); //usuwa z listy
    }

    public void saveProjectAs(ActionEvent actionEvent) {
        String name = activeProject.getName();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz projekt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("graphDRAW format","*.gdf"));
        fileChooser.setInitialFileName(name + ".gdf");
        activeProject.setFile(fileChooser.showSaveDialog(null));
    }

    public void setProject(Project project) {
        activeProject = project;
    }
}
