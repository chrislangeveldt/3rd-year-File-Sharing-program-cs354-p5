import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;

/**
 * Class for Server
 * 
 * Compiling in terminal: javac Server.java
 * Usage in terminal: java Server
 */
public class Server {

    private ServerSocket serverSocket;
    private JFrame frame;
    private JTextArea enteredText;
    private JTextField typedText;
    private DefaultListModel<String> listModelUsers;
    private JList<String> usersList;
    private DefaultListModel<String> listModelRooms;
    private JList<String> roomsList;
    public static String mainKey;
    private int portCount;

    /**
     * Constructor for ServerSocket and setup of GUI
     * 
     * @param serverSocket assign the socket to this instance of server
     */
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.mainKey = "ddebf573527a51e64c2d95ad3a627ed350ed3fd6fa074c1ac411a4eb2ae425a7";
        this.portCount = 12346;
        frame = new JFrame();

        // create button with image record.png
        enteredText = new JTextArea(10, 32);
        typedText = new JTextField(32);

        listModelUsers = new DefaultListModel<String>();
        // set background color of list
        listModelUsers.addElement("Online Users:");
        listModelRooms = new DefaultListModel<String>();
        listModelRooms.addElement("Rooms:   ");

        usersList = new JList<String>(listModelUsers);
        roomsList = new JList<String>(listModelRooms);

        enteredText.setEditable(false);
        usersList.setEnabled(false);
        roomsList.setEnabled(false);
        // set text Color
        enteredText.setForeground(Color.WHITE);
        enteredText.setBackground(Color.BLACK);
        usersList.setForeground(Color.WHITE);
        usersList.setBackground(Color.BLACK);
        roomsList.setForeground(Color.WHITE);
        roomsList.setBackground(Color.BLACK);
        typedText.setForeground(Color.WHITE);
        typedText.setBackground(Color.BLACK);

        Container content = frame.getContentPane();
        content.setBackground(Color.BLACK);
        content.setForeground(Color.BLACK);
        content.add(new JScrollPane(enteredText), BorderLayout.CENTER);
        // content.add(typedText, BorderLayout.SOUTH);
        // add button next to typedText
        content.add(usersList, BorderLayout.EAST);
        content.add(roomsList, BorderLayout.WEST);
        enteredText.setPreferredSize(new Dimension(300, 50));

        content.add(typedText, BorderLayout.SOUTH);
        // frame.add(button, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setTitle("SERVER");
        typedText.requestFocusInWindow();
    }

    /**
     * The method that creates threads for handling each client
     */
    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(socket, enteredText, listModelUsers, listModelRooms,
                        portCount);
                portCount++;
                Thread thread = new Thread(clientHandler);
                thread.start();
            }

        } catch (IOException e) {
            closeServerSocket();
        }

    }

    /**
     * Method that closes server socket
     */
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Startup of server
     * 
     * @param args should only contain port number
     * @throws IOException regarding serverSocket creation
     */
    public static void main(String[] args) throws IOException {
        int port = 12345;

        ServerSocket serverSocket = new ServerSocket(port);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
