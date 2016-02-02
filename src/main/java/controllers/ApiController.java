package controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;

/**
 * Created by the mighty and powerful santiagomarti on 2/2/16.
 * All those who dare, in any way, copying or somehow reproducing
 * the God-inspired earth-shattering 8th Wonder
 * code below, shall be punished for 47 days straight on the realms
 * of the 9th Circle of Hell, while watching "Friday" sang live by
 * Rebecca Black
 */
public class ApiController {

    static String awsAccessKey = System.getenv().get("AWS_ACCESS_KEY");
    static String awsSecretKey = System.getenv().get("AWS_SECRET_KEY");

    private static SecureRandom random = new SecureRandom();
    private static S3Bucket picturesBucket;
    private static S3Service service;

    public static void init(){
        try {
            AWSCredentials awsCredentials = new AWSCredentials(awsAccessKey, awsSecretKey);
            service = new RestS3Service(awsCredentials);
            picturesBucket = service.getBucket("wemeep-pictures");
        } catch (S3ServiceException e){
            System.out.println(e.toString());
        }
    }

    public static String getRandomString() {
        return new BigInteger(130, random).toString(32);
    }

    public static Response getProfilePicture(Response response, Request request){
        return null;
    }

    public static Response getCommentPicture(Response response, Request request){
        return null;
    }

    /**
     * Saves a file to a temp file, uploads it to S3 bucket, deletes the temp file and returns the path.
     * @param response
     * @param request
     * @return
     */
    public static Response postProfilePicture(Response response, Request request){
        JsonObject ret = new JsonObject();
        OutputStream outputStream = null;
        String id = request.params(":id");
        if(id == null){
            ret.addProperty("Error", "Missing id");
            response.body(ret.getAsString());
            return response;
        }
        try {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/temp");
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig",multipartConfigElement);
            Collection<Part> files = request.raw().getParts();
            if(files.size() == 0 || files.size() > 1){
                ret.addProperty("Error", "No files or more than 1 file detected");
            }
            Part p = (Part) files.toArray()[0];
            String fileName =  getRandomString() + "_" + p.getSubmittedFileName();
            String tempFile = "/wemeep/" + fileName;
            InputStream inputStream = p.getInputStream();
            File auxFile = new File(tempFile);
            auxFile.createNewFile();
            outputStream = new FileOutputStream(auxFile);
            byte[] auxBytes = new byte[1024];
            int read = 0;
            while(inputStream.read(auxBytes, read, 1024) > -1) {
                outputStream.write(auxBytes);
                read += 1024;
            }

            File fileData = new File(tempFile);
            S3Object object = new S3Object(fileData);
            service.putObject(picturesBucket, object);
            ret.addProperty("Success", true);
            ret.addProperty("fileName", fileName);
            outputStream.close();
            inputStream.close();
            DBController controller = new DBController();
            controller.insertProfilePicture(fileName, id);
        } catch (ServletException | S3ServiceException | NoSuchAlgorithmException | IOException e2){
            System.out.println(e2.toString());
            ret.addProperty("Error", e2.toString());
        } finally {
            response.body(ret.toString());
            return response;
        }
    }

    public static Response postCommentPicture(Response response, Request request){
        JsonObject ret = new JsonObject();
        OutputStream outputStream = null;
        JsonParser parser = new JsonParser();
        JsonObject data = parser.parse(request.body()).getAsJsonObject();
        String rootMeepId, commentId, senderId;

        try {
            rootMeepId = data.get("rootMeepId").getAsString();
            commentId = data.get("commentId").getAsString();
            senderId = data.get("senderId").getAsString();
            if(rootMeepId == null || commentId == null || senderId == null)
                throw new Exception();
        } catch(Exception e) {
            ret.addProperty("Error", "Missing fields");
            response.body(ret.getAsString());
            return response;
        }

        try {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/temp");
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig",multipartConfigElement);
            Collection<Part> files = request.raw().getParts();
            if(files.size() == 0 || files.size() > 1){
                ret.addProperty("Error", "No files or more than 1 file detected");
            }
            Part p = (Part) files.toArray()[0];
            String fileName =  getRandomString() + "_" + p.getSubmittedFileName();
            String tempFile = "/wemeep/" + fileName;
            InputStream inputStream = p.getInputStream();

            outputStream = new FileOutputStream(new File(tempFile));
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            File fileData = new File(tempFile);
            S3Object object = new S3Object(fileData);
            service.putObject(picturesBucket, object);
            ret.addProperty("Success", true);
            ret.addProperty("fileName", fileName);
            outputStream.close();
            inputStream.close();
            DBController controller = new DBController();
            controller.insertCommentPicture(fileName, senderId, commentId, rootMeepId);
        } catch (ServletException | S3ServiceException | NoSuchAlgorithmException | IOException e2){
            System.out.println(e2.toString());
            ret.addProperty("Error", e2.toString());
        } finally {
            response.body(ret.toString());
            return response;
        }
    }
}
