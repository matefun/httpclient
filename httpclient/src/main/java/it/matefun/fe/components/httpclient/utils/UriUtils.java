package it.matefun.fe.components.httpclient.utils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author d6788
 */
public class UriUtils {

    public static String encodePage(String page) {
        if (page.charAt(0) != '/') {
            return '/' + page;
        }
        return page;
    }

    private UriUtils() {
    }
}
