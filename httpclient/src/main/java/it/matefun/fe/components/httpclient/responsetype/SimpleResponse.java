/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.matefun.fe.components.httpclient.responsetype;

import java.io.Serializable;

import org.apache.http.Header;

/**
 *
 * @author d6788
 */
public class SimpleResponse extends HttpResponse<String> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1272348742858281197L;

	private String uri;
	
    boolean cache_retrieved = false;

    public SimpleResponse(String result, int respCode, Header[] h) {
        super(result, respCode, h);
    }

    @Override
    public String toString() {
        return "SimpleResponse{" + "content=" + content + ", responseCode=" + responseCode + ", h=" + h + ", status=" + status.name() + ", emptyContent=" + emptyContent + '}';
    }

    @Override
    protected void setSize() {
        kb_content_size = content.length() / 1024;
    }

    @Override
    public boolean cacheRetrieved() {
        return cache_retrieved;
    }
    
    SimpleResponse setFromCache() {
        cache_retrieved = true;
        return this;
    }    
        
    @Override
    public SimpleResponse getClone() {
        return new SimpleResponse(this.getContent(), this.getResponseCode(), this.getHeaders()).setFromCache();
    }

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public void setUri(String uri) {
		this.uri = uri;
	}
    
    

}
