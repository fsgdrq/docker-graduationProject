import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server
{
    private static class HttpHandler implements Runnable{
        private final Socket sock;
        //
        private final Date initDate;
        HttpHandler(Socket client,Date initDate){this.sock = client;this.initDate= initDate;}
        //
        @Override
        public void run() {
            try(BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()))) {
                StringBuilder str = new StringBuilder();
                String line;
                while((line = in.readLine())!=null) {
                    if(line.length() == 0) {
                        break;
                    }
                    else {
                        str.append(line+'\n');//don't forget \n when using it to parse json
                    }
                }
                String jsonstr = str.toString();
                System.out.println("Recieve: "+jsonstr);
                JSONObject jsonobj = new JSONObject(jsonstr);
                /*String from = jsonobj.getString("from");
                if(from.equalsIgnoreCase("comModule")){
                    String ip = jsonobj.getString("ip");
                    logic.hostname=ip;
                }*/
                if(jsonobj.has("targetID")) {
                    //logic.parseJsonFromNodes(jsonstr);
                    String targetID = jsonobj.getString("targetID");
                    if(targetID.equalsIgnoreCase(logic.m_strDevID)) {
                        while(logic.m_bDataLock)
                        {
                            Thread.sleep(500);                                    //check the lock every 0.5s
                        }
                        logic.m_bDataLock = true;
                        if(jsonobj.has("type"))
                        {
                            String type = jsonobj.getString("type");
                            if(type.equalsIgnoreCase("AIS"))
                            {
                                Client.send(jsonstr,logic.m_strHostIP,8888);
                            }
                        }
                        logic.parseJsonFromNodes(jsonstr);
                        logic.chooseNode(logic.m_strModeName, logic.m_FromNodeID);
                        logic.m_bDataLock = false;
                        System.out.println("now search next node.");
                    }
                }
                else {
                    if(jsonobj.has("mode")){
                        String action = jsonobj.getString("mode");
                        if(action.equalsIgnoreCase("add")){


                            String name = jsonobj.getString("name");
                            logic.addMode(jsonstr,name);
                            System.out.println("ADD success!");
                        }
                        if(action.equalsIgnoreCase("delete")){

                            String name = jsonobj.getString("name");
                            logic.deleteMode(name);
                            System.out.println("Delete success!");
                        }
//                        if(action.equalsIgnoreCase("play")){
//                           Client.send("123","10.10.10.10");
//                        }
//                        if(action.equalsIgnoreCase("stop")){
//                            Client.send("123","10.10.10.10");
//                        }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    static class ThreadPool{
        private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        public void executorTask(HttpHandler task){
            System.out.println("service: accept a request.");
            executor.execute(task);
            System.out.printf("service:ThreadpoolSize:%d，ActiveThreadNum：%d，CompletedTaskNum:%d\n",executor.getPoolSize(),executor.getActiveCount(),executor.getCompletedTaskCount());
        }
        public void endThreadPool() {
            executor.shutdown();
        }
    }
    public static void main(String[] args) throws IOException{
        try{
            logic.m_strComHostName = args[0];
            logic.init();
            logic.timer();
            int port = 4096;
            ServerSocket ss = new ServerSocket(port);
            ThreadPool threadpool = new ThreadPool();
            System.out.println("thread pool build.");
            if(logic.m_bIsInit) {
                for (; ; ) {
                    Socket client = ss.accept();
                    HttpHandler hndlr = new HttpHandler(client,new Date());
                    //new Thread(hndlr).start();
                    threadpool.executorTask(hndlr);
                }
            }
            else{
                System.out.println("something wrong in inititaion.");
            }
            threadpool.endThreadPool();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
//class HttpHandler implements Runnable{
//    private final Socket sock;
//    HttpHandler(Socket client){this.sock = client;}
//    private Date initDate;
//    private String name;
//    public HttpHandler(Date initDate,String name){
//        this.initDate = initDate;
//        this.name = name;
//    }
//    @Override
//    public void run(){
//        Thread t = Thread.currentThread();
//        System.out.printf(" start--------------%s,TaskName:%s,Time:%s\n",t.getName(),this.name,this.initDate);
//        try(BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()))) {
//            StringBuilder str = new StringBuilder();
//            String line;
//            while((line = in.readLine())!=null) {
//                if(line.length() == 0) {
//                    break;
//                }
//                else {
//                    str.append(line+'\n');//don't forget \n when using it to parse json
//                }
//            }
//            String jsonstr = str.toString();
//            System.out.println("Recieve: "+jsonstr);
//            JSONObject jsonobj = new JSONObject(jsonstr);
//                /*String from = jsonobj.getString("from");
//                if(from.equalsIgnoreCase("comModule")){
//                    String ip = jsonobj.getString("ip");
//                    logic.hostname=ip;
//                }*/
//            if(jsonobj.has("targetID")) {
//                //logic.parseJsonFromNodes(jsonstr);
//                String targetID = jsonobj.getString("targetID");
//                if(targetID.equalsIgnoreCase(logic.m_strDevID)) {
//                    logic.parseJsonFromNodes(jsonstr);
//                    logic.chooseNode(logic.m_strModeName, logic.m_FromNodeID);
//                    System.out.println("now search next node.");
//                }
//            }
//            else {
//                if(jsonobj.has("mode")){
//                    String action = jsonobj.getString("mode");
//                    if(action.equalsIgnoreCase("add")){
//                        String name = jsonobj.getString("name");
//                        logic.addMode(jsonstr,name);
//                        System.out.println("ADD success!");
//                    }
//                    if(action.equalsIgnoreCase("delete")){
//                        String name = jsonobj.getString("name");
//                        logic.deleteMode(name);
//                        System.out.println("Delete success!");
//                    }
//                }
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//}
//class ThreadPool{
//    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
//    public void executorTask(HttpHandler task){
//        System.out.println("service: accept a request.");
//        executor.execute(task);
//        System.out.printf("service:ThreadpoolSize:%d，ActiveThreadNum：%d，CompletedTaskNum:%d\n",executor.getPoolSize(),executor.getActiveCount(),executor.getCompletedTaskCount());
//    }
//    public void endThreadPool() {
//        executor.shutdown();
//    }
//}
