package chat;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import chat.ChatStates.ServerState;

public class ChatServerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testStartChatServer() {
		ChatServer server = new ChatServer();
		assertEquals(ServerState.IDLE, server.getState());
	}

	@Test
	public void testHandleServerEvent() {
		fail("Not yet implemented");
	}
}

