package productdetector.model;


import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "TUSER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID")
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(name="USERNAME")
    private String username;

    @NotBlank
    @Size(max = 100)
    @Column(name="PASSWORD")
    private String password;

    @NotNull
    @Column(name="ENABLED")
    private Boolean enabled = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TUSER_ROLE",
            joinColumns = @JoinColumn(name = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
    private Set<Role> roles = new HashSet<>();

}