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
class HttpCacheDisabledManager implements HttpCacheManager {

    @Override
    public HttpCache getCache(String name) {
        return new NoHttpCache();
    }
    
}
