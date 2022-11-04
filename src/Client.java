import java.io.*;
import java.net.*;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.awt.*;
import java.awt.event.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public class Client implements ActionListener {

    private String username;
    public static String roomToJoin;

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private JFrame frame;
    private JButton btnUpload;
    private JTextArea enteredText;
    private JTextField typedText;
    public static JProgressBar progressBar;
    public static JProgressBar progressBarUpload;
    public static boolean isPaused = false;
    public static boolean isPausedUpload = false;

    private DefaultListModel<String> listModelUsers;
    private JList<String> usersList;
    private DefaultListModel<String> listModelRooms;
    private JList<String> roomsList;

    public static ArrayList<File> uploadedFiles;

    public static ArrayList<String> searchFiles;
    public static ArrayList<String> searchNames;
    public static int searchNum;

    public static String ip;

    // public static String strKey, strMainKey, strKeysKey;
    public static String key, mainKey, keysKey;
    public static String encryptedKey;

    public static int receivePort;

    /**
     * Constructor forPerforms actions regarding the GUI
     * 
     * @param e for the action performed
     * @throws NoSuchAlgorithmException
     */
    public Client(Socket socket, ObjectInputStream ois, ObjectOutputStream oos, String username)
            throws NoSuchAlgorithmException {
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
        this.username = username;
        uploadedFiles = new ArrayList<File>();
        searchFiles = new ArrayList<String>();
        searchNames = new ArrayList<String>();
        searchNum = -1;
        isPaused = false;
        // mainKey = "ddebf573527a51e64c2d95ad3a627ed350ed3fd6fa074c1ac411a4eb2ae425a7";
        keysKey = randomKey();

        // this.uploadsDir = "uploads";
        // downloadsDir = "downloads/";
        // if (System.getProperty("user.dir").endsWith("src")) {
        // // this.uploadsDir = "../uploads";
        // downloadsDir = "../downloads/";
        // }

        frame = new JFrame();

        btnUpload = new JButton();
        try {
            Image img = ImageIO.read(getClass().getResource("/icons/upload.png"));
            btnUpload.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            System.out.println(ex);
        }

        btnUpload.addActionListener(this);

        enteredText = new JTextArea(10, 32);
        typedText = new JTextField(32);

        listModelUsers = new DefaultListModel<String>();
        listModelUsers.addElement("Online Users:");

        listModelRooms = new DefaultListModel<String>();
        listModelRooms.addElement("Rooms:   ");

        usersList = new JList<String>(listModelUsers);
        roomsList = new JList<String>(listModelRooms);

        enteredText.setEditable(false);
        usersList.setFocusable(false);
        roomsList.setFocusable(false);
        enteredText.setBackground(Color.LIGHT_GRAY);
        typedText.addActionListener(this);

        Container content = frame.getContentPane();
        content.add(new JScrollPane(enteredText), BorderLayout.CENTER);

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        progressBar = new JProgressBar();
        progressBarUpload = new JProgressBar();

        progressBar.setStringPainted(true);
        progressBar.setSize(50, 10);
        progressBar.setVisible(true);
        progressBar.setString("Download");
        // set color to green
        progressBar.setForeground(Color.GREEN);
        progressBarUpload.setStringPainted(true);
        progressBarUpload.setSize(50, 10);
        progressBarUpload.setVisible(true);
        progressBarUpload.setString("Upload");
        // set color to blue
        progressBarUpload.setForeground(Color.BLUE);

        panel1.add(new JScrollPane(usersList));

        // content.add(panel1, BorderLayout.EAST);
        JPanel panel2 = new JPanel();

        panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
        panel2.add(progressBar);

        JButton btnPause = new JButton("Pause");
        JButton btnPauseUpload = new JButton("Pause");
        btnPause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // alternate state of isPaused
                isPaused = !isPaused;
                if (isPaused) {
                    btnPause.setText("Resume");
                } else {
                    btnPause.setText("Pause");
                }
            }
        });
        btnPauseUpload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // alternate state of isPaused
                isPausedUpload = !isPausedUpload;
                if (isPausedUpload) {
                    btnPauseUpload.setText("Resume");
                    // send /paused message object
                    try {
                        oos.writeObject(new Message("/paused", username));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    btnPauseUpload.setText("Pause");
                    // send /resumed message object
                    try {
                        oos.writeObject(new Message("/resumed", username));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                // send message /paused

            }
        });
        btnPause.setSize(50, 10);
        btnPauseUpload.setSize(50, 10);

        panel2.add(btnPause);

        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));
        panel3.add(progressBarUpload);
        // panel3.add(btnPauseUpload);

        JPanel panel4 = new JPanel();
        panel4.setLayout(new BoxLayout(panel4, BoxLayout.Y_AXIS));
        panel4.add(panel2);
        panel4.add(panel3);

        panel1.add(panel4);
        content.add(panel1, BorderLayout.EAST);
        content.add(roomsList, BorderLayout.WEST);

        enteredText.setPreferredSize(new Dimension(300, 50));
        btnUpload.setPreferredSize(new Dimension(40, 40));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(typedText);
        panel.add(btnUpload);
        typedText.requestFocusInWindow();
        content.add(panel, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setTitle("Client: " + username + "    |    Room: General");

        typedText.requestFocusInWindow();
    }

    /**
     * Performs actions regarding the GUI and calls sendMessage
     * 
     * @param e for the action performed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnUpload) {
            JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            jfc.setDialogTitle("Choose file/files: ");
            jfc.setMultiSelectionEnabled(true);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int returnValue = jfc.showDialog(null, "Select");
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File[] files = jfc.getSelectedFiles();
                for (File file : files) {
                    // Files.copy(file.toPath(), new File(uploadsDir, file.getName()).toPath(),
                    // StandardCopyOption.REPLACE_EXISTING);
                    uploadedFiles.add(file);
                }
            }

        } else {
            String text = typedText.getText();

            sendMessage(text);

            typedText.setText("");
            typedText.requestFocusInWindow();
        }
    }

    /**
     * Handles messages typed in input area
     * 
     * @param text the string type in input
     */
    public void sendMessage(String text) {
        if (text.startsWith("/")) {
            String help = "Commands: \n- /exit - shut down application\n- /create <name> - create room\n- /join <room> - join that room\n- /myfiles - list of my uploaded files\n- /search - search for file substring name\n- /download - request to download file\n- /help - show help\n";
            if (text.equals("/exit")) {
                closeEverything();
            } else if (text.equals("/help")) {
                enteredText.insert(help, enteredText.getText().length());
                return;
            } else if (text.startsWith("/create ")) {
                if (text.equals("/create ")) {
                    enteredText.insert("Usage: /create <room>\n", enteredText.getText().length());
                    return;
                }
                String room = "";
                try {
                    room = text.split("/create ", 2)[1];
                } catch (Exception e) {
                    enteredText.insert("Usage: /create <room>\n", enteredText.getText().length());
                    return;
                }
                if (room.isBlank() || !room.matches("^[0-9A-Za-z]*$")) {
                    enteredText.insert("Usage: /create <room>\n", enteredText.getText().length());
                    return;
                }
            } else if (text.startsWith("/join ")) {
                if (text.equals("/join ")) {
                    enteredText.insert("Usage: /join <room>\n", enteredText.getText().length());
                    return;
                }
                roomToJoin = text.split("/join ", 2)[1];
            } else if (text.equals("/myfiles")) {
                enteredText.insert("Uploaded Files:\n", enteredText.getText().length());
                for (File file : uploadedFiles) {
                    enteredText.insert("- " + file.getName() + "\n", enteredText.getText().length());
                }
                return;
            } else if (text.startsWith("/search ")) {
                if (text.equals("/search ")) {
                    enteredText.insert("Usage: /search <string>\n", enteredText.getText().length());
                    return;
                }
                searchNum = -1;
                searchFiles = new ArrayList<String>();
                searchNames = new ArrayList<String>();
            } else if (text.startsWith("/download ")) {
                if (text.equals("/download ")) {
                    enteredText.insert("Usage: /download <index corresponding to searched file>\n",
                            enteredText.getText().length());
                    return;
                }
                String index = text.split(" ", 2)[1];
                int num = -1;
                try {
                    num = Integer.parseInt(index);
                } catch (NumberFormatException e) {
                    enteredText.insert("Index given is not an integer\n", enteredText.getText().length());
                    return;
                }
                if (num < 0 || num >= searchFiles.size()) {
                    enteredText.insert("Index is out of bounds\n", enteredText.getText().length());
                    return;
                }

                key = randomKey();
                try {
                    encryptedKey = encrypt(keysKey, key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // String encryptedKey = "";
                // try {
                // encryptedKey = encrypt(keysKey, key);
                // } catch (Exception e) {
                // e.printStackTrace();
                // }
                // System.out.println(encryptedKey);

                text = "@" + searchNames.get(num) + " /download " + encryptedKey + " " + ip + " " + receivePort + " "
                        + searchFiles.get(num);
            } else {
                enteredText.insert(help, enteredText.getText().length());
                return;
            }
        }

        try {
            Message msg = new Message(text, username);

            oos.writeObject(msg);
            oos.flush();
        } catch (IOException e) {
            System.out.println("Error sending message: " + e.getMessage());

        }
    }

    /**
     * Creates the thread that listens for messages
     */
    public void listenForMessage() {
        ClientListenerThread clientListenerThread = new ClientListenerThread(username, socket, ois, oos, frame,
                enteredText,
                listModelUsers, listModelRooms);
        Thread thread = new Thread(clientListenerThread);
        thread.start(); // waiting for msgs
    }

    static String randomKey() {
        String key = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(161);

        for (int i = 0; i < 32; i++) {

            int index = (int) (key.length() * Math.random());

            sb.append(key.charAt(index));
        }

        String str = sb.toString();

        return str;
    }

    public String encrypt(String k, String str) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(k);
        Key aesKey = new SecretKeySpec(decodedKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        // encrypt the text
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(str.getBytes());
        String s = new String(encrypted);
        return s.replaceAll(" ", "x");
    }

    /**
     * Closes socket and streams neatly
     */
    public void closeEverything() {
        try {
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Closes socket and streams neatly before final connection is made
     */
    public static void closeEverything(ObjectInputStream ois, ObjectOutputStream oos, Socket socket) {
        try {
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Starts of the client side
     * 
     * @throws NoSuchAlgorithmException
     */
    public static void main(String[] args) throws NoSuchAlgorithmException {
        int port = 12345;
        Socket socket = null;

        ip = "";
        // find my ip address
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ip = JOptionPane.showInputDialog("Enter your IP address: ", ip);

        String serverIP = "";
        while (serverIP.isBlank()) {
            serverIP = JOptionPane.showInputDialog("Enter the server IP address: ", "localhost");
        }

        try {
            socket = new Socket(serverIP, port);
        } catch (UnknownHostException e) {
            System.out.println("ERROR: Unknown host");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("ERROR: Couldn't get the connection to " + serverIP);
            System.exit(0);
        }

        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            closeEverything(ois, oos, socket);
        }

        String username = JOptionPane.showInputDialog("Enter your unique username: ");
        while (true) {
            if (username.isBlank() || !username.matches("^[0-9A-Za-z]*$") || username.equals("SERVER")) {
                continue;
            }
            try {
                oos.writeObject(new String(username));
                oos.flush();

                String resp = (String) ois.readObject();
                if (resp.equals("username unique")) {
                    break;
                }
            } catch (Exception e) {
                closeEverything(ois, oos, socket);
            }
            username = JOptionPane.showInputDialog("Enter your unique username: ", "Username already exists");
        }

        Client client = new Client(socket, ois, oos, username);

        client.listenForMessage();
    }
}
