import java.io.PrintWriter;

public class SenderThread {

    Thread sendingThread;
    int threadID;
    boolean active = true;
    String username = null;

    public SenderThread(int threadID, PrintWriter clientOut, ChatServer server) {
        this.threadID = threadID;
        //Run method
        sendingThread = new Thread(() -> {
            //Sending loop
            String response;
            while (active) {
                response = server.checkForMessage(threadID);
                //If a valid message if found
                if (response != null) {
                    //Send the appropriate message to the client
                    clientOut.println(response);
                }
            }
        });
        sendingThread.start();
    }

    //Method to shutdown this listening thread
    public void shutdown() {
        active = false;
        try {
            sendingThread.join();
        } catch (InterruptedException e) {
            //Thread has crashed due to InterruptedException, so shutdown isn't required
            System.out.println("[" + threadID + "-S] InterruptedException - thread has crashed");
        }
    }

    //Method to receive username from ChatServer class - validation already handled
    public void setUsername(String user) {
        username = user;
    }

}