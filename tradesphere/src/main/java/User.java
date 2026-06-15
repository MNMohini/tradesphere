import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
   private int id;
   private String name;
   private String role;
    private long phoneNumber;
    @id
    private String email;
    private String password;

}