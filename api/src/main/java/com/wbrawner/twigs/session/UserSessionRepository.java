package com.wbrawner.twigs.session;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends PagingAndSortingRepository<Session, String> {
    List<Session> findByUserId(String userId);

    Optional<Session> findByToken(String token);

    Optional<Session> findByUserIdAndToken(String userId, String token);

    void deleteAllByExpirationBefore(Date expiration);
}