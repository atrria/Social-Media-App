package main.controllers;

import javafx.scene.control.Alert;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Klasa bazowa implementująca funkcje używane w każdym kontrolerze.
 */
public class Controller
{
	/**
	 * Prywatna, statyczna zmienna przechowująca URL bazy.
	 */
	private static String dbaseURL;
	
	/**
	 * Prywatna, statyczna zmienna przechowująca login do bazy.
	 */
	private static String username;
	
	/**
	 * Prywatna, statyczna zmienna przechowująca hasło do bazy.
	 */
	private static String password;
	
	/**
	 * Funkcja pobierająca URL, login i hasło do bazy z pliku konfiguracyjnego.
	 */
	public void getProperties()
	{
		try
		{
			InputStream propertiesInputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
			Properties properties = new Properties();
			properties.load(propertiesInputStream);
			dbaseURL = properties.getProperty("dbaseURL");
			username = properties.getProperty("username");
			password = properties.getProperty("password");
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
		}
	}
	
	/**
	 * Funkcja pokazująca alert z podaną wiadomością ostrzeżenia.
	 *
	 * @param warningMessage zawartość wiadomości ostrzeżenia
	 */
	public void showAlert(String warningMessage)
	{
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Ostrzeżenie");
		alert.setHeaderText("Ostrzeżenie");
		alert.setContentText(warningMessage);
		alert.showAndWait();
	}
	
	/**
	 * Funkcja obsługująca haszowanie hasła algorytmem MD5.
	 *
	 * @param pass podane hasło w oryginalnej postaci
	 * @return zahaszowane hasło
	 */
	protected String hashPassword(String pass)
	{
		// TODO SHA256 instead of MD5
		String hashPass = "";
		
		try
		{
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(pass.getBytes());
			byte[] messageDigestBytes = messageDigest.digest();
			
			BigInteger bigInteger = new BigInteger(1, messageDigestBytes);
			
			hashPass = bigInteger.toString(16);
			
			while (hashPass.length() < 32)
			{
				hashPass = "0" + hashPass;
			}
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		
		return hashPass;
	}
	
	/**
	 * Funkcja tworząca i zwracająca połączenie z bazą danych w przypadku powodzenia.
	 *
	 * @return połączenie z bazą w przypadku powodzenia, null w przeciwnym wypadku
	 */
	protected Connection establishConnection()
	{
		Connection connection = null;
		try
		{
			connection = DriverManager.getConnection(dbaseURL, username, password);
		}
		catch (SQLException se)
		{
			System.out.println("Couldn't connect: print out a stack trace and exit.");
			se.printStackTrace();
			System.exit(1);
		}
		return connection;
	}
}
