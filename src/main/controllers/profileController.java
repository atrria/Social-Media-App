package main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.sql.*;

/**
 * Klasa odpowiadająca za <b>profil</b> użytkownika - wyświetlanie informacji o nim oraz funkcjonalność w postaci sześciu przycisków przenoszących do innych scen:
 * <ol>
 *     <li>wyświetlania oraz wyszukiwania wśród znajomych,</li>
 *     <li>wyświetlania oraz dołączania do grup,</li>
 *     <li>wyszukiwania użytkowników oraz dodawania od znajomych,</li>
 *     <li>wyświetlania oraz dodawania nowego wykształcenia,</li>
 *     <li>wyświetlania oraz dodawania nowego zatrudnienia,</li>
 *     <li>wyświetlania oraz dodawania nowych stron.</li>
 * </ol>
 * W scenie obsługiwanej przez tę klasę oraz w kolejnych zawarto ikony z serwisu www.Flaticon.com.
 * Icons made by <a href="https://www.flaticon.com/authors/freepik">Freepik</a> from <a href="https://www.flaticon.com/">www.flaticon.com</a>
 */
public class profileController extends Controller
{
	/**
	 * Główny panel sceny.
	 */
	@FXML
	BorderPane borderPane;
	
	/**
	 * Tekst wyświetlany na profilu, zawierający imię oraz nazwisko użytkownika.
	 */
	@FXML
	Text userNameLabel;
	
	/**
	 * Tekst wyświetlany na profilu, zawierający datę urodzenia oraz miasto użytkownika.
	 */
	@FXML
	Text userDateCityLabel;
	
	/**
	 * Tekst wyświetlany na profilu, zawierający opis użytkownika.
	 */
	@FXML
	Text userBioLabel;
	
	/**
	 * Tekst wyświetlany na profilu, zawierający licznik znajomych użytkownika.
	 */
	@FXML
	Text userFriendsCounter;
	
	/**
	 * Ikona edycji (ołówek) wyświetlana na w prawym dolnym rogu zdjęcia profilowego.
	 */
	@FXML
	ImageView editButton;
	
	/**
	 * Imię aktualnie zalogowanego użytkownika.
	 */
	private String loggedUserFirstName;
	
	/**
	 * Nazwisko aktualnie zalogowanego użytkownika.
	 */
	private String loggedUserLastName;
	
	/**
	 * Id zalogowanego użytkownika.
	 */
	private int loggedUserId;
	
	/**
	 * Zmienna przechowująca imię i nazwisko użytkownika będącego właścicielem aktualnego profilu.
	 */
	private String[] userData;
	
	/**
	 * Zmienna przechowująca datę urodzenia, miasto oraz opis użytkownika będącego właścicielem aktualnego profilu.
	 */
	private String[] userInfo;
	
	/**
	 * Funkcja inicjalizująca wartości pól tekstowych o użytkowniku.
	 */
	public void init()
	{
		userNameLabel.setText(userData[0] + " " + userData[1]);
		String[] userInfo = getUserInfo();
		userDateCityLabel.setText(userInfo[0] + ", " + userInfo[1]);
		userBioLabel.setText(userInfo[2]);
		getUserId();
		getUserFriendsCounter();
		
		if (!userData[0].equals(loggedUserFirstName) || !userData[1].equals(loggedUserLastName))
		{
			// funkcjonalność możliwa do łatwego wprowadzenia w przyszłości - wyświetlanie profili innych użytkowników niż zalogowany
			editButton.setVisible(false);
		}
	}
	
	/**
	 * Funkcja zliczająca odpowiednim zapytaniem znajomych użytkownika oraz wyświetlająca wynik na profilu.
	 */
	private void getUserFriendsCounter()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				String statement = "select count(*) from projekt.uzytkownicy "
								   + "where uzytkownicy.id_uzytkownicy "
								   + "in (select id_znajomi from projekt.uzytkownik_lewo "
								   + "where id_uzytkownicy = ?)"
								   + "or uzytkownicy.id_uzytkownicy "
								   + "in (select id_uzytkownicy "
								   + "from projekt.uzytkownik_prawo "
								   + "where id_znajomi = ?)";
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				preparedStatement.setInt(1, loggedUserId);
				preparedStatement.setInt(2, loggedUserId);
				ResultSet resultSet = preparedStatement.executeQuery();
				resultSet.next();
				
				userFriendsCounter.setText(userFriendsCounter.getText() + resultSet.getInt(1));
				
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
	 * Funkcja pobierająca z bazy id zalogowanego użytkownika.
	 */
	private void getUserId()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				String statement = "select id_uzytkownicy from projekt.znajdz_uzytkownikow where imie=? and nazwisko=?";
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				preparedStatement.setString(1, loggedUserFirstName);
				preparedStatement.setString(2, loggedUserLastName);
				ResultSet resultSet = preparedStatement.executeQuery();
				resultSet.next();
				
