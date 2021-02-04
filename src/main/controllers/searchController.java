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
 * Klasa odpowiadająca za <b>wyszukiwanie</b> użytkowników - wyświetlanie listy oraz dodawanie do znajomych.
 */
public class searchController extends Controller
{
	/**
	 * Główny panel sceny.
	 */
	@FXML
	BorderPane borderPane;
	
	/**
	 * Lista użytkowników poza aktualnie zalogowanym.
	 */
	@FXML
	ListView<String> usersList;
	
	/**
	 * Pole wprowadzania imienia lub nazwiska szukanego użytkownika.
	 */
	@FXML
	TextField inputUser;
	
	/**
	 * Zmienna nasłuchująca zmian w liście.
	 */
	ObservableList<String> itemsUsers = FXCollections.observableArrayList();
	
	/**
	 * Zmienna służąca do filtrowania listy.
	 */
	FilteredList<String> filteredUsers = new FilteredList<>(itemsUsers, s -> true);
	
	/**
	 * Zmienna przechowująca imię i nazwisko użytkownika.
	 */
	private String[] userData;
	
	/**
	 * Zmienna przechowująca id użytkownika.
	 */
	private int userId;
	
	/**
	 * Zmienna przechowująca imię i nazwisko użytkownika, którego zalogowany użytkownik chce dodać do znajomych.
	 */
	private String selectedUser;
	
	/**
	 * Funkcja obsługująca pobranie danych z poprzedniej sceny i zapisanie ich do zmiennej {@code userData}
	 * oraz wyświetlenie listy wszystkich użytkowników.
	 * @param userData dane pobrane z poprzedniej sceny
	 * @param loggedUserId id zalogowanego użytkownika
	 */
	public void transferMessage(String[] userData, int loggedUserId)
	{
		this.userData = userData;
		this.userId = loggedUserId;
		showAllUsers();
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
	 * Funkcja obsługująca wyświetlenie użytkowników zapisanych w bazie.
	 */
	public void showAllUsers()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				getUsers(connection);
				connection.close();
			}
			catch (SQLException sqlException)
			{
				sqlException.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja pobierająca z bazy listę użytkowników poza aktualnie zalogowanym i wyświetlająca je jako listę.
	 * @param connection połączenie z bazą
	 */
	private void getUsers(Connection connection)
	{
		try
		{
			try
			{
				itemsUsers.clear();
				String statement = "select imie, nazwisko from projekt.znajdz_uzytkownikow where id_uzytkownicy <> ?";
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				preparedStatement.setInt(1, userId);
				ResultSet resultSet = preparedStatement.executeQuery();
				
				while (resultSet.next())
				{
					itemsUsers.add(resultSet.getString(1) + " " + resultSet.getString(2));
				}
				usersList.setItems(itemsUsers);
				
				resultSet.close();
				preparedStatement.close();
			}
			catch (SQLException e)
			{
				System.out.println("Blad podczas przetwarzania danych:" + e);
				e.printStackTrace();
			}
			connection.close();
		}
		catch (SQLException sqlException)
		{
			sqlException.printStackTrace();
		}
	}
	
	/**
	 * Funkcja obsługująca filtrowanie listy użytkowników na podstawie wprowadzonego imienia i nazwiska.
	 */
	public void filterUsersButton()
	{
		filteredUsers.setPredicate(s -> s.contains(inputUser.getText()));
		SortedList<String> sortedUsers = new SortedList<>(filteredUsers);
		usersList.setItems(sortedUsers);
		usersList.scrollTo(0);
	}
	
	/**
	 * Funkcja obsługująca dodawanie użytkownika do znajomych zalogowanego użytkownika.
	 */
	public void addUserAsFriend()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				addFriend(connection);
				connection.close();
			}
			catch (SQLException sqlException)
			{
				sqlException.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja sprawdzająca czy użytkownicy o podanych id są znajomymi.
	 * @param connection połączenie z bazą
	 * @param idFriend id znajomego
	 * @return prawda jeśli {@code idFriend} i {@code idUser} są znajomymi, w przeciwnym wypadku fałsz
	 */
	private boolean findUserInFriends(Connection connection, int idFriend)
	{
		boolean userExists = false;
		if (connection != null)
		{
			try
			{
				String statement = "select exists (select from projekt.znajomi where (id_uzytkownicy=? and id_znajomi=?) or (id_uzytkownicy=? and id_znajomi=?))";
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				preparedStatement.setInt(1, userId);
				preparedStatement.setInt(2, idFriend);
				preparedStatement.setInt(3, idFriend);
				preparedStatement.setInt(4, userId);
				ResultSet resultSet = preparedStatement.executeQuery();
				resultSet.next();
				userExists = resultSet.getBoolean(1);
				resultSet.close();
				preparedStatement.close();
			}
			catch (SQLException e)
			{
				System.out.println("Blad podczas przetwarzania danych:" + e);
			}
		}
		return userExists;
	}
	
	/**
	 * Funkcja obsługująca dodawanie wybranego z listy użytkownika jako znajomego.
	 * @param connection połączenie z bazą
	 */
	private void addFriend(Connection connection)
	{
		if (connection != null)
		{
			try
			{
				try
				{
					String[] friendName = selectedUser.split(" ");
					String statement = "select id_uzytkownicy from projekt.znajdz_id_uzytkownika where imie=? and nazwisko=?";
					PreparedStatement preparedStatement = connection.prepareStatement(statement);
					preparedStatement.setString(1, friendName[0]);
					preparedStatement.setString(2, friendName[1]);
					ResultSet resultSet = preparedStatement.executeQuery();
					resultSet.next();
					int idFriend = resultSet.getInt(1);
					
					if(!findUserInFriends(connection, idFriend))
					{
						statement = "insert into projekt.znajomi (id_uzytkownicy, id_znajomi) values (?, ?)";
						preparedStatement = connection.prepareStatement(statement);
						preparedStatement.setInt(1, userId);
						preparedStatement.setInt(2, idFriend);
						preparedStatement.executeUpdate();
					}
					else
					{
						showAlert("Użytkownik znajduje się już w znajomych.");
					}
					
					preparedStatement.close();
					resultSet.close();
				}
				catch (SQLException e)
				{
					System.out.println("Blad podczas przetwarzania danych:" + e);
					e.printStackTrace();
				}
				connection.close();
			}
			catch (SQLException sqlException)
			{
				sqlException.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja obsługująca dodawanie użytkownika do znajomych po kliknięciu na niego na liście.
	 */
	public void handleListClick()
	{
		if (!usersList.getSelectionModel().isEmpty())
		{
			selectedUser = usersList.getSelectionModel().getSelectedItem();
			addUserAsFriend();
		}
	}
}
