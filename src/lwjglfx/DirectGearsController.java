package lwjglfx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by AqD on 2014/08/18.
 */
public class DirectGearsController implements Initializable {
    @FXML
    private StackPane root;
    @FXML
    private MyCanvas canvas;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
    }
}