				loggedUserId = resultSet.getInt(1);
				
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
	 * Funkcja zwracająca datę urodzenia, miasto oraz opis użytkownika pobrane z bazy.
	 * @param connection połączenie z bazą
	 * @param firstName  imię zalogowanego użytkownika
	 * @param lastName   nazwisko zalogowanego użytkownika
	 * @return tablica z datą urodzenia, miastem i opisem użytkownika
	 */
	private String[] getInfo(Connection connection, String firstName, String lastName)
	{
		String[] userInfo = {};
		if (connection != null)
		{
			try
			{
				try
				{
					String statement = "select data_urodzenia, miasto, opis from projekt.znajdz_info_uzytkownika where imie=? and nazwisko=?";
					PreparedStatement preparedStatement = connection.prepareStatement(statement);
					preparedStatement.setString(1, firstName);
					preparedStatement.setString(2, lastName);
					ResultSet resultSet = preparedStatement.executeQuery();
					resultSet.next();
					
					userInfo = new String[]{resultSet.getString(1), resultSet.getString(2), resultSet.getString(3)};
					
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
		return userInfo;
	}
	
	/**
	 * Funkcja tworząca połączenie z bazą i pobierająca z niej informacje o użytkowniku przy użyciu funkcji {@link #getInfo(Connection, String, String)}.
	 * @return tablica z datą urodzenia, miastem i opisem użytkownika
	 */
	private String[] getUserInfo()
	{
		Connection connection = establishConnection();
		if (connection != null)
		{
			try
			{
				userInfo = getInfo(connection, userData[0], userData[1]);
				connection.close();
			}
			catch (SQLException sqlException)
			{
				sqlException.printStackTrace();
			}
		}
		return userInfo;
	}
	
	/**
	 * Funkcja obsługująca pobranie danych z poprzedniej sceny i zapisanie ich do zmiennej {@code userData}
	 * oraz zainicjowanie szczegółów profilu.
	 * @param transferData dane pobrane z poprzedniej sceny
	 * @param loggedUserFirstName imię zalogowanego użytkownika
	 * @param loggedUserLastName nazwisko zalogowanego użytkownika
	 */
	public void transferMessage(String[] transferData, String loggedUserFirstName, String loggedUserLastName)
	{
		userData = transferData;
		this.loggedUserFirstName = loggedUserFirstName;
		this.loggedUserLastName = loggedUserLastName;
		init();
	}
	
	/**
	 * Funkcja obsługująca przycisk przenoszący do widoku znajomych.
	 */
	public void friendsButton()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../ui/view/friendsPage.fxml"));
			BorderPane newPane = loader.load();
			friendsController controller = loader.getController();
			controller.transferMessage(userData, loggedUserId);
			borderPane.getChildren().setAll(newPane);
		}
		catch (IOException e)
		{
			System.out.println("Problem ładowania sceny. " + e);
		}
	}
	
	/**
	 * Funkcja obsługująca przycisk przenoszący do widoku grup.
	 */
	public void groupsButton()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../ui/view/groupsPage.fxml"));
			BorderPane newPane = loader.load();
			groupsController controller = loader.getController();
			controller.transferMessage(userData, loggedUserId);
			borderPane.getChildren().setAll(newPane);
		}
		catch (IOException e)
		{
			System.out.println("Problem ładowania sceny. " + e);
		}
	}
	
	/**
	 * Funkcja obsługująca przycisk przenoszący do widoku obserwowania.
	 */
	public void followButton()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../ui/view/followPage.fxml"));
			BorderPane newPane = loader.load();
			followController controller = loader.getController();
			controller.transferMessage(userData, loggedUserId);
			borderPane.getChildren().setAll(newPane);
		}
		catch (IOException e)
		{
			System.out.println("Problem ładowania sceny. " + e);
		}
	}
	
	/**
	 * Funkcja obsługująca przycisk przenoszący do widoku wyszukiwania.
	 */
	public void searchButton()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../ui/view/searchPage.fxml"));
			BorderPane newPane = loader.load();
			searchController controller = loader.getController();
			controller.transferMessage(userData, loggedUserId);
			borderPane.getChildren().setAll(newPane);
		}
		catch (IOException e)
		{
			System.out.println("Problem ładowania sceny. " + e);
		}
	}
	
	/**
	 * Funkcja obsługująca przycisk przenoszący do widoku edycji informacji o użytkowniku,
	 * widocznego wyłącznie dla profilu zalogowanego użytkownika.
	 */
	public void editProfileButton()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../ui/view/editUserPage.fxml"));
			BorderPane newPane = loader.load();
			editUserController controller = loader.getController();
			controller.transferMessage(userData, loggedUserId);
			borderPane.getChildren().setAll(newPane);
		}
		catch (IOException e)
		{
			System.out.println("Problem ładowania sceny. " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Funkcja obsługująca przycisk przenoszący do widoku edukacji.
	 */
	public void educationButton()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../ui/view/educationPage.fxml"));
			BorderPane newPane = loader.load();
			educationController controller = loader.getController();
			controller.transferMessage(userData, loggedUserId);
			borderPane.getChildren().setAll(newPane);
		}
		catch (IOException e)
		{
			System.out.println("Problem ładowania sceny. " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Funkcja obsługująca przycisk przenoszący do widoku zatrudnienia.
	 */
	public void jobButton()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../ui/view/jobPage.fxml"));
			BorderPane newPane = loader.load();
			jobController controller = loader.getController();
			controller.transferMessage(userData, loggedUserId);
			borderPane.getChildren().setAll(newPane);
		}
		catch (IOException e)
		{
			System.out.println("Problem ładowania sceny. " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Funkcja obsługująca przycisk przenoszący do widoku stron.
	 */
	public void pagesButton()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../ui/view/pagesPage.fxml"));
			BorderPane newPane = loader.load();
			pagesController controller = loader.getController();
			controller.transferMessage(userData, loggedUserId);
			borderPane.getChildren().setAll(newPane);
		}
		catch (IOException e)
		{
			System.out.println("Problem ładowania sceny. " + e);
			e.printStackTrace();
		}
	}
}
