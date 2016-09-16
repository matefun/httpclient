/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.matefun.fe.components.httpclient.beans;

import java.util.function.Predicate;
import org.apache.http.Header;

import it.matefun.fe.components.httpclient.interfaces.HttpResponseInterface;

/**
 *
 * @author kaiser
 */
public class RequestOptions {
    
    public enum ROType {
        USE_CACHE, NO_CACHE;
    }

    /**
     * Build an object that contains further informations about the request.
     * 
     * TAKE CARE: in order cache your http call the PoolElementInfo object must contain
     * the cache informations from Json configuration file -- if the cache has
     * not been configured in the cache any cache-parameter within here will be
     * ignored
     *
     *
     * @param useCache - ROType.USE_CACHE to cache the request, ROType.NO_CACHE otherwise
     * otherwise
     */
    public RequestOptions(ROType useCache) {        
        if (ROType.USE_CACHE.equals(useCache)) {
            cacheTtl = DEFAULT_TTL;
        } else {
            cacheTtl = -1;
        }       
    }
    
    private static final long DEFAULT_TTL = 0;
    private Predicate<HttpResponseInterface> cachePredicate;
    private String encoding = "utf-8";
    private Header[] headers;
    private long cacheTtl;

    /**
     * @return - the ttl cache for the specific request! If not set the ttl value 
     * will be the default value for the cache http. This value is expressed in seconds
     */
    public long getCacheTtl() {
        return cacheTtl;
    }

    /**
     * 
     * @param cacheTtl - set the cache time to live (in seconds) for the specific object 
     * @return - the current object
     * @throws IllegalArgumentException - if passed value is less than 0
     */
    public RequestOptions setCacheTtl(long cacheTtl) throws IllegalArgumentException {
        if (cacheTtl < 0) {
            throw new IllegalArgumentException("Only positive values accepted as ttl");
        }
        this.cacheTtl = cacheTtl;
        return this;
    }

    /**
     * @return - the current cache predicate if set, a default always-true predicate otherwise
     */
    public Predicate<HttpResponseInterface> getCachePredicate() {
        return null == cachePredicate ? (HttpResponseInterface t) -> {return true;} : cachePredicate;
    }

    /**
     * Set a cache predicate if you need to apply your own policy to choose if
     * the result of an http call should be placed in cache or not
     * 
     * @param cachePredicate - the predicate for result evaluation
     * @return - the current object
     */
    public RequestOptions setCachePredicate(Predicate<HttpResponseInterface> cachePredicate) {
        this.cachePredicate = cachePredicate;
        return this;
    }

    /**
     * 
     * @return the current encoding (default utf-8) 
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * 
     * @param encoding - the encoding you need to use
     * @return - the current object
     */
    public RequestOptions setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * 
     * @return - the current headers 
     */
    public Header[] getHeaders() {
        return headers;
    }

    /**
     * 
     * @param headers - the headers you need to use
     * @return - the current object
     */
    public RequestOptions setHeaders(Header[] headers) {
        this.headers = headers;
        return this;
    }


}
