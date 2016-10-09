package fr.emiage.b213.view;


import java.util.ArrayList;
import java.util.List;

import fr.emiage.b213.model.Course;
import fr.emiage.b213.model.Entity;
import fr.emiage.b213.model.Media;
import fr.emiage.b213.model.Subcourse;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
/**
 * Classe de contrôle pour la scene contenant les cours/sous-cours/vidéos
 * @author Mathieu Febvay
 */

public class CourseOverviewController
{
	// @FXML permer de faire le lien entre le fichier de vue fxml et le controller (cf. SceneBuilder)
	@FXML
	private TreeTableView<Entity> courseTable;
	
	@FXML
	private TreeTableColumn<Entity, String> courseColumn;
	
	@FXML
	private TreeTableColumn<Entity, Button> downloadColumn;
	
	@FXML
	private TreeTableColumn<Entity, ProgressBar> progressColumn;
	
	private List<Course> tabCourse;
		
	/**
	 * Default construtor
	 */
	public CourseOverviewController()
	{
		
	}	
	
	public void setTabCourse(List<Course> tabCourse)
	{
		this.tabCourse = tabCourse;
	}

	// Initialisation du controller
	public void init()
	{
		if (this.tabCourse == null)
		{
			System.out.println("Problème d'initialisation du tableau de cours");
		}
		else
		{			
			// On définit d'abord un noeud racine qui ne sera pas afficher et sur lequel on va accrocher les autres noeuds
			TreeItem<Entity> rootNode = new TreeItem<>(new Entity());
			int i = 0;
			
			ArrayList<TreeItem<Entity>>  treeItemCourse = new ArrayList<>();
			
			for (Course course : tabCourse)
			{
				ArrayList<TreeItem<Entity>>  treeItemSubcourse = new ArrayList<>();
				
				// On créer un item dans le noeud
				treeItemCourse.add(new TreeItem<>(course));
				
				if(course.getLstSubcourse() != null)
				{				
					int j = 0;
				
					for (Subcourse subcourse : course.getLstSubcourse())
					{
						ArrayList<TreeItem<Entity>>  treeItemMedia = new ArrayList<>();
						
						// On va créer un item dans le noeud
						treeItemSubcourse.add(new TreeItem<>(subcourse));
						
						if (subcourse.getLstMedia() != null)
						{
							int k = 0;
							
							for (Media media : subcourse.getLstMedia())
							{
								// On créer un item dans le noeud
								treeItemMedia.add(new TreeItem<>(media));
								// On accroche les noeuds des vidéos aux noeuds des sous-cours
								treeItemSubcourse.get(j).getChildren().add(treeItemMedia.get(k));
								k++;
							}							
						}
						// On accroche les noeuds des sous cours au noeuds des cours
						treeItemCourse.get(i).getChildren().add(treeItemSubcourse.get(j));
						j++;
					}
					// On accroche les noeuds des cours au noeud racine
					rootNode.getChildren().add(treeItemCourse.get(i));
					i++;
				}
			}
			 
			// On demande d'afficher les valeurs des cellules en fonction de ce contiennet les objets
			
			// Ici on récupère le bouton de Entity donc pour chaque cours/sous-cours/vidéos
			downloadColumn.setCellValueFactory(param -> {
				final TreeItem<Entity> item = param.getValue();
				final Entity data = item.getValue();
				return new SimpleObjectProperty<>(((Entity) data).getButton());
			});
			
			// Ici on récupère le nom de Entity donc pour chaque cours/sous-cours/vidéos
			courseColumn.setCellValueFactory(param -> {
				final TreeItem<Entity> item = param.getValue();
				final Entity data = item.getValue();
				return new SimpleStringProperty(data.getName());
			});
			
			// Ici on récupère la barre de progression de Entity donc pour chaque cours/sous-cours/vidéos
			progressColumn.setCellValueFactory(param -> {
				final TreeItem<Entity> item = param.getValue();
				final Entity data = item.getValue();
				return new SimpleObjectProperty<>(((Entity) data).getProgress());
			});
			
			// On définit le noeud root
			courseTable.setRoot(rootNode);
			// On n'affiche pas le noeud root
			courseTable.setShowRoot(false);
		}
	}	
}
