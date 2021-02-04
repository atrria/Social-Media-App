package main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Klasa odpowiadająca za ekran powitalny obsługujący <b>logowanie</b> - za pomocą imienia, nazwiska i hasła.
 * Możliwe jest istnienie wyłącznie unikalnych trójek imion, nazwisk i haseł, oraz <b>rejestrację</b> - dodawanie nowego użytkownika
 * (obowiązkowe pola to imię, nazwisko, hasło, data urodzenia i miasto. Pole krótki opis jest nieobowiązkowe).
 */
public class welcomeController extends Controller
{
	/**
	 * Główny panel sceny.
	 */
	@FXML
	BorderPane borderPane;
	
	/**
	 * Widok rejestracji.
	 */
	@FXML
	VBox rejestracjaVBox;
	
	/**
	 * Widok logowania.
	 */
	@FXML
	VBox logowanieVBox;
	
	/**
	 * Pole wprowadzania imienia nowego użytkownika.
	 */
	@FXML
	TextField imieRejestracja;
	
	/**
	 * Pole wprowadzania imienia istniejącego użytkownika.
	 */
	@FXML
	TextField imieLogowanie;
	
	/**
	 * Pole wprowadzania nazwiska nowego użytkownika.
	 */
	@FXML
	TextField nazwiskoRejestracja;
	
	/**
	 * Pole wprowadzania nazwiska istniejącego użytkownika.
	 */
	@FXML
	TextField nazwiskoLogowanie;
	
	/**
	 * Pole wprowadzania hasła nowego użytkownika.
	 */
	@FXML
	PasswordField hasloRejestracja;
	
	/**
	 * Pole wprowadzania hasła istniejącego użytkownika.
	 */
	@FXML
	PasswordField hasloLogowanie;
	
	/**
	 * Pole wprowadzania daty urodzenia nowego użytkownika.
	 */
	@FXML
	TextField data;
	
	/**
	 * Pole wprowadzania miasta nowego użytkownika.
	 */
	@FXML
	TextField miasto;
	
	/**
	 * Pole wprowadzania krótkiego opisu nowego użytkownika.
	 */
	@FXML
	TextField opis;
	
	/**
	 * Widoczność widoku rejestracji.
	 */
	public void zarejestrujVBox()
	{
		rejestracjaVBox.setVisible(true);
		logowanieVBox.setVisible(false);
	}
	
	/**
	 * Widoczność widoku logowania.
	 */
	public void zalogujVBox()
	{
		logowanieVBox.setVisible(true);
		rejestracjaVBox.setVisible(false);
	}
	
