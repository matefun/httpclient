/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.matefun.fe.components.httpclient.interfaces;

import java.io.Serializable;
import org.apache.http.Header;

/**
 *
 * @author d6788
 * @param <T>
 */
public interface HttpResponseInterface<T> extends Serializable, Cloneable {

    public enum RespType {
        /**
         * OK means a status code of the 20* type.
         */
        OK, 
        /**
         * REDIRECTION means a status code Moved temporarily/permanently
         */        
        REDIRECTION, 
        /**
         * The 404 status code
         */
        NOTFOUND
    }
    
    /**
     * 
     * @return a RespType object
     */
    public RespType status();
    
    /**
     * 
     * @return the response status code
     */        
    public int getResponseCode();
    /**
     * 
     * @return all headers of the response
     */
    public Header[] getHeaders();

    /**
     * 
     * @param headerName - the name of the header to look for
     * @return -- the first header found with the name (equalsIgnoreCase) -- null if no values are found
     */
    public Header getHeader(String headerName);
    
    /**
     * don't check for null value or empty strings/zero size array, use this 
     * @return true if there is not content
     */
    public boolean emptyContent();
    
    public T getContent();
    
    public long getSize();
    
    public boolean cacheRetrieved();
    
    public HttpResponseInterface getClone();
    
    public String getUri();

    public void setUri(String uri);    
    
}
