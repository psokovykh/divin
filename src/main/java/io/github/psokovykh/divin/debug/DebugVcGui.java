package io.github.psokovykh.divin.debug;

import io.github.psokovykh.divin.vc.BasicVC;
import io.github.psokovykh.divin.core.PriorityMessage;
import io.github.psokovykh.divin.core.Threadlike;
import io.github.psokovykh.divin.util.FlexibleJFXThread;
import io.github.psokovykh.divin.vc.ViewController;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.security.InvalidParameterException;

//To every editor: pls, leave first line of that doc as it is, count it as an easter-egg joke
/**
 * Class created to demonstrate my teacher that I can create some GUIs and PDFs.
 *
 * If talking seriously, it is used to show dumps of queues of a {@link BasicVC}.
 * So, u can see that messages have correct priorities and are sorted accordingly (if
 * you are lucky enough to press "Create Dump" in right moment).
 *
 * Yeah, it's hard to press key in right time, but it is also difficult to look in
 * a table changing with (as example) 60fps frequency. Assuming uniform distribution,
 * every time is right.
 */
@SuppressWarnings("unused")
public class DebugVcGui implements Threadlike {
	private static Logger logger = LoggerFactory.getLogger( DebugVcGui.class );

	/**
	 * Thread to perform JFX actions at.
	 * @see FlexibleJFXThread
	 */
	private FlexibleJFXThread jfxThread;

	/** Active module to be watched over. We can't use
	 * {@link ViewController} interface here,
	 * cuz the Debug GUI depends on realisation with two queues
	 */
	private BasicVC am;

	/** Main window to show information on */
	private Stage theWindow;

	/** */
	private TableView<PriorityMessage> responsesTable;

	/** */
	private TableView<PriorityMessage> requestsTable;
	public DebugVcGui(FlexibleJFXThread jfxThread, BasicVC am) {
		this.jfxThread = jfxThread;
		this.am = am;
	}

	/**
	 * Initialises window and listeners, then shows the window.
	 * Note latency due to inderect call of jfx function
	 * @see FlexibleJFXThread#performJfxForeground(Runnable)
	 */
	@Override
	public void start() {
		this.jfxThread.performJfxForeground(this::initWindow);
		this.jfxThread.performJfxForeground(()->this.theWindow.show());
		this.jfxThread.performJfxForeground(this::makeSnapshot);
	}
	/**
	 * Closes the window.
	 * Note latency due to inderect call of jfx function
	 * @see FlexibleJFXThread#performJfxForeground(Runnable)
	 */
	@Override
	public void interrupt() {
		this.jfxThread.performJfxForeground(()-> {
			this.theWindow.hide(); //Is equivalent to close()
		});
	}

	/**
	 * Blocks thread until the window is closed
	 *
	 * The realisation now is only correct if we are not hiding the window
	 * for other reason than actually closing it
	 *
	 * Note latency due to inderect call of jfx function
	 * @see FlexibleJFXThread#performJfxForeground(Runnable)
	 */
	@Override
	public void join() throws InterruptedException {
		var joinedThread = Thread.currentThread();
		this.jfxThread.performJfxForeground(()-> {
			if(this.theWindow.isShowing()){
				this.theWindow.setOnHidden(e -> joinedThread.interrupt());
			}else{
				joinedThread.interrupt();
			}
		});
		Thread.sleep(Long.MAX_VALUE);
	}

	//  Wanna priority queue in FlexibleJFXThread, so that can make sense
	/**
	 * Just noop in that case.
	 * @throws InvalidParameterException never
	 */
	@Override
	public void setPriority(int priority) {
		//noop
	}

