import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Consumer;

import javax.swing.text.View;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/*
 * THis is the server client of the game WordGuesser
 */
public class WordGuessServer extends Application {
	
	//Put all variables that need to be changed here
	Server serverConnect;
	static int port = 0;
	GameCommunicator info;
	
	static int clientsLeft = 0;	//tracks all clients that leave
	
	//Master game tracker of all games
	ArrayList<GameTracker> tracker = new ArrayList<GameTracker>();		//player number -1
	
	//List of words
	ArrayList<String> superheros = new ArrayList<String>();
	ArrayList<String> dessert = new ArrayList<String>();
	ArrayList<String> transportation = new ArrayList<String>();
	
	//This checks if input is a number
	public final static boolean isNumeric(String s) {
	    if (s != null && !"".equals(s.trim()))
	        return s.matches("^[0-9]*$");
	    else
	        return false;
	}   
	
	/*
	 * GUI Variables
	 */
	Label welcomeLabel, portLabel, waitingLabel, currentGameLabel;//currentGameLabel show the message on Scene 3 top
	TextField serverport;//Get the port number input from server listening
	Button serverChoice; // This is the button for Scene 1 "Enter" button;
	HashMap<String, Scene> sceneMap;//sceneMap use to store 3 different GUI scenes on server  
	HBox portBox;
	VBox startBox; // a Container to store the all contents such as labels,TextField,button in scene 1
	Scene startScene; //First Scene in server : Scene1
    BorderPane startPane;
    Server serverConnection;
    PauseTransition pause = new PauseTransition(Duration.seconds(5));
    //Client clientConnection;
    ListView<String> listItemsServer = new ListView<String>();
    ObservableList<String> listArr = FXCollections.observableArrayList();
	
	public Scene createServerGui() {
			
			BorderPane pane = new BorderPane();
			pane.setPadding(new Insets(70));
			pane.setStyle("-fx-background-color: coral");
			
			pane.setCenter(listItemsServer);
		
			return new Scene(pane, 800, 600);
			
	}
	
	/*
	 * This function updates the listView accordingly to the client
	 */
	void updateListView(int idx, GameCommunicator c) {
		GameTracker t = tracker.get(c.clientNum - 1);
		String playerNum = "Player #: " + c.clientNum;
		String guessLeft = "Guess left: " + c.numGuesses;
		
		//makes guess string
		String strGuess = "Guess: ";
		if(c.guess == ' ') {
			strGuess += "Undecided";
		}
		else {
			strGuess += Character.toString(c.guess);
		}
		
		//makes category string
		String categoryDesc = "Category: ";
		if(c.category == 1) {
			categoryDesc += "Superheros";
		}
		else if(c.category == 2) {
			categoryDesc += "Dessert";
		}
		else if(c.category == 3) {
			categoryDesc += "Transportation";
		}
		else {
			categoryDesc += "Undecided";
		}
		
		//makes category left string
		String categoryLeft = "Category Left: ";
		int countCat = 0;
		if(!t.guessedCategory1) {
			countCat++;
		}
		if(!t.guessedCategory2) {
			countCat++;
		}
		if(!t.guessedCategory3) {
			countCat++;
		}
		categoryLeft += countCat;
		
		String endGame = "";
		if(c.gameLost) {
			endGame += "--> Lost";
		}
		else if(c.gameWon) {
			endGame += "--> Won";
		}
		
		String finalString = playerNum + " | " + guessLeft + " | " + strGuess + " | " + categoryDesc + " | " + categoryLeft + " "
				+ endGame;
		
		//"Player #1 | Guesses Left: 6 | Guess: Undecided | Category: Transportation | Category left: 3 --> Lost"
		listArr.set(c.clientNum,finalString);
		listItemsServer.setItems(listArr);
	}
	
	//This function fills the arraylist with words
	void fillCategories() {
		superheros.add("Superman");
		superheros.add("Spiderman");
		superheros.add("Batman");
		
		dessert.add("Cake");
		dessert.add("Ice Cream");
		dessert.add("Cupcake");
		
		transportation.add("Car");
		transportation.add("Train");
		transportation.add("Plane");
	}
	
