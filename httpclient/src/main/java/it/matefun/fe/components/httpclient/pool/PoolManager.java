/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.matefun.fe.components.httpclient.pool;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.matefun.fe.components.httpclient.beans.PoolElementInfo;
import it.matefun.fe.components.httpclient.cache.HttpCacheManager;
import it.matefun.fe.components.vertx.jsonmanager.JsonObject;

/**
 *
 * @author d6788
 */
public enum PoolManager {

    HTTP;
    private static final Logger LOG = LoggerFactory.getLogger(PoolManager.class);
    private static final Map<String, PoolInstance> connectors = new LinkedHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Cleaner cleaner = new Cleaner();
    private static HttpCacheManager cacheManager;
    
    static {
        scheduler.scheduleWithFixedDelay(cleaner, 30_000, 120_000, TimeUnit.MILLISECONDS);
    }

    /**
     *
     * Returns a pool to make get/post to specific configuration
     *
     * @param instanceName - the name of the connection pool you need
     * @return - the pool Instance
     * @throws IllegalArgumentException
     */
    public PoolInstance getPoolInstance(String instanceName) throws IllegalArgumentException {
        PoolInstance current = connectors.get(instanceName);
        if (current == null) {
            throw new IllegalArgumentException("No pool instance found with name: " + instanceName);
        }
        return current;
    }

    private void addPool(String poolName, JsonObject jo) {
        if (connectors.get(poolName) != null) {
            LOG.warn("Instance of " + poolName + " already exists - skipping");
            return;
        }
        ObjectMapper om = new ObjectMapper();
        PoolElementInfo pi = om.convertValue(jo.toMap(), PoolElementInfo.class).setPool_name(poolName);
        try {
            connectors.put(poolName, new PoolInstance(pi));
        } catch (IllegalArgumentException ia) {
            LOG.error(ia.getMessage());
        }
    }

    public void addPools(final JsonObject config) {
        if (cacheManager == null) {
            cacheManager = HttpCacheManager.cacheDisabled();
        }
        addPools(config, cacheManager);
    }

    /**
     *
     * @param config - a json representing a ConnElementInfo
     * @param instance - the hazelcast instance
     */
    public void addPools(final JsonObject config, HttpCacheManager instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Hazelcast Instance cannot be null");
        }
        if (cacheManager == null) {
            cacheManager = instance;
        }
        config.getFieldNames().stream().forEach((field) -> {
            JsonObject current = config.getObject(field);
            PoolManager.HTTP.addPool(field, current);
        });

    }

    /**
     * Shutdown all the pools and the scheduled pool jobs
     */
    public void shutdown() {
        LOG.info("Shutting down the whole HTTP Connector");
        scheduler.shutdownNow();
        connectors.entrySet().stream().forEach((pool) -> {
            pool.getValue().shutDown();
        });
    }

    /**
     *
     * @return a map with K poolname and V the stats jsonobject
     */
    public Map<String, JsonObject> getStats() {
        Map<String, JsonObject> result = new LinkedHashMap<>();
        connectors.entrySet().stream().forEach((pool) -> {
            result.put(pool.getKey(), pool.getValue().poolStats());
        });
        return result;
    }

    private static class Cleaner implements Runnable {

        @Override
        public void run() {
            LOG.debug("Cleaning the pools from expired and idles connections");
            connectors.entrySet().stream().forEach((pool) -> {
                pool.getValue().cleanPool();
            });
        }
    }
    
    public HttpCacheManager getCacheManager(){
        return cacheManager;
    }
}
