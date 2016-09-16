/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.matefun.fe.components.httpclient.responsetype;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;

/**
 *
 * @author d6788
 */
public class StreamResponse extends HttpResponse<InputStream> implements Closeable {

    private String uri;
	
    public StreamResponse(InputStream result, int respCode, Header[] h) {
        super(result, respCode, h);
    }
    
    @Override
    public String toString() {
        return "StreamResponse{" + "content=" + content + ", responseCode=" + responseCode + ", h=" + h + ", status=" + status.name() + ", emptyContent=" + emptyContent + '}';
    }    

    @Override
    public void close() throws IOException {
        if (content != null) {
            content.close();
        }
    }

    @Override
    protected void setSize() {
        kb_content_size = 0;
    }

    @Override
    public boolean cacheRetrieved() {
        return false;
    }

    @Override
    public StreamResponse getClone() {
        return new StreamResponse(this.getContent(), this.getResponseCode(), this.getHeaders());
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