	//This function is used trying to fill the remaining words to guess (and also making the words to start)
	//It'll check if GameCommunicator guess char is at it's default value ' '. If it is, then make the empty string
	//if not, then fill in words based on guess in GameCommunicator
	public void fillWord(GameCommunicator com) {	// _ _ _ _ _   _ _ _ _ _ 
		boolean makeWord = false;	//boolean to check if we need to fill in letters or make the blank word
		if(com.guess == ' ') {
			makeWord = true;
		}
		
		//Find the client number and find their word for the current category they are at
		String wordToGuess;
		int clientNum = com.clientNum;
		if(com.category == 1) {
			wordToGuess = tracker.get(clientNum-1).category1Word;
		}
		else if(com.category == 2) {
			wordToGuess = tracker.get(clientNum-1).category2Word;
		}
		else {	//category 3
			wordToGuess = tracker.get(clientNum-1).category3Word;
		}
		
		//if user just selected category and we have to show user what letters are needed to be guessed
		String emptyWord = "";
		if(makeWord) {
			for(int i = 0; i < wordToGuess.length(); i++) {
				//Add characters to avoid blurring out here
				if(wordToGuess.charAt(i) == ' ')
					emptyWord += wordToGuess.charAt(i) + " ";
				//Letter user can guess here
				else
					emptyWord += "_ ";
			}
			
			com.word = emptyWord;
			return;
		}
		
		//User is guessing word and need to fill in letters
		else {
			char charGuess = com.guess;
			char[] newWord = com.word.toCharArray();
			// - - -   - - - - -
			// ice cream
			for(int i = 0; i < wordToGuess.length(); i++) {
				if(Character.toLowerCase(wordToGuess.charAt(i)) == Character.toLowerCase(charGuess)) {
					newWord[2*i] = wordToGuess.charAt(i);
				}
			}
			
			com.word = String.valueOf(newWord);
			return;
		}
		
	}
	
	//This function checks to see if category is won or lost or game is still going.
	//If category is lost or won, this will check also if whole game is lost or won.
	void checkGameState(GameCommunicator com) {
		GameTracker curr = tracker.get(com.clientNum-1);
		
		//Check if category is lost
		if(com.numGuesses == 0) {
			com.categoryLose = true;
			com.word = "";
			com.numLetters = -1;
			Random rand = new Random();
			
			//Add category attempt to category
			if(com.category == 1) {
				curr.category1Attempt++;
				com.attemptCat1++;
				
				//Keep looping until you find a word that isn't used
				String cat = superheros.get(rand.nextInt(3));
				while(curr.isInArr(cat)) {
					cat = superheros.get(rand.nextInt(3));
				}
				
				//Assign new word to category
				curr.category1Word = cat;
				
			}
			else if(com.category == 2) {
				curr.category2Attempt++;
				com.attemptCat2++;
				
				//Keep looping until you find a word that isn't used
				String cat = dessert.get(rand.nextInt(3));
				while(curr.isInArr(cat)) {
					cat = dessert.get(rand.nextInt(3));
				}
				
				//Assign new word to category
				curr.category2Word = cat;
				
			}
			else {	//category 3
				curr.category3Attempt++;
				com.attemptCat3++;
				
				//Keep looping until you find a word that isn't used
				String cat = transportation.get(rand.nextInt(3));
				while(curr.isInArr(cat)) {
					cat = transportation.get(rand.nextInt(3));
				}
				
				//Assign new word to category
				curr.category3Word = cat;
				
			}
			
			//Check if game is lost
			if(curr.category1Attempt == 3 ||
				curr.category2Attempt == 3 ||
				curr.category3Attempt == 3) {
				
				com.gameLost = true;
			}
			
			return;
		}
		
		//Check if category is won
		boolean didWin = true;
		for(int i = 0; i < com.word.length(); i++) {
			if(com.word.charAt(i) == '_') {
				didWin = false;
				break;
			}
		}
		
		if(didWin) {
			//change booleans for which category is won/ can be picked
			if(com.category == 1) {
				curr.guessedCategory1 = true;
				com.canPickCat1 = false;
			}
			else if(com.category == 2) {
				curr.guessedCategory2 = true;
				com.canPickCat2 = false;
			}
			else {
				curr.guessedCategory3 = true;
				com.canPickCat3 = false;
			}
			
			com.categoryWon = true;
			
			//Check if won whole game
			if(curr.guessedCategory1 && curr.guessedCategory2 && curr.guessedCategory3) {
				com.gameWon = true;
			}
			
			return;
		}
		
		//Currently category is still not done and return back
		return;
	}
	
