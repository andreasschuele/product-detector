package productdetector.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "TPRODUCT")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID")
    private Long id;

    @Column(name="NAME")
    private String name;

    @Column(name="NOTES")
    private String notes;

    @Lob
    @Column(name="MAIN_IMAGE")
    private byte[] mainImage;

    @Column(name="MAIN_IMAGE_TYPE")
    private String mainImageType;

    @NotNull
    @Column(name="ACTIVE")
    private Boolean active;

    @Column(name="VISION_LABEL")
    private String visionLabel;

}
