package controllers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

/**
 * Created by the mighty and powerful santiagomarti on 2/2/16.
 * All those who dare, in any way, copying or somehow reproducing
 * the God-inspired earth-shattering 8th Wonder
 * code below, shall be punished for 47 days straight on the realms
 * of the 9th Circle of Hell, while watching "Friday" sang live by
 * Rebecca Black
 */
public class DBController {

    private static Connection c;

    public void init() {

        try {
            Thread.sleep(10 * 1000);
        } catch(InterruptedException e){
            e.getMessage();
        }

        InetAddress dbAddr = null;
        try {
            dbAddr = InetAddress.getByName("dbmedia");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        c = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://" + dbAddr.getHostAddress() + ":5432/postgres",
                            //.getConnection("jdbc:postgresql://54.233.99.166:49162/postgres",
                            "postgres", "postgres");
            Statement stmt = c.createStatement();
            String profilePictures = "create table if not exists profile_pictures (id SERIAL primary key, userId char(50) not null, fileName char(50) not null," +
                    " createdAt timestamp DEFAULT now() NOT NULL)";
            String commentPictures = "create table if not exists comment_pictures (id SERIAL primary key, senderId char(50) not null, fileName char(50) not null," +
                    " commentId char(50) not null, rootMeepId char(50) not null, " +
                    " createdAt timestamp DEFAULT now() NOT NULL)";
            stmt.execute(profilePictures);
            stmt.execute(commentPictures);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void insertProfilePicture(String fileName, String userId){
        try {
            String insert = String.format("insert into profile_pictures (fileName, userId) values ('%s','%s')" +
                    fileName, userId);
            Statement stmt = c.createStatement();
            stmt.execute(insert);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void insertCommentPicture(String fileName, String senderId, String commentId, String rootMeepId){
        try {
            String insert = String.format("insert into comment_pictures (fileName, senderId, commentId, rootMeepId) values ('%s','%s')" +
                    fileName, senderId, commentId, rootMeepId);
            Statement stmt = c.createStatement();
            stmt.execute(insert);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
