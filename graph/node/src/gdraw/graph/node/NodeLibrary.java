package gdraw.graph.node;

import gdraw.graph.util.LibraryPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Accordion;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import gdraw.main.MainController;

public class NodeLibrary extends TitledPane {
    private MainController controller;
    private File path;
    private Accordion parent;
    private LibraryPane pane;
    private ImageViewWithName selected;
    private NodeLibraryRef ref;

    public NodeLibrary(File path, Accordion parent, MainController controller, LibraryPane pane) {
        super(path.getName(), pane);
        setMaxWidth(400);
        this.controller = controller;
        this.parent = parent;
        this.path = path;
        if(!path.exists()) path.mkdir();
        this.pane = pane;
        this.setOnContextMenuRequested(controller::libraryContextMenu);
        ref = new NodeLibraryRef(this);
        show();
    }

    public void contextMenu(ContextMenu contextMenu){
        if(selected != null) selected.contextMenu(contextMenu);
    }

    private void show() {
        /*URL zipUrl = Main.class.getResource(path);
        File zipFile = new File(zipUrl.toURI()); */
        //ZipFile zip = new ZipFile(path);
        pane.clear();
        if(path.isDirectory() && path.listFiles().length > 0)
            for(File file: path.listFiles()) {
                pane.add(
                        new ImageViewWithName(
                                this,
                                file.getName(),
                                new Image(file.toURI().toString())));
            }
    }

    public void addNode(File nodePath) throws IOException {
        /*URL zipUrl = Main.class.getResource(path);
        File zipFile = new File(zipUrl.toURI());
        ZipFile zip = new ZipFile(path);*/
/*        for(File file : path.listFiles())
            if(file.getName() == nodePath.getName())
                return;*/
//            addOrRemoveNode(nodePath, true);

        Files.copy(nodePath.toPath(), Paths.get(path.getAbsolutePath() + "/" + nodePath.getName()), StandardCopyOption.REPLACE_EXISTING);
        show();
    }

    /*public void addOrRemoveNode(String nodePathOrName, boolean add) throws URISyntaxException, IOException {
        URL oldUrl = Main.class.getResource(path.getAbsolutePath());
        File oldFile = new File(oldUrl.toURI());
        ZipFile in = new ZipFile(oldFile);

        URL newURL = Main.class.getResource(path.getAbsolutePath().replace(".zip", "_tmp.zip"));
        path = new File(newURL.toURI());
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path));

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

        path.renameTo(oldFile);

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
    }*/

    public void setSelected(ImageViewWithName selected) {
        this.selected = selected;
    }

    public void unselect() {
        if(selected != null)
            selected.unselect();
    }


    public ObservableList<TitledPane> getLibraryList() {
        return parent.getPanes();
    }

    private NodeLibraryRef getRef() {
        return ref;
    }

    public File getPath(String name) {
        /*URL zipUrl = Main.class.getResource(path);
        File zipFile = new File(zipUrl.toURI());
        ZipFile zip = new ZipFile(path);/
        File dir = path.getParentFile();
        String path = dir.getAbsolutePath() + File.separator + name;
        File file = new File(path);
        file.delete();                                  //W razie gdyby istniało już

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
        byte[] bytesIn = new byte[4096 * 1024];
        int read = 0;
        while ((read = zip.getInputStream(zip.getEntry(name)).read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
         */
        for(File file : path.listFiles())
            if(file.getName().equals(name))
                return file;

        return null;
    }

    public void remove(String name) {
        //addOrRemoveNode(name, false);
        getPath(name).delete();
        show();
    }

    public NodeLibrary newLibraryWithNode(String libraryName, String nodeName) throws IOException, URISyntaxException {
        /*URL zipUrl = Main.class.getResource(path);
        File zipFile = new File(zipUrl.toURI());
        ZipFile zip = new ZipFile(path);*/
        File dir = path.getParentFile();
        String path = dir.getAbsolutePath() + "/" + libraryName;
        File file = new File(path);
        file.mkdir();
/*        ZipOutputStream newZip = new ZipOutputStream(new FileOutputStream(file));

        addEntry(nodeName, zip.getInputStream(zip.getEntry(nodeName)), newZip);
*/
        NodeLibrary ret = new NodeLibrary(file, parent, controller, new LibraryPane());
        ret.addNode(getPath(nodeName));
        return ret;
    }

    public void toGraph(){ if(selected != null) selected.addToGraph(); }

    public void toGraph(Image image) {
        controller.addNode(image);
    }

    public String getName() {
        /*URL zipUrl = Main.class.getResource(path);
        File zipFile = new File(zipUrl.toURI());
        return zipFile.getName();*/
        return path.getName();
    }

    public ObservableList<NodeLibraryRef> getLibraryListRef() {
        ArrayList<NodeLibraryRef> res = new ArrayList<>();
        parent.getPanes().forEach(tp -> res.add(((NodeLibrary) tp).getRef()));
        return FXCollections.observableList(res);
    }
}
