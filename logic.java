/*
************ @author   gmliuruiqiang@126.com
* ********** @date     2017-09-09
* ********** @version  1.1
* *********  @brief    增加了防止并发时信息重定义的锁；增加了Cache型线程池模块。
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
public class logic {
    static String m_strDevName = "";
    static String m_strDevID = "" ;
    static String m_strDevType = "";
    static String m_strStatus = "";
    static String m_strModeDir = "/file/mode";             //dir for searching mode files
    //static String jsonFile = "/file/test.json";   //COPY test.json /file
    static String m_strModeName = "";
    static String m_strNodeInfoFile = "/file/mode/init.json";
    static String m_strHostIP="";
    static String m_FromNodeID;
    static String m_strComHostName;
    static Boolean m_bIsInit = false;
    static Boolean m_bDataLock = false;     //data lock for fromNodeID and fromNodeName

    /* ********   @name     init()
    * *********   @brief   读入/file/mode，初始化一个节点的数据
     *********    @param
     *********    @return void
     */
    static void init(){
        try {
            String strInitJson = openJSONfile(m_strNodeInfoFile);
            JSONObject jsonobj = new JSONObject(strInitJson);
            m_strDevName = jsonobj.getString("name");
            m_strDevID = jsonobj.getString("ID");
            m_strDevType = jsonobj.getString("type");
            m_strStatus = jsonobj.getString("status");
            m_strHostIP = jsonobj.getString("Host");
            m_bIsInit = true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /* ********   @name     changeStatus()
    * *********   @brief   节点变换状态
     *********    @param
     *********    @return void
     */
    static void changeStatus()throws Exception{
        if(m_strStatus.equals("on")){
            m_strStatus = "off";
            JSONObject obj = new JSONObject();
            obj.put("targetID","web");
            obj.put("id",m_strDevID);
            obj.put("name",m_strDevName);
            obj.put("status",m_strStatus);
            Client.send(obj.toString());
        }
        else{
            m_strStatus = "on";
            JSONObject obj = new JSONObject();
            obj.put("targetID","web");
            obj.put("id",m_strDevID);
            obj.put("name",m_strDevName);
            obj.put("status",m_strStatus);
            Client.send(obj.toString());
        }
    }

    /* ********   @name     statusOn()
    * *********   @brief   节点变换状态为开
    *********    @param
    *********    @return void
    */
    static void statusOn()throws  Exception{
        System.out.println("Dev Name: "+m_strDevName+"Dev ID: "+m_strDevID+" [Status on]");
        if(!m_strStatus.equalsIgnoreCase("on"))
        {
            m_strStatus = "on";
            JSONObject obj = new JSONObject();
            obj.put("targetID","web");
            obj.put("id",m_strDevID);
            obj.put("name",m_strDevName);
            obj.put("status",m_strStatus);
            Client.send(obj.toString());
        }
    }

    /* ********   @name     statusOff()
    * *********   @brief    节点变换状态为关
    *********     @param
    *********     @return   void
    */
    static void statusOff()throws Exception{
        System.out.println("Dev Name: "+m_strDevName+"Dev ID: "+m_strDevID+" [Status off]");
        if(!m_strStatus.equalsIgnoreCase("off"))
        {
            m_strStatus = "off";
            JSONObject obj = new JSONObject();
            obj.put("targetID","web");
            obj.put("id",m_strDevID);
            obj.put("name",m_strDevName);
            obj.put("status",m_strStatus);
            Client.send(obj.toString());
        }
    }

    /* ********   @name     timer()
    * *********   @brief    设定定时发送心跳包
    *********     @param
    *********     @return   void
    */
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
        },10,60000);
    }

    /* ********   @name      NodeInfo()
   * *********    @brief     生成心跳包
   *********      @param
   *********      @return    String 返回心跳包字符串
   */
    static String NodeInfo()throws Exception{
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("name",m_strDevName);
        jsonObj.put("id",m_strDevID);
        jsonObj.put("type",m_strDevType);
        jsonObj.put("status",m_strStatus);
        jsonObj.put("targetID","Registry");
        jsonObj.put("targetName","Registry");
        return jsonObj.toString();
    }

    /* ********   @name     openJSONfile()
    * *********   @brief    打开JSON模式文件
    *********     @param
    *********     @return   String 返回JSON字符串
    */
    static String openJSONfile(String fullFileName){
        StringBuilder result = new StringBuilder();
        File file = new File(fullFileName);
        if(!file.exists())
            return result.toString();                                              //  **ATENTION** corrupted?
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

    /* ********   @name     chooseNode()
    * *********   @brief    选择下一个转发节点
    *********     @param    mode：模式名
    *********     @param    fromNodeName：源节点名
    *********     @return   void
    */
    static void chooseNode(String mode,String fromNodeID)throws InterruptedException{  //send several times
        //System.out.println("now delay");
        Thread.sleep(500);                                        //防止节点太快使先后顺序更明显
        String filename = m_strModeDir+"/"+mode+".json";
        String modeJsonStr = openJSONfile(filename);
        try {
            //System.out.println("try");
            JSONObject jsonObj = new JSONObject(modeJsonStr);
            JSONArray jsonArray = jsonObj.getJSONArray("nodes");
            int nodeNum = jsonArray.length();
            String[] nodeSrc= new String[nodeNum];
            String[] nodeName = new String[nodeNum];
            for(int j = 0;j<nodeNum;j++){
                String nodeData_1 = jsonArray.getString(j);
                JSONObject node_1 = new JSONObject(nodeData_1);
                int id = Integer.parseInt(node_1.getString("id"));
                String src_1 = node_1.getString("src");
                String nodeName_1 = node_1.getString("name");
                nodeSrc[id]=src_1;
                nodeName[id]=nodeName_1;
            }
            for(int i = 0;i<nodeNum;i++){
                String nodeData = jsonArray.getString(i);
                JSONObject node = new JSONObject(nodeData);
                String nodesrc = node.getString("src");
                if(nodesrc.equals(m_strDevID)) {
                    JSONArray nodeimport = node.getJSONArray("link_import");
                    int flag = 0;
                    for (int k = 0; k < nodeimport.length(); k++) {
                        String nodeimportlink = nodeimport.getString(k);
                        JSONObject importlink = new JSONObject(nodeimportlink);
                        String source_id = importlink.getString("source_id");
                        int sourceid = Integer.parseInt(source_id);
                        String source = nodeSrc[sourceid];
                        //System.out.println(source);
                        //System.out.println(fromNodeName);
                        if(source.equals(fromNodeID)) {
                            //System.out.println(source);
                            flag = 1;
                            String act= importlink.getString("target_interface");
                            if(act.equalsIgnoreCase("localx"))
                                statusOn();
                            if(act.equalsIgnoreCase("localy"))
                                statusOff();
                            break;
                        }
                    }
                    if(flag == 1) {
                        action();
                        JSONArray nodelink = node.getJSONArray("linking");
                        JSONArray ja = new JSONArray();
                        for (int j = 0; j < nodelink.length(); j++) {
                            String linking = nodelink.getString(j);
                            JSONObject nodelinking = new JSONObject(linking);
                            String target_id = nodelinking.getString("target_id");
                            int targetid = Integer.parseInt(target_id);
                            String targetSrc = nodeSrc[targetid];
                            String result =createJson(m_strDevID,mode,3,targetSrc);//重发三次后删除
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

    /* ********   @name     chooseNode2()  一次转发
    * *********   @brief    选择下一个转发节点
    *********     @param    mode：模式名
    *********     @param    fromNodeName：源节点名
    *********     @return   void
    */
    static void chooseNode2(String mode,String fromNodeName){      //send as a JSONArray one time
        String filename = m_strModeDir+"/"+mode+".json";
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
                if(nodename.equals(m_strDevName)) {
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
                        changeStatus();   //how to judge on or off
                        action();
                        JSONArray nodelink = node.getJSONArray("linking");
                        JSONArray ja = new JSONArray();
                        for (int j = 0; j < nodelink.length(); j++) {
                            String linking = nodelink.getString(j);
                            JSONObject nodelinking = new JSONObject(linking);
                            JSONObject obj = new JSONObject();
                            String target_id = nodelinking.getString("target_id");
                            int targetid = Integer.parseInt(target_id);
                            String targetName = nodeName[targetid];
                            obj.put("from",m_strDevName);
                            obj.put("targetName",targetName);
                            obj.put("mode",mode);
                            obj.put("TTL",3);
                            //重发三次后删除
                            ja.put(obj);
                        }
                        Client.send(ja.toString());
                    }
                    break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /* ********   @name     createJson()
    * *********   @brief    生成转发的JSON字符串
    *********     @param    nodeName：设备自身节点名
    *********     @param    modeName：模式名
    *********     @param    TTL：生存周期（未启用）
    *********     @param    targetID：目标节点ID
    *********     @return   String 生成转发的JSON字符串
    */
    static String createJson(String nodeID,String modeName,int TTL,String targetID) throws Exception{
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("from",nodeID);
        jsonObj.put("mode",modeName);
        jsonObj.put("TTL",TTL);
        jsonObj.put("targetID",targetID);
        return jsonObj.toString();
    }

    /* ********   @name     parseJsonFromNodes()
    * *********   @brief    解析其他节点发来的JSON字符串
    *********     @param    jsonStr：其他节点发来的JSON字符串
    *********     @return   void
    */
    static  void parseJsonFromNodes(String jsonStr) throws Exception{
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            m_FromNodeID = jsonObj.getString("from");
            m_strModeName = jsonObj.getString("mode");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /* ********   @name     addMode()
    * *********   @brief    增加模式
    *********     @param    jsonStr：增加模式的模式内容
    *********     @param    mode：模式名
    *********     @return   void
    */
    static void addMode(String jsonStr,String mode) throws JSONException{
        String fileName = m_strModeDir+"/"+mode+".json";
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

    /* ********   @name     createFile()
    * *********   @brief    创建模式文件
    *********     @param    newfileName：模式文件名
    *********     @return   boolean true 创建成功
    *********                       false 创建失败
    */
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

    /* ********   @name     action()
    * *********   @brief    节点设备行为（暂无特别行为）
    *********     @param
    *********     @return   void
    */
    private static void action()throws Exception{
        System.out.println(m_strDevName + ": " + m_strDevType + " now is "+m_strStatus);
        Client.send(NodeInfo());
    }

    /* ********   @name     deleteMode()
    * *********   @brief    删除模式
    *********     @param    mode：待删除模式的模式名
    *********     @return   void
    */
    static void deleteMode(String mode){
        String filename = m_strModeDir+"/"+mode+".json";
        File file = new File(filename);
        if(file.exists())
            file.delete();
        else{
            System.out.println("Can not find this mode .");
        }
    }

}

