package chat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ChatGui {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
				
				ChatServer server = ChatServer.getInstance();
				new Thread(server).start();

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ChatClient client = new ChatClient();
				new Thread(client).start();
			}
		});
	}

	public ChatGui() {

	}

	private static void createAndShowGUI() {
		JFrame guiFrame = new JFrame("ChatServer");
		guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		MyPanel guiPanel = new ChatGui().new MyPanel();
		guiFrame.add(guiPanel);	

		JMenuBar menuBar = new JMenuBar();
		JMenu startMenu = new JMenu("Start");
		startMenu.setMnemonic(KeyEvent.VK_S);

		JMenuItem serverMenuItem = new JMenuItem("Server");
		serverMenuItem.setName("Server");
		serverMenuItem.setToolTipText("Start server");
		serverMenuItem.addActionListener(new ChatGui().new StartMenuListener());
		startMenu.add(serverMenuItem);

		JMenuItem clientMenuItem = new JMenuItem("Client");
		clientMenuItem.setName("Client");
		clientMenuItem.setToolTipText("Start client");
		clientMenuItem.addActionListener(new ChatGui().new StartMenuListener());
		startMenu.add(clientMenuItem);

		menuBar.add(startMenu);
		guiFrame.setJMenuBar(menuBar);

		JButton serverButton = new JButton("Server");
		JButton clientButton = new JButton("Client");
		clientButton.setBackground(Color.RED);
		
		guiPanel.add(serverButton);
		guiPanel.add(clientButton);
		
		guiFrame.pack();
		guiFrame.setVisible(true);
	}

	class StartMenuListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JMenuItem menuItem = (JMenuItem)e.getSource();
			System.out.println("Name:" + menuItem.getName());
			switch(menuItem.getName()) {
			case "Server":
				ChatServer server = ChatServer.getInstance();
				new Thread(server).start();
				break;
			case "Client":
				ChatClient client = new ChatClient();
				new Thread(client).start();
				break;
			}
		}
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
			g.drawString("Chat panel!", 10, 20);
		}  
	}
}

