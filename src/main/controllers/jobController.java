package main.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.sql.*;

/**
 * Klasa odpowiadająca za <b>zatrudnienie</b> użytkownika - wyświetlanie listy oraz dodawanie nowego.
 */
public class jobController extends Controller
{
	/**
	 * Główny panel sceny.
	 */
	@FXML
	BorderPane borderPane;
	
	/**
	 * Lista firm, w których zatrudniony jest użytkownik.
	 */
	@FXML
	ListView<String> jobsList;
	
	/**
	 * Pole wprowadzania nazwy nowej firmy.
	 */
	@FXML
	TextField inputJob;
	
	/**
	 * Zmienna nasłuchująca zmian w liście.
	 */
	ObservableList<String> itemsJobs = FXCollections.observableArrayList();
	
	/**
	 * Zmienna przechowująca imię i nazwisko użytkownika.
	 */
	private String[] userData;
	
	/**
	 * Zmienna przechowująca id użytkownika.
	 */
	private int userId;
	
	/**
	 * Funkcja obsługująca pobranie danych z poprzedniej sceny i zapisanie ich do zmiennej {@code userData}
	 * oraz wyświetlenie listy wszystkich firm, w których zalogowany użytkownik jest zatrudniony.
	 *
	 * @param userData dane pobrane z poprzedniej sceny
	 * @param loggedUserId id zalogowanego użytkownika
	 */
	public void transferMessage(String[] userData, int loggedUserId)
	{
		this.userData = userData;
		this.userId = loggedUserId;
		showJobs();
	}
	
	/**
	 * Funkcja obsługująca wyświetlenie firm, w których pracował zalogowany użytkownik.
	 */
	private void showJobs()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				getJobs(connection, userData[0], userData[1]);
				connection.close();
			}
			catch (SQLException sqlException)
			{
				sqlException.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja pobierająca z bazy listę firm w których zatrudniony jest aktualnie zalogowany użytkownik i wyświetlająca je jako listę.
	 * @param connection połączenie z bazą
	 * @param firstName  imię zalogowanego użytkownika
	 * @param lastName   nazwisko zalogowanego użytkownika
	 */
	private void getJobs(Connection connection, String firstName, String lastName)
	{
		if (connection != null)
		{
			try
			{
				itemsJobs.clear();
				String statement = "select nazwa_firmy from projekt.lista_prac where imie=? and nazwisko=?";
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				preparedStatement.setString(1, firstName);
				preparedStatement.setString(2, lastName);
				ResultSet resultSet = preparedStatement.executeQuery();
				
				while (resultSet.next())
				{
					itemsJobs.add(resultSet.getString(1));
				}
				jobsList.setItems(itemsJobs);
				
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
	 * Funkcja obsługująca dodawanie firmy, w której zatrudniony jest zalogowany użytkownik.
	 */
	public void addUsersJob()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				addJob(connection);
				connection.close();
			}
			catch (SQLException sqlException)
			{
				sqlException.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja obsługująca dodawanie firmy.
	 * @param connection połączenie z bazą
	 */
	private void addJob(Connection connection)
	{
		if (connection != null)
		{
			try
			{
				String jobName = inputJob.getText();
				
				// przynajmniej jedno słowo mogące zawierać znaki diakrytyczne, kolejne słowa oddzielane spacją lub -
				String regex = "([a-zA-Z0-9\\p{L}]+[\\s-]?)*";
				
				if(jobName != null && jobName.matches(regex))
				{
					String statement = "insert into projekt.prace (nazwa_firmy) values (?)";
					PreparedStatement preparedStatement = connection.prepareStatement(statement);
					preparedStatement.setString(1, jobName);
					preparedStatement.executeUpdate();
					
					statement = "select id_prace from projekt.znajdz_id_pracy where nazwa_firmy=?";
					preparedStatement = connection.prepareStatement(statement);
					preparedStatement.setString(1, jobName);
					ResultSet resultSet = preparedStatement.executeQuery();
					resultSet.next();
					int jobId = resultSet.getInt(1);
					resultSet.close();
					preparedStatement.close();
					
					String statement1 = "insert into projekt.uzytkownicy_prace (id_uzytkownicy, id_prace) values (?, ?)";
					PreparedStatement preparedStatement1 = connection.prepareStatement(statement1);
					preparedStatement1.setInt(1, userId);
					preparedStatement1.setInt(2, jobId);
					preparedStatement1.executeUpdate();
					preparedStatement1.close();
					connection.close();
					
					showJobs();
				}
				else
				{
					showAlert("Podano niepoprawne wartości w nazwie firmy.");
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
