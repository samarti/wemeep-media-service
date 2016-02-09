package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by the mighty and powerful santiagomarti on 2/9/16.
 * All those who dare, in any way, copying or somehow reproducing
 * the God-inspired earth-shattering 8th Wonder
 * code below, shall be punished for 47 days straight on the realms
 * of the 9th Circle of Hell, while watching "Friday" sang live by
 * Rebecca Black
 */
public class Utils {

    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
}
