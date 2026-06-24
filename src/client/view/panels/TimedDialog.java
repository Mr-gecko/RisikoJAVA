package client.view.panels;

import java.awt.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class TimedDialog {

	private static final int showtime = 1850;
	private static final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
	
	private ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
	private JDialog dialog;
	private boolean stop = false;
	
	public TimedDialog(JDialog dialog) {
		 this.dialog = dialog;
	}
	
	public TimedDialog(Component parent, String message, String title, int messageType) {
		JOptionPane optionPane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE);
		this.dialog = optionPane.createDialog(parent, "Game Starts!");
	}
	
	public TimedDialog(Component parent, JPanel panel, String title, int messageType) {
		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE);
		this.dialog = optionPane.createDialog(parent, "Game Starts!");
	}
	
	public void setStop(boolean value) {
		this.stop = value;
	}

	public void show() {
		if (!stop) {
			exec.schedule(new Runnable() {
				 public void run() {
					 SwingUtilities.invokeLater(() -> {
						 dialog.setVisible(false);
						 dialog.dispose();
					 });
				 }
			 }, showtime, timeUnit);
		}
		dialog.setVisible(true); // to stop if modal
	}
	
	public void show(int showtime, TimeUnit timeUnit) {
		exec.schedule(new Runnable() {
			 public void run() {
				 SwingUtilities.invokeLater(() -> {
					 dialog.setVisible(false);
					 dialog.dispose();
				 });
			 }
		 }, showtime, timeUnit);
		 
		 dialog.setVisible(true); // to stop if modal
	}
	
	public static void oneUse(Component parent, String message, String title, int messageType) {
        SwingUtilities.invokeLater(() -> {
        	new TimedDialog(
	        		new JOptionPane(message, messageType).createDialog(parent, title))
	        	.show();
        });
	}
	
	public static void oneUse(Component parent, JPanel panel, String title, int messageType) {
		SwingUtilities.invokeLater(() -> {
			new TimedDialog(parent, panel, title, messageType)
				.show();
		});
	}
	
	public static void oneUse(Component parent, String message, String title, int messageType, int showTime, TimeUnit timeUnit) {
        SwingUtilities.invokeLater(() -> {
        	new TimedDialog(
	        		new JOptionPane(message, messageType).createDialog(parent, title))
	        	.show(showTime, timeUnit);
        });
	}
	
	public static void oneUse(Component parent, JPanel panel, String title, int messageType, int showTime, TimeUnit timeUnit) {
		SwingUtilities.invokeLater(() -> {
			new TimedDialog(parent, panel, title, messageType)
				.show(showTime, timeUnit);
		});
	}
	
}
