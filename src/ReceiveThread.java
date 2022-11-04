import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.*;

public class ReceiveThread implements Runnable {
    private JProgressBar progressBar;
    private boolean isPaused;
    private JTextArea enteredText;
    private int port;

    public ReceiveThread(JProgressBar progressBar, JTextArea enteredText, int port) {
        this.progressBar = progressBar;
        this.enteredText = enteredText;
        this.port = port;
    }

    @Override
    public void run() {

        ServerSocket serverSocket = null;
        Socket socket = null;
        ObjectInputStream ois = null;
        String fileName = null;
        try {
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            ois = new ObjectInputStream(socket.getInputStream());

            fileName = (String) ois.readObject();
            // receive file in chuncks amd update progressBar
            long size = (long) ois.readObject();
            int fileSize = (int) size;

            // calculate buffer size to be 1% of file size
            int bufferSize = fileSize / 100;
            if (bufferSize > 64 * 1024) {
                bufferSize = 64 * 1024;
            }
            byte[] buffer = new byte[bufferSize];

            // byte[] buffer = new byte[1 * 1024];
            int read = 0;
            int totalRead = 0;
            int remaining = fileSize;
            FileOutputStream fos = new FileOutputStream(fileName);
            while ((read = ois.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
                // System.out.println("read " + totalRead + " bytes.");
                // if paused, wait
                isPaused = Client.isPaused;
                while (isPaused) {
                    isPaused = Client.isPaused;
                    // try {
                    // Thread.sleep(1);
                    // } catch (InterruptedException e) {
                    // e.printStackTrace();
                    // }
                    // System.out.println("paused");
                }
                fos.write(buffer, 0, read);
                progressBar.setValue((int) (totalRead * 100 / fileSize));

                // try {
                // // if file size is > 5MB, sleep for 1ms
                // if (fileSize > 5 * 1024 * 1024) {
                // Thread.sleep(0);
                // } else {
                // Thread.sleep(10);
                // }
                // } catch (InterruptedException e) {
                // e.printStackTrace();
                // }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            if (ois != null) {
                ois.close();
            }
            if (socket != null) {
                socket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
            enteredText.insert("File " + fileName + " received successfully.\n", enteredText.getText().length());
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressBar.setValue(0);
        } catch (IOException e) {

        }

    }

}
