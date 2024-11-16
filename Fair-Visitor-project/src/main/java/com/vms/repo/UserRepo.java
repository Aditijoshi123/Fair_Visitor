package com.vms.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vms.entity.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    User findByEmail(String email);
}