	//This function takes care of the behavior of what to do when server gets a letter guess
	//and change variables accordingly
	void guessBehavior(GameCommunicator comm) {
		//Grabs the gameTracker of current client
		GameTracker curr = tracker.get(comm.clientNum-1);
		
		String currWord = "";	//store word of current category
		//Check to see if letter is in the word of current category
		//First check what category word
		if(comm.category == 1) {
			currWord = curr.category1Word;
		}
		else if(comm.category == 2) {
			currWord = curr.category2Word;
		}
		else {	//category 3
			currWord = curr.category3Word;
		}
		
		boolean charInWord = false;
		//Second check to see if guess is in word
		for(int i = 0; i < currWord.length(); i++) {
			if(Character.toLowerCase(currWord.charAt(i)) == Character.toLowerCase(comm.guess)) {
				charInWord = true;
				break;
			}
		}
		
		/*Third: Based on if character is in word-
		 * 	1. If character is not in word
		 * 		-subtract numGuesses from both GameComm and tracker
		 * 		-change guess back to default ' '
		 * 	2. If character is in word
		 * 		-call fillWord function (this function fills in word in GameCommunicator)
		 * 		-change guess back to default ' '
		 */
		if(!charInWord) {	//character not in word
			comm.numGuesses -= 1;
			curr.numGuesses -= 1;
		}
		else {	//character in word
			fillWord(comm);
		}
		
		//Lastly, Call function to check state of game
		checkGameState(comm);
	}
	
	//Inner class Server that deals with making server thread and storing clients
	public class Server{

		int count = 1;	
		ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
		TheServer server;
		private Consumer<Serializable> callback;
		
		
		Server(Consumer<Serializable> call){
		
			callback = call;
			server = new TheServer();
			server.start();
		}
		
		public class TheServer extends Thread{
			
			public void run() {
			
				try(ServerSocket mysocket = new ServerSocket(port)){
			    System.out.println("Server Connected on Port: " + port);
			  
				
			    while(true) {
			
					ClientThread c = new ClientThread(mysocket.accept(), count);
					Platform.runLater( new Runnable() {
			    		public void run() {
			    			System.out.println("client has connected to server: " + "client #" + count);
			    		}
			    	});
					tracker.add(new GameTracker());	//Note the count might be 1 off
					
					//get random word from each category and store it in GameTracker
					GameTracker curr = tracker.get(count-1);
					Random r1 = new Random();
					curr.category1Word = superheros.get(r1.nextInt(3));
					curr.category2Word = dessert.get(r1.nextInt(3));
					curr.category3Word = transportation.get(r1.nextInt(3));
					//Adds category words into used words
					curr.usedWords.add(curr.category1Word);
					curr.usedWords.add(curr.category2Word);
					curr.usedWords.add(curr.category3Word);
					
					clients.add(c);
					c.start();
					
					count++;
					
				    }
				}//end of try
					catch(Exception e) {
						Platform.runLater( new Runnable() {
				    		public void run() {
				    			System.out.println("Server socket did not launch");
				    		}
				    	});
					}
				}//end of while
			}
		

			class ClientThread extends Thread{
				
			
				Socket connection;
				int count;
				ObjectInputStream in;
				ObjectOutputStream out;
				GameCommunicator c;
				
				ClientThread(Socket s, int count){
					this.connection = s;
					this.count = count;	
				}
				
				//This updates certain clients with GameCommunicator
				//the index of the client is count - 1
				public void updateClients(GameCommunicator s, int count) {
					ClientThread t = clients.get(s.clientNum - 1);
					try {
						 t.out.writeObject(s);
						 t.out.reset();
					}
					catch(Exception e) {System.out.println("Error?");}
				}
				
