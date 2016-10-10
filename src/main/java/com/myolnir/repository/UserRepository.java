package com.myolnir.repository;

import com.myolnir.model.UserAccountDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserAccountDO, Long> {

    @Query("select u from UserAccountDO u where email = :email")
    UserAccountDO findByEmail(@Param("email") final String email);
}
