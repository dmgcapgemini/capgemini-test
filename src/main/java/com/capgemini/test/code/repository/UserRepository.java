package com.capgemini.test.code.repository;

import com.capgemini.test.code.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByIdAndRoom_Id(Long id, Long roomId);

  boolean existsByEmail(String email);

  boolean existsByDni(String dni);

  boolean existsByPhone(String phone);
}
