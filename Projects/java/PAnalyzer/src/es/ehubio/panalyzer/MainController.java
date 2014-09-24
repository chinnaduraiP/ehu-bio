package es.ehubio.panalyzer;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeView.EditEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import es.ehubio.proteomics.ScoreType;

public class MainController implements Initializable {
	private static final Logger logger = Logger.getLogger(MainController.class.getName());
	
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
	@FXML private TextField textDecoy;
	@FXML private ChoiceBox<ScoreType> choiceScoreType;
	private final FileChooser fileChooser = new FileChooser();	
	
	private final Configuration config = new Configuration();
	private final MainModel model;
	private final Stage view;
	
	private final Set<String> files = new HashSet<>();
	private final Map<MainModel.State, Set<Object>> mapNodes = new HashMap<>();
	private final Set<Object> listNodes = new HashSet<>();
	private final StringWriter logString = new StringWriter();	
	
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
		if( model.getConfig() == null )
			model.setConfig(config);
		config.setInputs(this.files);
		updateView();
	}
	
	@FXML private void handleClearFiles( ActionEvent event ) {
		treeExperiment.getRoot().getChildren().clear();
		files.clear();
		model.reset();
		updateView();
	}
	
	@FXML private void handleLoadFiles( ActionEvent event ) {
		String decoy = textDecoy.getText().trim();
		textDecoy.setText(decoy);
		config.setDecoyRegex(decoy.length()==0?null:decoy);
		model.loadData();
		choiceScoreType.getItems().clear();
		choiceScoreType.getItems().addAll(model.getPsmScoreTypes());
		choiceScoreType.getSelectionModel().selectFirst();
		updateView();
	}
	
	@FXML private void handleResetFilter( ActionEvent event ) {
	}
	
	@FXML private void handleApplyFilter( ActionEvent event ) {
		updateView();
	}
	
	@FXML private void handleSaveFiles( ActionEvent event ) {
		updateView();
	}
	
	@FXML private void handleTreeEdit( EditEvent<String> event ) {
		config.setDescription(treeExperiment.getRoot().getValue());
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Handler handler = new LogHandler();
		Logger logger = Logger.getLogger(MainModel.class.getName());
		logger.addHandler(handler);
		MainController.logger.addHandler(handler);
		
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
		textDecoy.setText("decoy");
		
		enableByStates(treeExperiment,MainModel.State.INIT,MainModel.State.CONFIGURED);
		enableByStates(buttonAdd,MainModel.State.INIT,MainModel.State.CONFIGURED);
		enableByStates(buttonClear,MainModel.State.CONFIGURED,MainModel.State.LOADED,MainModel.State.RESULTS,MainModel.State.SAVED);
		enableByStates(buttonLoad,MainModel.State.CONFIGURED);
		enableByStates(textDecoy,MainModel.State.CONFIGURED);
		enableByStates(tabFilter,MainModel.State.LOADED,MainModel.State.RESULTS,MainModel.State.SAVED);
		enableByStates(tabFilter.getContent(),MainModel.State.LOADED,MainModel.State.RESULTS);
		enableByStates(tabResults,MainModel.State.RESULTS,MainModel.State.SAVED);
		enableByStates(tabResults.getContent(),MainModel.State.RESULTS,MainModel.State.SAVED);
		enableByStates(tabBrowser,MainModel.State.SAVED);
		
		showWelcome();
		
		updateView();		
	}
	
	private void enableByStates( Object node, MainModel.State... states ) {
		for( MainModel.State status : states ) {
			Set<Object> list = mapNodes.get(status);
			if( list == null ) {
				list = new HashSet<>();
				mapNodes.put(status, list);
			}
			list.add(node);
		}
		listNodes.add(node);
	}
	
	private void disableObject( Object object, boolean disabled ) {
		try {
			Method method = object.getClass().getMethod("setDisable", boolean.class);
			method.invoke(object, disabled);
		} catch( Exception e ) {			
		}
	}
	
	private void updateView() {
		textSummary.setText(logString.toString());
		labelStatus.setText(model.getStatus());
		for( Object node : listNodes )
			disableObject(node,true);
		for( Object node : mapNodes.get(model.getState()) )
			disableObject(node,false);
	}
	
	private static void showWelcome() {
		StringWriter string = new StringWriter();
		PrintWriter pw = new PrintWriter(string);
		pw.println(String.format("--- Welcome to %s ---\n", MainModel.SIGNATURE));
		pw.println("In a normal execution you should follow these steps:");
		pw.println("1. Load experiment file(s) in the 'Experiment' tab");
		pw.println("2. Apply some quality criteria in the 'Filter' tab");
		pw.println("3. Check and export the results in the 'Results' tab");
		pw.println("4. Browse the results using the integrated 'Browser' tab or your favorite web browser outside this application");
		pw.close();
		logger.info(string.toString());
	}
	
	private final class LogHandler extends Handler {
		@Override
		public void publish(LogRecord record) {
			if( !isLoggable(record) )
				return;
			synchronized(log) {
				if( record.getLevel().equals(Level.INFO) )
					log.println(record.getMessage());
				else
					log.println(String.format("%s: %s", record.getLevel(), record.getMessage()));
			}
		}

		@Override
		public void flush() {
			log.flush();
		}

		@Override
		public void close() throws SecurityException {
			log.close();
		}
		
		private final PrintWriter log = new PrintWriter(logString);
	}
}