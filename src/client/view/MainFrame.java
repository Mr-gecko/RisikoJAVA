package client.view;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import client.controller.ClientInput;
import client.resources.GameResources;
import client.view.panels.ColorSlotPanel;
import client.view.panels.GameView;
import client.view.panels.LobbyView;
import client.view.panels.RoundPanel;
import client.view.panels.TimedDialog;
import model.AttackReport;
import model.Game;
import model.Lobby;
import model.Player;
import model.PlayerColor;
import model.enums.AttackResult;
import model.enums.TurnPhase;
import model.utils.Logger;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel containerPanel;
    private LobbyView lobbyView;
    private GameView gameView;

    private static final String LOBBY_VIEW = "LOBBY_VIEW";
    private static final String GAME_VIEW = "GAME_VIEW";

//    int ratioX = 16;
//    int ratioY = 10;
//    int size = 80;
//    int X = size * ratioX;
//    int Y = size * ratioY;
    int X = 1200;
    int Y = 850;
    Dimension minimum = new Dimension(X, Y);

    Logger logger = new Logger("MF");

    public MainFrame() {
        setTitle("Risiko");
        setIconImage(GameResources.LOGO.getImage());
        setSize(minimum);
        setResizable(false);
        setLocation(200, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.cardLayout = new CardLayout();
        this.containerPanel = new JPanel(cardLayout);

        this.lobbyView = new LobbyView();
        this.containerPanel.add(lobbyView, LOBBY_VIEW);

        try {
            this.gameView = new GameView(null); // supplier set later by ClientViewController
            this.containerPanel.add(gameView, GAME_VIEW);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.add(containerPanel);
        this.setVisible(false);
        cardLayout.show(containerPanel, LOBBY_VIEW);
        logger.log("initiated");
    }

    // #### VIEW UPDATE METHODS — called by ClientViewController ####

    public void updateLobby(Lobby lobby) {
        SwingUtilities.invokeLater(() -> lobbyView.update(lobby));
    }

    public void updateGame() {
        SwingUtilities.invokeLater(() -> gameView.update());
    }

    public void showLobbyView() {
        SwingUtilities.invokeLater(() -> {
            gameView.reset();
            cardLayout.show(containerPanel, LOBBY_VIEW);
            setSize(minimum);
//            setExtendedState(JFrame.NORMAL);
        });
    }

    public void showGameView() {
        SwingUtilities.invokeLater(() -> {
            cardLayout.show(containerPanel, GAME_VIEW);
            setSize(minimum);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        });
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, message, "error", JOptionPane.ERROR_MESSAGE)
        );
    }

    public void showMessage(String message, String title) {
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE)
        );
    }

    public void jumpToStartUpFrame() {
        SwingUtilities.invokeLater(() -> {
            new StartUpFrame().setVisible(true);
            this.dispose();
        });
    }

    // #### WIRING — called once by ClientViewController ####

    public void setSnapshotSupplier(Supplier<Game> supplier) {
        this.gameView.setSnapshotSupplier(supplier);
    }

    public void setOnStartGame(Runnable action) {
        lobbyView.actionsPanel.startButton.addActionListener(e -> action.run());
    }
    
    public void setOnAddBot(Runnable action) {
    	lobbyView.actionsPanel.addBotButton.addActionListener(e -> action.run());
    }
    
    public void setOnRemoveBot(Runnable action) {
    	lobbyView.actionsPanel.removeBotButton.addActionListener(e -> action.run());
    }

    public void setOnLeaveLobby(Runnable action) {
        lobbyView.actionsPanel.leaveButton.addActionListener(e -> action.run());
    }
    
    public void setOnLeaveLobbyAsSpectator(Runnable action) {
    	lobbyView.actionsPanel.leaveSpectatorButton.addActionListener(e -> action.run());
    }

    public void setOnToggleReady(Runnable action) {
        lobbyView.actionsPanel.readyButton.addActionListener(e -> action.run());
    }
    
//    public void setOnTradeCards(Runnable action) {
//    	gameView.getTradeButton().addActio
//    }
    
    public void setOnCardsButton(Runnable action) {
        gameView.getCardsButton().addActionListener(e -> action.run());
    }

    public void setOnLeaveGame(Runnable action) {
        gameView.getLeaveButton().addActionListener(e -> action.run());
    }
    
    public void setOnSurrenderGame(Runnable action) {
    	gameView.getSurrendereButton().addActionListener(e -> action.run());
    }
    
    public void setOnLeaveGameAsSpectator(Runnable action) {
    	gameView.getGameActionPanel().getLeaveSpectatorButton().addActionListener(e -> action.run());
    }

    public void setOnNextPhase(Runnable action) {
    	gameView.getNextPhaseButton().addActionListener(e -> action.run());
    }
    
    public void setOnCountryClicked(Consumer<ClientInput> callback) {
        gameView.setOnCountryClicked(callback);
    }
    
    public void setOnOptionChanged(Runnable action) {
    	gameView.getOptionsPanel().setOnOptionChanged(action);
    }

