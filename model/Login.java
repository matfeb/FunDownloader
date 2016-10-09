package fr.emiage.b213.model;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Classe pour la connexion et la récupération d'un cookie sur le serveur du site FUN
 * 
 * @author Mathieu Febvay
 *
 */
public class Login 
{
	
	private final StringProperty mailAddress;
	private final StringProperty password;
	
	// Ces liens sont suceptibles de variation
	// Dernière mise à jour des liens 17/02/2016
	private String				startURLAddress = new String("https://www.fun-mooc.fr/");
	private String				loginURLAddress = new String("https://www.fun-mooc.fr/login_ajax");
	private String 				setCookie;
	private String 				csrfToken;
	private DataOutputStream  	printout;
	private BufferedReader      inputLogin;	
	
	/**
	 * Default constructor
	 */
	public Login ()
	{
		this(null,null);
	}
	
	/**
	 * Constructor with custom data
	 * 
	 * @param mailAddress
	 * @param password
	 */
	public Login (String mailAddress, String password)
	{
		this.mailAddress = new SimpleStringProperty(mailAddress);
		this.password = new SimpleStringProperty(password);
	}
	
	/**
	 * Get the mail address 
	 * 
	 * @return mailAddress
	 */	
	public String getMailAddress ()
	{
		return this.mailAddress.get();
	}
	
	/**
	 * Get the password
	 * 
	 * @return password
	 */
	public String getPassword ()
	{
		return this.password.get();
	}
	
	/**
	 * Get the mail address StringProperty
	 * @return mailAddress
	 */
	public StringProperty getMailAddressProperty()
	{
		return this.mailAddress;
	}	
	
	/**
	 * Get the password StringProperty
	 * 
	 * @return password
	 */
	public StringProperty getPasswordProperty()
	{
		return this.password;
	}
	
	/**
	 * Set the email address
	 * 
	 * @param mailAddress
	 */
	public void setMailAddress(String mailAddress)
	{
		this.mailAddress.set(mailAddress);
	}
	
	/**
	 * Set the password
	 * 
	 * @param password
	 */
	public void setPassword (String password)
	{
		this.password.set(password);
	}
	
	/**
	 * @return cookie
	 */
	public String getCookie() 
	{
		return setCookie;
	}

	/**
	 * @return csrfToken
	 */
	public String getCsrfToken() 
	{
		return this.csrfToken;
	}
	
	/**
	 * Connect to the FUN server
	 * 
	 * @return login state
	 */
	public boolean funConnection()
	{
		boolean connectionSuccess = false;

		try
		{						
			URL urlStart = new URL(this.startURLAddress);
			URL urlLogin = new URL(this.loginURLAddress);
		    
		    // URL connection channel
			HttpsURLConnection urlConnStart = (HttpsURLConnection)urlStart.openConnection();
		    
		    CookieHandler.setDefault(new CookieManager());
		    
		    urlConnStart.connect();
		    
		    String headerName = null;
		    
		    // For each connection http response field
		    for (int i=1; (headerName = urlConnStart.getHeaderFieldKey(i))!=null; i++) 
		    {
		    	// Save cookie's token
		    	// On recherche dans le HTTP Header (qui est une chaine de caractère) la clé "Set-Cookie" et on récupère la valeur 
		    	if (headerName.equals("Set-Cookie"))
		    	{
		    		// On extrait la clé
		    		if ((urlConnStart.getHeaderField(i).substring(0, urlConnStart.getHeaderField(i).indexOf("="))).equals("csrftoken"))
		    		{
		    			setCookie = urlConnStart.getHeaderField(i);
		    			csrfToken = setCookie.substring(setCookie.indexOf("=")+1, setCookie.indexOf(";"));
		    		}			    		
		    	}		
		    }
		    
		    urlConnStart.disconnect();
		    
		    // URL connection channel
		    HttpsURLConnection urlConnLogin = (HttpsURLConnection)urlLogin.openConnection();
		    
		    // Let the run-time system (RTS) know that we want input
		    urlConnLogin.setDoInput (true);
		    
		    // Let the RTS know that we want to do output
		    urlConnLogin.setDoOutput (true);
		    
		    // No caching, we want live stream
		    urlConnLogin.setUseCaches (false);;   
		   	
		    // Set the request method
		    urlConnLogin.setRequestMethod("POST");
		    		   
		    // Specify the content type (Cookie properties)
		    urlConnLogin.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		    urlConnLogin.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36");
		    urlConnLogin.setRequestProperty("Cookie", this.setCookie); 
		    urlConnLogin.setRequestProperty("X-CSRFToken", this.csrfToken);
		    urlConnLogin.setRequestProperty("Connection", "keep-alive");
		    urlConnLogin.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		    urlConnLogin.setRequestProperty("Host", "https://www.fun-mooc.fr/");
		    urlConnLogin.setRequestProperty("Origin", "https://www.fun-mooc.fr/");
		    urlConnLogin.setRequestProperty("Referer", "https://www.fun-mooc.fr/login");
		    
		    // Open the output stream
		    printout = new DataOutputStream (urlConnLogin.getOutputStream ());
		    
		    // Set the string to send
		    String content = null;
		    if ((this.getMailAddress() != null) && (this.getPassword() != null))
		    {
		    	// On définie les données à envoyer au serveur
		    	content = new String("email=" + URLEncoder.encode(this.getMailAddress(), "UTF-8") + "&password=" + URLEncoder.encode(this.getPassword(), "UTF-8" ));		   
		    }
		    
		    // On envoie les données au serveur
		    printout.writeBytes (content);
		    printout.flush ();
   
		    // Close the output stream
		    printout.close ();	
		    
		    // Open the input stream
		    inputLogin = new BufferedReader(new InputStreamReader(urlConnLogin.getInputStream ()));
		    String str;
		    
		    // While text exists, display it.
		    while (null != ((str = inputLogin.readLine())))
		    {
			    if (str.contains("\"success\": true"))	    	
			    {
			    	connectionSuccess = true;
			    }
		    }	
		    
		    // Close the input stream
		    inputLogin.close ();
		    urlConnLogin.disconnect();
		}
		catch (MalformedURLException me)
	    {
			System.err.println("MalformedURLException: " + me);
			me.printStackTrace();
	    }
		catch (IOException ioe)
	    {
			System.err.println("IOException: " + ioe.getMessage());
	    }
		
		return connectionSuccess;
	}
}
