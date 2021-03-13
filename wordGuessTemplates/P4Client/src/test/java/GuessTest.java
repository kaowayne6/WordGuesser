import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GuessTest {

	GameCommunicator c;
	
	@BeforeEach
	void init() {
		c = new GameCommunicator(1);
	}
	
	@Test
	void testGameCommunicatorIntialization() {
		assertEquals("GameCommunicator", c.getClass().getName(), "GameTracker not initialized correctly");
	}
	
	@Test
	void testParameterConstructor() {
		assertEquals(1, c.clientNum, "client number not initalized correctly");
	}
	
	@Test
	void testNextRound() {
		assertEquals("", c.word, "Next round function not resetting values");
	}
	
	@Test
	void testCompleteReset() {
		c.playAgain = true;
		c.completeReset();
		assertFalse(c.playAgain, "Values not being completely reset after complete reset.");
	}
	
	//This tests that complete reset doesn't reset clientNum
	@Test
	void testCompleteResetNotClientNum() {
		c.clientNum = 5;
		c.completeReset();
		assertEquals(5, c.clientNum, "Client number gets reset in complete reset even though it was not needed");
	}
}
