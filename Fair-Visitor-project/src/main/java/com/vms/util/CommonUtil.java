package com.vms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
//import com.vms.config.*;
import org.springframework.stereotype.Component;

import com.vms.dto.AddressDto;
import com.vms.dto.AllPendingVisitsDTO;
import com.vms.entity.Address;
import com.vms.repo.AddressRepo;
import com.vms.service.ResidentService;
@Component
public class CommonUtil {	
	private Logger LOGGER = LoggerFactory.getLogger(ResidentService.class);
	@Autowired
	AddressRepo addressRepo;
	
	@Autowired
    private RedisTemplate<String, AllPendingVisitsDTO> redisTemplate;
	
	@Autowired
	RedisKeyCleanupScheduler redisKeyCleanup;

    public Address convertAddressDTOT(AddressDto addressDto){   	
            Address address = addressRepo.findByLine1AndLine2AndCityAndPincode(
            	addressDto.getLine1(),
            	addressDto.getLine2(),
            	addressDto.getCity(),
            	addressDto.getPincode()
            );
            if(address==null)
            {
            address = Address.builder()
                    .line1(addressDto.getLine1())
                    .line2(addressDto.getLine2())
                    .city(addressDto.getCity())
                    .pincode(addressDto.getPincode())
                    .build();
            }
        return address;
    }

	public void removeVisitFromCache(Long flatid, Long visitid) {
		// TODO Auto-generated method stub	
		String key = "pendingVisits:flatId:"+flatid;
		try {
        AllPendingVisitsDTO pendingVisitsDto = redisTemplate.opsForValue().get(key);        
        pendingVisitsDto.getVisits().removeIf(visit -> visit.getVisitId().equals(visitid));
        
        if(pendingVisitsDto!=null)
        	redisTemplate.opsForValue().set(key, pendingVisitsDto);
		}
		catch (QueryTimeoutException | RedisConnectionFailureException e) {
			redisKeyCleanup.markKeyForCleanup(key);
            LOGGER.error("Unexpected error occurred: " + key);
        }
		catch(Exception e)
		{
			redisKeyCleanup.markKeyForCleanup(key);
			LOGGER.error("Unexpected error occurred2: " + key, e);
		}
	}
}
