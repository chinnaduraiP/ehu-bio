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

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeView.EditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import es.ehubio.proteomics.ScoreType;
import es.ehubio.proteomics.pipeline.PAnalyzer;

public class MainController implements Initializable {
	private static final Logger logger = Logger.getLogger(MainController.class.getName());
	
	@FXML private TextArea textSummary;
	@FXML private Label labelSignature;
	@FXML private Label labelStatus;
	@FXML private TreeView<String> treeExperiment;
	@FXML private Button buttonAdd;
	@FXML private Button buttonClear;
	@FXML private Button buttonLoad;
	@FXML private Button buttonFilter;
	@FXML private Button buttonReset;
	@FXML private Button buttonSave;
	@FXML private Tab tabFilter;
	@FXML private Tab tabResults;
	@FXML private Tab tabBrowser;
	@FXML private TextField textDecoy;
	@FXML private ChoiceBox<ScoreType> choiceScoreType;
	@FXML private Label labelRank;
	@FXML private Label labelPsmFdr;
	@FXML private Label labelPeptideLength;
	@FXML private Label labelPeptideFdr;
	@FXML private Label labelProteinFdr;
	@FXML private Label labelGroupFdr;
	@FXML private TextField textRank;	
	@FXML private CheckBox checkBestPsm;
	@FXML private TextField textPsmFdr;
	@FXML private TextField textPeptideLength;
	@FXML private TextField textPeptideFdr;
	@FXML private TextField textProteinFdr;
	@FXML private TextField textGroupFdr;
	@FXML private TextArea textResults;
	@FXML private TableView<CountBean> tableCounts;
	@FXML private TableColumn<CountBean, String> colCountType;
	@FXML private TableColumn<CountBean, Integer> colTargetCount;
	@FXML private TableColumn<CountBean, Integer> colDecoyCount;
	@FXML private TableColumn<CountBean, Integer> colTotalCount;
	@FXML private TableView<FdrBean> tableFdr;
	@FXML private TableColumn<FdrBean, String> colFdrLevel;
	@FXML private TableColumn<FdrBean, Double> colFdrValue;
	@FXML private TableColumn<FdrBean, Double> colFdrThreshold;
	@FXML private CheckBox checkFilterDecoys;
	@FXML private WebView webBrowser;
	private final FileChooser fileChooser = new FileChooser();
	private final DirectoryChooser directoryChooser = new DirectoryChooser();
	
