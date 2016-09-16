/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.matefun.fe.components.httpclient.cache;

/**
 *
 * @author amon
 */
public interface HttpCacheManager {

    public static HttpCacheManager cacheDisabled(){
        return new HttpCacheDisabledManager();
    }
 
    
    public HttpCache getCache(String name);
}
