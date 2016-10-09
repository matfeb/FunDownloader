package fr.emiage.b213.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fr.emiage.b213.MainApp;
import fr.emiage.b213.model.Course;
import fr.emiage.b213.model.Login;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
/**
 * Controller pour la scene de connexion
 * @author Mathieu
 *
 */
public class LoginController implements Initializable
{
	@FXML
	private TextField mailAddressTF;
	
	@FXML
	private PasswordField passwordTF;
	
	@FXML
	private Button connectionBTN;
	
	@FXML
	private ProgressIndicator progress;
	
	@FXML
	private CheckBox saveMailAddress;
	
	private MainApp mainApp;
	
	/**
	 * Default construtor
	 */
	public LoginController()
	{
	}
	
	/**
	 * Handle connection button OnClick
	 */
	@FXML
	private void handleConnection()
	{
		// On va sauvegarder l'adresse mail de l'utilisateur dans le registre système si il a coché la case
		Preferences prefs = Preferences.systemNodeForPackage(PreferencesController.class);
		try
		{
			if (saveMailAddress.isSelected())
			{
				prefs.put("Login", mailAddressTF.getText());
				prefs.putBoolean("SaveMail", true);
			}
			else
			{
				prefs.put("Login", "");
				prefs.putBoolean("SaveMail", false);
			}
		}
		// Le programme doit être executé en mode administrateur pour pouvoir accéder au registre du système
		catch (SecurityException e)
		{
			Alert alert = new Alert(AlertType.ERROR);
	        alert.initOwner(mainApp.getPrimaryStage());
	        alert.setTitle("Erreur de connexion");
	        alert.setHeaderText("Désolé, une erreur est survenue");
	        alert.setContentText("Veuillez lancer l'application avec des privilèges administrateur");

	        alert.showAndWait();
		}
		
		Login login = new Login(mailAddressTF.getText(), passwordTF.getText());
		if(login.funConnection())   
		{
			mainApp.setCookie(login.getCookie());
			
			// Pour ne pas bloqué l'affichage on va télécharger tous les liens dans une tâche à part
			Task<List<Course>> task = new Task<List<Course>>() 
			{
				List<Course> tabCourseTmp;
				String cookieTmp;
				
				@Override
				protected List<Course> call() throws Exception 
				{
					try
					{	    	
						// URL of Form
					     URL urlDashboard = new URL("https://www.fun-mooc.fr/dashboard");
					    
					    // URL connection channel
					     HttpsURLConnection urlConnDashboard = (HttpsURLConnection) urlDashboard.openConnection();
					    
					    // Let the run-time system (RTS) know that we want input
					    urlConnDashboard.setDoInput (true);
					    		    
					    // No caching, we want live stream
					    urlConnDashboard.setUseCaches (false);
					    
					    // Set the request method
					    urlConnDashboard.setRequestMethod("GET");
					    		   
					    // Specify the content type (Cookie properties)
					    urlConnDashboard.setRequestProperty("Content-Type", "text/html; charset=utf-8");
					    urlConnDashboard.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36");
					    urlConnDashboard.setRequestProperty("Cookie", this.cookieTmp); 
					    urlConnDashboard.setRequestProperty("HTTPS", "1");
					    urlConnDashboard.setRequestProperty("Connection", "keep-alive");
					    urlConnDashboard.setRequestProperty("Host", "https://www.fun-mooc.fr/");
					    urlConnDashboard.setRequestProperty("Referer", "https://www.fun-mooc.fr//login?next=/dashboard");
					    urlConnDashboard.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
					    urlConnDashboard.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4");		    
					    
					    // Get source code. 
					    BufferedReader inputDashboard = new BufferedReader(new InputStreamReader(urlConnDashboard.getInputStream ()));
					    
					    String str = "";
					    StringBuffer htmlDashboard = new StringBuffer();
					    
					    // While text exists, get it
					    while (null != ((str = inputDashboard.readLine())))
					    {
						    htmlDashboard.append(str);
					    }
					 
					    inputDashboard.close ();
					    urlConnDashboard.disconnect();
					    
					    String utf8HtmlDashboard = new String(htmlDashboard.toString().getBytes(), "UTF-8");
					    
					    String formatedUrl;
						Document doc = Jsoup.parse(utf8HtmlDashboard);
						Elements courses = doc.select("div.wrapper-course-details > h3 > a");
								
						tabCourseTmp = new ArrayList<Course>(courses.size());
						int max = courses.size();
						System.out.println(courses.size() + " cours à télécharger");
						int i = 0;
						for (Element link : courses) 
						{		        
						    if (link.attr("href").endsWith("info"))
						    {	
						    	formatedUrl = "https://www.fun-mooc.fr/" + link.attr("href").replace("info", "courseware");
						    	tabCourseTmp.add(new Course(link.text(), formatedUrl, this.cookieTmp));
						    	tabCourseTmp.get(i).initiateSubcourses();
						    	i++;
						    	updateProgress(i, max);	    
						    }
						}	 
						System.out.println(tabCourseTmp.size() + " tableaux de cours créés");
				
					}
					catch (MalformedURLException me)
				    {
						System.err.println("MalformedURLException: " + me);
				    }
					catch (IOException ioe)
				    {
						System.err.println("IOException: " + ioe.getMessage());
				    }
					
					return tabCourseTmp;
				}
				
				public Task<List<Course>> init(String cookieTmp)
				{
					this.cookieTmp = cookieTmp;
					return this;
				}
			}.init(login.getCookie());
			
			// Quand la tâche est finie
			task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
				@Override
			    public void handle(WorkerStateEvent t) 
				{
					mainApp.setTabCourse(task.getValue());					
					mainApp.initRootLayout();
			    }
			});
			
			Thread th = new Thread(task);
			th.setDaemon(true);
			
			progress.progressProperty().bind(task.progressProperty());
			progress.setVisible(true);
			connectionBTN.setDisable(true);
			mailAddressTF.setDisable(true);
			passwordTF.setDisable(true);
			saveMailAddress.setDisable(true);
			th.start();
		}
		// Si l'adresse email ou le mot de passe n'est pas correct on affiche une boite de dialogue
		else
		{
			System.out.println("Connexion échouée");
			Alert alert = new Alert(AlertType.ERROR);
	        alert.initOwner(mainApp.getPrimaryStage());
	        alert.setTitle("Erreur de connexion");
	        alert.setHeaderText("Désolé, une erreur est survenue");
	        alert.setContentText("Vérifiez l'adresse e-mail et/ou le mot de passe");

	        alert.showAndWait();
	        
	        mailAddressTF.setText("");
	        passwordTF.setText("");
		}
	}
	// Fonction qui s'execute juste après avoir chargé les FXML plus haut
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		Preferences prefs = Preferences.systemNodeForPackage(PreferencesController.class);
		if(prefs.get("Login", null) != "" && (prefs.getBoolean("SaveMail", false) != false))
		{
			mailAddressTF.setText(prefs.get("Login", null));
			saveMailAddress.setSelected(true);
		}
		else
		{
			mailAddressTF.setText("");
			saveMailAddress.setSelected(false);
		}
	}
	
	public void setMainApp(MainApp mainApp)
	{
		this.mainApp = mainApp;
	}

	
	
	
}
