package main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.sql.*;

/**
 * Klasa odpowiadająca za <b>edycję</b> profilu użytkownika.
 */
public class editUserController extends Controller
{
	/**
	 * Główny panel sceny.
	 */
	@FXML
	BorderPane borderPane;
	
	/**
	 * Pole wprowadzania nowego imienia.
	 */
	@FXML
	TextField inputFirstName;
	
	/**
	 * Pole wprowadzania nowego nazwiska.
	 */
	@FXML
	TextField inputLastName;
	
	/**
	 * Pole wprowadzania nowego hasła.
	 */
	@FXML
	TextField inputPassword;
	
	/**
	 * Pole wprowadzania nowego miasta.
	 */
	@FXML
	TextField inputCity;
	
	/**
	 * Pole wprowadzania nowego opisu.
	 */
	@FXML
	TextField inputBio;
	
	/**
	 * Zmienna przechowująca imię i nazwisko użytkownika.
	 */
	private String[] userData;
	
	/**
	 * Zmienna przechowująca id użytkownika.
	 */
	private int userId;
	
	/**
	 * Funkcja obsługująca pobranie danych z poprzedniej sceny i zapisanie ich do zmiennej {@code userData}.
	 * @param userData     dane pobrane z poprzedniej sceny
	 * @param loggedUserId id zalogowanego użytkownika
	 */
	public void transferMessage(String[] userData, int loggedUserId)
	{
		this.userData = userData;
		this.userId = loggedUserId;
	}
	
	/**
	 * Funkcja obsługująca aktualizowanie imienia/nazwiska/hasła/miasta/opisu użytkownika w bazie.
	 * @param info nazwa kolumny, która zostanie zmodyfikowana
	 * @param input nowa wartość, na którą kolumna {@code info} zostanie zmieniona
	 * @param regex wyrażenie regularne, sprawdzające poprawność znaków
	 */
	public void updateUsersInfo(String info, String input, String regex)
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				updateInfo(connection, info, input, regex);
				connection.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja aktualizująca wartość w bazie.
	 * @param connection połączenie z bazą
	 * @param info nazwa kolumny, która zostanie zmodyfikowana
	 * @param input nowa wartość, na którą kolumna {@code info} zostanie zmieniona
	 * @param regex wyrażenie regularne, sprawdzające poprawność znaków
	 */
	private void updateInfo(Connection connection, String info, String input, String regex)
	{
		if (connection != null)
		{
			try
			{
				if (input != null && input.matches(regex))
				{
					String statement = "update projekt.uzytkownicy set " + info + " = ? where id_uzytkownicy=?";
					PreparedStatement preparedStatement = connection.prepareStatement(statement);
					preparedStatement.setString(1, input);
					preparedStatement.setInt(2, userId);
					preparedStatement.executeUpdate();
					preparedStatement.close();
					connection.close();
				}
				else
				{
					showAlert("Niepoprawne wartości w polu " + info + ".");
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja obsługująca aktualizowanie imienia użytkownika.
	 */
	public void updateUsersFirstName()
	{
		userData[0] = inputFirstName.getText();
		updateUsersInfo("imie", inputFirstName.getText(), "([a-zA-Z0-9\\p{L}]+[\\s-]?)*");
		inputFirstName.clear();
	}
	
	/**
	 * Funkcja obsługująca aktualizowanie nazwiska użytkownika.
	 */
	public void updateUsersLastName()
	{
		userData[1] = inputLastName.getText();
		updateUsersInfo("nazwisko", inputLastName.getText(), "[A-ZŻŚŁ]([a-zA-Z\\p{L}]+)((-)?([A-ZŻŚŁ]([a-zA-Z\\p{L}]*))*)?");
		inputLastName.clear();
	}
	
	/**
	 * Funkcja obsługująca aktualizowanie hasła użytkownika.
	 */
	public void updateUsersPassword()
	{
		if(inputPassword.getText().length()<5||inputPassword.getText().length()>30)
		{
			showAlert("Podano zbyt krótkie lub zbyt długie hasło.\nHasło powinno zawierać przynajmniej 5 ale nie więcej niż 30 znaków");
		}
		else
		{
			String hashPass = hashPassword(inputPassword.getText());
			updateUsersInfo("haslo", hashPass, ".*");
			inputPassword.clear();
		}
	}
	
	/**
	 * Funkcja obsługująca aktualizowanie miasta użytkownika.
	 */
	public void updateUsersCity()
	{
		updateUsersInfo("miasto", inputCity.getText(), "[A-ZŻŹÓŚĆŁ]([a-zA-Z\\p{L}]+)((-|\\s)?([A-ZŻŹÓŚĆŁ]([a-zA-Z\\p{L}]*))*){0,2}");
		inputCity.clear();
	}
	
	/**
	 * Funkcja obsługująca aktualizowanie opisu użytkownika.
	 */
	public void updateUsersBio()
	{
		updateUsersInfo("opis", inputBio.getText(), ".*");
		inputBio.clear();
	}
	
	/**
	 * Funkcja obsługująca przycisk powrotu do profilu zalogowanego użytkownika.
	 */
	public void returnButton()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../ui/view/profilePage.fxml"));
			BorderPane newPane = loader.load();
			profileController controller = loader.getController();
			controller.transferMessage(userData, userData[0], userData[1]);
			borderPane.getChildren().setAll(newPane);
		}
		catch (IOException e)
		{
			System.out.println("Problem ładowania sceny. " + e);
		}
	}
}
