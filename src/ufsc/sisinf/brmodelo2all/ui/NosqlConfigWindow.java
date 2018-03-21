package ufsc.sisinf.brmodelo2all.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;

import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;
import ufsc.sisinf.brmodelo2all.control.NosqlConfigurationData;

public class NosqlConfigWindow extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = -3378029138434324390L;
    private NosqlConfigurationData configData;
    /**
     *
     */
    public NosqlConfigWindow(Frame owner) {
        super(owner);
        configData = NosqlConfigurationData.getInstance();
        setTitle(mxResources.get("aboutGraphEditor"));
        setLayout(new BorderLayout());

        // Creates the gradient panel
        JPanel panel = new JPanel(new BorderLayout()) {

            /**
             *
             */
            private static final long serialVersionUID = -5062895855016210947L;

            /**
             *
             */
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Paint gradient background
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, Color.WHITE, getWidth(), 0, getBackground()));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(8, 8, 12, 8)));

        JLabel subtitleLabel = new JLabel("Configurações de Conversão");
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 18, 0, 0));
        subtitleLabel.setOpaque(false);
        panel.add(subtitleLabel, BorderLayout.CENTER);

        getContentPane().add(panel, BorderLayout.NORTH);

        JPanel content = new JPanel();
        JTextField dbNameTextArea = new JTextField(configData.getDbName());
        dbNameTextArea.setSize(100, 100);
        JLabel geralSectionTitle = new JLabel("Geral");
        geralSectionTitle.setFont(geralSectionTitle.getFont().deriveFont(Font.BOLD));
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(geralSectionTitle);
        JPanel dbNamePanel = new JPanel();
        dbNamePanel.setLayout(new BoxLayout(dbNamePanel, BoxLayout.LINE_AXIS));
        dbNamePanel.add(new JLabel("Nome do Banco: "));
        dbNamePanel.add(dbNameTextArea);
        content.add(new JLabel(" "));
        content.add(dbNamePanel);
        content.add(new JSeparator(SwingConstants.HORIZONTAL));
        content.add(new JLabel(" "));
        JLabel mongoSectionTitle = new JLabel("Mongo");
        mongoSectionTitle.setFont(mongoSectionTitle.getFont().deriveFont(Font.BOLD));
        content.add(new JLabel(" "));
        content.add(mongoSectionTitle);

//      Mongo validationLevel
        JPanel mongoValidationLevelPainel = new JPanel();
        JRadioButton moderate = new JRadioButton("Moderate", configData.getMongoValidationLevel() == "MODERATE");
        JRadioButton strict = new JRadioButton("Strict", configData.getMongoValidationLevel() == "STRICT");
        moderate.setActionCommand("MODERATE");
        strict.setActionCommand("STRICT");
        ButtonGroup validationLevelGroup = new ButtonGroup();
        validationLevelGroup.add(moderate);
        validationLevelGroup.add(strict);
        mongoValidationLevelPainel.add(new JLabel("Nivel de Validação: "));
        mongoValidationLevelPainel.add(moderate);
        mongoValidationLevelPainel.add(strict);
        content.add(mongoValidationLevelPainel);
//        Mongo Validation Action
        JPanel mongoValidationActionPainel = new JPanel();
        JRadioButton warning = new JRadioButton("Warning",  configData.getMongoValidationActions() == "WARNING");
        JRadioButton error = new JRadioButton("Error", configData.getMongoValidationActions() == "ERROR");
        warning.setActionCommand("WARNING");
        error.setActionCommand("ERROR");
        ButtonGroup validationActionGroup = new ButtonGroup();
        validationActionGroup.add(warning);
        validationActionGroup.add(error);
        mongoValidationActionPainel.add(new JLabel("Ação de Validação: "));
        mongoValidationActionPainel.add(warning);
        mongoValidationActionPainel.add(error);
        content.add(mongoValidationActionPainel);

        getContentPane().add(content, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY), BorderFactory.createEmptyBorder(16, 8, 8, 8)));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        JButton applyButton = new JButton("Aplicar");

        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configData.setDbName(dbNameTextArea.getText());
                configData.setMongoValidationActions(validationActionGroup.getSelection().getActionCommand());
                configData.setMongoValidationLevel(validationLevelGroup.getSelection().getActionCommand());
                setVisible(false);
            }
        });
        // Adds OK button to close window
        JButton closeButton = new JButton("Fechar");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        buttonPanel.add(applyButton);
        buttonPanel.add(closeButton);

        // Sets default button for enter key
        getRootPane().setDefaultButton(applyButton);

        setResizable(true);
        setSize(400, 400);
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