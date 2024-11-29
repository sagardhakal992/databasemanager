package com.sagardhakal.dbadmin.Repositories;


import com.sagardhakal.dbadmin.Models.DatabaseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDetailRepository extends JpaRepository<DatabaseDetail,Long> {
}