	private Configuration config = new Configuration();
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
		config.setFilterDecoys(true);
		updateView();
	}
	
	@FXML private void handleClearFiles( ActionEvent event ) {
		treeExperiment.getRoot().getChildren().clear();
		files.clear();
		model.reset();
		updateView();
	}
	
	@FXML private void handleLoadFiles( ActionEvent event ) {
		logSeparator("Loading");
		String decoy = textDecoy.getText().trim();
		textDecoy.setText(decoy);
		config.setDecoyRegex(decoy.length()==0?null:decoy);
		model.loadData();
		choiceScoreType.getItems().clear();
		choiceScoreType.getItems().addAll(model.getPsmScoreTypes());		
		if( model.getPsmScoreTypes().contains(ScoreType.XTANDEM_EVALUE) )
			choiceScoreType.setValue(ScoreType.XTANDEM_EVALUE);
		else if( model.getPsmScoreTypes().contains(ScoreType.MASCOT_EVALUE) )
			choiceScoreType.setValue(ScoreType.MASCOT_EVALUE);
		else
			choiceScoreType.getSelectionModel().selectFirst();
		resetFilter();
		updateView();
	}
	
	@FXML private void handleApplyFilter( ActionEvent event ) {
		logSeparator("Filtering");
		try {
			config.setPsmScore(choiceScoreType.getValue());
			config.setPsmRankThreshold(tryInteger(textRank, labelRank));
			config.setBestPsmPerPrecursor(checkBestPsm.isSelected());
			config.setPsmFdr(tryDouble(textPsmFdr, labelPsmFdr));
			config.setMinPeptideLength(tryInteger(textPeptideLength, labelPeptideLength));
			config.setPeptideFdr(tryDouble(textPeptideFdr, labelPeptideFdr));
			config.setProteinFdr(tryDouble(textProteinFdr, labelProteinFdr));
			config.setGroupFdr(tryDouble(textGroupFdr, labelGroupFdr));
			model.filterData();
			updateResults();
		} catch (Exception e) {
			logger.warning(e.getMessage());
			return;
		}
		updateView();
	}
	
	@FXML private void handleReset( ActionEvent event ) {
		resetFilter();
		model.setConfig(config);
		updateView();
	}
	
	private void resetFilter() {
		config.setBestPsmPerPrecursor(true);
		config.setMinPeptideLength(7);
		config.setPeptideFdr(0.01);
		config.setGroupFdr(0.01);
	}
	
	private void updateResults() {
		PAnalyzer.Counts target = model.getTargetCounts();
		PAnalyzer.Counts decoy = model.getDecoyCounts();
		tableCounts.setItems(FXCollections.observableArrayList(
			new CountBean("Minimum proteins (grouped)",target.getMinimum(),decoy.getMinimum()),
			new CountBean("Maximum proteins (un-grouped)",target.getMaximum(),decoy.getMaximum()),
			new CountBean("Conclusive proteins",target.getConclusive(),decoy.getConclusive()),
			new CountBean("Indistinguishable proteins (grouped)",target.getIndistinguishableGroups(),decoy.getIndistinguishableGroups()),
			new CountBean("Indistinguishable proteins (un-grouped)",target.getIndistinguishable(),decoy.getIndistinguishable()),
			new CountBean("Ambigous proteins (grouped)",target.getAmbiguousGroups(),decoy.getAmbiguousGroups()),
			new CountBean("Ambigous proteins (un-grouped)",target.getAmbiguous(),decoy.getAmbiguous()),
			new CountBean("Non-conclusive proteins",target.getNonConclusive(),decoy.getNonConclusive()),
			new CountBean("Total peptides",target.getPeptides(),decoy.getPeptides()),
			new CountBean("Unique peptides",target.getUnique(),decoy.getUnique()),
			new CountBean("Discriminating peptides",target.getDiscriminating(),decoy.getDiscriminating()),
			new CountBean("Non-discriminating peptides",target.getNonDiscriminating(),decoy.getNonDiscriminating()),
			new CountBean("Total PSMs",target.getPsms(),decoy.getPsms())
			));
		tableFdr.setItems(FXCollections.observableArrayList(
			new FdrBean("Protein group", model.getGroupFdr().getRatio(), config.getGroupFdr()),
			new FdrBean("Protein", model.getProteinFdr().getRatio(), config.getProteinFdr()),
			new FdrBean("Peptide", model.getPeptideFdr().getRatio(), config.getPeptideFdr()),
			new FdrBean("PSM", model.getPsmFdr().getRatio(), config.getPsmFdr())			
			));
	}

	private Integer tryInteger(TextField field, Label label) throws Exception {
		Integer result = null;
		String str = field.getText().trim();
		if( str.length() > 0 )
			try {						
				result = Integer.parseInt(str);
			} catch( Exception e ) {
				throw new Exception(String.format("%s must be an integer value", label.getText()));
			}
		field.setText(str);
		return result;
	}
	
	private Double tryDouble(TextField field, Label label) throws Exception {
		Double result = null;
		String str = field.getText().trim();
		if( str.length() > 0 )
			try {						
				result = Double.parseDouble(str);
			} catch( Exception e ) {
				throw new Exception(String.format("%s must be a decimal value", label.getText()));
			}
		field.setText(str);
		return result;
	}
	
	@FXML private void handleSaveFiles( ActionEvent event ) {				
		File dir = directoryChooser.showDialog(view);		
		logSeparator("Saving");
		config.setFilterDecoys(checkFilterDecoys.isSelected());
		config.setOutput(new File(dir,config.getDescription()).getAbsolutePath());
		File html = model.saveData();
		webBrowser.getEngine().load(String.format("file://%s",html.getAbsolutePath()));
		updateView();
	}
	
	@FXML private void handleTreeEdit( EditEvent<String> event ) {
		config.setDescription(treeExperiment.getRoot().getValue());
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Handler handler = new LogHandler();
		Logger.getLogger(MainModel.class.getName()).addHandler(handler);
		logger.addHandler(handler);
		
		fileChooser.setTitle("Load identification files");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(
			new FileChooser.ExtensionFilter("mzIdentML", "*.mzid", "*.mzid.gz"));
		
		directoryChooser.setTitle("Select destination directory");
		directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		
		config.setDescription("MyExperiment");
		TreeItem<String> rootItem = new TreeItem<>(config.getDescription());
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
		
		colCountType.setCellValueFactory(new PropertyValueFactory<CountBean,String>("type"));
		colTargetCount.setCellValueFactory(new PropertyValueFactory<CountBean,Integer>("target"));
		colDecoyCount.setCellValueFactory(new PropertyValueFactory<CountBean,Integer>("decoy"));
		colTotalCount.setCellValueFactory(new PropertyValueFactory<CountBean,Integer>("total"));
		colFdrLevel.setCellValueFactory(new PropertyValueFactory<FdrBean,String>("level"));
		colFdrValue.setCellValueFactory(new PropertyValueFactory<FdrBean,Double>("value"));
		colFdrThreshold.setCellValueFactory(new PropertyValueFactory<FdrBean,Double>("threshold"));
		
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
		//choiceScoreType.setValue(config.getPsmScore());
		textRank.setText(valueOf(config.getPsmRankThreshold()));
		checkBestPsm.setSelected(Boolean.TRUE.equals(config.getBestPsmPerPrecursor()));
		textPsmFdr.setText(valueOf(config.getPsmFdr()));
		textPeptideLength.setText(valueOf(config.getMinPeptideLength()));
		textPeptideFdr.setText(valueOf(config.getPeptideFdr()));
		textProteinFdr.setText(valueOf(config.getProteinFdr()));
		textGroupFdr.setText(valueOf(config.getGroupFdr()));
		checkFilterDecoys.setSelected(Boolean.TRUE.equals(config.getFilterDecoys()));
		for( Object node : listNodes )
			disableObject(node,true);
		for( Object node : mapNodes.get(model.getState()) )
			disableObject(node,false);		
	}
	
	private String valueOf( Object o ) {
		return o == null ? "" : o.toString();
	}
	
	private static void showWelcome() {
		StringWriter string = new StringWriter();
		PrintWriter pw = new PrintWriter(string);
		pw.println(String.format("--- Welcome to %s ---\n", MainModel.SIGNATURE));
		pw.println("In a normal execution you should follow these steps:");
		pw.println("1. Load experiment file(s) in the 'Experiment' tab");
		pw.println("2. Apply some quality criteria in the 'Filter' tab");
		pw.println("3. Check and export the results in the 'Results' tab");
		pw.print("4. Browse the results using the integrated 'Browser' tab or your favorite web browser outside this application");
		pw.close();
		logger.info(string.toString());
	}
	
	private void logSeparator( String msg ) {
		logger.info(msg==null?"":String.format("\n--- %s ---", msg));
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
	
	public static class CountBean {
		public CountBean( String type, int target, int decoy ) {
			this.type = type;
			this.target = target;
			this.decoy = decoy;
			total = target+decoy;
		}
		public String getType() {
			return type;
		}
		public int getTarget() {
			return target;
		}
		public int getDecoy() {
			return decoy;
		}
		public int getTotal() {
			return total;
		}
		private final String type;
		private final int target;
		private final int decoy;
		private final int total;
	}
	
	public static class FdrBean {
		public FdrBean( String level, double value, Double threshold ) {
			this.level = level;
			this.value = value;
			this.threshold = threshold;
		}
		public String getLevel() {
			return level;
		}
		public double getValue() {
			return value;
		}
		public Double getThreshold() {
			return threshold;
		}
		private final String level;
		private final double value;
		private final Double threshold;		
	}
}