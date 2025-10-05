package org.example.project.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.example.project.DataHolder;

public interface DataRepo extends MongoRepository<DataHolder,String>{
}
