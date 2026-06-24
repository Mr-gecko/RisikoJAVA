package client.view.panels;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import client.view.renderers.CountryRenderer;
import model.enums.TurnPhase;
import java.awt.Component;
import javax.swing.Box;

public class GameOptionsPanel extends JPanel {

	private ButtonGroup radioGroup;
	private JRadioButton infoRadioButton;
	private JRadioButton deployRadioButton;
	private JRadioButton orderRadioButton;
	
	private Runnable onOptionChanged;

	public void setOnOptionChanged(Runnable onOptionChanged) {
	    this.onOptionChanged = onOptionChanged;
	}
	
	public GameOptionsPanel() {
		
		
		this.radioGroup = new ButtonGroup();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 43, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 21, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		infoRadioButton = new JRadioButton("info");
		GridBagConstraints gbc_infoRadioButton = new GridBagConstraints();
		gbc_infoRadioButton.anchor = GridBagConstraints.WEST;
		gbc_infoRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_infoRadioButton.gridx = 1;
		gbc_infoRadioButton.gridy = 1;
		add(infoRadioButton, gbc_infoRadioButton);
		
		this.radioGroup.add(infoRadioButton);
		
		deployRadioButton = new JRadioButton("deploy");
		deployRadioButton.setSelected(true);
		GridBagConstraints gbc_deployRadioButton = new GridBagConstraints();
		gbc_deployRadioButton.anchor = GridBagConstraints.WEST;
		gbc_deployRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_deployRadioButton.gridx = 1;
		gbc_deployRadioButton.gridy = 2;
		add(deployRadioButton, gbc_deployRadioButton);
		this.radioGroup.add(deployRadioButton);
		
		orderRadioButton = new JRadioButton("orders");
		GridBagConstraints gbc_orderRadioButton = new GridBagConstraints();
		gbc_orderRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_orderRadioButton.anchor = GridBagConstraints.WEST;
		gbc_orderRadioButton.gridx = 1;
		gbc_orderRadioButton.gridy = 3;
		add(orderRadioButton, gbc_orderRadioButton);
		this.radioGroup.add(orderRadioButton);
		
		
		infoRadioButton.addActionListener(e -> { if (onOptionChanged != null) onOptionChanged.run(); });
		deployRadioButton.addActionListener(e -> { if (onOptionChanged != null) onOptionChanged.run(); });
		orderRadioButton.addActionListener(e -> { if (onOptionChanged != null) onOptionChanged.run(); });
		
	}
	
	public String getSelectedOption() {
		if (infoRadioButton.isSelected()) return "info";
		if (deployRadioButton.isSelected()) return "deploy";
		if (orderRadioButton.isSelected()) return "order";
		else return "deploy";
	}
	
	public ButtonGroup getButtonGroup() {
		return this.radioGroup;
	}
	
	public void selectOrder() {
		this.orderRadioButton.setEnabled(true);
		this.deployRadioButton.setEnabled(false);
		this.orderRadioButton.setSelected(true);
	}
	
	public void selectDeploy() {
		this.deployRadioButton.setEnabled(true);
		this.orderRadioButton.setEnabled(false);
		this.deployRadioButton.setSelected(true);
	}
	
	public void update(TurnPhase phase) {
		switch (phase) {
		case DEPLOY -> selectDeploy();
		case MOVE -> selectOrder();
		case ATTACK -> selectOrder();
		}
	}
	
	public void setSpectatorMode(boolean value) {
		setVisible(!value);
	}
	
}
