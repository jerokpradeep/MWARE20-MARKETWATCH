package in.codifi.mw.cache;

import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Getter
@Setter
public class RedisConfig {
	private static RedisConfig redisConfig = null;
	private JedisPool jedisPool = null;

// Private constructor to ensure singleton pattern
	private RedisConfig() {
		// Create a JedisPoolConfig to configure the pool (e.g., max connections,
		// timeout)
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(50); // Set max connections
		poolConfig.setMaxIdle(20); // Set max idle connections
		poolConfig.setMinIdle(1); // Set min idle connections
		poolConfig.setTestOnBorrow(true); // Test connections before borrowing

		// Create a JedisPool with the JedisPoolConfig
		this.jedisPool = new JedisPool(poolConfig, "localhost", 6701, 2000); // Replace with actual host/port
	}

	// Singleton pattern to get instance
	public static RedisConfig getInstance() {
		if (redisConfig == null) {
			redisConfig = new RedisConfig();
		}
		return redisConfig;
	}

// Method to get a Jedis resource from the pool
	public Jedis getJedis() {
		if (jedisPool == null) {
			throw new IllegalStateException("Jedis pool is not initialized.");
		}
		return jedisPool.getResource(); // Get a Jedis instance from the pool
	}

// Close the pool when you're done using it (called on shutdown)
	public void closePool() {
		if (jedisPool != null) {
			jedisPool.close();
		}
	}
}
