package com.vms.util;

import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.vms.dto.AllPendingVisitsDTO;

@Component
public class RedisKeyCleanupScheduler {
	private Logger LOGGER = LoggerFactory.getLogger(RedisKeyCleanupScheduler.class);
    private static final ConcurrentSkipListSet<String> keysForCleanup = new ConcurrentSkipListSet<>();
    
    @Autowired
    private RedisTemplate<String, AllPendingVisitsDTO> redisTemplate;
	 
    public void markKeyForCleanup(String redisKey) {
        keysForCleanup.add(redisKey);
    }
	
    @Scheduled(fixedRate = 60000)
    public void attemptCleanup() {
        if (keysForCleanup.isEmpty()) {
            return;
        }

        for (String key : keysForCleanup) {
            try {
                redisTemplate.delete(key);
                keysForCleanup.remove(key);
            } catch (QueryTimeoutException | RedisConnectionFailureException e) {
                LOGGER.error("Redis is still down, cannot delete key: " + key);
                break;
            }
        }
    }
}
