package fr.emiage.b213.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;

/**
 * Classe générique dont Course, Subcourse et Media vont hériter
 * Cette classe est nécessaire pour le TreeTableView qui ne prend qu'un unique type pour l'affichage
 * il s'agit donc de la seule solution trouvée pour l'instant (ou alors il fallait prendre un type Object)
 * 
 * @author Mathieu Febvay
 *
 */
public class Entity 
{
	 private final StringProperty 	name;
	 private String				 	url;
	 private String 				cookie;
	 private Button					btn;
	 private ProgressBar			progressBar;
	
	 public Entity()
	 {
		 this(null, null, null);
	 }
	 
	 public Entity(String name)
	 {
		 this(name, null, null);
	 }
	 
	 public Entity(String name, String url)
	 {
		 this(name, url, null);
	 }
	 
	 public Entity(String name, String url, String cookie)
	 {
		 this.name = new SimpleStringProperty(name);
		 this.url = url;
		 this.cookie = cookie;
		 this.btn = new Button("Télécharger");
		 this.progressBar = new ProgressBar();
		 this.progressBar.setVisible(false);
	 }
	 
	 public String getName() 
    {
        return name.get();
    }

    public void setName(String pName) 
    {
        this.name.set(pName);
    }

    public StringProperty nameProperty()
    {
        return name;
    } 
    
    public String getUrl() 
    {
        return url;
    }

    public void setUrl(String pUrl) 
    {
        this.url = pUrl;
    }
    
    public String getCookie()
    {
    	return this.cookie;
    }
    
    public void setCookie(String cookie)
    {
    	this.cookie = cookie;
    }
    
    public Button getButton()
    {
    	return this.btn;
    }
    
    public ProgressBar getProgress()
    {
    	return this.progressBar;
    }
}
