package be.iccbxl.pid.reservationsspringboot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "troupes")
public class Troupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The troupe name must not be empty.")
    @Size(min = 2, max = 60, message = "The name must be between 2 and 60 characters long.")
    private String name;

    @NotBlank(message = "The logo URL must not be empty.")
    @Size(max = 255, message = "The URL must be less than 255 characters.")
    private String logo_url;

    @OneToMany(mappedBy = "troupe")
    private List<Artist> artists = new ArrayList<>();
}
