package es.ehubio.panalyzer;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.controlsfx.dialog.Dialogs;

import es.ehubio.panalyzer.Configuration.Replicate;
import es.ehubio.proteomics.ScoreType;
import es.ehubio.proteomics.pipeline.PAnalyzer;

@SuppressWarnings("deprecation")
public class MainController implements Initializable {
	private static final Logger logger = Logger.getLogger(MainController.class.getName());
	
	@FXML private TextArea textSummary;
	@FXML private Label labelSignature;
	@FXML private Label labelStatus;
	@FXML private TreeView<String> treeExperiment;
	@FXML private Button buttonAddReplicate;
	@FXML private Button buttonAddFractions;
	@FXML private Button buttonClear;
	@FXML private Button buttonLoad;
	@FXML private Button buttonFilter;
	@FXML private Button buttonReset;
	@FXML private Button buttonSave;
	@FXML private TabPane tabPane;
	@FXML private VBox vboxFilter;
	@FXML private HBox hboxResults;
	@FXML private Tab tabFilter;
	@FXML private Tab tabResults;
	@FXML private Tab tabBrowser;
	@FXML private TextField textDecoy;
	@FXML private ChoiceBox<ScoreType> choiceScoreType;
	@FXML private Label labelRank;
	@FXML private Label labelPsmFdr;
	@FXML private Label labelPeptideLength;
	@FXML private Label labelPeptideFdr;
	@FXML private Label labelPeptideReplicates;
	@FXML private Label labelProteinFdr;
	@FXML private Label labelProteinReplicates;
	@FXML private Label labelGroupFdr;
	@FXML private TextField textRank;	
	@FXML private CheckBox checkBestPsm;
	@FXML private TextField textPsmFdr;
	@FXML private TextField textPeptideLength;
	@FXML private TextField textPeptideFdr;
	@FXML private TextField textPeptideReplicates;
	@FXML private TextField textProteinFdr;
	@FXML private TextField textProteinReplicates;
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
	
	private final List<Set<String>> files = new ArrayList<>();
	private final Map<MainModel.State, Set<Object>> mapNodes = new HashMap<>();
	private final Set<Object> listNodes = new HashSet<>();
	private StringWriter logString;
	private PrintWriter log;
	
	public MainController( MainModel model, Stage view ) {
		resetLog();
		this.model = model;
		this.view = view;
	}
	
	private void resetLog() {
		logString = new StringWriter();
		log = new PrintWriter(logString);
	}
	
	@FXML private void handleAddReplicate( ActionEvent event ) {
		int count = treeExperiment.getRoot().getChildren().size()+1;
		TreeItem<String> rep = new TreeItem<String>(String.format("Replicate #%s", count));
		rep.setExpanded(true);
		treeExperiment.getRoot().getChildren().add(rep);
		if( event != null )
			updateView();
	}
	
	@FXML private void handleAddFractions( ActionEvent event ) {
		List<File> files = fileChooser.showOpenMultipleDialog(view);
		if( files == null )
			return;
		Set<String> set = new HashSet<>();
		for( File file : files ) {
			if( !set.add(file.getAbsolutePath()) )
				continue;
			TreeItem<String> item = new TreeItem<>(file.getName());
			int replicate = treeExperiment.getRoot().getChildren().size()-1;
			treeExperiment.getRoot().getChildren().get(replicate).getChildren().add(item);			
		}
		this.files.add(set);
		if( model.getConfig() == null )
			model.setConfig(config);
		updateView();
	}
	
	@FXML private void handleClearFiles( ActionEvent event ) {
		treeExperiment.getRoot().getChildren().clear();
		files.clear();
		config.getReplicates().clear();
		model.reset();
		resetLog();
		handleAddReplicate(event);		
	}
	
