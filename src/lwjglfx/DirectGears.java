package lwjglfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import jogamp.opengl.windows.wgl.WindowsWGLDrawableFactory;

import javax.media.opengl.GLDrawableFactory;
import java.net.URL;

/**
 * Tested on Windows x64, steps required: <ol>
 *     <li>Make sure you have jre8\bin\prism-es2.dll (JDK 8u11 ships with them)</li>
 *     <li>Download Linux version of JDK, use its ext\jfxrt.jar</li>
 *     <li>Compile classes from javafx-src.zip\com\sun\prism\es2\WinGL*.java</li>
 *     <li>Copy the compiled WinGL*.class into ext\jfxrt.jar</li>
 *     <li>Run java with -Xshare:dump to re-generate class cache</li>
 *     <li>Start this demo with <i>-Dprism.order=es2</i></li>
 * </ol>
 * <p/>
 * Created by AqD on 2014/08/18.
 */
public class DirectGears extends Application {
    public static WindowsWGLDrawableFactory factory = (WindowsWGLDrawableFactory) GLDrawableFactory.getDesktopFactory();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(final Stage stage) {
        assert (factory != null);
        stage.setTitle("DirectGears");

        stage.setWidth(1440);
        stage.setHeight(810);

        stage.getIcons().add(new Image("lwjgl_32x32.png"));

        final Screen screen = Screen.getPrimary();
        final Rectangle2D screenBounds = screen.getVisualBounds();

        if (screenBounds.getWidth() < stage.getWidth() || screenBounds.getHeight() < stage.getHeight()) {
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());

            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
        }

        final URL fxmlURL = getClass().getClassLoader().getResource("dgears.fxml");
        final FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL);

        Pane content;
        try {
            content = fxmlLoader.load();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return;
        }
        try {
            final Scene scene = new Scene(content);
            scene.getStylesheets().add(getClass().getClassLoader().getResource("gears.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return;
        }
    }
}
