/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.matefun.fe.components.httpclient.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PoolElementInfo {

    private String pool_name;
    private String host;
    private int port = 80;
    private String protocol = "http";
    private int poolsize = 1;
    private String auth_username;
    private String auth_password;
    private String proxyhost;
    private int proxyport;
    private String proxy_username;
    private String proxy_password;
    private String proxyprotocol = "http";
    private int socket_timeout = 2_000;
    private int connection_timeout = 1_000;

    private CacheConfiguration cache;

    public CacheConfiguration getCache() {
        return cache;
    }



    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CacheConfiguration {

        private int max_elem_number = 1000;
        private int max_elem_size_kb = 32;
        private int max_seconds_lifetime = 600; //10 minutes as default http cache

        /**
         *
         * @return the default number of seconds (TTL) in cache. This can be
         * overwritten passing the TTL in cache_get* operation
         */
        public int getMax_seconds_lifetime() {
            return max_seconds_lifetime;
        }

        public void setMax_seconds_lifetime(int max_seconds_lifetime) {
            this.max_seconds_lifetime = max_seconds_lifetime;
        }

        /**
         *
         * @return the biggest size reachable by the map
         */
        public int getMax_elem_number() {
            return max_elem_number;
        }

        public void setMax_elem_number(int max_elem_number) {
            this.max_elem_number = max_elem_number;
        }

        /**
         *
         * @return the elem size in kb of the biggest cache element
         */
        public int getMax_elem_size_kb() {
            return max_elem_size_kb;
        }

        public void setMax_elem_size_kb(int max_elem_size_kb) {
            this.max_elem_size_kb = max_elem_size_kb;
        }

    }

    public PoolElementInfo() {
        super();
    }

    public static PoolElementInfo createConnElementInfo(String host, int port) {
        PoolElementInfo ci = new PoolElementInfo();
        ci.setHost(host);
        ci.setPort(port);
        return ci;
    }

    public static PoolElementInfo createConnElementInfo(String host, int port, int socket_timeout, int connection_timeout) {
        PoolElementInfo ci = new PoolElementInfo();
        ci.setHost(host);
        ci.setPort(port);
        ci.setSocket_timeout(socket_timeout);
        ci.setConnection_timeout(connection_timeout);
        return ci;
    }

    public int getSocket_timeout() {
        return socket_timeout;
    }

    public void setSocket_timeout(int socket_timeout) {
        this.socket_timeout = socket_timeout;
    }

    public int getConnection_timeout() {
        return connection_timeout;
    }

    public void setConnection_timeout(int connection_timeout) {
        this.connection_timeout = connection_timeout;
    }

    public String getProxyprotocol() {
        return proxyprotocol;
    }

    public void setProxyprotocol(String proxyprotocol) {
        this.proxyprotocol = proxyprotocol;
    }

    public boolean useProxy() {
        return (proxyhost != null && !proxyhost.isEmpty());
    }

    public boolean useAuth() {
        return (auth_username != null && auth_password != null && !auth_username.isEmpty() && !auth_password.isEmpty());
    }

    public boolean proxyUseAuth() {
        return (proxy_username != null && proxy_password != null && !proxy_username.isEmpty() && !proxy_password.isEmpty());
    }

    public String getProxyhost() {
        return proxyhost;
    }

    public void setProxyhost(String proxyhost) {
        this.proxyhost = proxyhost;
    }

    public int getProxyport() {
        return proxyport;
    }

    public void setProxyport(int proxyport) {
        this.proxyport = proxyport;
    }

    public String getProxy_username() {
        return proxy_username;
    }

    public void setProxy_username(String proxy_username) {
        this.proxy_username = proxy_username;
    }

    public String getProxy_password() {
        return proxy_password;
    }

    public void setProxy_password(String proxy_password) {
        this.proxy_password = proxy_password;
    }

    public String getAuth_password() {
        return auth_password;
    }

    public void setAuth_password(String auth_password) {
        this.auth_password = auth_password;
    }

    public String getAuth_username() {
        return auth_username;
    }

    public void setAuth_username(String auth_username) {
        this.auth_username = auth_username;
    }

    public int getPoolsize() {
        return poolsize;
    }

    public void setPoolsize(int poolsize) {
        this.poolsize = poolsize;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getPool_name() {
        return pool_name;
    }

    public PoolElementInfo setPool_name(String pool_name) {
        this.pool_name = pool_name;
        return this;
    }

    public boolean isUsingCache() {
        return cache != null;
    }

    @Override
    public String toString() {
        return "ConnElementInfo{" + "host=" + host + ", port=" + port + ", protocol=" + protocol + ", poolsize=" + poolsize + ", auth_username=" + auth_username + ", auth_password=" + auth_password + ", proxyhost=" + proxyhost + ", proxyport=" + proxyport + ", proxyprotocol=" + proxyprotocol + ", socket_timeout=" + socket_timeout + ", connection_timeout=" + connection_timeout + ", cache=" + cache + '}';
    }

}
