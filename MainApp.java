package fr.emiage.b213;

/**
 * @author Mathieu Febvay
 * Classe principale de lancement du programme et du stage
 * EMIAGE B213
 * 
 */


import java.io.IOException;
import java.util.List;

import fr.emiage.b213.model.Course;
import fr.emiage.b213.view.CourseOverviewController;
import fr.emiage.b213.view.LoginController;
import fr.emiage.b213.view.PreferencesController;
import fr.emiage.b213.view.RootController;
import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainApp extends Application 
{
	private Stage 				primaryStage;
	private BorderPane 			loginLayout;
	private BorderPane 			rootLayout;
	private AnchorPane 			courseOverview;
	private AnchorPane			preferencesLayout;
	private List<Course> 		tabCourse;
	private String 				cookie;

	/**
	 * Lancement du stage principal
	 */
	@Override
	public void start(Stage primaryStage) 
	{
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("FUN MOOC");
		
		// Je lance l'invite de connexion
		initLoginLayout();
	}
	
	/**
	 * Ecran de connexion
	 */
	public void initLoginLayout()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/LoginLayout.fxml"));
            loginLayout = (BorderPane)loader.load();

            // Show the scene containing the login layout.
            Scene scene = new Scene(loginLayout);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
            
            // Je définie un controller pour cette vue
            LoginController loginControl = loader.getController();
            loginControl.setMainApp(this);
        } 
		catch (IOException e) 
		{
            e.printStackTrace();
        }
	}
	
	/**
	 * Scene root qui est l'affichage qui vient après la connexion
	 * Elle contient un menu et une scene au centre qui va contenir les cours et les fonctions
	 */
	public void initRootLayout()
	{		
		try
		{
			FXMLLoader rootLoader = new FXMLLoader();
			FXMLLoader courseloader = new FXMLLoader();
            rootLoader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane)rootLoader.load();
            courseloader.setLocation(MainApp.class.getResource("view/CourseOverview.fxml"));
            courseOverview = (AnchorPane)courseloader.load();
            
            // J'insère directement la scene des cours sur la scene root et je la place au milieur car root est un layout BorderPane
            rootLayout.setCenter(courseOverview); 
            
            // J'assigne mes controller pour gérer les événements
            CourseOverviewController courseControl = courseloader.getController();
            
            RootController rootControl = rootLoader.getController();
            rootControl.setMainApp(this); 
            
            // Si l'initialisation s'est mal faite il y a eu un problème donc je ne lance pas cette scene
            if (this.tabCourse != null)
            {
            	courseControl.setTabCourse(this.getTabCourse());
            	courseControl.init();
            }
            
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
            
        } 
		catch (IOException e) 
		{
            e.printStackTrace();
        }
	}
	
	/**
	 * La scene des préfèrences, même principe que les autres scenes
	 */
	public void showPreferences()
	{		
		try 
		{
			FXMLLoader preferencesLoader = new FXMLLoader();
			preferencesLoader.setLocation(MainApp.class.getResource("view/PreferencesLayout.fxml"));
			preferencesLayout = (AnchorPane)preferencesLoader.load();
					
			Stage preferencesStage = new Stage();
			preferencesStage.setResizable(false);
			preferencesStage.setTitle("Préfèrences utilisateur");
			preferencesStage.initModality(Modality.WINDOW_MODAL);
			preferencesStage.initOwner(primaryStage);
			
			Scene scene = new Scene(preferencesLayout);
			preferencesStage.setScene(scene);
			
			PreferencesController prefControl = preferencesLoader.getController();
			prefControl.setStage(preferencesStage);
			prefControl.setMainApp(this);
			
			// J'attends une réponse de la part de l'utilisateur lorsque cette scene est affichée
			preferencesStage.showAndWait();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
		
	public Stage getPrimaryStage() 
	{
        return primaryStage;
    }
	
	/**
	 * Cookie qui vient du serveur FUN suite à l'authentifation
	 * @param cookie
	 */
	public void setCookie(String cookie)
	{
		this.cookie = cookie;
	}
	
	/**
	 * Cookie qui vient du serveur FUN suite à l'authentifation
	 * @return cookie
	 */
	public String getCookie()
	{
		return this.cookie;
	}

	/**
	 * La liste des cours auquel l'utilisateur est inscrit
	 * @return the tabCourse
	 */
	public List<Course> getTabCourse() 
	{
		return tabCourse;
	}

	/**
	 * La liste des cours auquel l'utilisateur est inscrit
	 * @param tabCourse the tabCourse to set
	 */
	public void setTabCourse(List<Course> tabCourse) 
	{
		this.tabCourse = tabCourse;
	}

	/**
	 * Point d'entrée du programme
	 * @param args
	 */
	public static void main(String[] args) 
	{		
		launch(args);
	}	
}
