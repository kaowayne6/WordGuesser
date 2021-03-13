import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/*
 * This is the client code for the game
 * The client changes certain variables in GameCommunicator and sends it back to server for answers
 * such as if letter is in word to guess, number of letters, and number of remaining guesses
 */
public class WordGuessClientRG extends Application {
	
	//Game info variables
	int clientNum = -1;
	GameCommunicator info = new GameCommunicator(-1);
	Client clientConnection;
	Stage pStage;
	
	ArrayList<Character> usedLetters = new ArrayList<Character>();
	
	static int port = 5555;
	static String ip = "127.0.0.1";
	
	// Constants for the size of the window
	public static final int WIDTH = 480;
	public static final int HEIGHT = 270;
	
	// Values for what attempt this is, what player we are, etc
	int attemptNumber, playerNumber, guessesLeft;
	String category = "blank.";
	String currentWord = "____";
	String categorySceneTitle = "Let's Guess Words!";
	String resultsSceneTitle = "Congratulations! You've finished the game!";
	
	// Map that contains the different scenes
	HashMap<String, Scene> sceneMap;
	Label guessesLabel;
	Label attemptLabel;
	Label titleLabel;
	Label resultsTitleLabel;

	//sends the player's guess to the server to be checked
	public void playerGuess(GameCommunicator player)
	{
		//allow the player to pick which category they want
		//player.category = (user pick);
		info.category = player.category;
		//word is picked and the number
		//take in user input and add it to player and info
		//player.guess = (user input);
		info.guess = player.guess; //send it back to the server

		player.nextRound(); //sets up for the next round of guessing
	}
	
	//This is a inner class that constructs a client socket
	public class Client extends Thread{

		
		Socket socketClient;
		
		ObjectOutputStream out;
		ObjectInputStream in;
		
		private Consumer<Serializable> callback;
		
		Client(Consumer<Serializable> call){
		
			callback = call;
		}
		
