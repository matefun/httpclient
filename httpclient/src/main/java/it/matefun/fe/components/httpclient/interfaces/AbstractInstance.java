/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.matefun.fe.components.httpclient.interfaces;

import it.matefun.fe.components.httpclient.beans.RequestOptions;
import it.matefun.fe.components.httpclient.responsetype.BytesResponse;
import it.matefun.fe.components.httpclient.responsetype.SimpleResponse;
import it.matefun.fe.components.httpclient.responsetype.StreamResponse;
import it.matefun.fe.components.httpclient.utils.UriUtils;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInstance {

    protected static final String UTF8 = "utf-8";
    protected static Logger LOG = LoggerFactory.getLogger(AbstractInstance.class.getName());
    protected volatile int no_cache_requests;
    protected volatile int cache_requests;
    protected volatile int cache_hit;
    protected volatile int cache_miss;
    protected static final String QM = "?";
    protected static final Registry<ConnectionSocketFactory> sockRegistry;
    protected CloseableHttpClient httpclient;
    protected HttpHost target;
    protected String php;
    protected static final long DEFAULT_TTL = 0;

    static {
        sockRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(SSLContexts.createSystemDefault(), new BrowserCompatHostnameVerifier()))
                .build();
    }

//    protected HttpResponseInterface execute(HttpHost target, String charset, HttpRequest req, Response type, long ttl, Predicate<HttpResponseInterface> predicate) throws IOException {
//        return doCall(target, charset, req, type, ttl, predicate);
//    }

    protected abstract HttpResponseInterface doCall(HttpHost target, String charset, HttpRequest req, Response type, long ttl, Predicate<HttpResponseInterface> predicate) throws IOException;

    protected <T extends HttpResponseInterface> T doGet(String uri, String charset, Response type, Header[] headers, long ttl, Predicate<HttpResponseInterface> predicate, boolean log_uri) throws IOException {
        long start = System.currentTimeMillis();
        uri = UriUtils.encodePage(uri);
        BasicHttpRequest req = new BasicHttpRequest(Methods.GET.name(), uri);
        if (headers != null && headers.length > 0) {
            req.setHeaders(headers);
        }
        HttpResponseInterface iresponse = doCall(target, charset, req, type, ttl, predicate);
        if(log_uri) {
            iresponse.setUri(php + uri);
            LOG.debug("[GET][" + iresponse.getResponseCode() + "] " + iresponse.getUri() + " {" + (System.currentTimeMillis() - start) + " ms" + (iresponse.cacheRetrieved() ? " [FROM CACHE]}" : "}"));
        } else {
            LOG.debug("[GET][" + iresponse.getResponseCode() + "] " + php + uri + " {" + (System.currentTimeMillis() - start) + " ms" + (iresponse.cacheRetrieved() ? " [FROM CACHE]}" : "}"));
        }
        return (T) iresponse;
    }

    protected SimpleResponse doPost(String uri, HttpEntity content, String charset) throws IOException {
        return doPost(uri, content, charset, null, true);
    }

    protected SimpleResponse doPost(String uri, HttpEntity content, String charset, boolean closeRequestEntity) throws IOException {
        return doPost(uri, content, charset, null, closeRequestEntity);
    }

    protected SimpleResponse doPost(String uri, HttpEntity content, String charset, Header[] headers, boolean closeRequestEntity) throws IOException {
        long start = System.currentTimeMillis();
        uri = UriUtils.encodePage(uri);
        HttpPost postRequest = new HttpPost(target.toURI().concat(uri));
        postRequest.setEntity(content);
        if (headers != null && headers.length > 0) {
            postRequest.setHeaders(headers);
        }
        HttpResponse http_response = httpclient.execute(target, postRequest);
        SimpleResponse response = new SimpleResponse(EntityUtils.toString(http_response.getEntity(), charset), http_response.getStatusLine().getStatusCode(), http_response.getAllHeaders());
        EntityUtils.consumeQuietly(http_response.getEntity());
        HttpClientUtils.closeQuietly(http_response);
        if (closeRequestEntity) {
            EntityUtils.consumeQuietly(content);
        }
        response.setUri(php + uri);
        LOG.debug("[POST][" + response.getResponseCode() + "] " + response.getUri() + " {" + (System.currentTimeMillis() - start) + " ms}");
        no_cache_requests++;
        return response;
    }

    public SimpleResponse get(String uri) throws IOException {
        return doGet(uri, UTF8, Response.STRING, null, -1, (i) -> {
            return true;
        }, false);
    }

    public SimpleResponse get(String uri, boolean log_uri) throws IOException {
        return doGet(uri, UTF8, Response.STRING, null, -1, (i) -> {
            return true;
        }, log_uri);
    }
    
    public SimpleResponse get(String uri, List<NameValuePair> params) throws IOException {
        return get(uri.concat(QM).concat(URLEncodedUtils.format(params, UTF8)));
    }

    public SimpleResponse get(String uri, List<NameValuePair> params, boolean log_uri) throws IOException {
        return get(uri.concat(QM).concat(URLEncodedUtils.format(params, UTF8)), log_uri);
    }
    
    public SimpleResponse get(String uri, RequestOptions ropts) throws IOException {
        return doGet(uri, ropts.getEncoding(), Response.STRING, ropts.getHeaders(), ropts.getCacheTtl(), ropts.getCachePredicate(), false);
    }

    public SimpleResponse get(String uri, RequestOptions ropts, boolean log_uri) throws IOException {
        return doGet(uri, ropts.getEncoding(), Response.STRING, ropts.getHeaders(), ropts.getCacheTtl(), ropts.getCachePredicate(), log_uri);
    }
    
    public SimpleResponse get(String uri, List<NameValuePair> params, RequestOptions ropts) throws IOException {
        return get(uri.concat(QM).concat(URLEncodedUtils.format(params, UTF8)), ropts);
    }

    public SimpleResponse get(String uri, List<NameValuePair> params, RequestOptions ropts, boolean log_uri) throws IOException {
        return get(uri.concat(QM).concat(URLEncodedUtils.format(params, UTF8)), ropts, log_uri);
    }
    
    public BytesResponse getBytes(String uri) throws IOException {
        return doGet(uri, UTF8, Response.BYTEARRAY, null, -1, (i) -> {
            return true;
        }, false);
    }

    public BytesResponse getBytes(String uri, List<NameValuePair> params) throws IOException {
        return getBytes(uri.concat(QM).concat(URLEncodedUtils.format(params, UTF8)));
    }

    public BytesResponse getBytes(String uri, RequestOptions ropts) throws IOException {
        return doGet(uri, ropts.getEncoding(), Response.BYTEARRAY, ropts.getHeaders(), ropts.getCacheTtl(), ropts.getCachePredicate(), false);
    }

    public BytesResponse getBytes(String uri, List<NameValuePair> params, RequestOptions ropts) throws IOException {
        return getBytes(uri.concat(QM).concat(URLEncodedUtils.format(params, UTF8)), ropts);
    }

    public StreamResponse getStream(String uri) throws IOException {
        return doGet(uri, UTF8, Response.STREAM, null, -1, (i) -> {
            return true;
        }, false);
    }

    public StreamResponse getStream(String uri, List<NameValuePair> params) throws IOException {
        return getStream(uri.concat(QM).concat(URLEncodedUtils.format(params, UTF8)));
    }

    public StreamResponse getStream(String uri, RequestOptions ropts) throws IOException {
        return doGet(uri, ropts.getEncoding(), Response.STREAM, ropts.getHeaders(), ropts.getCacheTtl(), ropts.getCachePredicate(), false);
    }

    public StreamResponse getStream(String uri, List<NameValuePair> params, RequestOptions ropts) throws IOException {
        return getStream(uri.concat(QM).concat(URLEncodedUtils.format(params, UTF8)), ropts);
    }

    public SimpleResponse post(String uri, HttpEntity content) throws IOException {
        return doPost(uri, content, UTF8);
    }

    public SimpleResponse post(String uri, HttpEntity content, String encoding) throws IOException {
        return doPost(uri, content, encoding);
    }

    public SimpleResponse post(String uri, HttpEntity content, Header[] headers) throws IOException {
        return doPost(uri, content, UTF8, headers, true);
    }

    public SimpleResponse post(String uri, HttpEntity content, String encoding, Header[] headers) throws IOException {
        return doPost(uri, content, encoding, headers, true);
    }

    public SimpleResponse post(String uri, HttpEntity content, List<NameValuePair> pars) throws IOException {
        return post(uri, content, pars, UTF8);
    }

    public SimpleResponse post(String uri, HttpEntity content, List<NameValuePair> pars, Header[] headers) throws IOException {
        return post(uri, content, pars, UTF8, headers);
    }

    public SimpleResponse post(String uri, HttpEntity content, List<NameValuePair> pars, String encoding) throws IOException {
        return doPost(uri.concat(QM).concat(URLEncodedUtils.format(pars, encoding)), content, encoding);
    }

    public SimpleResponse post(String uri, HttpEntity content, List<NameValuePair> pars, String encoding, Header[] headers) throws IOException {
        return doPost(uri.concat(QM).concat(URLEncodedUtils.format(pars, encoding)), content, encoding, headers, true);
    }

    public SimpleResponse post(String uri, HttpEntity content, boolean closeRequestEntity) throws IOException {
        return doPost(uri, content, UTF8, closeRequestEntity);
    }

    public SimpleResponse post(String uri, HttpEntity content, String encoding, boolean closeRequestEntity) throws IOException {
        return doPost(uri, content, encoding, closeRequestEntity);
    }

    public SimpleResponse post(String uri, HttpEntity content, Header[] headers, boolean closeRequestEntity) throws IOException {
        return doPost(uri, content, UTF8, headers, closeRequestEntity);
    }

    public SimpleResponse post(String uri, HttpEntity content, String encoding, Header[] headers, boolean closeRequestEntity) throws IOException {
        return doPost(uri, content, encoding, headers, closeRequestEntity);
    }

    public SimpleResponse post(String uri, HttpEntity content, List<NameValuePair> pars, boolean closeRequestEntity) throws IOException {
        return post(uri, content, pars, UTF8, closeRequestEntity);
    }

    public SimpleResponse post(String uri, HttpEntity content, List<NameValuePair> pars, Header[] headers, boolean closeRequestEntity) throws IOException {
        return post(uri, content, pars, UTF8, headers, closeRequestEntity);
    }

    public SimpleResponse post(String uri, HttpEntity content, List<NameValuePair> pars, String encoding, boolean closeRequestEntity) throws IOException {
        return doPost(uri.concat(QM).concat(URLEncodedUtils.format(pars, encoding)), content, encoding, closeRequestEntity);
    }

    public SimpleResponse post(String uri, HttpEntity content, List<NameValuePair> pars, String encoding, Header[] headers, boolean closeRequestEntity) throws IOException {
        return doPost(uri.concat(QM).concat(URLEncodedUtils.format(pars, encoding)), content, encoding, headers, closeRequestEntity);
    }
    
    protected SimpleResponse doPut(String uri, HttpEntity content, String charset, Header[] headers, boolean closeRequestEntity) throws IOException {
        long start = System.currentTimeMillis();
        uri = UriUtils.encodePage(uri);
        HttpPut putRequest = new HttpPut(target.toURI().concat(uri));
        putRequest.setEntity(content);
        if (headers != null && headers.length > 0) {
            putRequest.setHeaders(headers);
        }
        HttpResponse http_response = httpclient.execute(target, putRequest);
        SimpleResponse response = new SimpleResponse(EntityUtils.toString(http_response.getEntity(), charset), http_response.getStatusLine().getStatusCode(), http_response.getAllHeaders());
        EntityUtils.consumeQuietly(http_response.getEntity());
        HttpClientUtils.closeQuietly(http_response);
        if (closeRequestEntity) {
            EntityUtils.consumeQuietly(content);
        }
        response.setUri(php + uri);
        LOG.debug("[PUT][" + response.getResponseCode() + "] " + response.getUri() + " {" + (System.currentTimeMillis() - start) + " ms}");
        no_cache_requests++;
        return response;
    }
    
    public SimpleResponse put(String uri, HttpEntity content, List<NameValuePair> pars) throws IOException {
        return put(uri, content, pars, UTF8 ,null, true);
    }
    
    public SimpleResponse put(String uri, HttpEntity content, List<NameValuePair> pars, Header[] headers) throws IOException {
        return put(uri, content, pars, UTF8 ,headers, true);
    }
    
    public SimpleResponse put(String uri, HttpEntity content, List<NameValuePair> pars, String encoding, Header[] headers, boolean closeRequestEntity) throws IOException {
        return doPut(uri.concat(QM).concat(URLEncodedUtils.format(pars, encoding)), content, encoding, headers, closeRequestEntity);
    }

    public SimpleResponse put(String uri, HttpEntity content, String encoding, Header[] headers, boolean closeRequestEntity) throws IOException {
        return doPut(uri, content, encoding, headers, closeRequestEntity);
    }

    public SimpleResponse put(String uri, HttpEntity content, Header[] headers) throws IOException {
        return doPut(uri, content, UTF8, headers, true);
    }

    public SimpleResponse put(String uri, HttpEntity content, String encoding, Header[] headers) throws IOException {
        return doPut(uri, content, encoding, headers, true);
    }
    
    protected SimpleResponse doDelete(String uri, String charset, Header[] headers) throws IOException {
        long start = System.currentTimeMillis();
        uri = UriUtils.encodePage(uri);
        HttpDelete delRequest = new HttpDelete(target.toURI().concat(uri));
        if (headers != null && headers.length > 0) {
            delRequest.setHeaders(headers);
        }
        HttpResponse http_response = httpclient.execute(target, delRequest);
        SimpleResponse response = new SimpleResponse(EntityUtils.toString(http_response.getEntity(), charset), http_response.getStatusLine().getStatusCode(), http_response.getAllHeaders());
        EntityUtils.consumeQuietly(http_response.getEntity());
        HttpClientUtils.closeQuietly(http_response);

        response.setUri(php + uri);
        LOG.debug("[DELETE][" + response.getResponseCode() + "] " + response.getUri() + " {" + (System.currentTimeMillis() - start) + " ms}");
        no_cache_requests++;
        return response;
    }

    public SimpleResponse delete(String uri, String encoding, Header[] headers) throws IOException {
        return doDelete(uri, encoding, headers);
    }

    public SimpleResponse delete(String uri, Header[] headers) throws IOException {
        return doDelete(uri, UTF8, headers);
    }
    
    public SimpleResponse delete (String uri, List<NameValuePair> pars, String encoding, Header[] headers) throws IOException {
    	return doDelete(uri.concat(QM).concat(URLEncodedUtils.format(pars, encoding)), encoding, headers);
    }

    protected SimpleResponse doPatch(String uri, HttpEntity content, String charset, Header[] headers, boolean closeRequestEntity) throws IOException {
        long start = System.currentTimeMillis();
        uri = UriUtils.encodePage(uri);
        HttpPatch patchRequest = new HttpPatch(target.toURI().concat(uri));
        patchRequest.setEntity(content);
        if (headers != null && headers.length > 0) {
            patchRequest.setHeaders(headers);
        }
        HttpResponse http_response = httpclient.execute(target, patchRequest);
        SimpleResponse response = new SimpleResponse(EntityUtils.toString(http_response.getEntity(), charset), http_response.getStatusLine().getStatusCode(), http_response.getAllHeaders());
        EntityUtils.consumeQuietly(http_response.getEntity());
        HttpClientUtils.closeQuietly(http_response);
        if (closeRequestEntity) {
            EntityUtils.consumeQuietly(content);
        }
        response.setUri(php + uri);
        LOG.debug("[PATCH][" + response.getResponseCode() + "] " + response.getUri() + " {" + (System.currentTimeMillis() - start) + " ms}");
        no_cache_requests++;
        return response;
    }

    public SimpleResponse patch(String uri, HttpEntity content, Header[] headers) throws IOException {
        return doPatch(uri, content, UTF8, headers, true);
    }

    protected abstract void shutDown();

    protected static enum Methods {
        GET
    }

    protected static enum Response {
        STRING, BYTEARRAY, STREAM
    }
}
