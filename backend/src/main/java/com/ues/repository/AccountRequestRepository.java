package com.ues.repository;

import com.ues.model.AccountRequest;
import com.ues.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRequestRepository extends JpaRepository<AccountRequest, Long> {
    Optional<AccountRequest> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<AccountRequest> findByStatus(RequestStatus status);
}
