import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ChatServer {

	private static ChatServer chatServer;
	private ServerSocket serverSocket;
	private int threadCount = 0;
	private int activeThreads = 0;
	private final ArrayList<String> users = new ArrayList<>();
	private final ArrayList<String> messages = new ArrayList<>();
	private ArrayList<Integer> accessed = new ArrayList<>();

	//'Sender' thread check for message method
	public synchronized String checkForMessage(int threadID) {
		//Check if there is an available message and the requesting thread hasn't already received it
		if (messages.size() > 0 && !(accessed.contains(threadID))) {
			//Get the first stored message
			String returnMessage = messages.get(0);
			//Add the current thread's ID to the accessed ArrayList so it can't request the same message again
			accessed.add(threadID);
			//Check if access count is equal to the number of active threads
			if (accessed.size() >= activeThreads) {
				//Reset the accessed store
				accessed = new ArrayList<>();
				//Remove the first stored messages as all active threads have processed it
				messages.remove(0);
			}
			return returnMessage;
		} else {
			//Return null to indicate no valid message has been found
			return null;
		}
	}
	
	public ChatServer(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			//Port cannot be used to run the server - display error and stop the program
			System.out.println("Cannot start the server on port " + port);
			System.exit(0);
		}
	}
	
	public void listen() {
		Thread thisThread = new Thread() {
			//Username variable
			private String username = null;
			//Assign a thread ID
			private final int threadID = threadCount;
			SenderThread thisSender;
			
			//Run method
			public void run () {
				//Increment the thread count
				threadCount++;
				try {
					//Accept a connection from a client
					Socket clientSocket = serverSocket.accept();
					System.out.println("[" + threadID + "] Connection accepted (" + serverSocket.getLocalPort() + ":" + clientSocket.getPort() + ")");
					//Initiate next thread to listen for new connection
					activeThreads++;
					chatServer.listen();
					
					//Setup the ability to read the data from the client
					InputStreamReader clientCharStream = new InputStreamReader(clientSocket.getInputStream());
					BufferedReader clientIn = new BufferedReader(clientCharStream);
					//Set up the ability to send the data to the client
					PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
					//Create a new 'sender' thread
					thisSender = new SenderThread(threadID, clientOut, chatServer);
					
					//This thread will act as a 'listener'
					while (true) {
						String userInput;
						try {
							userInput = clientIn.readLine();
							//Handle faulty client disconnect (linux.bath.ac.uk command line)
							if (userInput == null) {
								System.out.println("[" + threadID + "] No data being received - shutting down threads");
								this.shutdown();
								return;
							}
						//Handle a client disconnecting by shutting down the corresponding thread
						} catch (SocketException e) {
							System.out.println("[" + threadID + "] Connection lost - shutting down threads");
							this.shutdown();
							return;
						}
						//Assign username if not already assigned
						if (username == null) {
							if (!users.contains(userInput)) {
								username = userInput;
								users.add(userInput);
								messages.add("> " + username + " has joined the chat.");
								thisSender.setUsername(username);
								clientOut.println("YES");
							} else {
								//Username is already being used on the server so return an error
								clientOut.println("NO");
							}
						//Shutdown server if a client enters 'EXIT' on the terminal
						} else {
							//Format message with username at the front
							String message = "[" + username + "] " + userInput;
							//Add message to the store so the 'sender' threads can pick it up
							messages.add(message);
						}
					}
					
				} catch (IOException e) {
					//Thread has crashed due to IOException so shutdown this thread and start up another
					System.out.println("[" + threadID + "-L] IOException - thread has crashed");
					this.shutdown();
					//Start up a new listener thread
					chatServer.listen();
				}
			}
			
			//Shutdown method
			private void shutdown() {
				//Shutdown sender thread
				thisSender.shutdown();
				//Remove current username from active list
				users.remove(username);
				//Decrement active thread count
				activeThreads--;
			}
		};
		thisThread.start();
	}

	public static void main(String[] args) {
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
		//Initiate server
		chatServer = new ChatServer(port);
		System.out.println("Server listening on port " + port + "...");
		//Begin a thread to listen for the first client connection
		chatServer.listen();
		//Set up the ability to read user input from keyboard
		BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
		//Read input from the console
		while (true) {
			try {
				String consoleInput = userInput.readLine();
				if (consoleInput != null) {
					//If user enters 'EXIT' into the terminal then shut down the server
					if (consoleInput.equalsIgnoreCase("exit")) {
						break;
					}
				}
			} catch (IOException e) {
				System.out.println("IOException - server has crashed.");
				System.exit(1);
			}
		}
		//Shutdown the server
		System.out.println("Shutting down server...");
		//Clients will handle the disconnection on their end
		System.exit(0);
	}

}