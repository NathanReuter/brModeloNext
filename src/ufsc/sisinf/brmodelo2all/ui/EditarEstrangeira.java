package ufsc.sisinf.brmodelo2all.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class EditarEstrangeira extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	String t;

	public EditarEstrangeira() {
		setTitle("Chave Estrangeira");
		setResizable(false);
		setModal(true);
		setBounds(100, 100, 446, 166);
		getContentPane().setLayout(null);
		contentPanel.setBounds(0, 0, 420, 95);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);

		JLabel lblQualSerO = new JLabel("Qual ser\u00E1 o nome dessa nova chave estrangeira?");
		lblQualSerO.setBounds(102, 11, 275, 14);
		contentPanel.add(lblQualSerO);

		textField = new JTextField();
		textField.setBounds(147, 36, 147, 20);
		contentPanel.add(textField);
		textField.setColumns(10);
		{
			JLabel lblDeveseTerUma = new JLabel(
					"(Deve-se ter uma chave prim\u00E1ria com o mesmo nome na tabela relacionada)");
			lblDeveseTerUma.setBounds(23, 70, 387, 14);
			contentPanel.add(lblDeveseTerUma);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBounds(0, 93, 434, 33);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {

						t = textField.getText();

						setVisible(false);

					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
