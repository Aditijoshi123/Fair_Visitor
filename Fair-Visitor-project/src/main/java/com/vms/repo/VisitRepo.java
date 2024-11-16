package com.vms.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vms.entity.Flat;
import com.vms.entity.Visit;
import com.vms.enums.VisitStatus;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface VisitRepo extends JpaRepository<Visit, Long> {

    List<Visit> findByStatusAndFlat(VisitStatus status, Flat flat);

    Page<Visit> findByStatusAndFlat(VisitStatus status, Flat flat, Pageable pageable);

	List<Visit> findByStatusAndCreatedDateLessThanEqual(VisitStatus waiting, LocalDateTime thresholdTime);

}
