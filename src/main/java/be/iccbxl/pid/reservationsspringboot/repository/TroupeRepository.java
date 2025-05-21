package be.iccbxl.pid.reservationsspringboot.repository;

import be.iccbxl.pid.reservationsspringboot.model.Troupe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TroupeRepository extends JpaRepository<Troupe, Long> {
}
