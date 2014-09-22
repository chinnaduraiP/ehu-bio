package es.ehubio.panalyzer;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
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
	@FXML private Button buttonReset;
	@FXML private Button buttonFilter;
	@FXML private Button buttonSave;
	@FXML private Tab tabFilter;
	@FXML private Tab tabResults;
	@FXML private Tab tabBrowser;
	private final FileChooser fileChooser = new FileChooser();
	private final Set<String> files = new HashSet<>();
	
	private final MainModel model;
	private final Stage view;
	
	public enum Status { INIT, READY, LOADED, RESULTS, SAVED};
	private Status status = Status.INIT;
	private final Map<Status, Set<Object>> mapNodes = new HashMap<>();
	private final Set<Object> listNodes = new HashSet<>();
	
	public MainController( MainModel model, Stage view ) {
		this.model = model;
		this.view = view;
	}
	
	@FXML private void handleAddFiles( ActionEvent event ) {
		List<File> files = fileChooser.showOpenMultipleDialog(view);
		if( files == null )
			return;
		for( File file : files ) {
			if( !this.files.add(file.getAbsolutePath()) )
				continue;
			TreeItem<String> item = new TreeItem<>(file.getName());
			treeExperiment.getRoot().getChildren().add(item);			
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
	
	@FXML private void handleResetFilter( ActionEvent event ) {
	}
	
	@FXML private void handleApplyFilter( ActionEvent event ) {
		status = Status.RESULTS;
		updateStatus();
	}
	
	@FXML private void handleSaveFiles( ActionEvent event ) {
		status = Status.SAVED;
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
		
		enable(treeExperiment,Status.INIT,Status.READY);
		enable(buttonAdd,Status.INIT,Status.READY);
		enable(buttonClear,Status.READY,Status.LOADED,Status.RESULTS,Status.SAVED);
		enable(buttonLoad,Status.READY);
		enable(tabFilter,Status.LOADED,Status.RESULTS,Status.SAVED);
		enable(tabFilter.getContent(),Status.LOADED,Status.RESULTS);
		enable(tabResults,Status.RESULTS,Status.SAVED);
		enable(tabResults.getContent(),Status.RESULTS,Status.SAVED);
		enable(tabBrowser,Status.SAVED);		
		
		updateStatus();		
	}
	
	private void enable( Object node, Status... states ) {
		for( Status status : states ) {
			Set<Object> list = mapNodes.get(status);
			if( list == null ) {
				list = new HashSet<>();
				mapNodes.put(status, list);
			}
			list.add(node);
		}
		listNodes.add(node);
	}
	
	private void disable( Object object, boolean disabled ) {
		try {
			Method method = object.getClass().getMethod("setDisable", boolean.class);
			method.invoke(object, disabled);
		} catch( Exception e ) {			
		}
	}
	
	private void updateStatus() {
		textSummary.setText(model.getLog());
		labelStatus.setText(model.getStatus());
		for( Object node : listNodes )
			disable(node,true);
		for( Object node : mapNodes.get(status) )
			disable(node,false);
	}	
}