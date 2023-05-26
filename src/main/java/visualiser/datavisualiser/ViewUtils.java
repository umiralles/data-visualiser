package visualiser.datavisualiser;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import visualiser.datavisualiser.models.User;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ViewUtils {

    private static Scene scene;

    public static void setScene(Scene scene) {
        ViewUtils.scene = scene;
    }

    public static void switchTo(View view) {
        if (scene == null) {
            System.out.println("No scene was set");
            return;
        }

        try {
            URL resourceFile = Objects.requireNonNull(ViewUtils.class.getResource(view.getFileName()));
            Parent root = FXMLLoader.load(resourceFile);

            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendData(User userData) {
        Stage stage = (Stage) scene.getWindow();
        stage.setUserData(userData);
    }

    public static User receiveData() {
        Stage stage = (Stage) scene.getWindow();

        return (User) stage.getUserData();
    }

}
