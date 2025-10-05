package org.example.project;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field; // <-- Import this



@Document(collection = "mycollection")
public class DataHolder {

    @Id
    private String id;

    @Field("Col-1")
    public double value;

    public DataHolder() {}
    public DataHolder(double value) {

        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
