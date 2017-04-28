//used with Client.java

import org.json.JSONObject;
public class test {
    public static void main(String[] args)throws Exception{
        String result = createJson("Clothesline","Camera","morning");
        Client.send(result);
    }
    static String createJson(String nodeName, String targetName,String modeName) throws Exception{
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("from",nodeName);
        jsonObj.put("targetName",targetName);
        jsonObj.put("mode",modeName);
        return jsonObj.toString();
    }
}
