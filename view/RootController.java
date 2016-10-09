package fr.emiage.b213.view;

import fr.emiage.b213.MainApp;

import javafx.fxml.FXML;

import javafx.scene.control.MenuItem;

/**
 * Classe controller pour la scene root
 * @author Mathieu Febvay
 *
 */

public class RootController 
{
	@FXML
	private MenuItem preferencesItem;
	
	private MainApp mainApp;
	
	/**
	 * Default construtor
	 */
	public RootController()
	{
		
	}	
	
	public void setMainApp(MainApp mainApp)
	{
		this.mainApp = mainApp;
	}
	
	@FXML
	public void handlePreferences()
	{
		mainApp.showPreferences();
	}
	
}
