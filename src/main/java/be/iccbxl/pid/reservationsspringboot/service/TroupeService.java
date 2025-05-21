package be.iccbxl.pid.reservationsspringboot.service;

import be.iccbxl.pid.reservationsspringboot.model.Troupe;
import be.iccbxl.pid.reservationsspringboot.repository.TroupeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TroupeService {
    private final TroupeRepository repository;

    public TroupeService(TroupeRepository repository) {
        this.repository = repository;
    }

    public List<Troupe> getAllTroupes() {
        return repository.findAll();
    }

    public Troupe getTroupe(Long id) {
        return repository.findById(id).orElse(null);
    }
}
