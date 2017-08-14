package chat;

import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class GuiButton extends JButton {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

	public GuiButton(final String name, final ActionListener listener) {
		super(name);
		
		setActionCommand(name);
		setName(name);
		setOpaque(true);
		setBackground(Color.RED);

		this.addActionListener(listener);
	}
}
