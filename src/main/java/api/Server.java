package api;

import controllers.ApiController;
import controllers.DBController;

import static spark.Spark.*;

/**
 * Created by the mighty and powerful santiagomarti on 2/2/16.
 * All those who dare, in any way, copying or somehow reproducing
 * the God-inspired earth-shattering 8th Wonder
 * code below, shall be punished for 47 days straight on the realms
 * of the 9th Circle of Hell, while watching "Friday" sang live by
 * Rebecca Black
 */
public class Server {

    public static void main(String [] args){

        DBController controller = new DBController();
        controller.init();

        ApiController.init();

        get("/", (request, response) -> "WeMeep Media Service");

        get("/pictures/profile/:id", (request, response) -> ApiController.getProfilePicture(response, request).body());

        get("/pictures/comment/:id", (request, response) -> ApiController.getCommentPicture(response, request).body());

        post("/pictures/profile/:id", (request, response) -> ApiController.postProfilePicture(response, request).body());

        post("/pictures/comment/:id", (request, response) -> ApiController.postCommentPicture(response, request).body());

    }
}
