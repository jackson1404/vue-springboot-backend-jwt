package com.jackson.vue.jwt_backend_integrate.repository;

import com.jackson.vue.jwt_backend_integrate.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
