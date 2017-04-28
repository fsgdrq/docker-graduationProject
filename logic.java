package tes;
import java.io.*;
import java.util.*;
import org.json.*;
public class logic {
    static String name = "Camera";
    static String ID = "00001";
    static String type = "switch";
    static String status = "off";
    static String dir = "/file/mode";             //dir for searching mode files
    static String jsonFile = "/file/test.json";   //COPY test.json /file
    static String modeName;
    static String fromNodeName;
    /*public static void main(String[] args) {
        try {
            //createFile(nodeInfoFile);
            //timer();
            //System.out.println(Client.receive());
            //String data = openJSONfile(jsonFile);
            //addMode(data,"morning");
            //deleteMode("morning");
            //parseJsonFromNodes(createJson("Clothesline","Camera","morning"));
            //chooseNode(modeName,fromNodeName);
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }*/
    static void timer()throws Exception{
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                try {
                    Client.send(NodeInfo());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },10,1000);
    }
    static String NodeInfo()throws Exception{
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("name",name);
        jsonObj.put("id",ID);
        jsonObj.put("status",status);
        jsonObj.put("type",type);
        jsonObj.put("targetName","Local");
        return jsonObj.toString();
    }
    static String openJSONfile(String fullFileName){
        StringBuilder result = new StringBuilder();
        File file = new File(fullFileName);
        if(!file.exists())
            return result.toString();
        try {
            FileReader isr = new FileReader(fullFileName);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                result.append(System.lineSeparator()+line);
            }
            br.close();
            isr.close();
        }catch(Exception e){
            System.out.println("wrongs happen in opening files , maybe the file does not exist .");
            e.printStackTrace();
        }
        return result.toString();
    }
    static void chooseNode(String mode,String fromNodeName){
        String filename = dir+"/"+mode+".json";
        String modeJsonStr = openJSONfile(filename);
        try {
            JSONObject jsonObj = new JSONObject(modeJsonStr);
            JSONArray jsonArray = jsonObj.getJSONArray("nodes");
            int nodeNum = jsonArray.length();
            String[] nodeName= new String[nodeNum];
            for(int j = 0;j<nodeNum;j++){
                String nodeData_1 = jsonArray.getString(j);
                JSONObject node_1 = new JSONObject(nodeData_1);
                int id = Integer.parseInt(node_1.getString("id"));
                String nodename_1 = node_1.getString("name");
                nodeName[id]=nodename_1;
            }
            for(int i = 0;i<nodeNum;i++){
                String nodeData = jsonArray.getString(i);
                JSONObject node = new JSONObject(nodeData);
                String nodename = node.getString("name");
                if(nodename.equals(name)) {
                    JSONArray nodeimport = node.getJSONArray("link_import");
                    int flag = 0;
                    for (int k = 0; k < nodeimport.length(); k++) {
                        String nodeimportlink = nodeimport.getString(k);
                        JSONObject importlink = new JSONObject(nodeimportlink);
                        String source_id = importlink.getString("source_id");
                        int sourceid = Integer.parseInt(source_id);
                        String sourceName = nodeName[sourceid];
                        if(sourceName.equals(fromNodeName)) {
                            flag = 1;
                            break;
                        }
                    }
                    if(flag == 1) {
                        action();
                        JSONArray nodelink = node.getJSONArray("linking");
                        for (int j = 0; j < nodelink.length(); j++) {
                            String linking = nodelink.getString(j);
                            JSONObject nodelinking = new JSONObject(linking);
                            String target_id = nodelinking.getString("target_id");
                            int targetid = Integer.parseInt(target_id);
                            String targetName = nodeName[targetid];
                            String result =createJson(name,targetName,mode);
                            Client.send(result);
                        }
                    }
                    break;
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    static String createJson(String nodeName, String targetName,String modeName) throws Exception{
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("from",nodeName);
        jsonObj.put("targetName",targetName);
        jsonObj.put("mode",modeName);
        return jsonObj.toString();
    }
    static  void parseJsonFromNodes(String jsonStr) throws Exception{
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            fromNodeName = jsonObj.getString("from");
            modeName = jsonObj.getString("mode");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    static void addMode(String jsonStr,String mode) throws JSONException{
        String fileName = dir+"/"+mode+".json";
        createFile(fileName);
        File f = new File(fileName);
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(f));
            output.write(jsonStr);
            output.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    static boolean createFile(String newfileName){
        File f = new File(newfileName);
        if(f.exists()){
            System.out.println("File has been existed :"+f.getAbsoluteFile());
            return false;
        }
        else{
            try{
                f.createNewFile();
                return true;
            }catch(IOException e){
                e.printStackTrace();
                return false;
            }
        }
    }
    private static void action(){
        if(status.equals("off")) {
            System.out.println(name + ": " + type + "is on .");
            status = "on";
        }
    }
    static void deleteMode(String mode){
        String filename = dir+"/"+mode+".json";
        File file = new File(filename);
        if(file.exists())
            file.delete();
    }
}
