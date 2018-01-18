import java.io.*;
import java.net.Socket;

public class Client {
    static int port = 8974;

    /* ********   @name     send()
    * *********   @brief    发送字符串
    *********     @param    str：待发送的字符串
    *********     @return   void
    */
    public static void send(String str) throws IOException{
        /*try{
            JSONObject jsonobj = new JSONObject(str);
            name = jsonobj.getString("from");
            targetName = jsonobj.getString("targetName");
            mode = jsonobj.getString("mode");
            TTL = jsonobj.getInt("TTL");
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/
        String hostname = logic.m_strComHostName;
       //System.out.println("trying to connect...hostname: "+hostname+" port: "+port);
        try(Socket sock = new Socket(hostname,port);
            BufferedReader from = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));
            PrintWriter to = new PrintWriter(
                    new OutputStreamWriter(sock.getOutputStream()))
        ){
            //System.out.println("hostname: "+hostname+" port: "+port);
            to.print(str+"\r\n\r\n");
            to.flush();
            System.out.println("send to comModule: "+str);
        }catch(Exception e){
                e.printStackTrace();
        }

    }

    public static void send(String str,String hostname,int port) throws IOException{

        try(Socket sock = new Socket(hostname,port);
            BufferedReader from = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));
            PrintWriter to = new PrintWriter(
                    new OutputStreamWriter(sock.getOutputStream()))
        ){
            to.print(str+"\r\n\r\n");
            to.flush();
            System.out.println("send to "+hostname+" : "+str);
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}

