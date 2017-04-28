package tes;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
public class Server
{
    private static class HttpHandler implements Runnable{
        private final Socket sock;
        HttpHandler(Socket client){this.sock = client;}
        @Override
        public void run(){
            try(BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream())))
            {
                StringBuilder str = new StringBuilder();
                String line;
                while((line = in.readLine())!=null){
                    if(line.length() == 0) break;
                    else{
                        str.append(line+'\n');//don't forget \n when using it to parse json
                    }
                }
                String jsonstr = str.toString();
                JSONObject jsonobj = new JSONObject(jsonstr);
                if(jsonobj.has("targetName")) {
                    logic.parseJsonFromNodes(jsonstr);
                    logic.chooseNode(logic.modeName, logic.fromNodeName);
                }
                else{
                    if(jsonobj.has("action")){
                        String action = jsonobj.getString("action");
                        if(action.equalsIgnoreCase("Add")){
                            String mode = jsonobj.getString("mode");
                            logic.addMode(jsonstr,mode);
                        }
                        if(action.equalsIgnoreCase("Delete")){
                            String mode = jsonobj.getString("mode");
                            logic.deleteMode(mode);
                        }
                    }
                }
            }catch(Exception e){
            }
        }
    }
    public static void main(String[] args) throws IOException{
        try{
            logic.timer();
            int port = 4096;
            ServerSocket ss = new ServerSocket(port);
            for(;;){
                Socket client = ss.accept();
                HttpHandler hndlr = new HttpHandler(client);
                new Thread(hndlr).start();
            }
        }catch(Exception e){
        }
    }
}
