package fr.emiage.b213.model;

import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

import fr.emiage.b213.view.PreferencesController;

/**
 * Classe modèle d'un sous-cours
 * @author Mathieu Febvay
 *
 */
public class Subcourse extends Entity
{
    private List<Media>	lstMedia;

    /**
     * Default constructor.
     */
    public Subcourse() 
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
    public Subcourse(String name) 
    {
        super(name);
        this.getButton().setText("Télécharger la semaine de cours entière");
        
        this.getButton().setOnAction((event) -> {
        	    		
    		this.download();		
        });
    }
        
    public List<Media> getLstMedia()
    {
    	return this.lstMedia;
    }
    
    public void setLstMedia(List<Media> lstMedia)
    {
    	this.lstMedia = lstMedia;
    }      
    
    /**
     * Téléchargement en cliquant sur le bouton du sous-cours
     */
    public void download()
    {
    	Preferences pref = Preferences.systemNodeForPackage(PreferencesController.class);
    	
    	String dest = pref.get("filePath", System.getProperty("user.dir")) + File.separator + checkFileName(this.getName());
    	
    	File dirSubcourse = new File(dest);
		dirSubcourse.mkdir();
		
		for (Media media : this.getLstMedia())
		{
			media.setDest(dest);
			media.download();
		}
    }
    
    /**
     * Telechargement en ayant cliqué sur le bouton du cours
     * @param dest
     */
    public void download(String dest)
    {
    	String destSubcourse = dest + File.separator + checkFileName(this.getName());
    	
    	File dirSubcourse = new File(destSubcourse);
    	dirSubcourse.mkdir();
    	
    	for (Media media : this.getLstMedia())
		{
			media.setDest(destSubcourse);
    		media.download();
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
