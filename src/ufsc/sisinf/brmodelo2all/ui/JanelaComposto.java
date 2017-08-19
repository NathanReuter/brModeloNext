package ufsc.sisinf.brmodelo2all.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class JanelaComposto extends JDialog {

	private final JPanel contentPanel = new JPanel();

	String g;

	public JanelaComposto() {
		setTitle("Atributo Composto");
		setModal(true);
		setBounds(100, 100, 269, 155);
		getContentPane().setLayout(null);
		contentPanel.setBounds(0, 0, 253, 80);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);
		{
			JLabel lblNewLabel = new JLabel("Quantos atributos devem ser criados inicialmente?");
			lblNewLabel.setBounds(10, 11, 250, 14);
			contentPanel.add(lblNewLabel);
		}

		final JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5" }));
		comboBox.setBounds(107, 52, 31, 20);
		contentPanel.add(comboBox);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBounds(0, 84, 253, 33);
			getContentPane().add(buttonPane);
			buttonPane.setLayout(null);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {

						g = comboBox.getSelectedItem().toString();
						setVisible(false);

					}
				});
				okButton.setBounds(177, 5, 47, 23);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {

						g = "";
						setVisible(false);
					}
				});
				cancelButton.setBounds(102, 5, 65, 23);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	public String get() {

		return g;

	}

}
