# network-chat
Code sample from Peter Wetherall

This code formed the core functionality of a piece of Java coursework I developed for my Principles of Programming II module.

The task was to develop a TCP-based chat room server and client.

### Server
- The server can be started up by compiling and running the class ChatServer (`javac ChatServer.java`, `java ChatServer`)
- It handles multiple clients through the use of multi-threading; it uses two threads per client, one for sending and one for receiving data
- If a client disconnects the server will stop the relevant threads and remove any stored information on that client
- An optional custom port can be specified using the argument -p (e.g. `java ChatServer -p 14005`); if no or an invalid custom port is provided, the server will default to 14001
- The server will shut down if the message `EXIT` is entered into the console

### Client
- The client can be started up by compiling and running the class ChatClient (`javac ChatClient.java`, `java ChatClient`)
- The client, after a valid username has been entered, connects to the chat server
- Using multi-threading, the client enables the user to send messages to the server using the console
- It also simultaneously displays any messages from other clients sent via the server in real-time
- An optional custom port can be specified using the argument -p (e.g. `java ChatClient -p 14005`); if no or an invalid custom port is provided, the client will also default to 14001
