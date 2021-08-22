package productdetector.model;

import lombok.Data;
import org.hibernate.annotations.NaturalId;
import javax.persistence.*;

@Data
@Entity
@Table(name = "TROLE")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    @Column(name="NAME", length = 60)
    private RoleName name;

}
