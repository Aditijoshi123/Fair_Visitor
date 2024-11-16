package com.vms.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vms.entity.Address;

public interface AddressRepo extends JpaRepository<Address,Long> {

	Address findByLine1AndLine2AndCityAndPincode(String line1, String line2, String city, String pincode);
}
