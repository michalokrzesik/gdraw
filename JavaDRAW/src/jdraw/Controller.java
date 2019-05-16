package jdraw;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;

public class Controller
{
    public MenuItem newMI;
    public MenuItem openMI;
    public MenuItem saveMI;
    public MenuItem closeMI;
    public TabPane tabPane;

    public Button selectToolB;
    public Button lineButton;

    private SelectMode selectMode;
    private LineMode lineMode;

    public Tab settingsTab;

    public TextField widthContour;
    public TextField widthObject;
    public TextField heightObject;
    public CheckBox settingsCheckBox;
    public Button settingsButton;
    public ColorPicker colorPicker;

    public Slider slider;
    public Label sliderLabel;

    private final static double TAB_PANE_W = 1129;
    private final static double TAB_PANE_H = 857;

    private ArrayList<Project> projects;

    public void addNodeImg(ActionEvent actionEvent) {

    }

    public void addNodeLib(ActionEvent actionEvent) {

    }

    public enum SelectMode{
        RECT {                                                          //Zaznaczanie prostokątne
            public void START(double x, double y, Project project) {
                    project.getSelectStart().setLocation(x, y);         //Ustawienie punktu początkowego zaznaczania
                    project.getSelectedObjects().clear();               //Usunięcie poprzednio zaznaczonych elementów
            }
            public void SELECT(double x, double y, Project project){    //Rysowanie zaznaczenia za pomocą klasy LineObject
                    GraphicsContext gc = project.getCanvas().getGraphicsContext2D();
                    LineObject rect = new LineObject(project.getSelectStart().getX(), project.getSelectStart().getY());
                    rect.getPoints().add(rect.getArea());
                    rect.getPoints().add(new Point2D.Double(rect.getArea().getX(), y));
                    rect.getPoints().add(new Point2D.Double(x, y));
                    rect.getPoints().add(new Point2D.Double(x, rect.getArea().getY()));
                    rect.setColor(Color.BLANCHEDALMOND);
                    rect.setClose(true);
                    project.redraw();
                    rect.draw(gc);
            }
            public void END(double x, double y, Project project){                   //Sprawdzenie, czy obiekty znajdują się w zaznaczeniu
                ArrayList<DrawObject> objects = project.getDrawObjects();           //Za pomocą obiektu LineObject
                LineObject rect = new LineObject(project.getSelectStart().getX(), project.getSelectStart().getY());
                rect.getPoints().add(rect.getArea());
                rect.getPoints().add(new Point2D.Double(rect.getArea().getX(), y));
                rect.getPoints().add(new Point2D.Double(x, y));
                rect.getPoints().add(new Point2D.Double(x, rect.getArea().getY()));
                rect.setWHA();
                for(int i = 0; i < objects.size(); i++) {
                    Point2D.Double area = objects.get(i).getArea();
                    Point2D.Double last = new Point2D.Double(area.getX() + objects.get(i).getWidth(), area.getY() + objects.get(i).getHeight());

                    if(rect.contains(area) && rect.contains(last))                  //LineObject posiada metode sprawdzajaca, czy punkt jest wewnatrz niej
                        project.getSelectedObjects().add(new Integer(i));           //Dodaj obiekt do listy zaznaczonych
                }
                project.redraw();
            }
        },
        MOVE {                                                          //Zaznaczanie i przesuwanie obiektu
            public void START(double x, double y, Project project){
                if(project.getSelectedObjects().size() < 1)
                    RECT.START(x, y, project);                          //Jeśli nie ma zaznaczonych obiektów, zaznacz je prostokątnie
                else
                    project.getSelectStart().setLocation(x, y);         //W przeciwnym wypadku ustaw punkt startowy przesuwania
            }
            public void SELECT(double x, double y, Project project){
                if(project.getSelectedObjects().size() < 1)             //Jezeli nie ma zaznaczonych obiektów, zaznaczaj prostokątnie
                    RECT.SELECT(x, y, project);
                else {
                    for(Integer i : project.getSelectedObjects())       //Przesuń zaznaczone obiekty
                        project.getDrawObjects().get(i).translate(x - project.getSelectStart().getX(), y - project.getSelectStart().getY());
                    project.getSelectStart().setLocation(x, y);         //Zmień punkt startowy przesuwania
                    project.redraw();
                }
            }
            public void END(double x, double y, Project project){
                if(project.getSelectedObjects().size() < 1)             //Jeżeli nie ma zaznaczonych obiektów, dokończ zaznaczanie
                    RECT.END(x, y, project);
            }
        },
        FREE{                                                                   //Zaznaczanie i przesuwanie poszczególnych punktów obiektu
            @Override
            public void SELECT(double x, double y, Project project) {           //Sprawdzanie przy rysowaniu zaznaczenia, czy są punkty do zaznaczenia
                if(!project.getFreeSelect() || project.getSelectedObjects().size() == 0) {
                    GraphicsContext gc = project.getCanvas().getGraphicsContext2D();
                    LineObject rect = new LineObject(project.getSelectStart().getX(), project.getSelectStart().getY());
                    rect.getPoints().add(rect.getArea());
                    rect.getPoints().add(new Point2D.Double(rect.getArea().getX(), y));
                    rect.getPoints().add(new Point2D.Double(x, y));
                    rect.getPoints().add(new Point2D.Double(x, rect.getArea().getY()));
                    rect.setColor(Color.BLANCHEDALMOND);
                    rect.setClose(true);
                   rect.setWHA();
                    for (int i = 0; i < project.getDrawObjects().size(); i++) {
                        DrawObject drawObject = project.getDrawObjects().get(i);
                        boolean select = project.getSelectedObjects().contains(i);
                        for (int j = 0; j < drawObject.getPoints().size(); j++) {
                            if (rect.contains(drawObject.getPoints().get(j))) {
                                if (!drawObject.getSelectedPoints().contains(j))
                                    drawObject.getSelectedPoints().add(j);
                            } else if (drawObject.getSelectedPoints().contains(j))
                                drawObject.getSelectedPoints().remove(drawObject.getSelectedPoints().indexOf(j));
                        }
                        if (!select && drawObject.getSelectedPoints().size() > 0)
                            project.getSelectedObjects().add(i);
                        else if (select && drawObject.getSelectedPoints().size() == 0)
                            project.getSelectedObjects().remove(project.getSelectedObjects().indexOf(i));
                    }
                    project.redraw();
                    rect.getPoints().get(0).setLocation(project.getSelectStart());
                    rect.draw(gc);
                }
                else{                                                       //Lub przesuwanie zaznaczonych punktów, jeśli zaznaczanie już zostało zakończone
                    for(Integer i : project.getSelectedObjects())
                        project.getDrawObjects().get(i).translateSelected(x - project.getSelectStart().getX(), y - project.getSelectStart().getY());
                    project.getSelectStart().setLocation(x, y);
                    project.redraw();
                }
            }

            @Override
            public void END(double x, double y, Project project) {          //Ustawienie flagi, czy zaznaczanie zostało zakończone
                project.setFreeSelect(project.getSelectedObjects().size() > 0);
                project.redraw();
            }

            @Override
            public void START(double x, double y, Project project) {
                project.getSelectStart().setLocation(x, y);
                for(Integer i : project.getSelectedObjects())               //Sprawdzenie, czy są jakieś zaznaczone punkty
                    if(project.getDrawObjects().get(i).getSelectedPoints().size() == 0)
                        project.getSelectedObjects().remove(project.getSelectedObjects().indexOf(i));
                project.setFreeSelect(project.getSelectedObjects().size() > 0); //Aktualizacja flagi zaznaczonych punktów
            }
        };