	@FXML private void handleLoadFiles( ActionEvent event ) {
		logSeparator(false,"Loading");
		config.setFilterDecoys(true);
		for( int i = 0; i < treeExperiment.getRoot().getChildren().size(); i++ ) {
			Replicate replicate = new Replicate();
			replicate.setName(treeExperiment.getRoot().getChildren().get(i).getValue());
			for( String file : files.get(i) )
				replicate.getFractions().add(file);
			config.getReplicates().add(replicate);
		}
		String decoy = textDecoy.getText().trim();
		textDecoy.setText(decoy);
		config.setDecoyRegex(decoy.length()==0?null:decoy);
		Callable<Void> thread = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				model.loadData();
				return null;
			}
		};
		Runnable gui = new Runnable() {			
			@Override
			public void run() {
				choiceScoreType.getItems().clear();
				choiceScoreType.getItems().addAll(model.getPsmScoreTypes());		
				if( model.getPsmScoreTypes().contains(ScoreType.XTANDEM_EVALUE) )
					choiceScoreType.setValue(ScoreType.XTANDEM_EVALUE);
				else if( model.getPsmScoreTypes().contains(ScoreType.MASCOT_EVALUE) )
					choiceScoreType.setValue(ScoreType.MASCOT_EVALUE);
				else
					choiceScoreType.getSelectionModel().selectFirst();
				config.initialize();
				tabPane.getSelectionModel().select(tabFilter);
				updateView();
			}
		};
		GuiService service = new GuiService(thread, gui);
		service.start("Loading files ...");
	}
	
	@FXML private void handleApplyFilter( ActionEvent event ) {
		logSeparator(true,"Filtering");
		try {
			config.setDescription(treeExperiment.getRoot().getValue());
			config.setPsmScore(choiceScoreType.getValue());
			config.setPsmRankThreshold(tryInteger(textRank, labelRank));
			config.setBestPsmPerPrecursor(checkBestPsm.isSelected());
			config.setPsmFdr(tryDouble(textPsmFdr, labelPsmFdr));
			config.setMinPeptideLength(tryInteger(textPeptideLength, labelPeptideLength));
			config.setPeptideFdr(tryDouble(textPeptideFdr, labelPeptideFdr));
			config.setMinPeptideReplicates(tryInteger(textPeptideReplicates, labelPeptideReplicates));
			config.setProteinFdr(tryDouble(textProteinFdr, labelProteinFdr));
			config.setMinProteinReplicates(tryInteger(textProteinReplicates, labelProteinReplicates));
			config.setGroupFdr(tryDouble(textGroupFdr, labelGroupFdr));
			Callable<Void> thread = new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					model.filterData();
					return null;
				}
			};
			Runnable gui = new Runnable() {				
				@Override
				public void run() {
					updateResults();
					tabPane.getSelectionModel().select(tabResults);
					updateView();
				}
			};
			GuiService service = new GuiService(thread, gui);
			service.start("Aplying filter ...");
		} catch (Exception e) {
			logger.warning(e.getMessage());
			return;
		}		
	}
	
	@FXML private void handleReset( ActionEvent event ) {
		config.initialize();
		//model.setConfig(config);
		updateView();
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
		if( dir == null )
			return;
		logSeparator(true,"Saving");
		config.setFilterDecoys(checkFilterDecoys.isSelected());
		config.setOutput(new File(dir,config.getDescription()).getAbsolutePath());
		Callable<Void> thread = new Callable<Void>() {			
			@Override
			public Void call() throws Exception {
				model.saveData();
				return null;
			}
		};
		Runnable gui = new Runnable() {			
			@Override
			public void run() {
				webBrowser.getEngine().load(String.format("file://%s",model.getReportFile().getAbsolutePath()));				
				tabPane.getSelectionModel().select(tabBrowser);
				updateView();
			}
		};
		GuiService service = new GuiService(thread, gui);
		service.start("Saving results ...");
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
		handleAddReplicate(null);
		
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
		enableByStates(buttonAddFractions,MainModel.State.INIT,MainModel.State.CONFIGURED);
		enableByStates(buttonAddReplicate,MainModel.State.INIT,MainModel.State.CONFIGURED);
		enableByStates(buttonClear,MainModel.State.CONFIGURED,MainModel.State.LOADED,MainModel.State.RESULTS,MainModel.State.SAVED);
		enableByStates(buttonLoad,MainModel.State.CONFIGURED);
		enableByStates(textDecoy,MainModel.State.CONFIGURED);
		enableByStates(tabFilter,MainModel.State.LOADED,MainModel.State.RESULTS,MainModel.State.SAVED);
		enableByStates(vboxFilter,MainModel.State.LOADED);
		enableByStates(tabResults,MainModel.State.RESULTS,MainModel.State.SAVED);
		enableByStates(hboxResults,MainModel.State.RESULTS);
		enableByStates(tabBrowser,MainModel.State.SAVED);
		
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
		String log = logString.toString();
		textSummary.setText(log.isEmpty()?getWelcome():log);
		labelStatus.setText(model.getStatus());
		//choiceScoreType.setValue(config.getPsmScore());
		textRank.setText(valueOf(config.getPsmRankThreshold()));
		checkBestPsm.setSelected(Boolean.TRUE.equals(config.getBestPsmPerPrecursor()));
		textPsmFdr.setText(valueOf(config.getPsmFdr()));
		textPeptideLength.setText(valueOf(config.getMinPeptideLength()));
		textPeptideFdr.setText(valueOf(config.getPeptideFdr()));
		textPeptideReplicates.setText(valueOf(config.getMinPeptideReplicates()));
		textProteinFdr.setText(valueOf(config.getProteinFdr()));
		textProteinReplicates.setText(valueOf(config.getMinProteinReplicates()));
		textGroupFdr.setText(valueOf(config.getGroupFdr()));
		checkFilterDecoys.setSelected(Boolean.TRUE.equals(config.getFilterDecoys()));
		for( Object node : listNodes )
			disableObject(node,true);
		for( Object node : mapNodes.get(model.getState()) )
			disableObject(node,false);
		int last = treeExperiment.getRoot().getChildren().size()-1; 
		if( treeExperiment.getRoot().getChildren().get(last).getChildren().isEmpty() )
			buttonAddReplicate.setDisable(true);
		else
			buttonAddFractions.setDisable(true);
	}
	
	private String valueOf( Object o ) {
		return o == null ? "" : o.toString();
	}
	
	private static String getWelcome() {
		StringWriter string = new StringWriter();
		PrintWriter pw = new PrintWriter(string);
		pw.println(String.format("--- Welcome to %s ---\n", MainModel.SIGNATURE));
		pw.println("In a normal execution you should follow these steps:");
		pw.println("1. Load experiment file(s) in the 'Experiment' tab");
		pw.println("2. Apply some quality criteria in the 'Filter' tab");
		pw.println("3. Check and export the results in the 'Results' tab");
		pw.print("4. Browse the results using the integrated 'Browser' tab or your favorite web browser outside this application");
		pw.close();
		return string.toString();
	}
	
	private void logSeparator( boolean skip, String msg ) {
		logger.info(msg==null?"":String.format("%s--- %s ---", skip?"\n":"", msg));
	}
	
	private final class GuiService {
		private Service<Void> service;
		
		public GuiService(final Callable<Void> thread, final Runnable ok ) {
			final Runnable fail = new Runnable() {				
				@Override
				public void run() {
					updateView();
					Dialogs.create().owner(view)
						.title("Error Dialog")
						.masthead("An error ocurred")
						.message(model.getStatus())
						.showError();					
				}
			};
			create(thread,ok,fail);
		}
		
		@SuppressWarnings("unused")
		public GuiService(final Callable<Void> thread, final Runnable ok, final Runnable fail) {
			create(thread, ok, fail);
		}
		
		private void create(final Callable<Void> thread, final Runnable ok, final Runnable fail) {
			service = new Service<Void>() {				
				@Override
				protected Task<Void> createTask() {
					return new Task<Void>() {
						@Override
						protected Void call() throws Exception {
							ExecutorService pool = Executors.newSingleThreadExecutor();
							Future<Void> future = pool.submit(thread);
							do {
								try {
									future.get(200, TimeUnit.MILLISECONDS);
								} catch( TimeoutException e ) {									
								}
								updateProgress(model.getProgressPercent(), 100);
								updateMessage(model.getProgressMessage());
							} while( model.getState() == es.ehubio.panalyzer.MainModel.State.WORKING && !future.isDone() );							
							return null;
						}
					};
				}				
			};
			service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {				
				@Override
				public void handle(WorkerStateEvent event) {
					Platform.runLater(ok);
				}
			});
			service.setOnFailed(new EventHandler<WorkerStateEvent>() {				
				@Override
				public void handle(WorkerStateEvent event) {
					Platform.runLater(fail);
				}
			});
		}
		
		public void start( String msg ) {
			Dialogs.create().owner(view)
				.title("Progress Dialog")
				.masthead(msg)
				.showWorkerProgress(service);
			service.start();
		}
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
			synchronized(log) {
				log.flush();
			}
		}

		@Override
		public void close() throws SecurityException {
			synchronized(log) {
				log.close();
			}
		}		
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