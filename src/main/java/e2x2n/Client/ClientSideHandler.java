package e2x2n.Client;

import java.io.*;

public class ClientSideHandler implements Runnable {
    private BufferedReader in;

    public ClientSideHandler(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
