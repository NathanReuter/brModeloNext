package ufsc.sisinf.brmodelo2all.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;

import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.AssociativeRelationObject;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;
import ufsc.sisinf.brmodelo2all.util.AppConstants;

public class CommentWindow extends JDialog {

	private ModelingObject modelingObject;
	private JComponent[] fields;
	private mxCell cellObject;
	protected final ModelingComponent modelingComponent;
	int control;

	/**
	 * 
	 */
	private static final long serialVersionUID = -8803589863674906236L;
	public static final int WINDOW_WIDTH = 300;

	public CommentWindow(Frame owner, Object cell, final ModelingComponent modelingComponent) {
		super(owner);

		control = 0;
		this.modelingComponent = modelingComponent;
		String title = mxResources.get("objectProperties");
		setTitle(title);
		setLayout(new BorderLayout());
		cellObject = (mxCell) cell;
		modelingObject = (ModelingObject) cellObject.getValue();
		if (modelingObject instanceof AssociativeRelationObject) {
			cellObject = (mxCell) ((mxCell) cell).getParent();
			modelingObject = (ModelingObject) cellObject.getValue();
		}

		fields = new JComponent[modelingObject.attributesCount()];

		// montar as propriedades do objeto
		JPanel attributesPanel = createAttributesPanel();
		getContentPane().add(attributesPanel, BorderLayout.NORTH);
		if (fields[0] instanceof JTextField)
			((JTextField) fields[0]).selectAll();

		// painel dos botoes
		JPanel buttonsPanel = createButtonsPanel();
		getContentPane().add(buttonsPanel, BorderLayout.CENTER);

		setResizable(false);
		setSize(WINDOW_WIDTH, getWindowHeight());
	}

	public int getWindowHeight() {
		return modelingObject.windowHeight();
	}

	protected JPanel createAttributesPanel() {
		JPanel p = new JPanel();
		int count = modelingObject.attributesCount();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

		int fieldsTypes[] = new int[count];
		String fieldsNames[] = new String[count];
		String fieldsValues[] = new String[count];
		boolean fieldsEnabled[] = new boolean[count];
		modelingObject.getAttributes(fieldsTypes, fieldsNames, fieldsValues, fieldsEnabled);

		for (int i = 0; i < 1; i++) {
			fillAttributeComponents(p, fieldsNames[i], fieldsValues[i], fieldsTypes[i], fieldsEnabled[i], i);
		}

		modelingObject.createHandlers(fields);

		return p;
	}

	protected void fillAttributeComponents(JPanel panel, String name, String value, int type, boolean enabled,
			int fieldIndex) {
		boolean usesCaption = true;
		JComponent field;
		switch (type) {
		case AppConstants.TEXT_FIELD:

			JTextArea text = new JTextArea(value, 6, 6);
			JScrollPane scrollPane = new JScrollPane(text);
			scrollPane.setSize(10, 10);
			scrollPane.setEnabled(enabled);
			field = scrollPane;

			break;

		case AppConstants.CHECK_BOX:
			boolean selected = value == "true" ? true : false;
			usesCaption = false;
			JCheckBox checkBox = new JCheckBox(name, selected);
			checkBox.setEnabled(enabled);
			field = checkBox;
			break;

		case AppConstants.COMBO_BOX:
			String values[] = modelingObject.getComboValues(name);
			JComboBox combo = new JComboBox(values);
			combo.setSelectedItem(value);
			combo.setEnabled(enabled);
			field = combo;
			break;

		default:
			usesCaption = false;
			field = new JLabel(value);
		}

		if (usesCaption) {
			JLabel caption = new JLabel("Comentário");
			caption.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
			if (type == AppConstants.COMBO_BOX) {
				JPanel innerPanel = new JPanel();
				innerPanel.setLayout(new BorderLayout());
				innerPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
				innerPanel.add(caption, BorderLayout.WEST);
				innerPanel.add(field, BorderLayout.EAST);
				panel.add(innerPanel);
			} else {
				JPanel innerPanel = new JPanel();
				innerPanel.setLayout(new BorderLayout());
				innerPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
				innerPanel.add(caption, BorderLayout.WEST);
				panel.add(innerPanel);
				innerPanel = new JPanel();
				innerPanel.setLayout(new BorderLayout());
				innerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
				innerPanel.add(field, BorderLayout.CENTER);
				panel.add(innerPanel);
			}

		} else {
			JPanel innerPanel = new JPanel();
			innerPanel.setLayout(new BorderLayout());
			innerPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
			innerPanel.add(field, BorderLayout.WEST);
			panel.add(innerPanel);
		}

		fields[fieldIndex] = field;
	}

	protected JPanel createButtonsPanel() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
		p.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
		// Adds OK button to close window
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveModifications();
				setVisible(false);

			}
		});

		// Adds OK buttocurrentEditor.getGraphComponent().getGraph() to close
		// window
		JButton cancelButton = new JButton("Cancelar");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				setVisible(false);

			}
		});

		p.add(okButton);
		p.add(cancelButton);

		// Sets default button for enter key
		getRootPane().setDefaultButton(okButton);

		return p;
	}

	public void saveModifications() {
		int count = modelingObject.attributesCount();

		int fieldsTypes[] = new int[count];
		String fieldsNames[] = new String[count];
		String fieldsValues[] = new String[count];
		boolean fieldsEnabled[] = new boolean[count];
		modelingObject.getAttributes(fieldsTypes, fieldsNames, fieldsValues, fieldsEnabled);

		String newValues[] = new String[count];
		for (int i = 0; i < 1; i++) {

			switch (fieldsTypes[i]) {
			case AppConstants.TEXT_FIELD:

				JScrollPane t = (JScrollPane) fields[i];
				JTextArea ta = (JTextArea) t.getViewport().getView();
				newValues[i] = ta.getText();
				break;

			case AppConstants.CHECK_BOX:
				newValues[i] = ((JCheckBox) fields[i]).isSelected() ? "true" : "false";
				break;

			case AppConstants.COMBO_BOX:
				newValues[i] = (String) ((JComboBox) fields[i]).getSelectedObjects()[0];
				break;
			}
		}

		boolean modified = false;
		for (int i = 0; i < count && !modified; i++) {
			modified = newValues[i] != fieldsValues[i];
		}

		if (modified) {
			modelingObject.setAttributes(newValues);

			modelingComponent.getGraph().getModel().beginUpdate();
			try {
				Object[] objects = { cellObject };
				modelingComponent.getGraph().setCellStyle(modelingObject.getStyle(), objects);
				modelingComponent.getGraph().repaint(cellObject.getGeometry().getAlternateBounds());

			} finally {
				modelingComponent.getGraph().getModel().endUpdate();
			}
		}

		// refresh screen
		modelingComponent.getGraph().refresh();
	}

	/**
	 * Overrides {@link JDialog#createRootPane()} to return a root pane that
	 * hides the window when the user presses the ESCAPE key.O
	 */
	protected JRootPane createRootPane() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				setVisible(false);
			}
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		return rootPane;
	}
}
