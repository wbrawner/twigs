package com.wbrawner.twigs.user;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface PasswordResetRequestRepository extends PagingAndSortingRepository<PasswordResetRequest, Long> {
}