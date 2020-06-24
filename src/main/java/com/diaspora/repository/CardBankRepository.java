package com.diaspora.repository;

import com.diaspora.domain.CardBank;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the CardBank entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CardBankRepository extends JpaRepository<CardBank, Long> {

}
