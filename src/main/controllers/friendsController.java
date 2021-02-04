package main.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.sql.*;

/**
 * Klasa odpowiadająca za <b>znajomych</b> użytkownika - wyświetlanie listy oraz dodawanie nowych.
 */
public class friendsController extends Controller
{
	/**
	 * Główny panel sceny.
	 */
	@FXML
	BorderPane borderPane;
	
	/**
	 * Lista znajomych użytkownika.
	 */
	@FXML
	ListView<String> friendsList;
	
	/**
	 * Pole wprowadzania szukanego użytkownika.
	 */
	@FXML
	TextField inputFriend;
	
	/**
	 * Zmienna przechowująca imię i nazwisko użytkownika.
	 */
	private String[] userData;
	
	/**
	 * Zmienna przechowująca id użytkownika.
	 */
	private int userId;
	
	/**
	 * Zmienna nasłuchująca zmian w liście.
	 */
	ObservableList<String> itemsFriends = FXCollections.observableArrayList();
	
	/**
	 * Zmienna służąca do filtrowania listy.
	 */
	FilteredList<String> filteredFriends = new FilteredList<>(itemsFriends, s -> true);
	
	/**
	 * Funkcja obsługująca pobranie danych z poprzedniej sceny i zapisanie ich do zmiennej {@code userData}
	 * oraz wyświetlenie listy wszystkich znajomych.
	 *
	 * @param userData     dane pobrane z poprzedniej sceny
	 * @param loggedUserId id zalogowanego użytkownika
	 */
	public void transferMessage(String[] userData, int loggedUserId)
	{
		this.userData = userData;
		this.userId = loggedUserId;
		showAllFriends();
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
	
	/**
	 * Funkcja obsługująca wyświetlenie znajomych zapisanych w bazie.
	 */
	public void showAllFriends()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				getFriends(connection);
				connection.close();
			}
			catch (SQLException sqlException)
			{
				sqlException.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja pobierająca z bazy listę znajomych aktualnie zalogowanego użytkownika i wyświetlająca je jako listę.
	 *
	 * @param connection połączenie z bazą
	 */
	private void getFriends(Connection connection)
	{
		if (connection != null)
		{
			try
			{
				itemsFriends.clear();
				String statement = "select imie, nazwisko from projekt.uzytkownicy "
								   + "where uzytkownicy.id_uzytkownicy in "
								   + "(select id_znajomi from projekt.uzytkownik_lewo where id_uzytkownicy = ?) "
								   + "or uzytkownicy.id_uzytkownicy in "
								   + "(select id_uzytkownicy from projekt.uzytkownik_prawo where id_znajomi = ?)";
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				preparedStatement.setInt(1, userId);
				preparedStatement.setInt(2, userId);
				ResultSet resultSet = preparedStatement.executeQuery();
				
				while (resultSet.next())
				{
					itemsFriends.add(resultSet.getString(1) + " " + resultSet.getString(2));
				}
				friendsList.setItems(itemsFriends);
				
				resultSet.close();
				preparedStatement.close();
				connection.close();
			}
			catch (SQLException e)
			{
				System.out.println("Blad podczas przetwarzania danych:" + e);
			}
		}
	}
	
	/**
	 * Funkcja obsługująca filtrowanie listy znajomych na podstawie wprowadzonego imienia i nazwiska.
	 */
	public void filterUsersButton()
	{
		filteredFriends.setPredicate(s -> s.contains(inputFriend.getText()));
		SortedList<String> sortedFriends = new SortedList<>(filteredFriends);
		friendsList.setItems(sortedFriends);
		friendsList.scrollTo(0);
	}
}
