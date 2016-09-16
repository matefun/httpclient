/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.matefun.fe.components.httpclient.responsetype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;

/**
 *
 * @author d6788
 */
public class BytesResponse extends HttpResponse<List<Byte>> {

    private final byte[] byteContent;
    private boolean cache_retrieved = false;
    private String uri;

    private static List<Byte> bytesToBytes(byte[] input) {
        List<Byte> output = new ArrayList(input.length);
        for (byte current : input) {
            output.add(current);
        }
        return output;
    }

    public BytesResponse(byte[] result, int respCode, Header[] h) {
        super(bytesToBytes(result), respCode, h);
        byteContent = result;
    }

    @Override
    public String toString() {
        return "BytesResponse{" + "content=" + content + ", responseCode=" + responseCode + ", h=" + Arrays.toString(h) + ", status = " + status.name() + ", emptyContent=" + emptyContent + '}';
    }

    public byte[] getByteArray() {
        return byteContent;
    }

    @Override
    protected void setSize() {
        kb_content_size = content.size() / 1024;
    }

    @Override
    public boolean cacheRetrieved() {
        return cache_retrieved;
    }
    
    BytesResponse setFromCache() {
        cache_retrieved = true;
        return this;
    }
    
    @Override
    public BytesResponse getClone() {
        return new BytesResponse(this.getByteArray(), this.getResponseCode(), this.getHeaders()).setFromCache();
        
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
