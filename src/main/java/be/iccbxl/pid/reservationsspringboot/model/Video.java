package be.iccbxl.pid.reservationsspringboot.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;


    private String title;

    @Column(name = "video_url", unique = true)
    private String videoUrl;

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;
}
