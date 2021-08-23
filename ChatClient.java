import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

	private static Socket serverSocket;
	
	public ChatClient(String[] args) {
		//Default port
		int port = 14001;
		//Extract custom port from arguments if specified correctly
		if (args.length > 1) {
			boolean next = false;
			for (String arg : args) {
				if (next) {
					try {
						port = Integer.parseInt(arg);
					//If port is not a valid integer, show error message and use default
					} catch (NumberFormatException e) {
						System.out.println("Port must be a valid integer; using default port 14001.");
					}
				}
				next = arg.equals("-p");
			}
		}
		//Default address
		String address = "localhost";
		try {
			serverSocket = new Socket(address, port);
		} catch (IOException e) {
			//Show error message if connection cannot be established
			System.out.println("Cannot connect to " + address + " on port " + port + ".");
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		//Instantiate ChatClient object
		ChatClient client = new ChatClient(args);
		
		try {
			//Set up the ability to read user input from keyboard
			BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
			//Set up the ability to send the data to the server
			PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true);
			//Set up the ability to read the data from the server
			BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			
			String username;
			String isUserValid = "NO";
			//Username validation
			while (isUserValid.equals("NO")) {
				username = null;
				while (username == null) {
					System.out.print("Please enter a username: ");
					username = userInput.readLine();
					if (username != null) {
						if (username.length() == 0 || username.length() > 20) {
							System.out.println("Invalid username: must be between 1 and 20 characters long.");
							username = null;
						}
					}
				}
				System.out.println("Sending: " + username + " to server.");
				//Send the username to the server
				serverOut.println(username);
				//Check if username was available
				isUserValid = serverIn.readLine();
				if (isUserValid.equals("NO")) {
					//Show an error message to the user
					System.out.println("That username is already in use. Please choose another one.");
				}
			}
			
			//Show message to indicate that the client is connected successfully
			System.out.println("|\nYou are successfully connected to the chat server.");
			System.out.println("Type a message and press 'Enter' to talk.\n|");
			
			//Setup a listening thread
			//Run method
			Thread listeningThread = new Thread(() -> {
				try {
					//Listen for messages from the server and print them as appropriate
					while (true) {
						String serverResponse = serverIn.readLine();
						if (serverResponse == null) {
							//Program cannot connect to server
							System.out.println("Lost connection to the server. Please restart the program.");
							System.exit(0);
						}
						System.out.println(serverResponse);
					}
				} catch (IOException e) {
					//Program cannot connect to server
					System.out.println("Lost connection to the server. Please restart the program.");
					System.exit(0);
				}
			});
			//Start the listening thread
			listeningThread.start();
			
			//Sending loop
			while (true) {
				String userInputString = userInput.readLine();
				//Stop the user from sending an empty line
				if (userInputString.length() != 0) {
					//If user enters exit, quit the program
					if (userInputString.equalsIgnoreCase("exit")) {
						break;
					} else {
						serverOut.println(userInputString);
					}
				}
			}
		} catch (IOException e) {
			//Program has crashed due to IO exception
			System.out.println("IOException - client has crashed, please restart the program.");
			System.exit(1);
		} finally {
			try {
				serverSocket.close();
				System.exit(0);
			} catch (IOException e) {
				//Program has crashed due to IO exception
				System.out.println("IOException - client has crashed, please restart the program.");
				System.exit(1);
			}
		}
	}

}