//    public void setOnColorPicked(Consumer<PlayerColor> callback) {
//        for (ColorSlotPanel slot : lobbyView.getColorsPanel().getColorSlotPanels()) {
//            slot.getButton().addActionListener(e -> {
//                lobbyView.getColorsPanel().resetColorSlotPanels();
//                callback.accept(slot.getColor()); // slot.pickAction() returns the color
//            });
//        }
//    }
    
    public void setOnColorPicked(BiConsumer<ColorSlotPanel, PlayerColor> callback) {
        for (ColorSlotPanel slot : lobbyView.getColorsPanel().getColorSlotPanels()) {
            slot.getButton().addActionListener(e -> {
                lobbyView.getColorsPanel().resetColorSlotPanels();
                callback.accept(slot, slot.getColor());
            });
        }
    }
    
    public void updateReadyButton(boolean ready) {
        SwingUtilities.invokeLater(() ->
            lobbyView.actionsPanel.readyButton.setText(ready ? "unready" : "ready")
        );
    }
    
    public void setInteractable(boolean value) {
    	SwingUtilities.invokeLater(() -> {
    		gameView.getWorldPanel().setInteractable(value);
    		gameView.getNextPhaseButton().setEnabled(value);
    		gameView.getCardsButton().setEnabled(value);
    	});
    }
    
    // #### DIALOGS #######################################
    
    public void showTurnOrderDialog(List<Player> turnOrder) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            
            JLabel title = new JLabel("Turn Order:");
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(title);
            panel.add(Box.createVerticalStrut(10));
            
            for (int i = 0; i < turnOrder.size(); i++) {
                Player player = turnOrder.get(i);
                
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JLabel number = new JLabel((i + 1) + ". ");
                RoundPanel colorDot = new RoundPanel(10, player.getColor().getColor());
                colorDot.setPreferredSize(new Dimension(15, 15));
                JLabel name = new JLabel(player.getName());
                
                row.add(number);
                row.add(colorDot);
                row.add(name);
                panel.add(row);
            }
            
            TimedDialog.oneUse(this, panel, "Game Starts!", JOptionPane.PLAIN_MESSAGE);
        });
    }
    
    public void showGameStartDialog(Game game) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("Welcome to Risiko!");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(title);
            panel.add(Box.createVerticalStrut(10));

            JLabel setupTitle = new JLabel("Setup Phase:");
            setupTitle.setFont(setupTitle.getFont().deriveFont(Font.BOLD));
            setupTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(setupTitle);
            panel.add(Box.createVerticalStrut(5));

            int troops = game.getLobby().getPlayers().get(0).getAvailableTroops();
            String[] lines = {
                "Each player has " + troops + " troops to place on their countries.",
                "One troop has already been placed for on each one of your countries.",
                "Place troops by left clicking on your countries.",
                "When all players have placed all troops the game will start.",
                "You can only place on countries you own.",
                "You cannot withdraw troops during setup, so choose carefully"
            };

            for (String line : lines) {
                JLabel label = new JLabel("• " + line);
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(label);
            }

            JOptionPane.showMessageDialog(this, panel, "Game On!", JOptionPane.PLAIN_MESSAGE);
        });
    }
    
    public void showNextPhaseDialog(TurnPhase phase) {
    	TimedDialog.oneUse(this, ""+phase+ " phase started", "info", JOptionPane.PLAIN_MESSAGE);
    }
    
    public void showNotEnoughTroops() {
    	TimedDialog.oneUse(this, "not enough troops", "info", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showNotBordering() {
    	TimedDialog.oneUse(this, "countries do not border with each other", "info", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showCountryNotOwned() {
    	TimedDialog.oneUse(this, "you don't own this country", "info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showCannotAttackInMovePhase() {
        TimedDialog.oneUse(this, "cannot attack during the move phase", "info", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showCannotMoveInAttackPhase() {
        TimedDialog.oneUse(this, "cannot move during the attack phase", "info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showEndSetup() {
    	TimedDialog.oneUse(this, "Setup phase completed.\nWait for others players", "info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showGotEliminatedDialog() {
    	TimedDialog.oneUse(this, "You have been eliminated!", "Eliminated", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showEliminationDialog(String name) {
    	TimedDialog.oneUse(this, name + " have been eliminated!", "Eliminated", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showAttackResult(AttackReport report) {
        SwingUtilities.invokeLater(() -> {
            String title = switch (report.getResult()) {
                case VICTORY  -> "Victory!";
                case DEFEAT   -> "Defeat";
                case ADVANTAGE -> "Advantage";
                case TIE      -> "Draw";
            };

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel resultLabel = new JLabel(title);
            resultLabel.setFont(resultLabel.getFont().deriveFont(Font.BOLD, 14f));
            resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(resultLabel);
            panel.add(Box.createVerticalStrut(8));

            panel.add(makeLabel("Attacker losses: " + report.getAttackerLosses()));
            panel.add(makeLabel("Defender losses: " + report.getDefenderLosses()));
            panel.add(Box.createVerticalStrut(5));
            panel.add(makeLabel("Attacker remaining: " + report.getAttackerRemaining()));
            panel.add(makeLabel("Defender remaining: " + report.getDefenderRemaining()));
            panel.add(Box.createVerticalStrut(5));
            panel.add(makeLabel(report.isCloseFight() ? "Close fight!" : "One-sided battle."));

            TimedDialog.oneUse(this, panel, title, JOptionPane.PLAIN_MESSAGE, 5, TimeUnit.SECONDS);
        });
    }
    
    // no edt invokelater insde the method because it is needed from it to block the edt
    public void showWinnerDialog(Player winner) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("🏆 " + winner.getName() + " wins!");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(title);
            panel.add(Box.createVerticalStrut(10));

            RoundPanel colorDot = new RoundPanel(15, winner.getColor().getColor());
            colorDot.setPreferredSize(new Dimension(20, 20));
            colorDot.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(colorDot);

            
            
            JOptionPane.showMessageDialog(this, panel, "Game Over", JOptionPane.PLAIN_MESSAGE);
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    // #### SPECTATOR #####################################
    
    public void setSpectatorMode(boolean value) {
    	gameView.setSpectatorMode(value);
    	lobbyView.setSpectatorMode(value);
    }

    // #### GETTERS ####

    public GameView getGameView() { return gameView; }
    public LobbyView getLobbyView() { return lobbyView; }
}