import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GuessTest {
	
	GameTracker t;

	@BeforeEach
	void init() {
		t = new GameTracker();
	}
	
	@Test
	void testInitializationTracker() {
		assertEquals("GameTracker", t.getClass().getName(), "GameTracker not initialized correctly");
	}
	
	//Resets tracker values
	@Test
	void testResetTracker() {
		assertEquals("", t.category1Word, "Tracker not reset properly");
	}

	//Resets tracker values
	@Test
	void testArrayResetTracker() {
		assertEquals(0, t.usedWords.size(), "ArrayList not properly cleared out");
	}
	
	@Test
	void testIsInArrTrue() {
		t.usedWords.add("String");
		assertTrue(t.isInArr("String"), "Is in array function not working");
	}
	
	@Test
	void testIsInArrMultiple() {
		t.usedWords.add("String");
		t.usedWords.add("Test");
		t.usedWords.add("Toy");
		assertTrue(t.isInArr("Toy"), "Is in array function not working");
	}
	

}
