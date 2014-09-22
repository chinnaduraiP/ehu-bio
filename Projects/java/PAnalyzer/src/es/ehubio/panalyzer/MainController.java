package es.ehubio.panalyzer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class MainController implements Initializable {
	@FXML private TextArea textSummary;
	@FXML private Label labelSignature;
	@FXML private Label labelStatus;
	@FXML private TreeView<String> treeExperiment;
	@FXML private Button buttonAdd;
	@FXML private Button buttonClear;
	@FXML private Button buttonLoad;
	private final FileChooser fileChooser = new FileChooser();
	private final List<String> files = new ArrayList<>();
	
	private final MainModel model;
	private final Stage view;
	
	public enum Status { INIT, READY, LOADED, RESULTS};
	private Status status = Status.INIT;
	private final Map<Status, Set<Node>> mapNodes = new HashMap<>();
	private final Set<Node> listNodes = new HashSet<>();
	
	public MainController( MainModel model, Stage view ) {
		this.model = model;
		this.view = view;
	}
	
	@FXML private void handleAddFiles( ActionEvent event ) {
		List<File> files = fileChooser.showOpenMultipleDialog(view);
		if( files == null )
			return;
		for( File file : files ) {
			TreeItem<String> item = new TreeItem<>(file.getName());
			treeExperiment.getRoot().getChildren().add(item);
			this.files.add(file.getAbsolutePath());
		}
		status = Status.READY;
		updateStatus();
	}
	
	@FXML private void handleClearFiles( ActionEvent event ) {
		treeExperiment.getRoot().getChildren().clear();
		files.clear();
		status = Status.INIT;
		updateStatus();
	}
	
	@FXML private void handleLoadFiles( ActionEvent event ) {
		status = Status.LOADED;
		updateStatus();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		fileChooser.setTitle("Load identification files");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(
			new FileChooser.ExtensionFilter("mzIdentML", "*.mzid", "*.mzid.gz"));
		
		TreeItem<String> rootItem = new TreeItem<>("MyExperiment");
		rootItem.setExpanded(true);
		treeExperiment.setEditable(true);
		treeExperiment.setRoot(rootItem);
		treeExperiment.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
			@Override
			public TreeCell<String> call(TreeView<String> param) {
				return new TextFieldTreeCell();
			}
		});
		
		labelSignature.setText(MainModel.SIGNATURE);
		
		enable(buttonAdd,Status.INIT,Status.READY);
		enable(buttonClear,Status.READY,Status.LOADED,Status.RESULTS);
		enable(buttonLoad,Status.READY);
		
		updateStatus();		
	}
	
	private void enable( Node node, Status... states ) {
		for( Status status : states ) {
			Set<Node> list = mapNodes.get(status);
			if( list == null ) {
				list = new HashSet<>();
				mapNodes.put(status, list);
			}
			list.add(node);
		}
		listNodes.add(node);
	}
	
	private void updateStatus() {
		textSummary.setText(model.getLog());
		labelStatus.setText(model.getStatus());
		for( Node node : listNodes )
			node.setDisable(true);
		for( Node node : mapNodes.get(status) )
			node.setDisable(false);
	}
}