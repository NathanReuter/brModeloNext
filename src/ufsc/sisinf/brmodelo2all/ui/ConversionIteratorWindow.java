package ufsc.sisinf.brmodelo2all.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ConversionIteratorWindow extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -2955881058927788521L;
	private int result = -1;

	public ConversionIteratorWindow(Frame arg0, String[] messages, String[] alternatives, int suggestionIndex) {
		super(arg0);

		setLayout(new BorderLayout());

		JPanel messagePanel = createMessagePanel(messages);
		getContentPane().add(messagePanel, BorderLayout.NORTH);

		JPanel questionsPanel = createQuestionsPanel(alternatives, suggestionIndex);
		getContentPane().add(questionsPanel, BorderLayout.CENTER);

		// panel dos botï¿½es
		JPanel buttonsPanel = createButtonsPanel();
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

		setResizable(false);
		setSize(500, 215);
	}

	public JPanel createMessagePanel2() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 5));
		JLabel text = new JLabel("O que fazer a respeito do relacionamento (0,1)<->(0,n)");
		text.setFont(new Font(text.getFont().getFamily(), Font.BOLD, 12));
		text.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		p.add(text);
		text = new JLabel("encontrado entre as entidades \"Pessoas\" e \"AutomÃ³veis\"?");
		text.setFont(new Font(text.getFont().getFamily(), Font.BOLD, 12));
		text.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
		p.add(text);

		return p;
	}

	public JPanel createMessagePanel(String[] messages) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 5));

		for (int i = 0; i < messages.length; i++) {
			JLabel text = new JLabel(messages[i]);
			text.setFont(new Font(text.getFont().getFamily(), Font.BOLD, 12));
			text.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
			p.add(text);
		}

		return p;
	}

	public JPanel createQuestionsPanel2() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		JRadioButton firstOption = new JRadioButton(
				"1) Adicionar a(s) chave(s) da tabela de menor cardinalidade à tabela de maior cardinalidade", true);
		JRadioButton secondOption = new JRadioButton("2) Criar uma tabela para o relacionamento", false);
		JRadioButton thirdOption = new JRadioButton("3) Deste ponte em diante aceitar todas as sugestões", false);
		p.add(firstOption);
		p.add(secondOption);
		p.add(thirdOption);

		return p;
	}

	public JPanel createQuestionsPanel(String[] alternatives, int suggestionIndex) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		ButtonGroup radioGroup = new ButtonGroup();
		for (int i = 0; i < alternatives.length; i++) {
			JRadioButton alternative = new JRadioButton(alternatives[i], i == suggestionIndex);
			p.add(alternative);
			radioGroup.add(alternative);
		}

		return p;
	}

	public JPanel createButtonsPanel() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
		p.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
		// Adds OK button to close window
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel radioPanel = (JPanel) getContentPane().getComponent(1);
				for (int i = 0; i < radioPanel.getComponentCount() && result == -1; i++) {
					if (((JRadioButton) radioPanel.getComponent(i)).isSelected()) {
						result = i;
					}
				}
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

	public int getResult() {
		return result;
	}
}
