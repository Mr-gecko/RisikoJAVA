package client.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.util.SwingUtils;

import model.*;
import model.network.*;
import model.utils.Logger;
import client.controller.ClientGameController;
import client.controller.ClientLobbyController;
import client.controller.ClientViewController;
import client.network.*;
import client.resources.GameResources;

public class StartUpFrame extends JFrame {
	
	
	private static final int DEFAULT_SERVER_PORT = 1234;
	private ClientNetwork clientNetwork;
	private ClientLobbyController clientLobbyController;
	private ClientGameController clientGameController;
	private Player player;
	
	private JTextField nameField;
	private JTextField ipField;
	private JTextField portField;
	private JTextField lobbyCodeField;
	private JButton joinButton;
	private JButton createButton;
	
	Logger logger = new Logger("SUF");

	public StartUpFrame() {
		setTitle("Risiko");
        setIconImage(GameResources.LOGO.getImage());
		setSize(460, 500);
		setResizable(false);
		setLocation(200, 100);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        contentPane.add(createJoinAndCreateLobbyPanel());
        this.setContentPane(contentPane);
        this.lobbyCodeField.requestFocusInWindow();
	}
	
	
	public void testingMethod() {
		this.joinButton.doClick();
	}
	
	/**
	 * load player profile
	 */
	private void loadPlayerProfile() {
		// load some type of information about the player (ex: color and nickname)
		String name = this.nameField.getText();
		if (!name.isBlank() && !name.isEmpty() && name.length() < 15) {
			this.player = new Player(name, PlayerColor.NOT_SELECTED);
		} else {
			this.player = new Player("Player", PlayerColor.NOT_SELECTED);
		}
		
		SwingUtilities.invokeLater(() -> {
		    this.lobbyCodeField.requestFocusInWindow();
		});
		
	}
	
	
	
	
	
