package ufsc.sisinf.brmodelo2all.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.mxgraph.util.mxResources;

public class NoSqlEditor extends JInternalFrame implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -595106534695591045L;
	private RSyntaxTextArea textArea;
	private String menu = "Opcoes";
	private String menuItemSave = "Salvar";
	private String menuItemOpen = "Abrir";

	public NoSqlEditor() {

		textArea = new RSyntaxTextArea(20, 60);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
		textArea.setCodeFoldingEnabled(true);
		textArea.setAntiAliasingEnabled(true);
		RTextScrollPane scrollPane = new RTextScrollPane(textArea);
		scrollPane.setFoldIndicatorEnabled(true);
		setTitle(mxResources.get("physicalModeling"));
		setLayout(new BorderLayout());
		setClosable(true);
		setMaximizable(true);
		setIconifiable(true);
		setResizable(true);
		setBounds(20 * AppMainWindow.windowCount, AppMainWindow.windowCount, AppMainWindow.EDITOR_WIDTH,
				AppMainWindow.EDITOR_HEIGHT);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setFrameIcon(null);

		/*
		 * Cria o menu e o item.
		 */
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu(this.menu);
		JMenuItem menuItemSave = new JMenuItem(this.menuItemSave);
		JMenuItem menuItemOpen = new JMenuItem(this.menuItemOpen);
		menu.add(menuItemSave);
		menu.add(menuItemOpen);
		menuItemSave.addActionListener(new SaveListener());
		menuItemOpen.addActionListener(new OpenListener());
		menuBar.add(menu);

		this.setJMenuBar(menuBar);
		this.add(scrollPane);
	}

	public void insertSqlInstruction(String instruction) {
		String newText = textArea.getText() + "\n" + instruction;
		textArea.setText(newText);
	}

	/**
	 * 
	 * Opção de salvar do NoSqlEditor.
	 *
	 */
	class SaveListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			// Demonstrate "Open" dialog:
			int rVal = fileChooser.showSaveDialog(NoSqlEditor.this);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				FileOutputStream fileout;
				String filename = fileChooser.getSelectedFile().getAbsolutePath() + ".json";
				File newFile = new File(filename);
				try {
					newFile.createNewFile();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				try {
					fileout = new FileOutputStream(newFile);
					Writer writer = new BufferedWriter(new OutputStreamWriter(fileout, "UTF-8"));
					writer.write(textArea.getText());
					writer.close();
					fileout.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
			if (rVal == JFileChooser.CANCEL_OPTION) {
			}
		}
	}

	class OpenListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			int result = fileChooser.showOpenDialog(NoSqlEditor.this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				FileInputStream fileInput = null;
				try {
					fileInput = new FileInputStream(selectedFile);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				Scanner scanner = new Scanner(fileInput);
				scanner.useDelimiter("\\A");
				String jsonText = scanner.hasNext() ? scanner.next() : "";
				scanner.close();
				textArea.setText(jsonText);
			}
		}

	}
}
