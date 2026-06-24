package client;

import java.awt.EventQueue;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import client.network.ClientNetwork;
import client.view.StartUpFrame;
import model.Player;

public class ClientApp {
	
	private static final int DEFAULT_SERVER_PORT = 1234;

	public static void main(String[] args) {
		try {
			FlatMacDarkLaf.setup();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    EventQueue.invokeLater(new Runnable() {
	        public void run() {
	            StartUpFrame frame = new StartUpFrame();
	            frame.setVisible(true);
	        }
	    });
	}
}
