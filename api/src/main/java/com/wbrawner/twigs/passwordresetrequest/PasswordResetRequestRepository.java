package com.wbrawner.twigs.passwordresetrequest;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface PasswordResetRequestRepository extends PagingAndSortingRepository<PasswordResetRequest, Long> {
}