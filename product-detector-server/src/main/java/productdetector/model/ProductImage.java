package productdetector.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Table(name = "TPRODUCT_IMAGE")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID")
    private Long id;

    @Column(name="productId")
    private Long productId;

    @Column(name="VISION_DATA_ID")
    private String visionDataId;

    @CreatedDate
    @Column(name="CREATED_AT")
    private Instant createdAt;

}
