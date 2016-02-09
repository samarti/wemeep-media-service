package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by the mighty and powerful santiagomarti on 2/5/16.
 * All those who dare, in any way, copying or somehow reproducing
 * the God-inspired earth-shattering 8th Wonder
 * code below, shall be punished for 47 days straight on the realms
 * of the 9th Circle of Hell, while watching "Friday" sang live by
 * Rebecca Black
 */
public class CommentController {

    private static Gson gson = new Gson();

    public String postNewComment(String rootMeepId, String body) throws Exception {
        Validator validator = new Validator();
        Comment com = gson.fromJson(body, Comment.class);
        if (!validator.validateComment(com)) {
            throw new Exception("Missing fields");
        }
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(ApiController.MEEP_SERVICE_URL + "/" + rootMeepId + "/comments");

        JsonObject json = new JsonObject();
        json.addProperty("senderName", com.senderName);
        json.addProperty("senderId", com.senderId);
        json.addProperty("type", "picture");
        StringEntity entity = new StringEntity(json.toString());
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        JsonParser parser = new JsonParser();
        JsonObject aux = parser.parse(result.toString()).getAsJsonObject();
        if(aux.has("Error"))
            throw new Exception("Error from Meep Service: " + aux.get("Error").getAsString());
        return aux.get("id").getAsString();
    }

    private class Comment {
        public String senderName, message, senderId, objectId, type;

    }

    private class Validator {

        public boolean validateComment(Comment c){
            if(c.type == null || (!c.type.equals("text") && !c.type.equals("picture")))
                return false;
            if(c.type.equals("text"))
                if(c.message == null || c.senderId == null || c.senderName == null)
                    return false;
            if(c.type.equals("picture"))
                if(c.senderId == null || c.senderName == null)
                    return false;
            return true;
        }
    }


}
