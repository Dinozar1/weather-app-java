package com.example.weatherappjava.service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serwis obsługujący cachowanie danych pogodowych w Redis
 */
public class RedisCacheService {
    private static final Logger LOGGER = Logger.getLogger(RedisCacheService.class.getName());

    // Stałe określające czas życia cache'a (TTL) w sekundach
    private static final int FORECAST_TTL = 3600; // 1 godzina dla prognoz
    private static final int HISTORICAL_TTL = 86400 * 7; // 7 dni dla danych historycznych

    // Połączenie z Redis
    private final JedisPool jedisPool;

    // Singleton instance
    private static RedisCacheService instance;

    /**
     * Metoda zwracająca instancję serwisu (Singleton pattern)
     */
    public static synchronized RedisCacheService getInstance() {
        if (instance == null) {
            instance = new RedisCacheService();
        }
        return instance;
    }

    /**
     * Konstruktor prywatny (Singleton pattern)
     */
    private RedisCacheService() {
        // Konfiguracja połączenia z Redis
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        // Połączenie z Redis - domyślnie localhost:6379
        this.jedisPool = new JedisPool(poolConfig, "localhost", 6379);

        LOGGER.info("Inicjalizacja połączenia z Redis");
    }

    /**
     * Sprawdza, czy dane są dostępne w cache
     */
    public boolean hasCache(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        } catch (JedisConnectionException e) {
            LOGGER.log(Level.WARNING, "Nie można połączyć się z Redis: " + e.getMessage());
            return false;
        }
    }

    /**
     * Pobiera dane z cache
     */
    public String getFromCache(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (JedisConnectionException e) {
            LOGGER.log(Level.WARNING, "Nie można połączyć się z Redis: " + e.getMessage());
            return null;
        }
    }

    /**
     * Zapisuje dane do cache z określonym TTL
     */
    public void saveToCache(String key, String data, boolean isForecast) {
        try (Jedis jedis = jedisPool.getResource()) {
            int ttl = isForecast ? FORECAST_TTL : HISTORICAL_TTL;
            jedis.setex(key, ttl, data);
            LOGGER.info("Zapisano dane do cache z kluczem: " + key + " (TTL: " + ttl + "s)");
        } catch (JedisConnectionException e) {
            LOGGER.log(Level.WARNING, "Nie można połączyć się z Redis: " + e.getMessage());
        }
    }

    /**
     * Generuje klucz cache dla prognozy pogody
     */
    public String generateForecastCacheKey(double latitude, double longitude) {
        return String.format("forecast:%f:%f", latitude, longitude);
    }

    /**
     * Generuje klucz cache dla danych historycznych
     */
    public String generateHistoricalCacheKey(double latitude, double longitude, LocalDate startDate, LocalDate endDate) {
        return String.format("historical:%f:%f:%s:%s", latitude, longitude, startDate, endDate);
    }

    /**
     * Usuwa wszystkie dane z cache
     */
    public void clearCache() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushAll();
            LOGGER.info("Cache został wyczyszczony");
        } catch (JedisConnectionException e) {
            LOGGER.log(Level.WARNING, "Nie można połączyć się z Redis: " + e.getMessage());
        }
    }

    /**
     * Zamyka połączenie z Redis
     */
    public void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            LOGGER.info("Połączenie z Redis zostało zamknięte");
        }
    }
}