	/**
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	public boolean connectToServer(String ip, int port) {
		this.clientNetwork = null;
		loadPlayerProfile();
		try {
			this.clientNetwork = new ClientNetwork(ip, port, player);
			this.clientLobbyController = new ClientLobbyController(clientNetwork);
			this.clientGameController = new ClientGameController(clientNetwork);
			return true;
		} catch (Exception e) { 
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(this, "[SUF] impossible to connect to server: "+e.getMessage(), "connection error", JOptionPane.ERROR_MESSAGE);				
			});
			return false;
		}
	}
	
	
	
	
	
	/**
	 * create and join lobby 
	 */
	public void createLobby() {
		String ip = getIpField();
		int port = getPortField();
		
		try {
			if (connectToServer(ip, port)) {
				MainFrame mainFrame = stepIntoMainFrame(); // mainframe visibility is false
//				this.clientNetwork.serverListener(mainFrame::handleServerPayload);
				long id = this.clientLobbyController.createLobby();
				logger.debug("id: " + id);
				
				// TODO: remove this line [HARDCODED]
//				this.clientLobbyController.changeLobbyCode(id, new Player("000000", null));
				
				logger.debug("changed");
				this.clientLobbyController.joinLobby(id, player);
				// from join lobby the client refers to the server stored player object
				logger.debug("joined");
				this.dispose(); // if joined successfully dispose this frame
				mainFrame.setVisible(true); // and show mainframe
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (this.clientNetwork != null && this.clientNetwork.isConnected()) {
		        this.clientNetwork.disconnect();
		    }
	    	SwingUtilities.invokeLater(() -> {				
	    		JOptionPane.showMessageDialog(this,  e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
			});
	    	return;
		}
	}
	
	
	
	
	
	/**
	 * join lobby
	 */
	// public for dev testing
	public MainFrame joinLobby() {
		String ip = getIpField();
		int port = getPortField();
		String code = getCodeField();
		
		if (code.isEmpty()) {
			SwingUtilities.invokeLater(() -> {				
				JOptionPane.showMessageDialog(this, "please enter a lobby code", "invalid lobby code", JOptionPane.ERROR_MESSAGE);
			});
	        return null;
	    } else if (!Lobby.isCodeValid(code)) {
	    	SwingUtilities.invokeLater(() -> {				
	    		JOptionPane.showMessageDialog(this,  "please enter a valid lobby code", "invalid lobby code", JOptionPane.ERROR_MESSAGE);
			});
	    	return null;
	    }
		
		try {
			if (connectToServer(ip, port)) {
				MainFrame mainFrame = stepIntoMainFrame();
//				this.clientNetwork.serverListener(mainFrame::handleServerPayload);
				boolean joined = this.clientLobbyController.joinLobby(code, player);
				if (joined) {
					//
				} else {
					this.clientLobbyController.joinLobbyAsSpectator(code, player);
					long lobbyId = this.clientLobbyController.getCurrentLobbyId();
				    Game game = this.clientGameController.getGameSnapshot(lobbyId, player);
					mainFrame.setSpectatorMode(true);
					
					if (game != null && game.isStarted()) {
						mainFrame.showGameView();
					} else {
						//
					}
					
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(this,  "lobby is full, joining as spectator", "Full Lobby", JOptionPane.INFORMATION_MESSAGE);
					});
					
				}
				// from join lobby the client refers to the server stored player object
				this.dispose();
				mainFrame.setVisible(true);
				return mainFrame;
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (this.clientNetwork != null && this.clientNetwork.isConnected()) {
		        this.clientNetwork.disconnect();
		    }
	    	SwingUtilities.invokeLater(() -> {				
	    		JOptionPane.showMessageDialog(this,  e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
			});
	    	return null;
		}
		return null;
	}
	
	
	
	
	
//	/**
//	 * open create mainFrame and dispose current frame
//	 * @return
//	 */
//	private MainFrame_old stepIntoMainFrame() {
//		MainFrame_old mainFrame = new MainFrame_old(this.clientNetwork, this.clientLobbyController, this.clientGameController, this.player);
////		this.setVisible(false);
//		return mainFrame;
//	}
	
	
	
	
	
	private MainFrame stepIntoMainFrame() {
	    MainFrame mainFrame = new MainFrame();
	    ClientViewController viewController = new ClientViewController(
	        mainFrame, clientNetwork, clientLobbyController, clientGameController, player
	    );
	    this.clientNetwork.serverListener(viewController::handleServerPayload); // setting up the consumer
	    return mainFrame;
	}
	
	
	
	
	
	/**
	 * local view panel
	 * @return
	 */
	private JPanel createJoinAndCreateLobbyPanel() {
		JPanel panel = new JPanel();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gridBagLayout);
		panel.setBorder(BorderFactory.createTitledBorder("Join Lobby"));
		
		JLabel nameLabel = new JLabel("name:");
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_nameLabel.anchor = GridBagConstraints.EAST;
		gbc_nameLabel.gridx = 1;
		gbc_nameLabel.gridy = 1;
		panel.add(nameLabel, gbc_nameLabel);
		nameLabel.setVisible(false);
		
		nameField = new JTextField("Player");
		GridBagConstraints gbc_nameField = new GridBagConstraints();
		gbc_nameField.insets = new Insets(0, 0, 5, 5);
		gbc_nameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameField.gridx = 2;
		gbc_nameField.gridy = 1;
		panel.add(nameField, gbc_nameField);
		nameField.setColumns(10);
		this.nameField.setVisible(false);
		
		JLabel ipLabel = new JLabel("server ip:");
		GridBagConstraints gbc_ipLabel = new GridBagConstraints();
		gbc_ipLabel.insets = new Insets(0, 0, 5, 5);
		gbc_ipLabel.anchor = GridBagConstraints.EAST;
		gbc_ipLabel.gridx = 1;
		gbc_ipLabel.gridy = 3;
		panel.add(ipLabel, gbc_ipLabel);
		
		ipField = new JTextField("127.0.0.1");
		GridBagConstraints gbc_ipField = new GridBagConstraints();
		gbc_ipField.insets = new Insets(0, 0, 5, 5);
		gbc_ipField.fill = GridBagConstraints.HORIZONTAL;
		gbc_ipField.gridx = 2;
		gbc_ipField.gridy = 3;
		panel.add(ipField, gbc_ipField);
		ipField.setColumns(10);
		// place holder
//		addPlaceholder(ipField, "127.0.0.1");
		
		JLabel portLabel = new JLabel("port:");
		GridBagConstraints gbc_portLabel = new GridBagConstraints();
		gbc_portLabel.insets = new Insets(0, 0, 5, 5);
		gbc_portLabel.anchor = GridBagConstraints.EAST;
		gbc_portLabel.gridx = 1;
		gbc_portLabel.gridy = 4;
		panel.add(portLabel, gbc_portLabel);
		
		portField = new JTextField(""+DEFAULT_SERVER_PORT);
		GridBagConstraints gbc_portField = new GridBagConstraints();
		gbc_portField.insets = new Insets(0, 0, 5, 5);
		gbc_portField.fill = GridBagConstraints.HORIZONTAL;
		gbc_portField.gridx = 2;
		gbc_portField.gridy = 4;
		panel.add(portField, gbc_portField);
		portField.setColumns(10);
		// place holder
//		addPlaceholder(portField, "1234");
		
		JLabel lobbyCodeLabel = new JLabel("lobby code:");
		GridBagConstraints gbc_lobbyCodeLabel = new GridBagConstraints();
		gbc_lobbyCodeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lobbyCodeLabel.anchor = GridBagConstraints.EAST;
		gbc_lobbyCodeLabel.gridx = 1;
		gbc_lobbyCodeLabel.gridy = 5;
		panel.add(lobbyCodeLabel, gbc_lobbyCodeLabel);
		
		
		lobbyCodeField = new JTextField();
		GridBagConstraints gbc_lobbyCodeField = new GridBagConstraints();
		gbc_lobbyCodeField.insets = new Insets(0, 0, 5, 5);
		gbc_lobbyCodeField.fill = GridBagConstraints.HORIZONTAL;
		gbc_lobbyCodeField.gridx = 2;
		gbc_lobbyCodeField.gridy = 5;
		panel.add(lobbyCodeField, gbc_lobbyCodeField);
		lobbyCodeField.setColumns(10);
		// place holder
		addPlaceholder(lobbyCodeField, "a1b2c3");
		
		// TODO: remove this line
//		this.lobbyCodeField.setText("000000");
		
		
		joinButton = new JButton("join");
		joinButton.addActionListener(e -> joinLobby());
		joinButton.addActionListener(null);;
		GridBagConstraints gbc_joinButton = new GridBagConstraints();
		gbc_joinButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_joinButton.insets = new Insets(0, 0, 5, 5);
		gbc_joinButton.gridx = 2;
		gbc_joinButton.gridy = 7;
		panel.add(joinButton, gbc_joinButton);
		
		createButton = new JButton("create");
		createButton.addActionListener(e -> createLobby());
		GridBagConstraints gbc_createButton = new GridBagConstraints();
		gbc_createButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_createButton.insets = new Insets(0, 0, 5, 5);
		gbc_createButton.gridx = 2;
		gbc_createButton.gridy = 8;
		panel.add(createButton, gbc_createButton);
		
		return panel;
	}
	
	
	
	
	/**
	 * get code from field
	 * @return
	 */
	private String getCodeField() {
		String code = lobbyCodeField.getText().trim();
		code = code.toUpperCase(); // case insensitive
		return code;
	}
	
	
	
	// [REMOVABLE]
	// used only in dev testing, could be removed and nothing will break
	public void setCodeField(String code) {
		code = code.toUpperCase();
		this.lobbyCodeField.setText(code);
	}
	
	
	
	
	/**
	 * get ip from field
	 * @return
	 */
	private String getIpField() {
		String ip = ipField.getText().trim();
		
		if (!ClientNetwork.isIpValid(ip)) {
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(this,  "invalid ip adress", "error", JOptionPane.ERROR_MESSAGE);
			});
			ip = null;
		}
		
