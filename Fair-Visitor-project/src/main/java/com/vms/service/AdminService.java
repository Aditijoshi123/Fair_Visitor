package com.vms.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vms.dto.AddressDto;
import com.vms.dto.AllUserResponseDto;
import com.vms.dto.UserDto;
import com.vms.dto.UserResponseDto;
import com.vms.entity.Address;
import com.vms.entity.Flat;
import com.vms.entity.User;
import com.vms.enums.UserStatus;
import com.vms.exception.NotFoundException;
import com.vms.repo.FlatRepo;
import com.vms.repo.UserRepo;
import com.vms.util.CommonUtil;

import jakarta.validation.Valid;

@Service
public class AdminService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FlatRepo flatRepo;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public void markInactive(Long userId){
        User user = userRepo.findById(userId).get();
        if(user==null){
            throw new NotFoundException("User does not exist");
        }
        user.setStatus(UserStatus.INACTIVE);
        userRepo.save(user);
    }

    public void markActive(Long userId){
        User user = userRepo.findById(userId).orElse(null);
        if(user==null){
            throw new NotFoundException("User does not exist");
        }
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);
    }

    @Transactional
    public AllUserResponseDto findAllWithPagination(Pageable pageable) {
       final Page<User> userPage = userRepo.findAll(pageable);
       List<User> users = userPage.stream().toList();
       List<UserResponseDto> userResponseDto=new ArrayList<>();
       AllUserResponseDto allUserResponseDto=new AllUserResponseDto();
       for(User user:users)
       {
    	   AddressDto address =new AddressDto();
    	   if(user.getAddress()!=null)
    	   {
    		   		address = AddressDto.builder()
                   .line1(user.getAddress().getLine1())
                   .line2(user.getAddress().getLine2())
                   .city(user.getAddress().getCity())
                   .pincode(user.getAddress().getPincode())
                   .build();
    	   }    	   
    	   String flatNo=null;
    	   if(user.getFlat()!=null)
    	   {
    		   flatNo= user.getFlat().getNumber();
    	   }    	   
    	   UserResponseDto mapToUserDto= new UserResponseDto();
    	   mapToUserDto.setAddress(address);
    	   mapToUserDto.setName(user.getName());
    	   mapToUserDto.setRole(user.getRole());
    	   mapToUserDto.setPhone(user.getPhone());
    	   mapToUserDto.setFlatNo(flatNo);
    	   mapToUserDto.setEmail(user.getEmail());
    	   mapToUserDto.setUserID(user.getId());    	   
    	   userResponseDto.add(mapToUserDto);
       }
       allUserResponseDto.setUserResponseDtoList(userResponseDto);
       allUserResponseDto.setTotalPages(userPage.getTotalPages());
       allUserResponseDto.setTotalRows(userPage.getTotalElements());
       return allUserResponseDto;
    }

    public Long createUser(UserDto userDto){        
        Flat flat = flatRepo.findByNumber(userDto.getFlatNo());
        if(flat==null)
        {
        	flat = Flat.builder().number(userDto.getFlatNo()).build();
        }
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setRole(userDto.getRole());
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFlat(flat);
        
        if(userDto.getAddress()!=null)
        {
        	AddressDto addressDto = userDto.getAddress();
        	Address address = commonUtil.convertAddressDTOT(addressDto);
        	user.setAddress(address);
        }     
        user = userRepo.save(user);
        return user.getId();
    }

    @Transactional
	public void updateUser(@Valid UserDto userDto,Long userId) {
		User user = userRepo.findById(userId).orElse(null);
        if(user==null){
            throw new NotFoundException("User does not exist");
        }
        Flat flat = flatRepo.findByNumber(userDto.getFlatNo());
        if(flat==null)
        {
        	flat = Flat.builder().number(userDto.getFlatNo()).build();
        }
        user.setId(userId);
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setRole(userDto.getRole());
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFlat(flat);
        
        if(userDto.getAddress()!=null)
        {
        	AddressDto addressDto = userDto.getAddress();
        	Address address = commonUtil.convertAddressDTOT(addressDto);
        	user.setAddress(address);
        }
	}

}
