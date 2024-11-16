package com.vms.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vms.entity.Flat;

public interface FlatRepo extends JpaRepository<Flat,Long> {

    Flat findByNumber(String number);
}
