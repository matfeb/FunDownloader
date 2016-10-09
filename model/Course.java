package fr.emiage.b213.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fr.emiage.b213.view.PreferencesController;

/**
 * Classe modèle d'un cours
 *
 * @author Mathieu Febvay
 */
public class Course extends Entity
{
    private List<Subcourse>	lstSubCourse; 

    /**
     * Default constructor.
     */
    public Course() 
    {
        super(null, null, null);
    }

    /**
     * Constructor with some initial data.
     * 
     * @param name
     * @param url
     * @param cookie
     */
    public Course(String name, String url, String cookie) 
    {
        super(name, url, cookie);
        this.getButton().setText("Télécharger le cours entier");
        
        // Action lorsque l'utilisateur clique sur le bouton -> téléchargement de l'ensemble des vidéos (cf. download())
        this.getButton().setOnAction((event) -> {
         		
    		this.download();	
        });
    }
    
    /**
     * Initialisation des sous-cours (par semaine sur le site FUN), même principe que pour un cours
     */
    public void initiateSubcourses()
    {
    	try
		{		    	
    		// URL of Form
		    URL urlCourse = new URL(this.getUrl());
		    
		    // URL connection channel
		    HttpsURLConnection urlConnCourse = (HttpsURLConnection) urlCourse.openConnection();
		    
		    if(urlConnCourse.getHeaderField("Location") != null)
		    {
			    urlCourse = new URL(urlConnCourse.getHeaderField("Location").replace("http", "https"));
			    
			    HttpsURLConnection urlConnCourseHTTPS = (HttpsURLConnection) urlCourse.openConnection();
			    
			    urlConnCourse.disconnect();
			    
			    // Let the run-time system (RTS) know that we want input
			    urlConnCourseHTTPS.setDoInput (true);
			    		    
			    // No caching, we want live stream
			    urlConnCourseHTTPS.setUseCaches (false);
			    
			    // Set the request method
			    urlConnCourseHTTPS.setRequestMethod("GET");
			    	   
			    // Specify the content type (Cookie properties)
			    urlConnCourseHTTPS.setRequestProperty("Content-Type", "text/html; charset=utf-8");
			    urlConnCourseHTTPS.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36");
			    urlConnCourseHTTPS.setRequestProperty("Cookie", this.getCookie()); 
			    urlConnCourseHTTPS.setRequestProperty("HTTPS", "1");
			    urlConnCourseHTTPS.setRequestProperty("Connection", "keep-alive");
			    urlConnCourseHTTPS.setRequestProperty("Host", "https://www.fun-mooc.fr");
			    urlConnCourseHTTPS.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			    urlConnCourseHTTPS.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4");		    
			    
			    // Get source code. 
			    BufferedReader inputCourse = new BufferedReader(new InputStreamReader(urlConnCourseHTTPS.getInputStream ()));
			    String str = "";
			    StringBuffer htmlDashboard = new StringBuffer();
			    
			    // While text exists, get it
			    while (null != ((str = inputCourse.readLine())))
			    {
				    htmlDashboard.append(str);
			    }
			 
			    inputCourse.close ();
			    urlConnCourseHTTPS.disconnect();
			    
			    String utf8HtmlDashboard = new String(htmlDashboard.toString().getBytes(), "UTF-8");
			    
			    // Appel à la lib JSoup qui permet de parser un flux html (cf. code source du site FUN dans un navigateur)
			    Document doc = Jsoup.parse(utf8HtmlDashboard);
				Elements subcourses = doc.select("div.chapter > h3");
				
				lstSubCourse = new ArrayList<Subcourse>(subcourses.size());
				
				int i = 0;
				
				 for (Element link : subcourses) 
				 {
					 lstSubCourse.add(new Subcourse(link.attr("aria-label")));
					 List<Media> lstMedia = new ArrayList<Media>();
				 
					 for (Element chapter : doc.select("div.chapter:eq(" + i + ") > ul > li > a"))
					 {
						 lstMedia.add(new Media(chapter.select("p").first().text(), chapter.attr("href"), this.getCookie()));			 
					 }
					 lstSubCourse.get(i).setLstMedia(lstMedia);
					 i++;
					 
				 }
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
		
    }
     
    public List<Subcourse> getLstSubcourse()
    {
    	return this.lstSubCourse;
    }
    
    public void setLstSubcourse(List<Subcourse> lstSubcourse)
    {
    	this.lstSubCourse = lstSubcourse;
    }   
    
    /**
     * Téléchargement de toutes les vidéos du cours
     * 
     */
    public void download()
    {
    	Preferences pref = Preferences.systemNodeForPackage(PreferencesController.class);
    	
    	String dest = pref.get("filePath", System.getProperty("user.dir")) + File.separator + checkFileName(this.getName());
    	
    	File dirCourse = new File(dest);
		dirCourse.mkdir();
		
		for (Subcourse subcourse : lstSubCourse)
		{
			subcourse.download(dest);
		}
    }
    
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
}
