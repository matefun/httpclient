package it.matefun.fe.components.httpclient.pool;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import it.matefun.fe.components.httpclient.beans.PoolElementInfo;
import it.matefun.fe.components.httpclient.cache.HttpCache;
import it.matefun.fe.components.httpclient.interfaces.AbstractInstance;
import it.matefun.fe.components.httpclient.interfaces.Cacheable;
import it.matefun.fe.components.httpclient.interfaces.HttpResponseInterface;
import it.matefun.fe.components.httpclient.interfaces.HttpResponseInterface.RespType;
import it.matefun.fe.components.httpclient.interfaces.Poolable;
import it.matefun.fe.components.httpclient.responsetype.BytesResponse;
import it.matefun.fe.components.httpclient.responsetype.SimpleResponse;
import it.matefun.fe.components.httpclient.responsetype.StreamResponse;
import it.matefun.fe.components.httpclient.utils.InputStreamCloner;
import it.matefun.fe.components.vertx.jsonmanager.JsonObject;


/**
 * This class creates an HTTP client Instance
 *
 * @author d6788
 */
public class PoolInstance extends AbstractInstance implements Cacheable, Poolable {

    protected PoolElementInfo pi;
    protected PoolingHttpClientConnectionManager ccm;
    private static final String cache_name_suffix = "_connector_map";
    
    private static final String NO_CACHE_REQUESTS = "no_cache_requests";
    private static final String CACHE_REQUESTS = "cache_requests";
    private static final String CACHE_HITS = "cache_hits";
    private static final String CACHE_MISSES = "cache_misses";
    private static final String CACHED_ELEMENTS = "cached_elements";
    private static final String RECEIVED_REQUESTS = "received_requests";
    private static final String USING_CACHE = "using_cache";
    

    private final HttpCache cache;

    protected PoolInstance(PoolElementInfo pi) throws IllegalArgumentException {
        if (pi.getHost() == null || pi.getHost().isEmpty()) {
            throw new IllegalArgumentException("Can't create a connection instance if the host is undefined");
        }
        this.pi = pi;
        if (pi.isUsingCache()) {
            String mapName = pi.getPool_name().concat(cache_name_suffix);
            cache = PoolManager.HTTP.getCacheManager().getCache(mapName);//getHazelcastInstance();
        } else {
            cache = null;
        }
        create();
    }

