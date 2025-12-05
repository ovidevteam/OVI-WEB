package ovi.web.dhybe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ovi.web.dhybe.dto.auth.UserDto;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDtoRepository extends JpaRepository<UserDto, Integer> {
    Optional<UserDto> findByUsername(String username);

    List<UserDto> findByRoleIgnoreCase(String role);

    List<UserDto> findByRoleIgnoreCaseAndStatus(String role, String status);
}
