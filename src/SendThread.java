import java.io.*;
import java.net.Socket;
import javax.swing.JProgressBar;

public class SendThread implements Runnable {
    private String host, fileName;
    // private boolean isPausedUploaded;
    private JProgressBar progressBar;
    private int port;

    public SendThread(String host, String fileName, JProgressBar progressBar, int port) {
        this.host = host;
        this.fileName = fileName;
        this.progressBar = progressBar;
        this.port = port;

    }

    @Override
    public void run() {

        Socket socket = null;
        ObjectOutputStream oos = null;
        while (true) {
            try {
                socket = new Socket(host, port);
                oos = new ObjectOutputStream(socket.getOutputStream());
                break;
            } catch (IOException e) {
            }
        }

        for (File file : Client.uploadedFiles) {
            if (file.getName().equals(fileName)) {
                try {
                    FileInputStream fileIn = new FileInputStream(file);
                    int bytes = 0;
                    // byte[] buffer = new byte[1 * 1024];
                    // buffer size 1% of file size
                    int bufferSize = (int) file.length() / 100;
                    if (bufferSize > 64 * 1024) {
                        bufferSize = 64 * 1024;
                    }
                    byte[] buffer = new byte[bufferSize];
                    oos.writeObject(file.getName());
                    oos.writeObject(file.length());
                    int fileSize = (int) file.length();
                    int sent = 0;
                    // progressBar.setIndeterminate(true);
                    while ((bytes = fileIn.read(buffer)) != -1) {
                        // calculate sending amount remaining and add to progress bar
                        sent += bytes;
                        progressBar.setValue((int) (sent * 100 / fileSize));

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
                        oos.write(buffer, 0, bytes);
                    }
                    oos.flush();
                    fileIn.close();
                    // if clientliostener is paused, wait until false

                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressBar.setValue(0);
                    progressBar.setIndeterminate(false);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try

        {
            if (oos != null) {
                oos.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {

        }
    }

}