        /**
         * Metoda wywoływana podczas przeciągania
         * @param x współrzędna kursora
         * @param y współrzędna kursora
         * @param project aktywny projekt
         */
        public abstract void SELECT(double x, double y, Project project);


        /**
         * Metoda wywoływana przy puszczeniu przycisku myszy
         * @param x współrzędna kursora
         * @param y współrzędna kursora
         * @param project aktywny projekt
         */
        public abstract void END(double x, double y, Project project);


        /**
         * Metoda wywoływana przy wciśnięciu przycisku myszy
         * @param x współrzędna kursora
         * @param y współrzędna kursora
         * @param project aktywny projekt
         */
        public abstract void START(double x, double y, Project project);
    }

    public enum LineMode
    {
        LINE                                //Linia pomiędzy dwoma punktami
        {
            @Override
            public void setLastPoint(double x, double y, Project project)
            {

            }

            @Override
            public void drawLine(double x, double y, Project project)
            {
                LineObject lineObject = (LineObject) project.getDrawObjects().get(project.getDrawObjects().size() - 1);
                lineObject.setLastPoint(new Point2D.Double(x, y));      //Zmiana ostatniego (drugiego) punktu
                lineObject.setWHA();

                project.redraw();
            }

            @Override
            public void setStartPoint(double x, double y, Project project)
            {
                LineObject lineObject = new LineObject(x, y);           //Stworzenie obiektu we wskazanym myszą miejscu
                lineObject.addPoint(new Point2D.Double(x, y));
                lineObject.addPoint(new Point2D.Double(x, y));
                project.getDrawObjects().add(lineObject);
            }
        },
        BEZIER {                                                        //Krzywa kwadratowa(?) Beziera
            @Override
            public void setLastPoint(double x, double y, Project project){
                CurveObject curveObject = (CurveObject) project.getDrawObjects().get(project.getDrawObjects().size() - 1);
                curveObject.changeControlPoint();                       //Zmiana flagi, sprawdzającej, czy dodajemy punkt, czy nadajemy zakrzywienie
                project.setCurveModify(curveObject.getControlPoint());
            }

            @Override
            public void drawLine(double x, double y, Project project){
                CurveObject curveObject = (CurveObject) project.getDrawObjects().get(project.getDrawObjects().size() - 1);
                if(curveObject.getControlPoint())                   //Ustawianie zakrzywienia
                    curveObject.setControl(x, y);
                else
                    curveObject.setLastPoint(new Point2D.Double(x, y));     //Lub punktu końcowego

                curveObject.setWHA();

                project.redraw();
            }

            @Override
            public void setStartPoint(double x, double y, Project project){
                CurveObject LcurveObject = null;
                if(project.getCurveModify())
                    LcurveObject = (CurveObject) project.getDrawObjects().get(project.getDrawObjects().size() - 1);
                if(LcurveObject == null || !LcurveObject.getControlPoint()) {
                    CurveObject curveObject = new CurveObject(x, y);
                    curveObject.addPoint(new Point2D.Double(x, y));
                    curveObject.addPoint(new Point2D.Double(x, y));
                    curveObject.addPoint(new Point2D.Double(x, y));
                    project.getDrawObjects().add(curveObject);
                    lastPoint = curveObject.getArea();
                }
            }
        },
        SPLINE {
            @Override
            public void setLastPoint(double x, double y, Project project){
                CurveObject curveObject = (CurveObject) project.getDrawObjects().get(project.getDrawObjects().size() - 1);
                curveObject.changeControlPoint();
                project.setCurveModify(curveObject.getControlPoint());
                if(curveObject.getControlPoint()) {
                    lastPoint.x = x;
                    lastPoint.y = y;
                    drawLine(x, y, project);
                }
            }

            @Override
            public void drawLine(double x, double y, Project project){
                CurveObject curveObject = (CurveObject) project.getDrawObjects().get(project.getDrawObjects().size() - 1);
                if(curveObject.getControlPoint())
                    curveObject.setControl(x, y);
                else
                    curveObject.setLastPoint(new Point2D.Double(x, y));

                curveObject.setWHA();

                project.redraw();
            }

            @Override
            public void setStartPoint(double x, double y, Project project) {
                CurveObject LcurveObject = null;
                if (project.getCurveModify())
                    LcurveObject = (CurveObject) project.getDrawObjects().get(project.getDrawObjects().size() - 1);
                if (LcurveObject == null || !LcurveObject.getControlPoint()) {
                    if (lastPoint == null) {
                        lastPoint = new Point2D.Double();
                        CurveObject curveObject = new CurveObject(x, y);
                        curveObject.addPoint(new Point2D.Double(x, y));
                        curveObject.addPoint(new Point2D.Double(x, y));
                        curveObject.addPoint(new Point2D.Double(x, y));
                        project.getDrawObjects().add(curveObject);
                    } else {
                        CurveObject curveObject = (CurveObject) project.getDrawObjects().get(project.getDrawObjects().size()-1);
                        curveObject.addPoint(new Point2D.Double(lastPoint.x, lastPoint.y));
                        curveObject.addPoint(new Point2D.Double(lastPoint.x, lastPoint.y));
                    }

                }
            }
        },
        OVAL {
            @Override
            public void setLastPoint(double x, double y, Project project){

            }

            @Override
            public void drawLine(double x, double y, Project project){
                OvalObject ovalObject = (OvalObject) project.getDrawObjects().get(project.getDrawObjects().size() - 1);
                ovalObject.setOval(x, y);
                ovalObject.setWHA();

                project.redraw();
            }

            @Override
            public void setStartPoint(double x, double y, Project project) {
                    OvalObject ovalObject = new OvalObject(x,y);
                    ovalObject.setClose(true);
                    for(int i = 0; i < 4; i++)
                        ovalObject.addPoint(new Point2D.Double(x, y));

                    project.getDrawObjects().add(ovalObject);
            }
        },
        POLYGONAL
        {
            @Override
            public void setLastPoint(double x, double y, Project project)
            {
                lastPoint.x = x;
                lastPoint.y = y;
                drawLine(x, y, project);
            }

            @Override
            public void drawLine(double x, double y, Project project)
            {
                LineObject lineObject = (LineObject) project.getDrawObjects().get(project.getDrawObjects().size() - 1);
                lineObject.setLastPoint(new Point2D.Double(x, y));
                lineObject.setWHA();

                project.redraw();
            }

            @Override
            public void setStartPoint(double x, double y, Project project)
            {
                if(lastPoint == null)
                {
                    lastPoint = new Point2D.Double();
                    LineObject lineObject = new LineObject(x, y);
                    lineObject.addPoint(new Point2D.Double(x, y));
                    lineObject.addPoint(new Point2D.Double(x, y));
                    project.getDrawObjects().add(lineObject);
                }
                else
                {
                    LineObject lineObject = (LineObject) project.getDrawObjects().get(project.getDrawObjects().size() - 1);
                    lineObject.addPoint(new Point2D.Double(lastPoint.x, lastPoint.y));
                }
            }
        },
        RECT {
            @Override
            public void setLastPoint(double x, double y, Project project){

            }

            @Override
            public void drawLine(double x, double y, Project project){
                LineObject lineObject = (LineObject) project.getDrawObjects().get(project.getDrawObjects().size() - 1);
                lineObject.setRect(x, y);
                lineObject.setWHA();

                project.redraw();
            }

            @Override
            public void setStartPoint(double x, double y, Project project) {
                LineObject lineObject = new LineObject(x,y);
                lineObject.setClose(true);
                lineObject.addPoint(new Point2D.Double(x, y));
                lineObject.addPoint(new Point2D.Double(x, y));
                lineObject.addPoint(new Point2D.Double(x, y));
                lineObject.addPoint(new Point2D.Double(x, y));
                project.getDrawObjects().add(lineObject);
            }
        };

