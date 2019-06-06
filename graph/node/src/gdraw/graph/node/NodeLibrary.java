package gdraw.graph.node;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import gdraw.main.MainController;
import gdraw.main.Main;

public class NodeLibrary extends TitledPane {
    private MainController controller;
    private String path;
    private Accordion parent;
    private FlowPane pane;
    private ImageViewWithName selected;

    public NodeLibrary(String path, Accordion parent, MainController controller) throws URISyntaxException {
        super(new File(Main.class.getResource(path).toURI()).getName(), null);
        this.controller = controller;
        this.parent = parent;
        this.path = path;
        pane = new FlowPane();
        this.getChildren().add(pane);
        try {
            show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void show() throws IOException, URISyntaxException {
        URL zipUrl = Main.class.getResource(path);
        File zipFile = new File(zipUrl.toURI());
        ZipFile zip = new ZipFile(zipFile);
        zip.stream().forEach(
                (e) -> pane.getChildren().add(
                        new ImageViewWithName(
                                this,
                                e.getName(),
                                new Image(getClass().getResource(e.getName()).toExternalForm())))
        );
    }

    public void addNode(String nodePath) throws IOException, URISyntaxException {
        URL zipUrl = Main.class.getResource(path);
        File zipFile = new File(zipUrl.toURI());
        ZipFile zip = new ZipFile(zipFile);

        if(zip.getEntry(nodePath.substring(nodePath.lastIndexOf(File.separator))) == null)
            addOrRemoveNode(nodePath, true);
    }

    public void addOrRemoveNode(String nodePathOrName, boolean add) throws URISyntaxException, IOException {
        URL oldUrl = Main.class.getResource(path);
        File oldFile = new File(oldUrl.toURI());
        ZipFile in = new ZipFile(oldFile);

        String newPath = path.replace(".zip", "_tmp.zip");
        URL newURL = Main.class.getResource(newPath);
        File newFile = new File(newURL.toURI());
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(newFile));

        in.stream().forEach((e) -> {
            if(add || e.getName() != nodePathOrName) {
                try {
                    addEntry(e.getName(), in.getInputStream(e), out);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        if(add) {
            URL nodeURL = Main.class.getResource(nodePathOrName);
            File nodeFile = new File(nodeURL.toURI());

            addEntry(nodeFile.getName(), new FileInputStream(nodeFile), out);
        }

        out.close();
        in.close();

        newFile.renameTo(oldFile);

        pane.getChildren().add(new ImageView(new Image(getClass().getResource(nodePathOrName).toExternalForm())));
    }

    private void addEntry(String name, InputStream inputStream, ZipOutputStream out) throws IOException {
        byte[] BUFFER = new byte [4096 * 1024];
        int bytesRead;
        out.putNextEntry(new ZipEntry(name));
        while ((bytesRead = inputStream.read(BUFFER)) != -1) {
            out.write(BUFFER, 0, bytesRead);
        }
        out.closeEntry();
        inputStream.close();
    }

    public void setSelected(ImageViewWithName selected) {
        this.selected = selected;
    }

    public void unselect() {
        selected.unselect();
    }


    public ObservableList<TitledPane> getLibraryList() {
        return parent.getPanes();
    }

    public String getPath(String name) throws URISyntaxException, IOException {
        URL zipUrl = Main.class.getResource(path);
        File zipFile = new File(zipUrl.toURI());
        ZipFile zip = new ZipFile(zipFile);
        File dir = zipFile.getParentFile();
        String path = dir + File.separator + name;
        File file = new File(path);
        file.delete();                                  //W razie gdyby istniało już

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
        byte[] bytesIn = new byte[4096 * 1024];
        int read = 0;
        while ((read = zip.getInputStream(zip.getEntry(name)).read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();

        return path;
    }

    public void remove(String name) throws IOException, URISyntaxException {
        addOrRemoveNode(name, false);
    }

    public NodeLibrary newLibraryWithNode(String libraryName, String nodeName) throws IOException, URISyntaxException {
        URL zipUrl = Main.class.getResource(path);
        File zipFile = new File(zipUrl.toURI());
        ZipFile zip = new ZipFile(zipFile);
        File dir = zipFile.getParentFile();
        String path = dir + File.separator + libraryName + ".zip";
        File file = new File(path);
        ZipOutputStream newZip = new ZipOutputStream(new FileOutputStream(file));

        addEntry(nodeName, zip.getInputStream(zip.getEntry(nodeName)), newZip);

        return new NodeLibrary(path, parent, controller);
    }

    public void toGraph(){ selected.addToGraph(); }

    public void toGraph(Image image) {
        controller.addNode(image);
    }

    public String getName() throws URISyntaxException {
        URL zipUrl = Main.class.getResource(path);
        File zipFile = new File(zipUrl.toURI());
        return zipFile.getName();
    }
}