		//Sends data to server
		public void send(GameCommunicator data) {
			
			try {
				out.writeObject(data);
				out.reset();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//This is where the program runs and takes care of all incoming data
		public void run() {
			
			try {
				socketClient= new Socket(ip, port);
			    out = new ObjectOutputStream(socketClient.getOutputStream());
			    in = new ObjectInputStream(socketClient.getInputStream());
			    socketClient.setTcpNoDelay(true);
			    System.out.println("Connected to server");
			}
			catch(Exception e) {System.out.println("Error");}
			
			while(true) {
				 
				try
				{
					GameCommunicator message = (GameCommunicator) in.readObject();
					callback.accept(message);
					info.copyVals(message);
					clientNum = info.clientNum;
					
					if(message.gameLost || message.gameWon) {
						//Change screen to win/ lose screen
						Platform.runLater( new Runnable() {
							public void run() {
								pStage.setScene(createResultsScene(pStage, message));
								pStage.show();
							}
						});	
					}

					//When client needs to choose from a category
					else if(message.category == -1 && message.numLetters == -1) {
						
						info = message;
						clientNum = message.clientNum;
						
						//change stage to pick category
						Platform.runLater( new Runnable() {
							public void run() {
								pStage.setScene(createCategoryScene(pStage, message));
								pStage.show();
							}
						});
				
					}
					
					//When client gets object back after it sends category in
					else if(message.category != -1 && message.numLetters != -1 && !message.categoryWon && !message.categoryLose) {
						
						//change stage to gameplay
						Platform.runLater( new Runnable() {
							public void run() {
								pStage.setScene(createGameplayScene(pStage, message));
								pStage.show();
							}
						});
					}
					
					else if(message.categoryLose || message.categoryWon) {
						
						//Resets variables for next round
						message.nextRound();
					
						//change stage to pick category
						Platform.runLater( new Runnable() {
							public void run() {
								pStage.setScene(createCategoryScene(pStage, message));
								pStage.show();
							}
						});	
					}
				
				}
				catch(Exception e) {}
			}
		
	    }


	}
	
	// Method that creates the first scene to put in the server info
		public Scene createStartScene(Stage stage)
		{
			
			// Create labels that go next to fields
			Label greeting, enterPort, enterIP;
			greeting = new Label("Greetings!");
			enterPort = new Label("Enter port #: ");
			enterIP = new Label("Enter IP address: ");
			
			// Create the fields for entering port and ip
			TextField portField, ipField;
			portField = new TextField("5555");
			ipField = new TextField("127.0.0.1");
			
			// Create the button to try and connect to the server
			Button enterButton = new Button("Enter");
			enterButton.setOnAction(e -> {
				ip = ipField.getText();
				port = Integer.parseInt(portField.getText());
				stage.setScene(createWaitingGui(pStage));
				
				clientConnection = new Client((data) -> {
					Platform.runLater(()->{
						info = (GameCommunicator)data;
					});

				});
				
				clientConnection.start();
				
			});
			
			// Organize all the nodes
			HBox portLine = new HBox(8, enterPort, portField);
			HBox ipLine = new HBox(8, enterIP, ipField);
			
			portLine.setAlignment(Pos.CENTER);
			ipLine.setAlignment(Pos.CENTER);
			
			VBox sceneLayout = new VBox(8, greeting, portLine, ipLine, enterButton);
			sceneLayout.setAlignment(Pos.CENTER);
			sceneLayout.setPadding(new Insets(16, 16, 16, 16));
			
			BorderPane mainPane = new BorderPane();
			mainPane.setCenter(sceneLayout);
			mainPane.setStyle("-fx-background-color: #ff00ff");
			
			return new Scene(mainPane, WIDTH, HEIGHT);
			
		}// end createStartScene()

		
		// Method that creates the scene to pick a category
		public Scene createCategoryScene(Stage stage, GameCommunicator c)
		{
			
			// Label that contains the attempt number
			int attemptTotalCat = c.attemptCat1 + c.attemptCat2 + c.attemptCat3 + 1;
			if(!c.canPickCat1)
				attemptTotalCat++;
			if(!c.canPickCat2)
				attemptTotalCat++;
			if(!c.canPickCat3)
				attemptTotalCat++;
			attemptLabel = new Label("Attempt #" + attemptTotalCat);
			attemptLabel.setAlignment(Pos.TOP_LEFT);
			attemptLabel.setPadding(new Insets(8, 8, 8, 8));
			
			// Label that contains the player number
			Label playerLabel = new Label("You are player #" + clientNum);
			playerLabel.setAlignment(Pos.BOTTOM_RIGHT);
			playerLabel.setPadding(new Insets(8, 8, 8, 8));
			
			titleLabel = new Label(categorySceneTitle);
			Label categoryLabel = new Label("Pick a category! ");
			
			// Create the dropdown list
			ArrayList<String> categoryStrings = new ArrayList<String>();
			if(info.canPickCat1)
				categoryStrings.add("Superheroes");
			if(info.canPickCat2)
				categoryStrings.add("Desserts");
			if(info.canPickCat3)
				categoryStrings.add("Transportation");

			ComboBox<String> categoriesMenu = new ComboBox<String>();
			categoriesMenu.getItems().addAll(categoryStrings);
			categoriesMenu.setValue("<Select>");
			
			// Create the next button
			Button nextButton = new Button("Next");
			nextButton.setOnAction(e -> {
				
				// Don't continue if a category hasn't been chosen
				if (categoriesMenu.getValue().equals("<Select>")) {
					return;
				}
				else
				{
					//depends on the category choice
					if(categoriesMenu.getValue().equals("Superheroes"))
					{
						info.category = 1;
					}
					else if(categoriesMenu.getValue().equals("Desserts"))
					{
						info.category = 2;
					}
					else if(categoriesMenu.getValue().equals("Transportation"))
					{
						info.category = 3;
					}
					
					clientConnection.send(info);
				}
				
				// Go to the guessing scene
				stage.setScene(createWaitingGui(pStage));
				
			});
			
			// Organize everything now
			BorderPane mainPane = new BorderPane();
			
			HBox categoryLine = new HBox(8, categoryLabel, categoriesMenu);
			categoryLine.setAlignment(Pos.CENTER);
			
			VBox sceneLayout = new VBox(8, titleLabel, categoryLine, nextButton);
			sceneLayout.setAlignment(Pos.TOP_CENTER);
			sceneLayout.setPadding(new Insets(16, 16, 16, 16));
			
			mainPane.setTop(attemptLabel);
			mainPane.setCenter(sceneLayout);
			mainPane.setBottom(playerLabel);
			
			mainPane.setStyle("-fx-background-color: #ff00ff");
			
			return new Scene(mainPane, WIDTH, HEIGHT);
			
		}// end createCategoryScene()
		
		
		// Method that creates the gameplay scene where you guess the letters
		public Scene createGameplayScene(Stage stage, GameCommunicator c)
		{
			
			// Label that contains the attempt number
			int attemptInCat = -1;
			if(info.category == 1) {
				attemptInCat = c.attemptCat1+1;
			}
			else if(info.category == 2) {
				attemptInCat = c.attemptCat2+1;
			}
			else {
				attemptInCat = c.attemptCat3+1;
			}
			Label attemptLabel = new Label("Attempt #" + attemptInCat);
			attemptLabel.setAlignment(Pos.TOP_LEFT);
			attemptLabel.setPadding(new Insets(8, 8, 8, 8));
			
			// Label that contains the player number
			Label playerLabel = new Label("You are player #" + clientNum);
			playerLabel.setAlignment(Pos.BOTTOM_RIGHT);
			playerLabel.setPadding(new Insets(8, 8, 8, 8));
			
			String strCategory = "";
			if(c.category == 1) {
				strCategory = "Superheros";
			}
			else if(c.category == 2) {
				strCategory = "Dessert";
			}
			else {
				strCategory = "Transportation";
			}
			Label titleLabel = new Label("Category: " + strCategory);
			
			Label wordLabel = new Label("Word: " + c.word);
			wordLabel.setFont(new Font(32));
			
			//guessesLeft = 6;
			guessesLabel = new Label("Guesses left: " + c.numGuesses);
			
			Label enterGuess = new Label("Enter letter guess: ");
			TextField guessField = new TextField();
			Button guessButton = new Button("Guess");
			guessButton.setOnAction(e -> {
				
				//guessButton.setDisable(true);
				boolean didUseLetter = false;
				
				for(int i = 0; i < usedLetters.size(); i++) {
					if(Character.toLowerCase(usedLetters.get(i)) == Character.toLowerCase(guessField.getText().charAt(0))) {
						didUseLetter = true;
						break;
					}
				}
				
				if(!didUseLetter) {
					c.guess = Character.toLowerCase(guessField.getText().charAt(0)); //gets the guess of the player
					clientConnection.send(info);
				}
				
			});
			
			// Organize everything now
			BorderPane mainPane = new BorderPane();
			
			HBox guessRow = new HBox(8, enterGuess, guessField, guessButton);
			guessRow.setAlignment(Pos.CENTER);
			
			VBox sceneLayout = new VBox(8, titleLabel, wordLabel, guessesLabel, guessRow);
			sceneLayout.setAlignment(Pos.TOP_CENTER);
			sceneLayout.setPadding(new Insets(16, 16, 16, 16));
			
			mainPane.setTop(attemptLabel);
			mainPane.setCenter(sceneLayout);
			mainPane.setBottom(playerLabel);
			
			mainPane.setStyle("-fx-background-color: #ff00ff");
			
			return new Scene(mainPane, WIDTH, HEIGHT);
			
		}// end createGameplayScene()
		
		
		// Method to create the results of a game (whether you win or lose)
		public Scene createResultsScene(Stage stage, GameCommunicator c)
		{
			
			// Create the label for the title
			resultsTitleLabel = new Label(resultsSceneTitle);
			Label gameResults;
			if(c.gameLost) {
				gameResults = new Label("You lost the game");
			}
			else {
				gameResults = new Label("You won the game");
			}
			
			// Create the buttons for restarting and quitting
			Button restartButton = new Button("Restart");
			restartButton.setOnAction(e -> {
				
				c.playAgain = true;
				stage.setScene(createWaitingGui(stage));
				clientConnection.send(c);
				
			});
			
			Button quitButton = new Button("Quit");
			quitButton.setOnAction(e -> {
				System.exit(0);
			});
			
			// Organize everything
			BorderPane mainPane = new BorderPane();
			
			HBox buttons = new HBox(64, restartButton, quitButton);
			buttons.setAlignment(Pos.CENTER);
			
			VBox sceneLayout = new VBox(32, resultsTitleLabel, gameResults, buttons);
			sceneLayout.setAlignment(Pos.CENTER);
			
			mainPane.setCenter(sceneLayout);
			
			mainPane.setStyle("-fx-background-color: #ff00ff");
			
			return new Scene(mainPane, WIDTH, HEIGHT);
			
		}// end createResultsScene()
		
		
		// Method to change the text on the title for the category scene
		public void setCategorySceneTitle(String title)
		{
			
			categorySceneTitle = title;
			titleLabel.setText(categorySceneTitle);
			
		}// end setCategorySceneTitle()
		
		
		// Method to change the text on the title for the results scene
		public void setResultsSceneTitle(String title)
		{
			
			resultsSceneTitle = title;
			resultsTitleLabel.setText(resultsSceneTitle);
			
		}// end setResultsSceneTitle()
		
		
		// Method to reset everything for when you restart the game
		public void resetGame()
		{
			
			setCategorySceneTitle("Let's Guess Words!");
			setResultsSceneTitle("Congratulations! You've finished the game!");
			
		}// end resetGame()
		
		/*
		 * All the GUI scenes
		 */
		public Scene createWaitingGui(Stage stage) {
				
			BorderPane pane = new BorderPane();
			pane.setPadding(new Insets(70));
			pane.setStyle("-fx-background-color: green");
			
			Label waitingLabel= new Label("Waiting for server response");
			waitingLabel.setFont(new Font("Cambria", 15));
			waitingLabel.setTextFill(Color.web("white"));
			
			pane.setCenter(waitingLabel);
			return new Scene(pane, 800, 600);
			
		}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("Word Guess Client");
		pStage = primaryStage;
		
		primaryStage.setScene(createStartScene(primaryStage));
		primaryStage.show();
	}

}
