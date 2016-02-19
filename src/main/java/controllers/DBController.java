package controllers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;

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

    private static final String profilePicturesTable = "profile_pictures";
    private static final String commentPicturesTable = "comment_pictures";
    private static final String meepPicturesTable = "meep_pictures";

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
                    //.getConnection("jdbc:postgresql://54.233.114.20:49162/postgres",
                            "postgres", "postgres");
            Statement stmt = c.createStatement();
            String profilePictures = "create table if not exists " + profilePicturesTable + " (id SERIAL primary key, userId char(100) not null, fileName char(200) not null," +
                    " createdAt timestamp DEFAULT now() NOT NULL)";
            String commentPictures = "create table if not exists " + commentPicturesTable + " (id SERIAL primary key, fileName char(100) not null," +
                    " commentId char(200) not null, createdAt timestamp DEFAULT now() NOT NULL)";
            String meepPictures = "create table if not exists " + meepPicturesTable + " (id SERIAL primary key, fileName char(100) not null," +
                    " meepId char(200) not null, createdAt timestamp DEFAULT now() NOT NULL)";
            stmt.execute(profilePictures);
            stmt.execute(commentPictures);
            stmt.execute(meepPictures);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void upsertProfilePicture(String fileName, String userId){
        try {
            String insert = "WITH upsert AS (UPDATE " + profilePicturesTable + " SET fileName = \'" + fileName + "\' WHERE userId = \'"+ userId + "\' RETURNING *) INSERT INTO " + profilePicturesTable + " (userId, fileName) SELECT \'"+ userId + "\', \'"+ fileName + "\' WHERE NOT EXISTS (SELECT * FROM upsert)";
            Statement stmt = c.createStatement();
            stmt.execute(insert);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void upsertCommentPicture(String fileName, String commentId){
        try {
            String insert = "WITH upsert AS (UPDATE " + commentPicturesTable + " SET fileName = \'" + fileName + "\' WHERE commentId = \'"+ commentId + "\' RETURNING *) INSERT INTO " + commentPicturesTable + " (commentId, fileName) SELECT \'"+ commentId + "\', \'"+ fileName + "\' WHERE NOT EXISTS (SELECT * FROM upsert)";
            Statement stmt = c.createStatement();
            stmt.execute(insert);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void upsertMeepPicture(String fileName, String meepId){
        try {
            String insert = "WITH upsert AS (UPDATE " + meepPicturesTable + " SET fileName = \'" + fileName + "\' WHERE meepId = \'"+ meepId + "\' RETURNING *) INSERT INTO " + meepPicturesTable + " (meepId, fileName) SELECT \'"+ meepId + "\', \'"+ fileName + "\' WHERE NOT EXISTS (SELECT * FROM upsert)";
            Statement stmt = c.createStatement();
            stmt.execute(insert);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public String getUserPicture(String userId){
        try {
            Statement stmt = c.createStatement();
            String get = "select fileName from " + profilePicturesTable + " where userId = \'" + userId + "\';";
            ResultSet set = stmt.executeQuery(get);
            if(set.next())
                return set.getString("fileName").trim();
            else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

    public String getCommentPicture(String commentId){
        try {
            Statement stmt = c.createStatement();
            String get = "select fileName from " + commentPicturesTable + " where commentId = \'" + commentId + "\';";
            ResultSet set = stmt.executeQuery(get);
            if(set.next())
                return set.getString("fileName").trim();
            else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }
}