    private void create() {
        ccm = new PoolingHttpClientConnectionManager(sockRegistry);
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true).setSoTimeout(pi.getSocket_timeout())
                .build();
        ccm.setDefaultSocketConfig(socketConfig);
        ccm.setMaxTotal(pi.getPoolsize());
        ccm.setDefaultMaxPerRoute(pi.getPoolsize());
        target = new HttpHost(pi.getHost(), pi.getPort(), pi.getProtocol());
        HttpClientBuilder htBuilder = HttpClients.custom().setConnectionManager(ccm);
        RequestConfig rq = RequestConfig.custom().setConnectTimeout(pi.getConnection_timeout()).build();
        htBuilder.setDefaultRequestConfig(rq);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (pi.useProxy()) {
            HttpHost proxy = new HttpHost(pi.getProxyhost(), pi.getProxyport(), pi.getProxyprotocol());
            if (pi.proxyUseAuth()) {
                credentialsProvider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(pi.getProxy_username(), pi.getProxy_password()));
            }
            htBuilder.setProxy(proxy);
        }
        if (pi.useAuth()) {
            credentialsProvider.setCredentials(new AuthScope(target), new UsernamePasswordCredentials(pi.getAuth_username(), pi.getAuth_password()));
        }
        //htBuilder.disableContentCompression();
        htBuilder.setDefaultCredentialsProvider(credentialsProvider);
        httpclient = htBuilder.build();
        php = target.getSchemeName() + "://" + target.getHostName() + (target.getPort() == 80 ? "" : ":" + target.getPort());
        infoLog();
    }

    @Override
    protected void shutDown() {
        HttpClientUtils.closeQuietly(httpclient);
        ccm.shutdown();
    }

    /**
     *
     * @return true if the pool configuration has a cache in config
     */
    public boolean usingCache() {
        return pi.isUsingCache();
    }

    private HttpResponseInterface call(HttpHost target, String charset, HttpRequest req, Response type) throws IOException {
        HttpResponseInterface iResponse;
        HttpResponse http_response = httpclient.execute(target, req);
//        LOG.debug("Headers ----------------------------------");
//        for (Header header : http_response.getAllHeaders()) { LOG.debug(header.getName() + ":" + header.getValue());}
        switch (type) {
            case BYTEARRAY:
                iResponse = new BytesResponse(http_response.getEntity() == null ? new byte[0] : EntityUtils.toByteArray(http_response.getEntity()), http_response.getStatusLine().getStatusCode(), http_response.getAllHeaders());
                break;
            case STREAM:
                InputStream clone = (http_response.getEntity() == null || http_response.getEntity().getContent() == null ? null : new InputStreamCloner(http_response.getEntity().getContent()).getClone());
                iResponse = new StreamResponse(clone, http_response.getStatusLine().getStatusCode(), http_response.getAllHeaders());
                break;
            case STRING:
            default:
                iResponse = new SimpleResponse(http_response.getEntity() == null ? "" : EntityUtils.toString(http_response.getEntity(), charset), http_response.getStatusLine().getStatusCode(), http_response.getAllHeaders());
                break;
        }
        EntityUtils.consumeQuietly(http_response.getEntity());
        HttpClientUtils.closeQuietly(http_response);
        return iResponse;
    }

    @Override
    protected HttpResponseInterface doCall(HttpHost target, String charset, HttpRequest req, Response type, long ttl, Predicate<HttpResponseInterface> predicate) throws IOException {
        HttpResponseInterface iResponse;
        if (usingCache() && !Response.STREAM.equals(type) && ttl != -1) {
            cache_requests++;
            iResponse = cache.get(req.getRequestLine().getUri());
            if (iResponse == null) {
                iResponse = call(target, charset, req, type);
                if (RespType.OK.equals(iResponse.status()) && canFitInCache(iResponse) && predicate.test(iResponse)) {
                    cache.put(req.getRequestLine().getUri(), iResponse.getClone(), (ttl == DEFAULT_TTL ? pi.getCache().getMax_seconds_lifetime() : ttl), TimeUnit.SECONDS);
                }
                cache_miss++;
                return iResponse;
            } else {
                cache_hit++;
            }
        } else {
            no_cache_requests++;
            iResponse = call(target, charset, req, type);
        }
        return iResponse;

    }

    public boolean canFitInCache(HttpResponseInterface iResponse) {
        return (iResponse.getSize() <= pi.getCache().getMax_elem_size_kb());
    }

    protected JsonObject poolStats() {
        JsonObject jo = new JsonObject();
        if (usingCache()) {
            jo.putNumber(NO_CACHE_REQUESTS, no_cache_requests)
                    .putNumber(CACHE_REQUESTS, cache_requests)
                    .putNumber(CACHE_HITS, cache_hit)
                    .putNumber(CACHE_MISSES, cache_miss)
                    .putNumber(CACHED_ELEMENTS, cache.size())
                    .putBoolean(USING_CACHE, Boolean.TRUE);
        } else {
            jo.putNumber(RECEIVED_REQUESTS, no_cache_requests)
                    .putBoolean(USING_CACHE, Boolean.FALSE);
        }
        return jo;
    }

    @Override
    public void emptyCache() {
        cache.clear();
    }

    protected void infoLog() {
        LOG.info("[" + (usingCache() ? "CACHED-" : "") + "POOL-ADDED] connection to " + pi.getHost() + ":" + pi.getPort() + (pi.useProxy() ? " through proxy " + pi.getProxyhost() + ":" + pi.getProxyport() : "") + " [CONN-TIMEOUT = " + pi.getConnection_timeout() + "ms | SOCK-TIMEOUT = " + pi.getSocket_timeout() + "]");
    }

    @Override
    public void cleanPool() {
        ccm.closeExpiredConnections();
        ccm.closeIdleConnections(15, TimeUnit.SECONDS);
    }

}
