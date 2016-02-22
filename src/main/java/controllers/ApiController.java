package controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    private static final String ROOT_URL = "http://d1edk0932xwypd.cloudfront.net/";

    public static final String MEEP_SERVICE_URL = "http://54.232.209.214:4567/meeps";

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
        JsonObject ret = new JsonObject();
        try {
            DBController controller = new DBController();
            String id = request.params(":id");
            if(id == null)
                throw new Exception("Missing user id");
            String fileName = controller.getUserPicture(id);
            if(fileName == null)
                throw new Exception("User not found");
            ret.addProperty("url", ROOT_URL + fileName);
        } catch (Exception e){
            ret.addProperty("Error", e.getMessage());
        } finally {
            response.body(ret.toString());
            return response;
        }
    }

    public static Response getCommentPicture(Response response, Request request){
        JsonObject ret = new JsonObject();
        try {
            DBController controller = new DBController();
            String id = request.params(":id");
            if(id == null)
                throw new Exception("Missing comment id");
            String fileName = controller.getCommentPicture(id);
            if(fileName == null)
                throw new Exception("Comment not found");
            ret.addProperty("url", ROOT_URL + fileName);
        } catch (Exception e){
            ret.addProperty("Error", e.getMessage());
        } finally {
            response.body(ret.toString());
            return response;
        }
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
        File auxFile = null;
        if(id == null){
            ret.addProperty("Error", "Missing user id");
            response.body(ret.getAsString());
            return response;
        }
        try {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/temp");
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig",multipartConfigElement);
            Collection<Part> files = request.raw().getParts();
            if(files.size() == 0 || files.size() > 1){
                throw new Exception("No files or more than 1 file detected");
            }
            Part p = (Part) files.toArray()[0];
            if(!p.getName().equals("picture")){
                throw new Exception("File must be called picture");
            }
            String extensionRemoved;
            try {
                String[] auxDotParts = p.getSubmittedFileName().split("\\.");
                extensionRemoved = auxDotParts[auxDotParts.length - 1];
            } catch (Exception e){
                throw new Exception("File must contain extension.");
            }
            String fileName =  (getRandomString() + "_" + p.getSubmittedFileName()).replaceAll("[^A-Za-z0-9 ]", "") + "." + extensionRemoved;
            String tempFile = "/" + fileName;
            //String tempFile = "/Users/santiagomarti/Desktop/" + fileName;
            InputStream inputStream = p.getInputStream();
            auxFile = new File(tempFile);
            auxFile.createNewFile();
            outputStream = new FileOutputStream(auxFile);
            byte[] auxBytes = new byte[1024];
            int read;
            while((read = inputStream.read(auxBytes)) != -1) {
                outputStream.write(auxBytes, 0, read);
            }
            outputStream.close();
            inputStream.close();
            if(auxFile.length() > 800 * 1000)
                throw new Exception("File must be lighter than 800kB");
            DBController controller = new DBController();
            String existentName = controller.getUserPicture(id);
            if(existentName != null)
                service.deleteObject(picturesBucket, existentName);
            S3Object object = new S3Object(auxFile);
            service.putObject(picturesBucket, object);
            ret.addProperty("Success", true);
            ret.addProperty("url", ROOT_URL + fileName);
            controller.upsertProfilePicture(fileName, id);
        } catch (Exception e2){
            System.out.println(e2.toString());
            ret.addProperty("Error", e2.getMessage());
        } finally {
            response.body(ret.toString());
            if(auxFile != null)
                auxFile.delete();
            return response;
        }
    }
    /**
     * Saves a file to a temp file, uploads it to S3 bucket, deletes the temp file and returns the path.
     * @param response
     * @param request
     * @return
     */
    public synchronized static Response postCommentPicture(Response response, Request request) {
        JsonObject ret = new JsonObject();
        OutputStream outputStream;
        String meepId = request.params(":id");
        System.out.println("1");
        File auxFile = null;
        if (meepId == null) {
            ret.addProperty("Error", "Missing meep id");
            response.body(ret.getAsString());
            return response;
        }
        try {
            System.out.println("2");
            //Chequeamos que vengan los datos del sender
            Map<String, String> urlData = Utils.splitQuery(request.queryString());
            if (!urlData.containsKey("senderName") || !urlData.containsKey("senderId"))
                throw new Exception("senderName or senderId missing");

            final File upload = new File("upload");
            if (!upload.exists() && !upload.mkdirs()) {
                throw new RuntimeException("Failed to create directory " + upload.getAbsolutePath());
            }

            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(upload);
            ServletFileUpload fileUpload = new ServletFileUpload(factory);
            List<FileItem> items = fileUpload.parseRequest(request.raw());

            FileItem item = items.stream()
                    .filter(e -> "picture".equals(e.getFieldName()))
                    .findFirst().get();

            if (item == null) {
                throw new Exception("File must be called picture");
            }
            String extensionRemoved;
            try {
                String[] auxDotParts = item.getName().split("\\.");
                extensionRemoved = auxDotParts[auxDotParts.length - 1];
            } catch (Exception e) {
                throw new Exception("File must contain extension.");
            }
            String fileName = (getRandomString() + "_" + item.getName()).replaceAll("[^A-Za-z0-9 ]", "") + "." + extensionRemoved;
            String tempFile = "/" + fileName;
            //String tempFile = "/Users/santiagomarti/Desktop/" + fileName;
            InputStream inputStream = item.getInputStream();
            auxFile = new File(tempFile);
            auxFile.createNewFile();
            outputStream = new FileOutputStream(tempFile);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.close();
            inputStream.close();
            if (auxFile.length() > 1500 * 1000)
                throw new Exception("File must be lighter than 1500kB");
            System.out.println("8");
            //Creamos el comentario en meep service
            ExternalServicesController externalServicesController = new ExternalServicesController();
            String result = externalServicesController.postNewComment(meepId, urlData.get("senderName"), urlData.get("senderId"), fileName);
            JsonParser parser = new JsonParser();
            JsonObject aux = parser.parse(result.toString()).getAsJsonObject();
            if(aux.has("Error"))
                throw new Exception("Error from Meep Service: " + aux.get("Error").getAsString());
            String commentId = aux.get("id").getAsString();
            String createdAt = aux.get("createdAt").getAsString();
            //Guardamos la info de la imagen en DB local
            DBController controller = new DBController();
            String existentName = controller.getCommentPicture(commentId);
            if (existentName != null)
                service.deleteObject(picturesBucket, existentName);
            S3Object object = new S3Object(auxFile);
            service.putObject(picturesBucket, object);
            controller.upsertCommentPicture(fileName, commentId);

            ret.addProperty("Success", true);
            ret.addProperty("url", ROOT_URL + fileName);
            ret.addProperty("id", commentId);
            ret.addProperty("createdAt", createdAt);
        } catch (Exception e2) {
            System.out.println(e2.getMessage());
            ret.addProperty("Error", e2.getMessage());
            ret.addProperty("Error2", e2.toString());
            for (int i = 0; i < e2.getStackTrace().length; i++)
                ret.addProperty("Error: " + i, e2.getStackTrace()[i].toString());
        } finally {
            System.out.println("11");
            response.body(ret.toString());
            if (auxFile != null)
                auxFile.delete();
            return response;
        }
    }

        /**
         * Saves a file to a temp file, uploads it to S3 bucket, deletes the temp file and returns the path.
         * @param response
         * @param request
         * @return
         */
    public synchronized static Response postMeepPicture(Response response, Request request){
        JsonObject ret = new JsonObject();
        OutputStream outputStream;
        String meepId = request.params(":id");
        File auxFile = null;
        if(meepId == null){
            ret.addProperty("Error", "Missing meep id");
            response.body(ret.getAsString());
            return response;
        }
        try {
            final File upload = new File("upload");
            if (!upload.exists() && !upload.mkdirs()) {
                throw new RuntimeException("Failed to create directory " + upload.getAbsolutePath());
            }

            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(upload);
            ServletFileUpload fileUpload = new ServletFileUpload(factory);
            List<FileItem> items = fileUpload.parseRequest(request.raw());

            FileItem item = items.stream()
                    .filter(e -> "picture".equals(e.getFieldName()))
                    .findFirst().get();

            if(item == null){
                throw new Exception("File must be called picture");
            }

            String extensionRemoved;
            try {
                String[] auxDotParts = item.getName().split("\\.");
                extensionRemoved = auxDotParts[auxDotParts.length - 1];
            } catch (Exception e){
                throw new Exception("File must contain extension.");
            }

            String fileName =  (getRandomString() + "_" + item.getName()).replaceAll("[^A-Za-z0-9 ]", "") + "." + extensionRemoved;
            String tempFile = "/" + fileName;
            InputStream inputStream = item.getInputStream();
            auxFile = new File(tempFile);
            auxFile.createNewFile();
            outputStream = new FileOutputStream(tempFile);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.close();
            inputStream.close();
            if(auxFile.length() > 1500 * 1000)
                throw new Exception("File must be lighter than 1500kB");


            DBController controller = new DBController();
            S3Object object = new S3Object(auxFile);
            service.putObject(picturesBucket, object);
            controller.upsertMeepPicture(fileName, meepId);
            ret.addProperty("Success", true);
            ret.addProperty("url", ROOT_URL + fileName);

            ExternalServicesController controller1 = new ExternalServicesController();
            String meepUpdateResult = controller1.addPictureUrlToMeep(meepId, ROOT_URL + fileName);
            JsonParser parser = new JsonParser();
            JsonObject meepUpdateResultJson = parser.parse(meepUpdateResult).getAsJsonObject();
            ret.addProperty("WasMeepUpdated", meepUpdateResultJson.get("Succes").getAsBoolean());
        } catch (Exception e2){
            System.out.println(e2.getMessage());
            ret.addProperty("Error", e2.getMessage());
            ret.addProperty("Error2", e2.toString());
            for(int i = 0; i < e2.getStackTrace().length; i++)
                ret.addProperty("Error " + i, e2.getStackTrace()[i].toString());
        } finally {
            response.body(ret.toString());
            if(auxFile != null)
                auxFile.delete();
            return response;
        }
    }
}
