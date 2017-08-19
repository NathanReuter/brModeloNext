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
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;

import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.AssociativeRelationObject;
import ufsc.sisinf.brmodelo2all.model.objects.Collection;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;
import ufsc.sisinf.brmodelo2all.model.objects.NoSqlAttributeObject;
import ufsc.sisinf.brmodelo2all.util.AppConstants;

public class PropertiesWindow extends JDialog {

	private ModelingObject modelingObject;
	private JComponent[] fields;
	private mxCell cellObject;
	protected final ModelingComponent modelingComponent;

	/**
	 * 
	 */
	private static final long serialVersionUID = -8803589863674906236L;
	public static final int WINDOW_WIDTH = 180;

	public PropertiesWindow(Frame owner, Object cell, final ModelingComponent modelingComponent) {
		super(owner);

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

		// panel dos botoes
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

		for (int i = 0; i < count; i++) {
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
			JTextField text = new JTextField(value, 10);
			text.setSize(150, 5);
			text.setEnabled(enabled);
			field = text;
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
			JLabel caption = new JLabel(name);
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

		// Adds OK button to close window
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
		for (int i = 0; i < count; i++) {

			switch (fieldsTypes[i]) {
			case AppConstants.TEXT_FIELD:
				newValues[i] = ((JTextField) fields[i]).getText();
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

		// Needed for the No-SQL cardinality view.
		checkCardinality(cellObject);

		// refresh screen
		modelingComponent.getGraph().refresh();

	}

	public void chamaRepaint() {

		repaint();
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

	/*
	 * Taken from ModelingManager.
	 */
	public void checkCardinality(Object insertedObject) {
		mxCell objectCell = (mxCell) insertedObject;
		if (objectCell.getId().equals("hasCardinality")) {
			for (int i = 0; i < objectCell.getChildCount(); i++) {
				ModelingObject childObject = (ModelingObject) objectCell.getChildAt(i).getValue();
				if (!(childObject instanceof NoSqlAttributeObject)) {
					this.modelingComponent.getGraph().getModel().remove(cellObject.getChildAt(i));
					this.modelingComponent.getGraph().refresh();
				}
			}
		}
		insertCardinality(insertedObject);
	}

	public void insertCardinality(Object insertedObject) {
		mxICell insertedCell = (mxICell) insertedObject;
		mxGraph graph = this.modelingComponent.getGraph();
		mxGeometry cellGeometry = insertedCell.getGeometry();
		Object object = null;
		if ((insertedCell.getValue() instanceof NoSqlAttributeObject)) {
			NoSqlAttributeObject attribute = (NoSqlAttributeObject) insertedCell.getValue();
			double newX = cellGeometry.getWidth() - 30.0D;
			double newY = 1.0D;
			if ((attribute.isOptional()) || (attribute.isMultiValued())) {
				object = graph.insertVertex(insertedCell, null,
						new ModelingObject("(" + attribute.getMinimumCardinality() + ", "
								+ attribute.getMaximumCardinality() + ")"),
						newX, newY, 30.0D, 20.0D - 5.0D, "horizontalAlign=left");
				insertedCell.setId("hasCardinality");
			}
		} else if (((insertedCell.getValue() instanceof Collection))
				&& (((Collection) insertedCell.getValue()).isBlock())) {
			Collection block = (Collection) insertedCell.getValue();
			double newX = cellGeometry.getWidth() - 30.0D;
			double newY = 1.0D;
			if ((block.isOptional()) || (block.isMultiValued())) {
				object = graph.insertVertex(insertedCell, null,
						new ModelingObject(
								"(" + block.getMinimumCardinality() + ", " + block.getMaximumCardinality() + ")"),
						newX, newY, 30.0D, 20.0D, "horizontalAlign=left");
				insertedCell.setId("hasCardinality");
			}
		}
		graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, "none", new Object[] { object });
	}
}
