package com.vms.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vms.dto.AddressDto;
import com.vms.dto.AllPendingVisitsDTO;
import com.vms.dto.VisitDto;
import com.vms.dto.VisitResponseDto;
import com.vms.dto.VisitorDto;
import com.vms.entity.Address;
import com.vms.entity.Flat;
import com.vms.entity.Visit;
import com.vms.entity.Visitor;
import com.vms.enums.VisitStatus;
import com.vms.exception.BadRequestException;
import com.vms.exception.NotFoundException;
import com.vms.repo.FlatRepo;
import com.vms.repo.VisitRepo;
import com.vms.repo.VisitorRepo;
import com.vms.util.CommonUtil;
import com.vms.util.RedisKeyCleanupScheduler;

@Service
public class GateKeeperService {
	private Logger LOGGER = LoggerFactory.getLogger(GateKeeperService.class);
    @Autowired
    private VisitorRepo visitorRepo;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private FlatRepo flatRepo;

    @Autowired
    private VisitRepo visitRepo;

    @Autowired
    private RedisTemplate<String, AllPendingVisitsDTO> redisTemplate;
    
    @Autowired
	RedisKeyCleanupScheduler redisKeyCleanup;
    
//    @Autowired
//    private RedisTemplate<String, VisitorDto> redisTemplate1;

//    public VisitorDto getVisitor(String email){
//        /*
//        Cache logic
//        Key: visitor:{email}
//        Value: Object VisitorDto
//         */
//        String key = "visitor:"+email;
//        VisitorDto visitorDto = redisTemplate1.opsForValue().get(key);
//        if(visitorDto == null){
//            Visitor visitor = visitorRepo.findByEmail(email);
//            if(visitor != null){
//                visitorDto = VisitorDto.builder()
//                        .name(visitor.getName())
//                        .email(visitor.getEmail())
//                        .phone(visitor.getPhone())
//                        .build();
//            }
//            redisTemplate1.opsForValue().set(key,visitorDto,24, TimeUnit.HOURS );
//        }
//        return visitorDto;
//    }

    public Long createVisitor(VisitorDto visitorDto){
    	Visitor visitor =new Visitor();
        visitor.setName(visitorDto.getName());
        visitor.setEmail(visitorDto.getEmail());
        visitor.setPhone(visitorDto.getPhone());
        
        if(visitorDto.getAddress()!=null)
        {
        	AddressDto addressDto = visitorDto.getAddress();
        	Address address = commonUtil.convertAddressDTOT(addressDto);
        	visitor.setAddress(address);
        } 

        visitor = visitorRepo.save(visitor);
        return visitor.getId();
    }


    public Long createVisit(VisitDto visitDto){
        Flat flat = flatRepo.findByNumber(visitDto.getFlatNumber());
        if(flat==null)
        	throw new BadRequestException("Invalid Flat number!Flat does not exist");
        Visitor visitor = visitorRepo.findByEmail(visitDto.getEmail());
        if(visitor==null)
        	throw new BadRequestException("Email does not exist create visitor");
        Visit visit = Visit.builder()
                .flat(flat)
                .imageUrl(visitDto.getUrlOfImage())
                .noOfPeople(visitDto.getNoOfPeople())
                .purpose(visitDto.getPurpose())
                .visitor(visitor)
                .status(VisitStatus.WAITING)
                .build();
        visit = visitRepo.save(visit);
        
        //Adding waiting visit to Redis
        String key = "pendingVisits:flatId:"+visit.getFlat().getId();	 
        try {   
        	VisitResponseDto visitResponseDto = VisitResponseDto.builder()
        			.purpose(visitDto.getPurpose())
        			.urlOfImage(visitDto.getUrlOfImage())
        			.email(visitDto.getEmail())
        			.noOfPeople(visitDto.getNoOfPeople())
        			.flatNumber(visitDto.getFlatNumber())
        			.status(VisitStatus.WAITING)
        			.visitorPhone(visitor.getPhone())
        			.visitorName(visitor.getName())
        			.build();
	        
	        AllPendingVisitsDTO pendingVisits = redisTemplate.opsForValue().get(key);
	        if(pendingVisits == null){
	        	pendingVisits = new AllPendingVisitsDTO();
	            List<VisitResponseDto> visitResponseDtoList = new ArrayList<>();
	            visitResponseDtoList.add(visitResponseDto);
	            pendingVisits.setVisits(visitResponseDtoList);
	        }
	        else{
	        	pendingVisits.getVisits().add(visitResponseDto);
	        }
	        redisTemplate.opsForValue().set(key,pendingVisits);
        }
        catch(QueryTimeoutException | RedisConnectionFailureException e)
        {
        	LOGGER.error("Redis is down, cannot set key: " +  e);
        	//Delete Redis key as it may have inconsistent data
	        redisKeyCleanup.markKeyForCleanup(key);
        }        
        return visit.getId();
    }

    @Transactional
    public String markEntry(Long id){
        Optional<Visit> visitOptional = visitRepo.findById(id);
        if(visitOptional.isEmpty()){
            throw new NotFoundException("Visit not Found");
        }
        Visit visit = visitOptional.get();
        if(visit.getStatus().equals(VisitStatus.APPROVED)){
            visit.setInTime(new Date());
           // visitRepo.save(visit); // without Transactional
        }
        else{
            throw new BadRequestException("Invalid State Transition");
        }
        return "Done";
    }


    @Transactional
    public String markExit(Long id){
        Visit visit = visitRepo.findById(id).get();
        if(visit == null){
            throw new NotFoundException("Visit not Found");
        }
        if(visit.getStatus().equals(VisitStatus.APPROVED) && visit.getInTime() != null){
            visit.setOutTime(new Date());
            visit.setStatus(VisitStatus.COMPLETED);
        }
        else{
            throw new BadRequestException("Invalid State Transition");
        }
        return "Done";
    }
}
