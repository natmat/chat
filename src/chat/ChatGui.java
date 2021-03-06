package chat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class ChatGui {
	private static MenuListener menuListener;
	private static ButtonListener buttonListener;
	private static GuiButton serverButton;
	private static GuiButton clientButton;
	private static GuiButton udpButton;
	private static JFrame guiFrame;
	private static GuiButton macButton;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private ChatGui() {
		System.out.println("CTOR");
	}

	static void createAndShowGUI() {
		System.out.println("createAndShowGUI");
		try {
			UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		guiFrame = new JFrame("ChatServer");
		guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		guiFrame.setLocation(screen.width - 300, screen.height - 200);

		JPanel guiPanel = new JPanel();
		guiPanel.setLayout(new FlowLayout());		
		guiFrame.add(guiPanel);	

		addButtons(guiPanel);
		addMenu(guiFrame);

		guiFrame.pack();
		guiFrame.setVisible(true);
	}

	private static void addButtons(final JPanel guiPanel) {
		buttonListener = new ButtonListener();
		
		serverButton = new GuiButton("Server", buttonListener);
		udpButton = new GuiButton("UDP", buttonListener);
		macButton = new GuiButton("MAC", buttonListener);
		clientButton = new GuiButton("Client", buttonListener);
		guiPanel.add(serverButton);
		guiPanel.add(udpButton);
		guiPanel.add(macButton);
		guiPanel.add(clientButton);
	}

	private static void addMenu(final JFrame guiFrame) {
		menuListener = new MenuListener();

		JMenuBar menuBar = new JMenuBar();
		JMenu startMenu = new JMenu("Start");
		startMenu.setMnemonic(KeyEvent.VK_S);

		JMenuItem serverMenuItem = new JMenuItem("Server");
		serverMenuItem.setName("Server");
		serverMenuItem.setToolTipText("Start server");
		serverMenuItem.addActionListener(menuListener);
		startMenu.add(serverMenuItem);

		JMenuItem clientMenuItem = new JMenuItem("Client");
		clientMenuItem.setName("Client");
		clientMenuItem.setToolTipText("Start client");
		clientMenuItem.addActionListener(menuListener);
		startMenu.add(clientMenuItem);

		menuBar.add(startMenu);
		guiFrame.setJMenuBar(menuBar);
	}

	class MyPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public MyPanel() {
			setBorder(BorderFactory.createLineBorder(Color.black));
		}

		public Dimension getPreferredSize() {
			return new Dimension(250, 200);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);       
		}  
	}
	
	static class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			GuiButton b = (GuiButton)e.getSource();
			switch(b.getName()) {
			case "Server":
				Chat.serverEvent();
				break;
			case "Client":
				Chat.clientEvent();
				break;
			case "UDP":
				Chat.udpEvent();
				break;
			case "MAC":
				Chat.macEvent();
				break;
			default:
				break;
			}
		}
	}

	static class MenuListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JMenuItem menuItem = (JMenuItem)e.getSource();
			System.out.println("Name:" + menuItem.getName());
			switch(menuItem.getName()) {
			case "Server":
				Chat.serverEvent();
				break;
			case "Client":
				Chat.clientEvent();
				break;
			case "UDP":
				Chat.udpEvent();
				break;
			}
		}
	}


	public static void setServerState(final Color stateColor) {
		serverButton.setBackground(stateColor);
	}

	public static void setUdpState(Color stateColor) {
		udpButton.setBackground(stateColor);
	};

	
	public static void setClientCount(int count) {
		clientButton.setText("Client " + count);
		guiFrame.pack();
	}

	public static void setClientState(Color colour) {
		clientButton.setBackground(colour);
	}
}
