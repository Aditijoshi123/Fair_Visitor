package com.vms.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vms.entity.Visitor;

@Repository
public interface VisitorRepo extends JpaRepository<Visitor,Long> {

    Visitor findByEmail(String email);
}
