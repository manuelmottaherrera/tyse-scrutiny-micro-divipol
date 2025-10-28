package com.tyse.scrutiny.micro.divipol.repository;

import com.tyse.scrutiny.micro.divipol.domain.Divipol;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data R2DBC repository for the Divipol entity.
 */
@Repository
public interface DivipolRepository extends R2dbcRepository<Divipol, Integer>, DivipolRepositoryCustom {}