		return ip;
	}
	
	
	
	
	
	/**
	 * get port from field
	 * @return
	 */
	private int getPortField() {
		int port;

		try {
			port = Integer.parseInt(portField.getText().trim());
		} catch (NumberFormatException e) {
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(this, "invalid port number", "error", JOptionPane.ERROR_MESSAGE);
			});
			port = 0;
		}
		
		return port;
	}
	
	
	
	
	
	private void addPlaceholder(JTextField textField, String placeholder) {
		Color oldForeground = textField.getForeground();
		Color placeholderForeground = Color.GRAY;
		
		textField.setText(placeholder);
		textField.setForeground(placeholderForeground);
		textField.addFocusListener(new FocusListener() {
		    @Override
		    public void focusGained(FocusEvent e) {
		        if (textField.getText().equals(placeholder)) {
		        	textField.setText("");
		        	textField.setForeground(oldForeground);
		        }
		    }

		    @Override
		    public void focusLost(FocusEvent e) {
		        if (textField.getText().isEmpty()) {
		        	textField.setText(placeholder);
		        	textField.setForeground(placeholderForeground);
		        }
		    }
		});
	}
	
	
	
	
	
	
	// #### DEV COMMANDS ##################################
	
	
	public MainFrame DEVcreateLobby(String customCode) {
		String ip = getIpField();
		int port = getPortField();
		
		try {
			if (connectToServer(ip, port)) {
				MainFrame mainFrame = stepIntoMainFrame(); // mainframe visibility is false
//				this.clientNetwork.serverListener(mainFrame::handleServerPayload);
				long id = this.clientLobbyController.createLobby();
				logger.debug("id: " + id);
				
				// TODO: remove this line [HARDCODED]
				this.clientLobbyController.changeLobbyCode(id, new Player(customCode, null));
				
				logger.debug("changed");
				this.clientLobbyController.joinLobby(id, player);
				// from join lobby the client refers to the server stored player object
				logger.debug("joined");
				this.dispose(); // if joined successfully dispose this frame
				mainFrame.setVisible(true); // and show mainframe
				return mainFrame;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (this.clientNetwork != null && this.clientNetwork.isConnected()) {
		        this.clientNetwork.disconnect();
		    }
	    	SwingUtilities.invokeLater(() -> {				
	    		JOptionPane.showMessageDialog(this,  e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
			});
	    	return null;
		}
		return null;
	}
	
	
}