        Point2D.Double lastPoint = null; //ostatni punkt przy łamanej

        /**
         * Metoda wywoływana przy wciśnięciu przycisku myszy
         * @param x współrzędna kursora myszy
         * @param y współrzędna kursora myszy
         * @param project aktywny projekt
         */
        public abstract void setStartPoint(double x, double y, Project project);

        /**
         * Metoda wywoływana przy przeciągnięciu myszy
         * @param x współrzędna kursora myszy
         * @param y współrzędna kursora myszy
         * @param project aktywny projekt
         */
        public abstract void drawLine(double x, double y, Project project);

        /**
         * Metoda wywoływana przy puszczeniu przycisku myszy
         * @param x współrzędna kursora myszy
         * @param y współrzędna kursora myszy
         * @param project aktywny projekt
         */
        public abstract void setLastPoint(double x, double y, Project project);
    }

    public void initialize() {
        projects = new ArrayList<>();
        selectMode = SelectMode.RECT;
        lineMode = LineMode.LINE;
        colorPicker.setValue(Color.BLACK);
        slider.setMax(200);
        slider.setMin(10);
        slider.adjustValue(100);
        sliderLabel.setText(String.format("%.0f%%", slider.getValue()));
    }

    /**
     * Metoda wywoływana przy zmianie wartości slidera przybliżenia
     * @param event
     */
    public void onSlide(MouseEvent event){
        String value = String.format("%.0f", slider.getValue());
        sliderLabel.setText(value + "%");
        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                project.reScale();
                project.setScale(Double.valueOf(value)/100);
                project.Scale();
                project.redraw();
                break;
            }
        }

    }

    /**
     * Funkcja nadaje parametry startowe pól w zakładce właściwości.
     * @param event
     */
    public void onSelectionSettings(Event event)
    {
        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                boolean [] values = new boolean[5];

                for(boolean b : values)
                {
                    b = false;
                }

                if(project.getDrawObjects().size() > 0 && project.getSelectedObjects().size() > 0)
                {
                    DrawObject tmp = project.getDrawObjects().get(project.getSelectedObjects().get(0)); //pierwszego obiektu

                    if(project.getDrawObjects() != null)
                    {
                        ArrayList<? extends DrawObject> array = project.getDrawObjects();

                        //porównaj z pierwszym wszystkie parametry
                        for (Integer i : project.getSelectedObjects())
                        {
                            if (tmp.getLine() == array.get(i).getLine())
                            {
                                values[0] = true;
                            } else
                            {
                                values[0] = false;
                                break;
                            }
                        }

                        for (Integer i : project.getSelectedObjects())
                        {
                            if (tmp.getWidth() == array.get(i).getWidth())
                            {
                                values[1] = true;
                            } else
                            {
                                values[1] = false;
                                break;
                            }
                        }

                        for (Integer i : project.getSelectedObjects())
                        {
                            if (tmp.getHeight() == array.get(i).getHeight())
                            {
                                values[2] = true;
                            } else
                            {
                                values[2] = false;
                                break;
                            }
                        }

                        for (Integer i : project.getSelectedObjects())
                        {
                            if (tmp.getClose() == array.get(i).getClose())
                            {
                                values[3] = true;
                            } else
                            {
                                values[3] = false;
                                break;
                            }
                        }

                        for (Integer i : project.getSelectedObjects())
                        {
                            if (tmp.getColor() == array.get(i).getColor())
                            {
                                values[4] = true;
                            } else
                            {
                                values[4] = false;
                                break;
                            }
                        }


                        //daj odpowiedni komunikat
                        if (values[0] == true)
                        {
                            widthContour.setText(new Double(tmp.getLine()).toString());
                        } else
                        {
                            widthContour.setText("Szerokość konturu");
                        }

                        if (values[1] == true)
                        {
                            widthObject.setText(new Double(tmp.getWidth()).toString());
                        } else
                        {
                            widthObject.setText("Szerokość obiektu");
                        }

                        if (values[2] == true)
                        {
                            heightObject.setText(new Double(tmp.getHeight()).toString());
                        } else
                        {
                            heightObject.setText("Wysokość obiektu");
                        }

                        if (values[3] == true)
                        {
                            settingsCheckBox.setSelected(tmp.getClose());
                        }

                        if (values[4] == true)
                        {
                            colorPicker.setValue((Color) tmp.getColor());
                        }
                    }
                }
                else
                {
                    widthContour.setText("Szerokość konturu");
                    widthObject.setText("Szerokość obiektu");
                    heightObject.setText("Wysokość obiektu");
                    colorPicker.setValue(Color.BLACK);
                }


                break;
            }
        }
    }

    /**
     * Funkcja obsługuje zakładkę właściwości. Wywoływana po naciśnięciu przycisku.
     * @param event
     */
    public void settings(ActionEvent event)
    {
        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                ArrayList<DrawObject> tmp = project.getDrawObjects();
                project.getSelectedObjects().iterator().forEachRemaining(e ->{
                    Double wc, wo, ho;
                    tmp.get(e).setColor(colorPicker.getValue());
                    try {
                        wc = Double.parseDouble(widthContour.getText());
                    }
                    catch (NumberFormatException err){
                        wc = null;
                    }
                    try {
                        wo = Double.parseDouble(widthObject.getText());
                    }
                    catch (NumberFormatException err){
                        wo = null;
                    }
                    try {
                        ho = Double.parseDouble(heightObject.getText());
                    }
                    catch (NumberFormatException err){
                        ho = null;
                    }
                    if(wc != null)
                        tmp.get(e).setLine(wc);

                    if(wo != null)
                        tmp.get(e).resize(wo, tmp.get(e).getHeight(), settingsCheckBox.isSelected());

                    if(ho != null)
                        tmp.get(e).resize(tmp.get(e).getWidth(), ho, settingsCheckBox.isSelected());
                });

                project.redraw();

                break;
            }
        }
    }

    /**
     * Funkcja odpowiedzialna za utworzenie nowego projektu.
     * @param event
     */
    public void newProject(ActionEvent event)
    {
        String array[] = newProjectAlert(); //zebranie informacji od użytkownika
        String projectName = array[0];
        double projectWidth = Double.parseDouble(array[1]);
        double projectHeight = Double.parseDouble(array[2]);

        Canvas canvas = new Canvas(projectWidth, projectHeight);
        clear(canvas);


        //żeby canvas był na środku
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefHeight(TAB_PANE_H);
        borderPane.setPrefWidth(TAB_PANE_W);
        borderPane.setCenter(canvas);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(borderPane);


        Tab tab = new Tab(projectName);
        tab.setContent(scrollPane);

        tabPane.getTabs().add(tab);

        //Stworzenie nowego projektu i dodanie go do listy obiektów
        Project project = new Project(projectName, (int) canvas.getWidth(), (int) canvas.getHeight(), tab, canvas, slider, sliderLabel);
        projects.add(project);
    }

    /**
     * Funkcja odpowiedzialna za utworzenie nowego projektu (wykorzystywana przy odczycie).
     * @param project projekt z którego ma być utworzona nowa zakładka
     */
    private void newProject(Project project)
    {
        Canvas canvas = new Canvas(project.getCanvas_width(), project.getCanvas_height());
        clear(canvas);

        //żeby canvas był na środku
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefHeight(TAB_PANE_H);
        borderPane.setPrefWidth(TAB_PANE_W);
        borderPane.setCenter(canvas);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(borderPane);


        Tab tab = new Tab(project.getName());
        tab.setContent(scrollPane);

        project.setSlider(slider, sliderLabel);
        project.setTab(tab);//zmiana zakładki
        project.setCanvas(canvas);
        projects.add(project);
        tabPane.getTabs().add(projects.get(projects.size() - 1).getTab());
        for(DrawObject drawObject : projects.get(projects.size() - 1).getDrawObjects())
            drawObject.refresh();
        tabPane.getSelectionModel().select(projects.get(projects.size() - 1).getTab());
        projects.get(projects.size()-1).redraw();
    }

    /**
     * Funkcja otwiera zapisany wcześniej projekt.
     * @param event
     */
    public void openProject(ActionEvent event)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otwórz projekt");
        //fileChooser.setInitialDirectory(new File(""));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JB087 FILES","*.jb087"));
        File selectedFile = fileChooser.showOpenDialog(null);

        //odczyt
        if(selectedFile != null)
        {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(selectedFile)))
            {
                Project project = (Project) inputStream.readObject();

                //dodanie zakładki
                newProject(project);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Funkcja zapisuje projekt.
     * @param event
     */
    public void saveProject(ActionEvent event)
    {
        String name = "*";
        for(Project a : projects)
        {
            if(a.getTab().isSelected() == true)
            {
                name = a.getName();
                break;
            }
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz projekt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JB087 FILES","*.jb087"));
        fileChooser.setInitialFileName(name + ".jb087");
        File file = fileChooser.showSaveDialog(null);

        //zapis
        if(file != null)
        {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file)))
            {
                //wśród wszystkich projektów szuka aktywnego i go zapisuje
                for(Project a : projects)
                {
                    if(a.getTab().isSelected() == true)
                    {
                        outputStream.writeObject(a);
                        break;
                    }
                }
            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }

    /**
     * Funkcja zamyka projekt.
     * @param event
     */
    public void closeProject(ActionEvent event)
    {
        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                tabPane.getTabs().remove(project.getTab()); //usuwa z panelu
                projects.remove(project.getTab()); //usuwa z listy
                break;
            }
        }
    }

    /**
     * Ustawianie trybu zaznaczania dowolnego; wywoływane przyciskiem "Kształt"
     * @param event
     */
    public void setFreeSelection(ActionEvent event){
        selectMode = SelectMode.FREE;
        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                project.setActiveSelectMode(selectMode);
                project.setToolSelect(true);

                break;
            }
        }
    }

    /**
     * Ustawienie zaznaczania prostokątnego; Wywoływany przy wciśnięciu elementu menu kontekstowego "Zaznaczanie prostokątne"
     * @param event
     */
    public void setRectSelection(ActionEvent event){
        selectToolB.setText("Zaznaczanie");
        selectMode = SelectMode.RECT;
        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                project.setActiveSelectMode(selectMode);
                project.setToolSelect(true);

                break;
            }
        }
    }

    /**
     * Ustawienie trybu przenoszenia zaznaczonych obiektów; Wywoływany przy elemencie menu kontekstowego "Przesuwanie"
     * @param event
     */
    public void setMoveSelection(ActionEvent event){
        selectToolB.setText("Przesuwanie");
        selectMode = SelectMode.MOVE;
        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                project.setActiveSelectMode(selectMode);
                project.setToolSelect(true);

                break;
            }
        }
    }

    /**
     * Ustawienie odpowiedniego tryby zaznaczania; Wywoływane przy wciśnięciu pierwszego przycisku narzędzi
     * @param event
     */
    public void selection(ActionEvent event){
        String text = selectToolB.getText();
        if(text.equals("Zaznaczanie"))
            selectMode = SelectMode.RECT;
        else
            selectMode = SelectMode.MOVE;
        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                project.setActiveSelectMode(selectMode);
                project.setToolSelect(true);

                break;
            }
        }
    }

    /**
     * Funkcja wywoływana po wybraniu opcji "Linia pomiędzy dwoma punktami".
     * @param event
     */
    public void drawLine(ActionEvent event)
    {
        lineButton.setText("Linia");
        lineMode = LineMode.LINE;

        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                project.setActiveLineMode(lineMode);
                project.setToolSelect(false);

                break;
            }
        }
    }

    /**
     * Funkcja wywoływana po wybraniu opcji "Krzywa Beziera".
     * @param event
     */
    public void drawBezier(ActionEvent event)
    {
        lineButton.setText("Krzywa Beziera");
        lineMode = LineMode.BEZIER;

        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                project.setActiveLineMode(lineMode);
                project.setToolSelect(false);

                break;
            }
        }
    }

    /**
     * Funkcja wywoływana po wybraniu opcji "Linia sklejana".
     * @param event
     */
    public void drawCurve(ActionEvent event)
    {
        lineButton.setText("Linia sklejana");
        lineMode = LineMode.SPLINE;

        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                project.setActiveLineMode(lineMode);
                project.setToolSelect(false);
                project.setCurveModify(false);

                break;
            }
        }
    }

    /**
     * Funkcja wywoływana przy wciśnięciu przycisku "Elipsa"
     * @param event
     */
    public void drawOval(ActionEvent event){
        lineMode = LineMode.OVAL;
        for(Project project : projects)
            if(project.getTab().isSelected() == true){
                project.setActiveLineMode(lineMode);
                project.setToolSelect(false);

                break;
            }
    }

    /**
     * Funkcja wywoływana przy wciśnięciu przycisku "Prostokąt"
     * @param event
     */
    public void drawRect(ActionEvent event){
        lineMode = LineMode.RECT;
        for(Project project : projects)
            if(project.getTab().isSelected() == true){
                project.setActiveLineMode(lineMode);
                project.setToolSelect(false);

                break;
            }
    }

    /**
     * Funkcja wywoływana po wybraniu opcji "łamana".
     * @param event
     */
    public void drawPolygon(ActionEvent event)
    {
        lineButton.setText("Łamana");                                          //musiałem skopiować Ł xdd
        lineMode = LineMode.POLYGONAL;

        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                project.setActiveLineMode(lineMode);
                project.setToolSelect(false);

                break;
            }
        }
    }

    /**
     * Funkcja wywoływana po wciśnięciu przycisku "linia".
     * @param event
     */
    public void line(ActionEvent event)
    {
        String text = lineButton.getText();
        if(text.equals("Linia"))
            lineMode = LineMode.LINE;
        else if(text.equals("Linia sklejana"))
            lineMode = LineMode.SPLINE;
        else if(text.equals("Krzywa Beziera"))
            lineMode = LineMode.BEZIER;
        else
            lineMode = LineMode.POLYGONAL;

        for(Project project : projects)
        {
            if(project.getTab().isSelected() == true)
            {
                project.setActiveLineMode(lineMode);
                project.setToolSelect(false);

                break;
            }
        }
    }

    /**
     * Funkcja zbiera dane od użytkownika w formie okienka popup.
     */
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
     * Czyści Canvas na biało.
     * @param canvas płótno
     */
    private void clear(Canvas canvas)
    {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}
