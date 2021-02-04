package main.controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Główna klasa obsługująca aplikację oraz inicjalizująca okno, jego rozmiar, tytuł i ikonę.
 */
public class Main extends Application
{
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("./../ui/assets/profile.png")));
		Parent root = FXMLLoader.load(getClass().getResource("../ui/view/welcomePage.fxml"));
		primaryStage.setTitle("Serwis społecznościowy");
		primaryStage.setScene(new Scene(root, 1100, 700));
		primaryStage.show();
	}
	
	/**
	 * Główny program.
	 *
	 * @param args argumenty głównego programu.
	 */
	public static void main(String[] args)
	{
		launch(args);
	}
}
