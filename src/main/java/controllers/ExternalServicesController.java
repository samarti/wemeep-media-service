package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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
public class ExternalServicesController {

    public String postNewComment(String rootMeepId, String senderName, String senderId, String picName) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(ApiController.MEEP_SERVICE_URL + "/" + rootMeepId + "/comments");

        JsonObject json = new JsonObject();
        json.addProperty("senderName", senderName);
        json.addProperty("senderId", senderId);
        json.addProperty("type", "picture");
        json.addProperty("pictureUrl", picName);
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

    public String addPictureUrlToMeep(String rootMeep, String pictureUrl) throws Exception{
        HttpClient client = new DefaultHttpClient();
        HttpPut put = new HttpPut(ApiController.MEEP_SERVICE_URL + "/" + rootMeep );

        JsonObject json = new JsonObject();
        json.addProperty("pictureUrl", pictureUrl);
        StringEntity entity = new StringEntity(json.toString());
        put.setEntity(entity);
        HttpResponse response = client.execute(put);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
}
