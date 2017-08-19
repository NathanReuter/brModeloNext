package ufsc.sisinf.brmodelo2all.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import com.mxgraph.model.mxCell;

import ufsc.sisinf.brmodelo2all.control.ModelingManager;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;

public class JanelaEstrangeira extends JDialog {
	private final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Launch the application.
	 */

	char f;

	/**
	 * Create the dialog.
	 */
	public JanelaEstrangeira(final Object u, final Object d, final ModelingManager m) {
		setTitle("Chave Estrangeira");
		setResizable(false);
		setModal(true);
		f = ' ';
		setBounds(100, 100, 265, 225);
		getContentPane().setLayout(null);
		{
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {

					boolean tem = false;

					if (f == 'u') {

						for (int i = 0; i < ((mxCell) d).getChildCount(); i++) {

							if (((mxCell) d).getChildAt(i).getStyle().equals("primaryKey")) {

								m.insertColunaDireta(u,
										((ModelingObject) ((mxCell) d).getChildAt(i).getValue()).getName());
								tem = true;
							}

						}
						if (tem == true) {

							dispose();

						}

						if (tem == false) {

							EditarEstrangeira es = new EditarEstrangeira();
							es.setVisible(true);
							if (es.t != null && es.isVisible() == false) {
								m.insertColunaDireta(u, es.t);
								dispose();
								es.dispose();
							}

						}

					} else if (f == 'd') {

						for (int i = 0; i < ((mxCell) u).getChildCount(); i++) {

							if (((mxCell) u).getChildAt(i).getStyle().equals("primaryKey")) {

								m.insertColunaDireta(d,
										((ModelingObject) ((mxCell) u).getChildAt(i).getValue()).getName());
								tem = true;
							}

						}
						if (tem == true) {

							dispose();

						}

						if (tem == false) {
							EditarEstrangeira es = new EditarEstrangeira();
							es.setVisible(true);
							if (es.t != null && es.isVisible() == false) {
								m.insertColunaDireta(d, es.t);
								dispose();
								es.dispose();
							}
						}

					}

				}
			});
			okButton.setBounds(114, 154, 47, 23);
			getContentPane().add(okButton);
			okButton.setActionCommand("OK");
			getRootPane().setDefaultButton(okButton);
		}
		{
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			cancelButton.setBounds(172, 154, 65, 23);
			getContentPane().add(cancelButton);
			cancelButton.setActionCommand("Cancel");
		}
		{
			JLabel lblDesejaCriarTambm = new JLabel("Deseja criar tamb\u00E9m chave(s) estrangeira(s)?");
			lblDesejaCriarTambm.setBounds(10, 11, 239, 14);
			getContentPane().add(lblDesejaCriarTambm);
		}

		JRadioButton radioButton = new JRadioButton(
				"Na tabela : " + ((ModelingObject) ((mxCell) u).getValue()).getName());
		radioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				f = 'u';

			}
		});
		buttonGroup.add(radioButton);
		radioButton.setBounds(37, 44, 200, 50);
		getContentPane().add(radioButton);
		{
			JRadioButton rdbtnNewRadioButton = new JRadioButton(
					"Na tabela : " + ((ModelingObject) ((mxCell) d).getValue()).getName());
			rdbtnNewRadioButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					f = 'd';

				}
			});
			buttonGroup.add(rdbtnNewRadioButton);
			rdbtnNewRadioButton.setBounds(37, 97, 200, 50);
			getContentPane().add(rdbtnNewRadioButton);
		}
	}
}
