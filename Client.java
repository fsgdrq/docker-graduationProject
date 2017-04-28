package tes;
import java.io.*;
import java.net.Socket;
public class Client {
    public static void send(String str) throws IOException{
        String hostname = "comModule";
        int port = 8974;
        try(Socket sock = new Socket(hostname,port);
            BufferedReader from = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));
            PrintWriter to = new PrintWriter(
                    new OutputStreamWriter(sock.getOutputStream()))
        ){
            to.print(str+"\r\n\r\n");
            to.flush();
        }

    }
}
