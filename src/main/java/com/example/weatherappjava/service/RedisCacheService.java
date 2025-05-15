package com.example.weatherappjava.service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton service for caching weather data in Redis.
 */
public class RedisCacheService {
    private static final Logger LOGGER = Logger.getLogger(RedisCacheService.class.getName());

    // Cache TTL constants (in seconds)
    private static final int FORECAST_TTL = 3600; // 1 hour for forecasts

    private final JedisPool jedisPool;
    private static RedisCacheService instance;

    /**
     * Returns the singleton instance of the cache service.
     */
    public static synchronized RedisCacheService getInstance() {
        if (instance == null) {
            instance = new RedisCacheService();
        }
        return instance;
    }

    /**
     * Private constructor initializing Redis connection pool.
     */
    private RedisCacheService() {
        // Configure Redis connection pool
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        // Connect to Redis (default: localhost:6379)
        this.jedisPool = new JedisPool(poolConfig, "localhost", 6379);
        LOGGER.info("Initialized Redis connection");
    }

    /**
     * Checks if data exists in the cache for a given key.
     */
    public boolean hasCache(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        } catch (JedisConnectionException e) {
            LOGGER.log(Level.WARNING, "Failed to connect to Redis: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves data from the cache.
     */
    public String getFromCache(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (JedisConnectionException e) {
            LOGGER.log(Level.WARNING, "Failed to connect to Redis: " + e.getMessage());
            return null;
        }
    }

    /**
     * Saves data to the cache with a specified TTL.
     */
    public void saveToCache(String key, String data, boolean isForecast) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (isForecast) {
                jedis.setex(key, FORECAST_TTL, data);
                LOGGER.info("Cached forecast data with key: " + key + " (TTL: " + FORECAST_TTL + "s)");
            } else {
                jedis.set(key, data);
                LOGGER.info("Cached historical data with key: " + key + " (no TTL)");
            }
        } catch (JedisConnectionException e) {
            LOGGER.log(Level.WARNING, "Failed to connect to Redis: " + e.getMessage());
        }
    }

    /**
     * Generates a cache key for forecast data.
     */
    public String generateForecastCacheKey(double latitude, double longitude, int forecastDays) {
        return String.format("forecast:%f:%f:%d", latitude, longitude, forecastDays);
    }

    /**
     * Generates a cache key for historical data.
     */
    public String generateHistoricalCacheKey(double latitude, double longitude, LocalDate startDate, LocalDate endDate) {
        return String.format("historical:%f:%f:%s:%s", latitude, longitude, startDate, endDate);
    }

    /**
     * Clears all data from the Redis cache.
     */
    public void clearCache() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushAll();
            LOGGER.info("Cache cleared");
        } catch (JedisConnectionException e) {
            LOGGER.log(Level.WARNING, "Failed to connect to Redis: " + e.getMessage());
        }
    }

    /**
     * Closes the Redis connection pool.
     */
    public void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            LOGGER.info("Redis connection closed");
        }
    }
}