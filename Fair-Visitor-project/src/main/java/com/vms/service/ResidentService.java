package com.vms.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.vms.dto.AllPendingVisitsDTO;
import com.vms.dto.VisitResponseDto;
import com.vms.entity.Flat;
import com.vms.entity.User;
import com.vms.entity.Visit;
import com.vms.entity.Visitor;
import com.vms.enums.VisitStatus;
import com.vms.exception.BadRequestException;
import com.vms.exception.NotFoundException;
import com.vms.repo.UserRepo;
import com.vms.repo.VisitRepo;
import com.vms.util.CommonUtil;
import com.vms.util.RedisKeyCleanupScheduler;


@Service
public class ResidentService {
	private Logger LOGGER = LoggerFactory.getLogger(ResidentService.class);
	
    @Autowired
    private VisitRepo visitRepo;

    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired
    RedisKeyCleanupScheduler redisKeyCleanup;
    
    @Autowired
    private RedisTemplate<String, AllPendingVisitsDTO> redisTemplate;


    public String updateVisit(Long id, VisitStatus visitStatus){
        if(visitStatus != VisitStatus.REJECTED && visitStatus != VisitStatus.APPROVED){
            throw new BadRequestException("Invalid state transition");
        }
        Visit visit = visitRepo.findById(id).get();
        if(visit == null){
            throw new NotFoundException("Visit not found");
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getFlat().getId() != visit.getFlat().getId()){
            throw new BadRequestException("Wrong visit id");
        }
        commonUtil.removeVisitFromCache(visit.getFlat().getId(),id);
        
        if( VisitStatus.WAITING.equals(visit.getStatus()) ){
            visit.setStatus(visitStatus);
            visit.setApprovedBy(user);
            visitRepo.save(visit);        
        }
        else {
            throw new BadRequestException("Invalid state transition");
        }
        return "Done";
    }

    public List<VisitResponseDto> getPendingVisits(Long userId){
        User user = userRepo.findById(userId).get();
        Flat flat = user.getFlat();
        List<VisitResponseDto> visitResponseDtoList = null;
        String key = "pendingVisits:flatId:"+flat.getId();
        try {
        	//Getting from Redis
	        AllPendingVisitsDTO pendingVisitsDto = redisTemplate.opsForValue().get(key);
	        if(pendingVisitsDto != null){
	        	visitResponseDtoList= pendingVisitsDto.getVisits();
	        }
	        else {
	        	//Getting from DB, maybe some issue in Redis and setting key in Redis again
		        List<Visit> visitList = visitRepo.findByStatusAndFlat(VisitStatus.WAITING, flat);
		        pendingVisitsDto =new AllPendingVisitsDTO();
		        
		        visitResponseDtoList =new ArrayList<>();
		        if(visitList!=null)
		        {
			        for(Visit visit : visitList ){
			            Visitor visitor = visit.getVisitor();
			            VisitResponseDto visitResponseDto = VisitResponseDto.builder()
			                    .flatNumber(flat.getNumber())
			                    .purpose(visit.getPurpose())
			                    .noOfPeople(visit.getNoOfPeople())
			                    .urlOfImage(visit.getImageUrl())
			                    .visitorName(visitor.getName())
			                    .visitorPhone(visitor.getPhone())
			                    .status(visit.getStatus())
			                    .email(visitor.getEmail())
			                    .visitId(visit.getId())
			                    .build();
			            visitResponseDtoList.add(visitResponseDto);
			        }
		        }
		        if(visitResponseDtoList!=null)
		        {
			        pendingVisitsDto.setVisits(visitResponseDtoList);
			        redisTemplate.opsForValue().set(key,pendingVisitsDto);
		        }
	        }
        }
        catch(QueryTimeoutException | RedisConnectionFailureException e)
        {
        	LOGGER.error("Redis Connection Failed");
        	//Fetch from DB incase of failure
        	List<Visit> visitList = visitRepo.findByStatusAndFlat(VisitStatus.WAITING, flat);
	        visitResponseDtoList =new ArrayList<>();
	        if(visitList!=null)
	        {
		        for(Visit visit : visitList ){
		            Visitor visitor = visit.getVisitor();
		            VisitResponseDto visitResponseDto = VisitResponseDto.builder()
		                    .flatNumber(flat.getNumber())
		                    .purpose(visit.getPurpose())
		                    .noOfPeople(visit.getNoOfPeople())
		                    .urlOfImage(visit.getImageUrl())
		                    .visitorName(visitor.getName())
		                    .visitorPhone(visitor.getPhone())
		                    .status(visit.getStatus())
		                    .email(visitor.getEmail())
		                    .visitId(visit.getId())
		                    .build();
		            visitResponseDtoList.add(visitResponseDto);
		        }
	        }
	        //Delete Redis key with inconsistent data
	        redisKeyCleanup.markKeyForCleanup(key);
        }       
        return visitResponseDtoList;
    }
}
