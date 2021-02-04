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
 * Klasa odpowiadająca za <b>grupy</b> - wyświetlanie listy oraz dołączanie do nowych.
 */
public class groupsController extends Controller
{
	/**
	 * Główny panel sceny.
	 */
	@FXML
	BorderPane borderPane;
	
	/**
	 * Lista grup, do których należy użytkownik.
	 */
	@FXML
	ListView<String> groupsList;
	
	/**
	 * Pole wprowadzania szukanej grupy.
	 */
	@FXML
	TextField inputGroup;
	
	/**
	 * Zmienna nasłuchująca zmian w liście.
	 */
	ObservableList<String> itemsGroups = FXCollections.observableArrayList();
	
	/**
	 * Zmienna służąca do filtrowania listy.
	 */
	FilteredList<String> filteredGroups = new FilteredList<>(itemsGroups, s -> true);
	
	/**
	 * Zmienna przechowująca imię i nazwisko użytkownika.
	 */
	private String[] userData;
	
	/**
	 * Zmienna przechowująca id użytkownika.
	 */
	private int userId;
	
	/**
	 * Zmienna przechowująca nazwę grupy, do której chce dołączyć użytkownik.
	 */
	private String selectedGroup;
	
	/**
	 * Funkcja obsługująca pobranie danych z poprzedniej sceny i zapisanie ich do zmiennej {@code userData}
	 * oraz wyświetlenie wszystkich grup zapisanych w bazie.
	 * @param userData dane pobrane z poprzedniej sceny
	 * @param loggedUserId id zalogowanego użytkownika
	 */
	public void transferMessage(String[] userData, int loggedUserId)
	{
		this.userData = userData;
		this.userId = loggedUserId;
		showAllGroups();
	}
	
	/**
	 * Funkcja obsługująca wyświetlenie grup zapisanych w bazie.
	 */
	public void showAllGroups()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				getGroups(connection);
				connection.close();
			}
			catch (SQLException sqlException)
			{
				sqlException.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja pobierająca z bazy listę grup i wyświetlająca je jako listę.
	 * @param connection połączenie z bazą
	 */
	private void getGroups(Connection connection)
	{
		if (connection != null)
		{
			try
			{
				itemsGroups.clear();
				String statement = "select * from projekt.lista_grup";
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				ResultSet resultSet = preparedStatement.executeQuery();
				
				while (resultSet.next())
				{
					itemsGroups.add(resultSet.getString(1) + ", grupa " + resultSet.getString(2));
				}
				groupsList.setItems(itemsGroups);
				
				resultSet.close();
				preparedStatement.close();
				connection.close();
			}
			catch (SQLException e)
			{
				System.out.println("Blad podczas przetwarzania danych:" + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja sprawdzająca czy zalogowany użytkownik znajduje się już w grupie o podanym id.
	 * @param connection połączenie z bazą
	 * @return prawda jeśli {@code idFriend} i {@code idUser} są znajomymi, w przeciwnym wypadku fałsz
	 */
	private boolean findGroupInUsersGroups(Connection connection)
	{
		boolean groupExistsInUsersGroups = false;
		if (connection != null)
		{
			try
			{
				String statement = "select exists (select from projekt.grupy, projekt.uzytkownicy_grupy "
								   + "where id_uzytkownicy=? and uzytkownicy_grupy.id_grupy=grupy.id_grupy and grupy.nazwa_grupy=?)";
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				preparedStatement.setInt(1, userId);
				preparedStatement.setString(2, selectedGroup);
				ResultSet resultSet = preparedStatement.executeQuery();
				resultSet.next();
				groupExistsInUsersGroups = resultSet.getBoolean(1);
				resultSet.close();
				preparedStatement.close();
			}
			catch (SQLException e)
			{
				System.out.println("Blad podczas przetwarzania danych:" + e);
			}
		}
		return groupExistsInUsersGroups;
	}
	
	/**
	 * Funkcja obsługująca dodawanie użytkownika do grupy.
	 */
	private void addGroupToUsersGroups()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				addUserToGroup(connection);
				connection.close();
			}
			catch (SQLException sqlException)
			{
				sqlException.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja obsługująca dodawanie użytkowika do wybranej grupy.
	 * @param connection połączenie z bazą
	 */
	private void addUserToGroup(Connection connection)
	{
		if (connection != null)
		{
			try
			{
				if (!findGroupInUsersGroups(connection))
				{
					String statement = "select * from projekt.znajdz_id_grupy where nazwa_grupy=?";
					PreparedStatement preparedStatement = connection.prepareStatement(statement);
					preparedStatement.setString(1, selectedGroup);
					ResultSet resultSet = preparedStatement.executeQuery();
					resultSet.next();
					int idGroup = resultSet.getInt(1);
					resultSet.close();
					preparedStatement.close();
					
					String statement1 = "insert into projekt.uzytkownicy_grupy (id_uzytkownicy, id_grupy) values (?, ?)";
					PreparedStatement preparedStatement1 = connection.prepareStatement(statement1);
					preparedStatement1.setInt(1, userId);
					preparedStatement1.setInt(2, idGroup);
					preparedStatement1.executeUpdate();
					preparedStatement1.close();
					connection.close();
				}
				else
				{
					showAlert("Użytkownik znajduje się już w wybranej grupie.");
				}
			}
			catch (SQLException e)
			{
				System.out.println("Blad podczas przetwarzania danych:" + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja obsługująca dodawanie użytkownika do grupy po kliknięciu na nią na liście.
	 */
	public void handleListClick()
	{
		if (!groupsList.getSelectionModel().isEmpty())
		{
			selectedGroup = groupsList.getSelectionModel().getSelectedItem().split(",")[0];
			addGroupToUsersGroups();
		}
	}
	
	/**
	 * Funkcja obsługująca filtrowanie listy użytkowników na podstawie wprowadzonego imienia i nazwiska.
	 */
	public void filterGroupsButton()
	{
		filteredGroups.setPredicate(s -> s.contains(inputGroup.getText()));
		SortedList<String> sortedUsers = new SortedList<>(filteredGroups);
		groupsList.setItems(sortedUsers);
		groupsList.scrollTo(0);
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
