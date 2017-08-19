package ufsc.sisinf.brmodelo2all.ui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.mxgraph.util.mxResources;

public class SqlEditor extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -5951065346955910406L;
	private RSyntaxTextArea textArea;

	public SqlEditor(Frame owner) {
		super(owner);

		textArea = new RSyntaxTextArea(20, 60);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
		textArea.setCodeFoldingEnabled(true);
		textArea.setAntiAliasingEnabled(true);
		RTextScrollPane sp = new RTextScrollPane(textArea);
		sp.setFoldIndicatorEnabled(true);

		setTitle(mxResources.get("physicalModeling"));
		setLayout(new BorderLayout());
		setSize(800, 600);

		getContentPane().add(sp);
	}

	public void insertSqlInstruction(String instruction) {
		String newText = textArea.getText() + "\n" + instruction;
		textArea.setText(newText);
	}
}
