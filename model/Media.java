package fr.emiage.b213.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fr.emiage.b213.view.PreferencesController;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 * Modèle de classe pour une vidéo
 * @author Mathieu Febvay
 *
 */
public class Media extends Entity
{
	private String dest = null;
	
    /**
     * Default constructor.
     */
    public Media() 
    {
        super(null, null, null);
    }

    /**
     * Constructor with some initial data.
     * 
     * @param name
     * @param id
     * @param tabSubCourses
     */
    public Media(String name, String url, String cookie) 
    {
        super(name, url, cookie);
        this.getButton().setText("Télécharger la ou les vidéos");
        
        this.getButton().setOnAction((event) -> {
        	
        	this.getButton().setText("En cours...");
    		this.getButton().setDisable(true);
        });
    }
    
    public String getDest()
    {
    	return this.dest;
    }
    
    public void setDest(String dest)
    {
    	this.dest = dest;
    }
    
    /**
     * Téléchargment de la vidéo si cela se fait en cliquant sur le bouton de la vidéo alors dest = null 
     * sinon on lui passe en paramètre le chemin de destination
     */
    public void download()
    {
    	// On définit une tâche externe au processus graphique pour ne pas bloquer l'IHM
    	Task<String> task = new Task<String>() 
		{
    		String urlTmp;
			String cookieTmp;
			String nameTmp;
			String destTmp;
			
			@Override
			protected String call() throws Exception 
			{
				String videoNameTmp = null;
				
				try
				{	    						
					// URL of Form
				     URL urlDashboard = new URL("https://www.fun-mooc.fr/" + this.urlTmp);
				    
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
				    
				    // Je me permets de rendre plus propre le code
				    String formatedSrc = utf8HtmlDashboard.replace("&lt;", "<");
				    formatedSrc = formatedSrc.replace("&gt;", ">");
				    formatedSrc = formatedSrc.replace("&#34;", "\"");
				    
				    // On utilise JSoup pour scrapper le code source et trouver les liens des vidéos
					Document doc = Jsoup.parse(formatedSrc);
					Elements courses = doc.select("div.videoplayer");
					System.out.println("Nombre de vidéos pour ce cours : " + courses.size());
					
					if (courses.size() > 0)
					{	
						int startIndex = 0;
						int endIndex = 0;
					
						for (Element link : courses) 
						{		  
							startIndex = link.select("source").attr("src").indexOf("https://fun.libcast.com/resource/");
							endIndex = link.select("source").attr("src").indexOf("/flavor/video/", startIndex);
							
							// Le if est uniquement là pour une question de confort visuel sur le progressBar (cf. infra dans downloadVideo)
							// Dans ce cas on se base sur le nombre de vidéos à télécharger alors que dans l'autre on se base sur la taille de la vidéo
							if (courses.size() > 1)
							{
								int i = 0;
					            int max = courses.size();
					            
								if (startIndex != -1 && endIndex != -1)
								{
									videoNameTmp = link.select("source").attr("src").substring(startIndex + 33, endIndex);
									
									// On récupère les préférences enregistrées dans le registre
									Preferences pref = Preferences.systemNodeForPackage(PreferencesController.class);
									if (destTmp != null)
									{
										downloadVideo(videoNameTmp, pref.get("videoQuality", "md"), destTmp, courses.size());
									}
									else
									{
										downloadVideo(videoNameTmp, pref.get("videoQuality", "md"), pref.get("filePath", System.getProperty("user.dir")), courses.size());
									}
									// Pour la barre de progression
									i++;
									updateProgress(i, max);
								}
							}
							else
							{
								if (startIndex != -1 && endIndex != -1)
								{
									videoNameTmp = link.select("source").attr("src").substring(startIndex + 33, endIndex);
									// On récupère les préférences enregistrées dans le registre
									Preferences pref = Preferences.systemNodeForPackage(PreferencesController.class);
									if (destTmp != null)
									{
										downloadVideo(videoNameTmp, pref.get("videoQuality", "md"), destTmp, courses.size());
									}
									else
									{
										downloadVideo(videoNameTmp, pref.get("videoQuality", "md"), pref.get("filePath", System.getProperty("user.dir")), courses.size());
									}
								}
							}
						}	
					}
					else
					{
						videoNameTmp = null;
					}
				}
				catch (MalformedURLException me)
			    {
					System.err.println("MalformedURLException: " + me);
			    }
				catch (IOException ioe)
			    {
					System.err.println("IOException: " + ioe.getMessage());
			    }
				
				return videoNameTmp;	
			}
			
			public Task<String> init(String cookieTmp, String urlTmp, String name, String destTmp)
			{
				this.cookieTmp = cookieTmp;
				this.urlTmp = urlTmp;
				this.nameTmp = name;
				this.destTmp = destTmp;
				return this;
			}
			
			public void downloadVideo(String name, String quality, String dest, int courseSize)
		    {
		    	try 
				{
					URL url;
					url = new URL("https://fun.libcast.com/resource/" + name + "/flavor/video/fun-" + quality + ".mp4");
			
			        HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
			        int responseCode;
					responseCode = httpsConn.getResponseCode();
		 
			        // always check HTTP response code first
			        if (responseCode == HttpsURLConnection.HTTP_OK) 
			        {
			            // opens input stream from the HTTP connection
			            InputStream inputStream = httpsConn.getInputStream();

			            String saveFilePath;
			            
			            saveFilePath = dest + File.separator + checkFileName(nameTmp) + "-" + checkFileName(name) + ".mp4";
			            
			            // opens an output stream to save into file
			            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
			 
			            int bytesRead = -1;
			            byte[] buffer = new byte[4096];
			            
			            // Si il n'y a qu'une seule vidéo à télécharge le barre de progression se base sur sa taille
				        if (courseSize == 1)
				        {
				        	int i = 0;
				            int max = httpsConn.getContentLength();
				            
				            while ((bytesRead = inputStream.read(buffer)) != -1) 
				            {
				                outputStream.write(buffer, 0, bytesRead);
				                i += buffer.length;
				                updateProgress(i, max);
				            }
				        }
				        // Sinon cf plus haut
				        else
				        {
				        	while ((bytesRead = inputStream.read(buffer)) != -1) 
				            {
				                outputStream.write(buffer, 0, bytesRead);
				            }
				        }
			 
			            outputStream.close();
			            inputStream.close();
			 
			            System.out.println("Fichier téléchargé");
			        } 
			        else 
			        {
			            System.out.println("Pas de fichier à télécharger. HTTP Code : " + responseCode);
			        }
			        httpsConn.disconnect();
				}
		        catch (IOException ioe) 
		        {
		        	System.err.println("IOException: " + ioe.getMessage());
		        }     
		    }
		    
			// J'enlève les caractère qui peuvent générer à la créationd de la vidéo
		    public String checkFileName(String fileName)
		    {
		    	String checkedFileName = fileName;
		    	
		    	checkedFileName = checkedFileName.replace(":", "-");
		    	checkedFileName = checkedFileName.replace("\\", "-");
		    	checkedFileName = checkedFileName.replace("/", "-");
		    	checkedFileName = checkedFileName.replace("*", "-");
		    	checkedFileName = checkedFileName.replace("?", " ");
		    	checkedFileName = checkedFileName.replace("|", " ");
		    	checkedFileName = checkedFileName.replace("\"", "'");
		    	checkedFileName = checkedFileName.replace("<", " ");
		    	checkedFileName = checkedFileName.replace(">", " ");
		    	
		    	return checkedFileName;
		    	
		    }
		// Obligatoire pour fournir des paramètres à une tâche	
		}.init(this.getCookie(), this.getUrl(), this.nameProperty().get(), this.getDest());
		
		// A la fin de la tâche
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
			@Override
		    public void handle(WorkerStateEvent t) 
			{
				getButton().setDisable(false);
	            getButton().setText("Télécharger la ou les vidéos");
	            getProgress().setVisible(false);
		    }
		});
		
		this.getProgress().progressProperty().bind(task.progressProperty());
		this.getProgress().setVisible(true);
		Thread th = new Thread(task);
		th.setDaemon(true);
		
		th.start();
    }    
}
