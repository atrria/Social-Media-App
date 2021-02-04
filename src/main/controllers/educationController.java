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
 * Klasa odpowiadająca za <b>wykształcenie</b> użytkownika - wyświetlanie listy oraz dodawanie nowych pozycji.
 */
public class educationController extends Controller
{
	/**
	 * Główny panel sceny.
	 */
	@FXML
	BorderPane borderPane;
	
	/**
	 * Lista szkół, do których uczęszczał użytkownik.
	 */
	@FXML
	ListView<String> educationList;
	
	/**
	 * Pole wprowadzania nazwy nowej szkoły.
	 */
	@FXML
	TextField inputSchoolName;
	
	/**
	 * Pole wprowadzania typu nowej szkoły.
	 */
	@FXML
	TextField inputSchoolType;
	
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
	ObservableList<String> itemsEducation = FXCollections.observableArrayList();
	
	/**
	 * Funkcja obsługująca pobranie danych z poprzedniej sceny i zapisanie ich do zmiennej {@code userData}
	 * oraz wyświetlenie listy wszystkich szkół, do których uczęszczał zalogowany użytkownik oraz ich typów.
	 * @param userData dane pobrane z poprzedniej sceny
	 * @param loggedUserId id zalogowanego użytkownika
	 */
	public void transferMessage(String[] userData, int loggedUserId)
	{
		this.userData = userData;
		this.userId = loggedUserId;
		showEudcation();
	}
	
	/**
	 * Funkcja obsługująca wyświetlenie szkół, do których uczęszczał zalogowany użytkownik i ich typów.
	 */
	private void showEudcation()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				getEducation(connection);
				connection.close();
			}
			catch (SQLException sqlException)
			{
				sqlException.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja pobierająca z bazy listę szkół, do których uczęszczał aktualnie zalogowany użytkownik oraz ich typów i wyświetlająca je jako listę.
	 * @param connection połączenie z bazą
	 */
	private void getEducation(Connection connection)
	{
		if (connection != null)
		{
			try
			{
				itemsEducation.clear();
				String statement = "select nazwa_szkoly, typ_szkoly from projekt.lista_szkol where id_uzytkownicy=?";
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				preparedStatement.setInt(1, userId);
				ResultSet resultSet = preparedStatement.executeQuery();
				
				while (resultSet.next())
				{
					itemsEducation.add(resultSet.getString(2) + ": " + resultSet.getString(1));
				}
				educationList.setItems(itemsEducation);
				
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
	 * Funkcja obsługująca dodawanie szkoły, do której uczęszczał zalogowany użytkownik i jej typu.
	 */
	public void addUserEducation()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				addEducation(connection);
				connection.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja obsługująca dodawanie szkoły i jej typu.
	 * @param connection połączenie z bazą
	 */
	private void addEducation(Connection connection)
	{
		if (connection != null)
		{
			try
			{
				String schoolName = inputSchoolName.getText();
				String schoolType = inputSchoolType.getText();
				
				// przynajmniej jedno słowo mogące zawierać litery diakrytyczne, kolejne słowa oddzielane spacją lub -
				String regex = "([a-zA-Z0-9\\p{L}]+[\\s-]?)*";
				
				if(schoolName!=null && schoolType!=null && schoolName.matches(regex) && schoolType.matches(regex))
				{
					String statement = "insert into projekt.szkoly (nazwa_szkoly, typ_szkoly) values (?, ?)";
					PreparedStatement preparedStatement = connection.prepareStatement(statement);
					preparedStatement.setString(1, schoolName);
					preparedStatement.setString(2, schoolType);
					preparedStatement.executeUpdate();
					
					statement = "select id_szkoly from projekt.szkoly where nazwa_szkoly=? and typ_szkoly=?";
					preparedStatement = connection.prepareStatement(statement);
					preparedStatement.setString(1, schoolName);
					preparedStatement.setString(2, schoolType);
					ResultSet resultSet = preparedStatement.executeQuery();
					resultSet.next();
					int schoodId = resultSet.getInt(1);
					resultSet.close();
					preparedStatement.close();
					
					String statement1 = "insert into projekt.uzytkownicy_szkoly (id_uzytkownicy, id_szkoly) values (?, ?)";
					PreparedStatement preparedStatement1 = connection.prepareStatement(statement1);
					preparedStatement1.setInt(1, userId);
					preparedStatement1.setInt(2, schoodId);
					preparedStatement1.executeUpdate();
					preparedStatement1.close();
					
					showEudcation();
					connection.close();
				}
				else
				{
					showAlert("Niepoprawne wartości w nazwie lub typie szkoły.");
				}
			}
			catch (SQLException e)
			{
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
			e.printStackTrace();
		}
	}
}
