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
 * Klasa odpowiadająca za <b>strony</b> użytkownika - wyświetlanie listy oraz dodawanie nowych.
 */
public class pagesController extends Controller
{
	/**
	 * Główny panel sceny.
	 */
	@FXML
	BorderPane borderPane;
	
	/**
	 * Lista stron użytkownika.
	 */
	@FXML
	ListView<String> pagesList;
	
	/**
	 * Pole wprowadzania nazwy nowej strony.
	 */
	@FXML
	TextField inputPage;
	
	/**
	 * Pole wprowadzania kategorii nowej strony.
	 */
	@FXML
	TextField inputCategory;
	
	/**
	 * Zmienna nasłuchująca zmian w liście.
	 */
	ObservableList<String> itemsPages = FXCollections.observableArrayList();
	
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
	 * oraz wyświetlenie listy stron.
	 * @param userData dane pobrane z poprzedniej sceny
	 * @param loggedUserId id zalogowanego użytkownika
	 */
	public void transferMessage(String[] userData, int loggedUserId)
	{
		this.userData = userData;
		this.userId = loggedUserId;
		showAllPages();
	}
	
	/**
	 * Funkcja obsługująca wyświetlenie stron zapisanych w bazie wraz z kategoriami i autorami.
	 */
	private void showAllPages()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				getPages(connection);
				connection.close();
			}
			catch (SQLException sqlException)
			{
				sqlException.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja pobierająca z bazy listę stron i wyświetlająca je jako listę.
	 * @param connection połączenie z bazą
	 */
	private void getPages(Connection connection)
	{
		if (connection != null)
		{
			try
			{
				itemsPages.clear();
				String statement = "select * from projekt.lista_stron";
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				ResultSet resultSet = preparedStatement.executeQuery();
				
				while (resultSet.next())
				{
					itemsPages.add(resultSet.getString(1) + ", kategoria: " + resultSet.getString(2) + ", autor: "
								   + resultSet.getString(3) + " " + resultSet.getString(4));
				}
				pagesList.setItems(itemsPages);
				
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
	 * Funkcja obsługująca dodawanie strony przez zalogowanego użytkownika.
	 */
	public void addUserPage()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				addPage(connection);
				connection.close();
			}
			catch (SQLException sqlException)
			{
				sqlException.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja obsługująca dodawanie strony.
	 * @param connection połączenie z bazą
	 */
	private void addPage(Connection connection)
	{
		if (connection != null)
		{
			try
			{
				String pageName = inputPage.getText();
				String pageCategory = inputCategory.getText();
				
				// przynajmniej jedno słowo mogące zawierać znaki diakrytyczne, cyfry oraz nawiasy okrągłe, kolejne słowa oddzielane spacją ' " _ / , lub -
				String regex = "([a-zA-Z0-9\\p{L}]+[\\s-/'\",_()]{0,3})*";
				
				if (pageName != null && pageCategory != null && pageName.matches(regex) && pageCategory.matches(regex))
				{
					String statement = "insert into projekt.strony (nazwa_strony, kategoria) values (?, ?)";
					PreparedStatement preparedStatement = connection.prepareStatement(statement);
					preparedStatement.setString(1, pageName);
					preparedStatement.setString(2, pageCategory);
					preparedStatement.executeUpdate();
					
					statement = "select id_strony from projekt.znajdz_id_strony where nazwa_strony=? and kategoria=?";
					preparedStatement = connection.prepareStatement(statement);
					preparedStatement.setString(1, pageName);
					preparedStatement.setString(2, pageCategory);
					ResultSet resultSet = preparedStatement.executeQuery();
					resultSet.next();
					int pageId = resultSet.getInt(1);
					resultSet.close();
					preparedStatement.close();
					
					String statement1 = "insert into projekt.uzytkownicy_strony (id_uzytkownicy, id_strony) values (?, ?)";
					PreparedStatement preparedStatement1 = connection.prepareStatement(statement1);
					preparedStatement1.setInt(1, userId);
					preparedStatement1.setInt(2, pageId);
					preparedStatement1.executeUpdate();
					preparedStatement1.close();
					connection.close();
					
					showAllPages();
				}
				else
				{
					showAlert("Podano niepoprawne wartości w nazwie strony.");
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
