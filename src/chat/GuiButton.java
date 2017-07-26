package chat;

import javax.swing.JButton;

public class GuiButton extends JButton {
	private static final long serialVersionUID = 1L;
	private String name;
	
	private static enum GuiButtonState {
		IDLE, 
		ACTIVE,
		CONNECTED
	};
	GuiButtonState state;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public GuiButton(final String name) {
		this.state = GuiButtonState.IDLE;
		this.name = name;
	}

	private void setState() {
		switch(this.state) {
		case IDLE:
			this.state = GuiButtonState.ACTIVE;
			break;
		case ACTIVE:
			this.state = GuiButtonState.IDLE;
		default:
			break;
		}
	}
}