	/**
	 * Funkcja wprowadzająca nowego użytkownika wraz z podanymi przez niego informacjami do bazy.
	 * Jeżeli użytkownik o podanym imieniu, nazwisku i haśle istnieje pokazywany jest alert z informacją a baza nie jest modyfikowana.
	 * @param connection połączenie z bazą
	 * @param firstName podane imię użytkownika
	 * @param lastName podane nazwisko użytkownika
	 * @param hashPass podane hasło użytkownika w zahaszowanej formie
	 * @param date podana data urodzenia użytkownika
	 * @param city podane miasto użytkownika
	 * @param bio podany opis użytkownika
	 */
	private void insertNewUser(Connection connection, String firstName, String lastName, String hashPass, Date date, String city, String bio)
	{
		if (connection != null)
		{
			try
			{
				if(!findUser(connection, firstName, lastName, hashPass))
				{
					String statement = "insert into projekt.uzytkownicy (imie, nazwisko, data_urodzenia, miasto, opis, haslo) values (?, ?, ?, ?, ?, ?)";
					PreparedStatement preparedStatement = connection.prepareStatement(statement);
					preparedStatement.setString(1, firstName);
					preparedStatement.setString(2, lastName);
					preparedStatement.setDate(3, date);
					preparedStatement.setString(4, city);
					preparedStatement.setString(5, bio);
					preparedStatement.setString(6, hashPass);
					preparedStatement.executeUpdate();
					preparedStatement.close();
					connection.close();
				}
				else
				{
					showAlert("Użytkownik o podanym imieniu i nazwisku już istnieje");
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
	 * Funkcja sprawdzająca walidację wartości wprowadzonych przez użytkownika do pól.
	 * W przypadku niepoprawnych wartości pokazywany jest alert z informacją.
	 * Jeśli wszystkie pola są poprawne użytkownik dodawany jest do bazy funkcją {@link #insertNewUser(Connection, String, String, String, Date, String, String)}.
	 * Scena zostaje zmieniona na widok zalogowanego użytkownika a jego dane logowania są do niej przekazywane.
	 */
	public void zarejestruj()
	{
		getProperties();
		if (!imieRejestracja.getText().matches("[A-ZŻŚŁ]([a-zA-Z\\p{L}]+)((\\s)?([A-ZŻŚŁ]([a-zA-Z\\p{L}]*))*)?"))
		{
			showAlert("Podano nieprawidłową wartość w polu imię");
			imieRejestracja.clear();
		}
		else if (!nazwiskoRejestracja.getText().matches("[A-ZŻŹÓŚĆŁ]([a-zA-Z\\p{L}]+)((-)?([A-ZŻŹÓŚĆŁ]([a-zA-Z\\p{L}]*))*)?"))
		{
			showAlert("Podano nieprawidłową wartość w polu nazwisko");
			nazwiskoRejestracja.clear();
		}
		else if(hasloRejestracja==null)
		{
			showAlert("Podano puste hasło");
			hasloRejestracja.clear();
		}
		else if(hasloRejestracja.getText().length()<5 || hasloRejestracja.getText().length()>30)
		{
			showAlert("Podano zbyt krótkie lub zbyt długie hasło.\nHasło powinno zawierać przynajmniej 5 ale nie więcej niż 30 znaków");
			hasloRejestracja.clear();
		}
		else if (!data.getText().matches("^(?:(?:31([/\\-.])(?:0?[13578]|1[02]|(?:Sty|Mar|Maj|Czer|Sie|Paź|Gru)))\\1"
										 + "|(?:(?:29|30)([/\\-.])(?:0?[1,3-9]|1[0-2]|(?:Sty|Mar|Kwi|Maj|Cze|Lip|Sie|Wrz|Paź|Lis|Gru))\\2))"
										 + "(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29([/\\-.])(?:0?2|(?:Feb))\\3"
										 + "(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\\d|2[0-8])"
										 + "([/\\-.])(?:(?:0?[1-9]|(?:Sty|Lut|Mar|Kwi|Maj|Cze|Lip|Sie|Wrz))|(?:1[0-2]|(?:Paź|Lis|Gru)))\\4"
										 + "(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$"))
		{
			showAlert("Podano nieprawidłową wartość w polu data");
			data.clear();
		}
		else if (!miasto.getText().matches("[A-ZŻŹÓŚĆŁ]([a-zA-Z\\p{L}]+)((-|\\s)?([A-ZŻŹÓŚĆŁ]([a-zA-Z\\p{L}]*))*){0,2}"))
		{
			showAlert("Podano nieprawidłową wartość w polu miasto");
			miasto.clear();
		}
		else
		{
			String firstName = imieRejestracja.getText();
			String lastName = nazwiskoRejestracja.getText();
			String pass = hasloRejestracja.getText();
			
			String hashPass = hashPassword(pass);
			if(hashPass.equals(""))
			{
				showAlert("Błąd hasła.");
			}
			else
			{
				Connection connection = establishConnection();
				if (connection != null)
				{
					try
					{
						if (!findUser(connection, firstName, lastName, hashPass))
						{
							SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
							insertNewUser(connection, firstName, lastName, hashPass, new Date(dateFormat.parse(data.getText()).getTime()), miasto.getText(), opis.getText());
							connection.close();
							changeScene(new String[]{firstName, lastName});
						}
						else
						{
							showAlert("Użytkownik o takim imieniu i nazwisku już istnieje.");
						}
					}
					catch (ParseException | SQLException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Funkcja sprawdzająca czy użytkownik o podanym imieniu i nazwisku znajduje się w bazie.
	 * @param connection połączenie z bazą
	 * @param firstName podane imię użytkownika
	 * @param lastName podane nazwisko użytkownika
	 * @param hashPass podane hasło użytkownika w zahaszowanej formie
	 * @return true jeśli użytkownik jest w bazie, w przeciwnym wypadku false
	 */
	private boolean findUser(Connection connection, String firstName, String lastName, String hashPass)
	{
		boolean userExists = false;
		if (connection != null)
		{
			try
			{
				String statement = "select exists (select from projekt.uzytkownicy where imie=? and nazwisko=? and haslo=?)";
				PreparedStatement preparedStatement = connection.prepareStatement(statement);
				preparedStatement.setString(1, firstName);
				preparedStatement.setString(2, lastName);
				preparedStatement.setString(3, hashPass);
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
	 * Funkcja sprawdzająca walidację wartości wprowadzonych przez użytkownika do pól.
	 * W przypadku niepoprawnych wartości pokazywany jest alert z informacją.
	 * Jeśli wszystkie pola są poprawne i użytkownik o podanych danych został odnaleziony w bazie
	 * za pomocą funkcji {@link #findUser(Connection, String, String, String)}
	 * scena zostaje zmieniona na widok zalogowanego użytkownika a jego dane logowania są do niej przekazywane.
	 */
	public void zaloguj()
	{
		getProperties();
		if (!imieLogowanie.getText().matches("[A-ZŻŚŁ]([a-zA-Z\\p{L}]+)((\\s)?([A-ZŻŚŁ]([a-zA-Z\\p{L}]*))*)?"))
		{
			showAlert("Podano nieprawidłową wartość w polu imię");
			imieLogowanie.clear();
		}
		else if (!nazwiskoLogowanie.getText().matches("[A-ZŻŚŁ]([a-zA-Z\\p{L}]+)((-)?([A-ZŻŚŁ]([a-zA-Z\\p{L}]*))*)?"))
		{
			showAlert("Podano nieprawidłową wartość w polu nazwisko");
			nazwiskoLogowanie.clear();
		}
		else if(hasloLogowanie==null)
		{
			showAlert("Podano puste hasło");
			hasloLogowanie.clear();
		}
		else
		{
			String firstName = imieLogowanie.getText();
			String lastName = nazwiskoLogowanie.getText();
			String pass = hasloLogowanie.getText();
			
			String hashPass = hashPassword(pass);
			
			if(hashPass.equals(""))
			{
				showAlert("Błąd hasła.");
			}
			else
			{
				Connection connection = establishConnection();
				if (connection != null)
				{
					try
					{
						if (findUser(connection, firstName, lastName, hashPass))
						{
							connection.close();
							changeScene(new String[]{firstName, lastName});
						}
						else
						{
							showAlert("Błędne dane logowania.\nUżytkownik o podanych danych logowania nie istnieje");
						}
					}
					catch (SQLException sqlException)
					{
						sqlException.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Funkcja obsługująca zmianę sceny na profil zalogowanego użytkownika oraz przekazanie danych do niej.
	 * @param transferData dane przekazane do nowej sceny
	 */
	public void changeScene(String[] transferData)
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../ui/view/profilePage.fxml"));
			BorderPane newPane = loader.load();
			profileController controller = loader.getController();
			controller.transferMessage(transferData, transferData[0], transferData[1]);
			borderPane.getChildren().setAll(newPane);
		}
		catch (IOException e)
		{
			System.out.println("Problem ładowania sceny. " + e);
			e.printStackTrace();
		}
	}
}
