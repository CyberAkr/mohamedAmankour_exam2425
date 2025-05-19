package be.iccbxl.pid.reservationsspringboot.repository;

import be.iccbxl.pid.reservationsspringboot.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    @Query("SELECT v FROM Video v JOIN v.show s JOIN s.artistTypes at JOIN at.artist a WHERE LOWER(a.firstname) = LOWER(:name)")
    List<Video> findVideosByArtistFirstname(@Param("name") String name);

}
