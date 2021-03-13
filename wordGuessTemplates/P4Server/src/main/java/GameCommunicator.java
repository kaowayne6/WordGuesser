import java.io.Serializable;

/*
 * This class is the object passed around between the client and the server. 
 * It includes variables that tell what both the server and client info on the current game state
 * and moves the game along.
 */
public class GameCommunicator implements Serializable{
	int clientNum;	//tracks what client this is
	int category;	//what category current client is on
	int numLetters;	//number of letters in word to guess
	int numGuesses;	//number of guesses left in this category
	char guess;		//character the client guessed
	String word;	//words remaining 
	boolean categoryWon;	//Flags true when user guessed the word in current category
	boolean categoryLose;   //Flags true when user didn't guess the word in current category
	boolean gameWon;	//flags when user wins the whole game
	boolean gameLost;	//flags when user losses the whole game
	boolean playAgain;	//flag whether to play again or not at the end of the game
	int attemptCat1;	//how many attempts in category 1
	int attemptCat2;	//how many attempts in category 2
	int attemptCat3;	//how many attempts in category 3
	boolean canPickCat1;	//tells client if they are allowed to pick category 1
	boolean canPickCat2;	//tells client if they are allowed to pick category 2
	boolean canPickCat3;	//tells client if they are allowed to pick category 3
	
	//This constructor takes an int to assign which client number the current client is when
	//a client connects the the server. The server will first make this object and send it to the client.
	GameCommunicator(int num){
		clientNum = num;
		category = -1;
		numLetters = -1;
		numGuesses = 6;
		guess = ' ';
		word = "";
		categoryWon = false;
		categoryLose = false;
		gameWon = false;
		gameLost = false;
		playAgain = false;
		attemptCat1 = 0;
		attemptCat2 = 0;
		attemptCat3 = 0;
		canPickCat1 = true;
		canPickCat2 = true;
		canPickCat3 = true;
	}

	//this "resets" the player for the next round of the game
	public void nextRound()
	{
		category = -1;
		numLetters = -1;
		numGuesses = 6;
		guess = ' ';
		word = "";
		categoryWon = false;
		categoryLose = false;
	}

	//for if the player decides to play again
	//basically reinitalizes ALL variables for new game
	public void completeReset()
	{
		category = -1;
		numLetters = -1;
		numGuesses = 6;
		guess = ' ';
		word = "";
		categoryWon = false;
		categoryLose = false;
		gameWon = false;
		gameLost = false;
		playAgain = false;
		attemptCat1 = 0;
		attemptCat2 = 0;
		attemptCat3 = 0;
		canPickCat1 = true;
		canPickCat2 = true;
		canPickCat3 = true;
	}
	
	public void copyVals(GameCommunicator c) {
		clientNum = c.clientNum;
		category = c.category;
		numLetters = c.numLetters;
		numGuesses = c.numGuesses;
		guess = c.guess;
		word = c.word;
		categoryWon = c.categoryWon;
		categoryLose = c.categoryLose;
		gameWon = c.gameWon;
		gameLost = c.gameLost;
		playAgain = c.playAgain;;
		attemptCat1 = c.attemptCat1;
		attemptCat2 = c.attemptCat2;
		attemptCat3 = c.attemptCat3;
		canPickCat1 = c.canPickCat1;
		canPickCat2 = c.canPickCat2;
		canPickCat3 = c.canPickCat3;
	}
}
