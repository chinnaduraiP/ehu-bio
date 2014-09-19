package es.ehubio.panalyzer;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {
	@Override
	public void start(Stage stage) {
		try {
			VBox root = (VBox)FXMLLoader.load(getClass().getResource("Main.fxml"));
			Scene scene = new Scene(root,640,480);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stage.setTitle("PAnalyzer v2.0-alpha1 (UPV/EHU)");
			stage.setScene(scene);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
