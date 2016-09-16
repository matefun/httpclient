/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.matefun.fe.components.httpclient.cache;

import java.util.concurrent.TimeUnit;

import it.matefun.fe.components.httpclient.interfaces.HttpResponseInterface;

/**
 *
 * @author amon
 */
class NoHttpCache implements HttpCache {

    @Override
    public <T> HttpResponseInterface<T> get(String key) {
        return null;
    }

    @Override
    public <T> void put(String key, HttpResponseInterface<T> response, long ttl, TimeUnit timeUnit) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int size() {
        return 0;
    }
    
}
