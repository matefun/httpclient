/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.matefun.fe.components.httpclient.responsetype;

import org.apache.http.Header;
import org.apache.http.HttpStatus;

import it.matefun.fe.components.httpclient.interfaces.HttpResponseInterface;

/**
 *
 * @author kaiser
 * @param <T>
 */
public abstract class HttpResponse<T> implements HttpResponseInterface<T> {

    protected final T content;
    protected final int responseCode;
    protected final Header[] h;
    protected boolean emptyContent;
    protected RespType status;
    protected long kb_content_size;

    public static RespType getRespType(int respCode) {
        switch (respCode) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_CREATED:
            case HttpStatus.SC_ACCEPTED:
            case HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION:
            case HttpStatus.SC_NO_CONTENT:
            case HttpStatus.SC_RESET_CONTENT:
            case HttpStatus.SC_PARTIAL_CONTENT:
            case HttpStatus.SC_MULTI_STATUS:
                return RespType.OK;
            case HttpStatus.SC_MOVED_PERMANENTLY:
            case HttpStatus.SC_MOVED_TEMPORARILY:
                return RespType.REDIRECTION;
            case HttpStatus.SC_NOT_FOUND:
                return RespType.NOTFOUND;
        }
        return null;
    }

    public HttpResponse(T result, int respCode, Header[] h) {
        emptyContent = (result == null);
        this.responseCode = respCode;
        this.content = result;
        this.h = h;
        this.status = getRespType(respCode);
        setSize();
    }

    @Override
    public T getContent() {
        return content;
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public Header[] getHeaders() {
        return h;
    }
      
    protected abstract void setSize();
    
    @Override
    public long getSize() {
        return kb_content_size;
    }
    

    /**
     *
     * @param headerName - the name of the header to look for
     * @return -- the first header found with the name (equalsIgnoreCase) --
     * null if no values are found
     */
    @Override
    public Header getHeader(String headerName) {
        for (Header current : h) {
            if (current.getName().equalsIgnoreCase(headerName)) {
                return current;
            }
        }
        return null;
    }

    @Override
    public boolean emptyContent() {
        return emptyContent;
    }
    
    @Override
    public RespType status() {
        return status;
    }
}