	/**
	 * <b>Must</b> be performed on jfx thread
	 * @see FlexibleJFXThread#performJfxForeground(Runnable)
	 */
	//TODO refactor this shit out totally
	private void initWindow(){
		this.theWindow = new Stage();
		this.theWindow.setTitle("Debug of some Active Module");
		TabPane tabPane = new TabPane();
		VBox.setVgrow(tabPane, Priority.ALWAYS);


		this.requestsTable = createTabWithTable(tabPane, "Common requests bus");
		this.responsesTable = createTabWithTable(tabPane, "Private responses queue");

		Button btnMakeSnapshot = new Button("Make new snapshot of queues");
		btnMakeSnapshot.setOnAction(this::makeSnapshot);
		Button btnCreatePdf = new Button("Write the snapshot to XML&PDF");
		btnCreatePdf.setOnAction(this::createXmlAndPdf);

		HBox btnsPane = new HBox();
		btnsPane.setSpacing(20);
		btnsPane.setAlignment(Pos.CENTER);
		btnsPane.getChildren().add(btnMakeSnapshot);
		btnsPane.getChildren().add(btnCreatePdf);

		VBox root = new VBox();
		root.setSpacing(20);
		root.setPadding(new Insets(0, 0, 20, 0));
		root.setAlignment(Pos.TOP_CENTER);
		root.getChildren().add(tabPane);
		root.getChildren().add(btnsPane);

		var width = 500;
		var height = 400;
		this.theWindow.setScene(new Scene(root, width, height));
		this.theWindow.setMinWidth(width);
		this.theWindow.setMinHeight(height);
	}

	private void createXmlAndPdf(ActionEvent event) {
		var dbgInfo = new DebugInfoXML(
				am.getOutRequestsQueue(),
				am.getInResponsesQueue()
		);
		try {
			File xmlFile = DebugInfoIO.outputDebugInfoXml(dbgInfo, this.theWindow);
			DebugInfoXML debugInfo = DebugInfoIO.inputDebugInfoXml(xmlFile);
			DebugInfoIO.outputDebugInfoPdf(debugInfo, this.theWindow);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	private void makeSnapshot(ActionEvent event) {
		makeSnapshot();
	}

	private void makeSnapshot() {
		this.requestsTable.getItems().clear();
		am.getOutRequestsQueue().stream().sorted().forEach(this.requestsTable.getItems()::add);

		this.responsesTable.getItems().clear();
		am.getInResponsesQueue().stream().sorted().forEach(this.responsesTable.getItems()::add);
	}


	private TableView<PriorityMessage> createTabWithTable(TabPane tpane, String title){
		Tab tab = new Tab();
		tab.setText(title);
		tab.setClosable(false);

		var scrlpane = new ScrollPane();
		scrlpane.setFitToWidth(true);
		scrlpane.setFitToHeight(true);

		TableView<PriorityMessage> table = new TableView<>();
		table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
		table.setEditable(false);
		table.setMaxWidth(Integer.MAX_VALUE);
		table.setPlaceholder(new Label("The list is empty yet :)"));

		//TODO nullchecks

		var priorColumn = createColumn(table, "Priority", 10);
		priorColumn.setCellValueFactory((cell)->
				new SimpleStringProperty(String.valueOf(cell.getValue().getPriority()))
		);
		var textColumn = createColumn(table, "Text", 90);
		textColumn.setCellValueFactory((cell)->
				new SimpleStringProperty(cell.getValue().getText())
		);
		textColumn.setCellFactory(tc -> {
			TableCell<PriorityMessage, String> cell = new TableCell<>();
			Text text = new Text();
			cell.setGraphic(text);
			cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
			text.wrappingWidthProperty().bind(textColumn.widthProperty());
			text.textProperty().bind(cell.itemProperty());
			return cell;
		});

		scrlpane.setContent(table);

		tab.setContent(scrlpane);
		tpane.getTabs().add(tab);

		return table;
	}

	@SuppressWarnings("unchecked")
	private TableColumn<PriorityMessage, String> createColumn(
			TableView table, String title, int widthPercents
	){
		TableColumn<PriorityMessage, String> column = new TableColumn(title);
		column.setSortable(false);
		column.setMaxWidth(1f * Integer.MAX_VALUE * widthPercents);
		table.getColumns().add(column);

		return column;
	}
}
