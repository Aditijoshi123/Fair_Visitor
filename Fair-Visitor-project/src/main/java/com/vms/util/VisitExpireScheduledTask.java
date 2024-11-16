package com.vms.util;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.vms.entity.Visit;
import com.vms.enums.VisitStatus;
import com.vms.repo.VisitRepo;

@Configuration
public class VisitExpireScheduledTask {
    private Logger LOGGER = LoggerFactory.getLogger(VisitExpireScheduledTask.class);

    @Autowired
    private VisitRepo visitRepo;
    
    @Autowired
    CommonUtil commonutil;

    @Scheduled(fixedDelay = 1800000)
    //Remove WAITING visits from DB
    public void markVisitAsExpired(){
        LOGGER.info("Marking visit as Expired");
        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(30);
		List<Visit> visitList = visitRepo.findByStatusAndCreatedDateLessThanEqual(VisitStatus.WAITING,thresholdTime);
        for(Visit visit:visitList){
            visit.setStatus(VisitStatus.EXPIRE);
            commonutil.removeVisitFromCache(visit.getFlat().getId(), visit.getId());
        }
        visitRepo.saveAll(visitList);
    }
}