				public void run(){
						
					try {
						in = new ObjectInputStream(connection.getInputStream());
						out = new ObjectOutputStream(connection.getOutputStream());
						connection.setTcpNoDelay(true);	
					}
					catch(Exception e) {
						System.out.println("Streams not open");
					}
					
					System.out.println("Client connected");
					c = new GameCommunicator(count);
					
					Platform.runLater( new Runnable() {
						public void run() {
							listArr.add("Player #" + count + 
									" | Guesses Left: 6 | Guess: Undecided | Category: Undecided | Category left: 3");
						}
					});
					
					updateClients(c, count);
						
						while(true) {
						    try {
						    	GameCommunicator data = (GameCommunicator) in.readObject();
						    	
						    	//When user wins or lose and want to play again
						    	if(data.playAgain) {
						    		tracker.get(data.clientNum-1).resetGame();
						    		data.completeReset();
						    		
						    		//Repopulates category words
						    		GameTracker curr = tracker.get(count-1);
									Random r1 = new Random();
									curr.category1Word = superheros.get(r1.nextInt(3));
									curr.category2Word = dessert.get(r1.nextInt(3));
									curr.category3Word = transportation.get(r1.nextInt(3));
									//Adds category words into used words
									curr.usedWords.add(curr.category1Word);
									curr.usedWords.add(curr.category2Word);
									curr.usedWords.add(curr.category3Word);
									
						    		Platform.runLater( new Runnable() {
										public void run() {
											updateListView(data.clientNum, data);
										}
									});
						    		
						    		updateClients(data, data.clientNum);	
						    	}
						    	
						    	//When user picks category, send user number of letters and word
						    	else if(data.category != -1 && data.numLetters == -1) {
						    		if(data.category == 1) {
						    			data.numLetters = tracker.get(data.clientNum-1).category1Word.length();
						    		}
						    		else if(data.category == 2) {
						    			data.numLetters = tracker.get(data.clientNum-1).category2Word.length();
						    		}
						    		else {	//category is 3
						    			data.numLetters = tracker.get(data.clientNum-1).category3Word.length();
						    		}
						    		
						    		//This changes GameCommunicator.word string to be empty blanks based on word
						    		//Ex. _ _ _ _ _
						    		fillWord(data);
						    		Platform.runLater( new Runnable() {
										public void run() {
											updateListView(data.clientNum, data);
										}
									});
						    		updateClients(data, data.clientNum);
						    	}
						    	
						    	//User entered a guess and guess being sent back to server
						    	else if(data.category != -1 && data.numLetters != -1 &&
						    			data.guess != ' ') {
						    		guessBehavior(data);
						    		Platform.runLater( new Runnable() {
										public void run() {
											updateListView(data.clientNum, data);
										}
									});
						    		updateClients(data, data.clientNum);
						    	}
						    	
						    	else {
						    		//Extra case I didn't account for?
						    	}
						    	
					    	}
						    //Usually when a client leaves
						    catch(Exception e) {
						    	Platform.runLater( new Runnable() {
						    		public void run() {
						    			System.out.println("Client " + count + " quit the game.");
						    		}
						    	});
						    	
						    	break;
						    }
						}
					}//end of run
				
				
			}//end of client thread
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
		
	}

	//This is the starting scene to enter port number for server
	@Override
	public void start(Stage primaryStage) throws Exception {
		fillCategories();
		listItemsServer.setItems(listArr);
		
		//GUI SETUP
		primaryStage.setTitle("(Server) Let's Play Word Guess!!!");
		
		welcomeLabel = new Label("Welcome to Word Guess!");
		welcomeLabel.setFont(new Font("Cambria", 30));
		welcomeLabel.setTextFill(Color.web("white"));
		
		portLabel = new Label("Enter port #:");
		portLabel.setFont(new Font("Cambria", 30));
		portLabel.setTextFill(Color.web("white"));
		
		this.serverChoice = new Button("Enter");
		serverChoice.setDisable(false);
		this.serverChoice.setStyle("-fx-pref-width: 150px");
		//Set pause event
		pause.setOnFinished(e->serverChoice.setDisable(false));
		//Set button listening 
		this.serverChoice.setOnAction(e->{ 
				primaryStage.setScene(sceneMap.get("server"));
			    primaryStage.setTitle("Welcome to Word Guess Server");
			    listArr.add("Client Information:");
			    listItemsServer.setItems(listArr);
			    port = Integer.parseInt(serverport.getText());
				serverConnect = new Server(data -> {
					Platform.runLater(()->{
						info = (GameCommunicator)data;
					});
				});
		 
					//}//end else
		}); //End Enter Button setOnAction
		
		this.serverport = new TextField("5555");
		this.serverport.setStyle("-fx-pref-width: 200px");
		
		portBox = new HBox(20,portLabel,serverport);
		this.startBox = new VBox(30, welcomeLabel,portBox,serverChoice);
		VBox.setMargin(startBox, new Insets(200,100,200,50));
		startPane = new BorderPane();
		startPane.setPadding(new Insets(0));
		BorderPane.setMargin(startBox, new Insets(20,50,300,100));
		startPane.setStyle("-fx-background-image:url(./wordguess.jpg);-fx-background-repeat:no-repeat");
		
		startPane.setCenter(startBox);
		startScene = new Scene(startPane,600,600);
	   
		startScene.setFill(Color.TRANSPARENT);
		
		listItemsServer = new ListView<String>();
		
		sceneMap = new HashMap<String, Scene>();
		
		sceneMap.put("server",  createServerGui());
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent t) {
		        Platform.exit();
		        System.exit(0);
		    }
		});
		
		primaryStage.setScene(startScene);
		primaryStage.show();
	}
	

}
