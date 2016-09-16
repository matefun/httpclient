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
public interface HttpCache {
    
    
    public <T> HttpResponseInterface<T> get(String key);
    
    public <T> void put(String key, HttpResponseInterface<T> response, long ttl, TimeUnit timeUnit);
    
    public void clear();
    
    public int size();
}
