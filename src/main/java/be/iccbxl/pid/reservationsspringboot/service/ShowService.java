package be.iccbxl.pid.reservationsspringboot.service;

import be.iccbxl.pid.reservationsspringboot.model.Location;
import be.iccbxl.pid.reservationsspringboot.model.Show;
import be.iccbxl.pid.reservationsspringboot.model.Tag;
import be.iccbxl.pid.reservationsspringboot.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ShowService {
    @Autowired
    private ShowRepository repository;


    public List<Show> getAll() {
        List<Show> shows = new ArrayList<>();

        repository.findAll().forEach(shows::add);

        return shows;
    }

    public Show get(String id) {
        Long indice = (long) Integer.parseInt(id);
        Optional<Show> show = repository.findById(indice);

        return show.isPresent() ? show.get() : null;
    }

    public void add(Show show) {
        repository.save(show);
    }

    public void update(String id, Show show) {
        repository.save(show);
    }

    public void delete(String id) {
        Long indice = (long) Integer.parseInt(id);

        repository.deleteById(indice);
    }

    public void save(Show show) {
        repository.save(show);
    }

    public List<Show> getByTag(Tag tag) {
        return repository.findByTagsContaining(tag);
    }

    public List<Show> getFromLocation(Location location) {
        return repository.findByLocation(location);
    }

    public Show getWithAssociations(String id) {
        Long indice = Long.parseLong(id);
        return repository.findByIdWithAssociations(indice).orElse(null);
    }
    public Show getBySlugOrId(String param) {
        try {
            Long id = Long.parseLong(param);
            return repository.findByIdWithAssociations(id).orElse(null);
        } catch (NumberFormatException e) {
            return repository.findBySlugWithAssociations(param).orElse(null);
        }
    }


    public List<Show> getWithoutTag(Tag tag) {
        return repository.findByTagsNotContaining(tag);
    }

}

