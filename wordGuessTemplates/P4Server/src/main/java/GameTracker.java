import java.util.ArrayList;

/*
 * This is the game tracker class
 * this stores information about the games of each individual game
 * specifically on the current game state of each client. This is different from GameCommunicator
 * because this stores the indepth information that will keep track of whether the game is lost or won,
 * what words the user is trying to guess in each category, and which category are guessed already
 */
public class GameTracker {
	int numGuesses;
	int category1Attempt;
	int category2Attempt;
	int category3Attempt;
	Boolean guessedCategory1;
	Boolean guessedCategory2;
	Boolean guessedCategory3;
	String category1Word;
	String category2Word;
	String category3Word;
	ArrayList<String> usedWords;
	
	GameTracker(){
		numGuesses = 6;
		category1Attempt = 0;
		category2Attempt = 0;
		category3Attempt = 0;
		guessedCategory1 = false;
		guessedCategory2 = false;
		guessedCategory3 = false;
		category1Word = "";
		category2Word = "";
		category3Word = "";
		usedWords = new ArrayList<String>();
	}
	
	//This resets the game completely
	void resetGame() {
		numGuesses = 6;
		category1Attempt = 0;
		category2Attempt = 0;
		category3Attempt = 0;
		guessedCategory1 = false;
		guessedCategory2 = false;
		guessedCategory3 = false;
		category1Word = "";
		category2Word = "";
		category3Word = "";
		usedWords = new ArrayList<String>();
	}
	
	//This checks to see if a string is in usedWords arrayList
	boolean isInArr(String str) {
		for(int i = 0; i < usedWords.size(); i++) {
			if(usedWords.get(i) == str){
				return true; 
			}
		}
		
		return false;
	}
}
