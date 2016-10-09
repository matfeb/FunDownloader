package fr.emiage.b213.view;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import fr.emiage.b213.MainApp;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
/**
 * Controller pour la scene des préférences
 * @author Mathieu Febvay
 *
 */
public class PreferencesController implements Initializable 
{	
	@FXML
	private RadioButton radioButtonLowQuality;
	
	@FXML
	private RadioButton radioButtonMediumQuality;
	
	@FXML
	private RadioButton radioButtonHighQuality;
	
	@FXML
	private TextField textFieldDestination;
	
	@FXML
	private Button browseDestinationPath;
	
	@FXML
	private Button cancelPreferences;
	
	@FXML
	private Button savePreferences;
	
	private MainApp mainApp;
	private Stage stage;
	
	/**
	 * Default construtor
	 */
	public PreferencesController()
	{
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) 
	{
		ToggleGroup group = new ToggleGroup();
		radioButtonLowQuality.setToggleGroup(group);
		radioButtonMediumQuality.setToggleGroup(group);
		radioButtonHighQuality.setToggleGroup(group);
		switch (getVideoQuality())
		{
			case "hd":
				radioButtonHighQuality.setSelected(true);
				break;
			case "md":
				radioButtonMediumQuality.setSelected(true);
				break;
			case "ld":
				radioButtonLowQuality.setSelected(true);
				break;
			default:
				radioButtonMediumQuality.setSelected(true);
				break;
		}
		if (getFilePath() != null)
		{
			textFieldDestination.setText(getFilePath());
		}
		else
		{
			textFieldDestination.setText(System.getProperty("user.dir"));
		}
	}
	
	public void setMainApp(MainApp mainApp)
	{
		this.mainApp = mainApp;
	}
	
	public void setStage(Stage stage)
	{
		this.stage = stage;
	}
	
	// Quand l'utilisateur va cliquer sur "Enregistrer"
	@FXML
	public void handleSave()
	{
		Preferences prefs = Preferences.systemNodeForPackage(PreferencesController.class);
		
		prefs.put("filePath", textFieldDestination.getText());
		if (radioButtonHighQuality.isSelected())
		{
			prefs.put("videoQuality", "hd");
		}
		else if (radioButtonMediumQuality.isSelected())
		{
			prefs.put("videoQuality", "md");
		}
		else if (radioButtonLowQuality.isSelected())
		{
			prefs.put("videoQuality", "ld");
		}
		stage.close();
	}
	
	// Quand l'utilisateur va cliquer sur "Annuler"
	@FXML
	public void handleCancel()
	{
		stage.close();
	}
	
	// Quand l'utilisateur va cliquer pour rechercher un dossier de destination par défault
	@FXML
	public void handleBrowse()
	{
		DirectoryChooser pathFileChooser = new DirectoryChooser();
		pathFileChooser.setTitle("Sélectionnez l'emplacement de destination");
	
		File file = pathFileChooser.showDialog(mainApp.getPrimaryStage());
		
		if ((file != null) && file.isDirectory())
		{
			textFieldDestination.setText(file.getAbsolutePath());
		}
	}
	
	// On récupère les préférences de l'utilisateur concernant le chemin de destination
	public String getFilePath()
	{
		Preferences prefs = Preferences.systemNodeForPackage(PreferencesController.class);
		String filePath = prefs.get("filePath", null);
		if (filePath != null)
		{
			return filePath;
		}
		else
		{
			return new String("");
		}
	}
	
	// On récupère les préférences de l'utilisateur concernant la qualité des vidéos
	public String getVideoQuality()
	{
		Preferences prefs = Preferences.systemNodeForPackage(PreferencesController.class);
		String videoQuality = prefs.get("videoQuality", null);
		if (videoQuality != null)
		{
			return videoQuality;
		}
		else
		{
			return new String("md");
		}
	}
	
	

	
	
}
