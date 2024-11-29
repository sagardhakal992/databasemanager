package com.sagardhakal.dbadmin.Service;

import com.sagardhakal.dbadmin.Models.DatabaseDetail;
import com.sagardhakal.dbadmin.Repositories.DatabaseDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseService {
    @Autowired
    private DatabaseDetailRepository detailRepository;
    @Autowired
    private DatabaseDetailRepository databaseDetailRepository;

    public List<DatabaseDetail>getAllDatabaseDetails(){
        return detailRepository.findAll();
    }

    public void createNewDatabase(DatabaseDetail databaseDetail) {
        detailRepository.save(databaseDetail);
    }

    public DatabaseDetail getDatabaseDetails(Long id)throws Exception {
        return databaseDetailRepository.findById(id).orElseThrow(()->new Exception("Database detail not found"));
    }

    public void delete(DatabaseDetail databaseDetail) {
        databaseDetailRepository.delete(databaseDetail);
    }
}
