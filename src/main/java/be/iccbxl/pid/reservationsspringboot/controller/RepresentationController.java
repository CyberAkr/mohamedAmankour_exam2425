package be.iccbxl.pid.reservationsspringboot.controller;

import be.iccbxl.pid.reservationsspringboot.model.Representation;
import be.iccbxl.pid.reservationsspringboot.service.RepresentationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class RepresentationController {
    @Autowired
    RepresentationService service;

    @GetMapping("/representations")
    public String index(Model model) {
        List<Representation> representations = service.getAll();

        model.addAttribute("representations", representations);
        model.addAttribute("title", "Liste des representations");

        return "representation/index";
    }

    @GetMapping("/representations/{id}")
    public String show(Model model, @PathVariable("id") String id) {
        Representation representation = service.get(id);

        model.addAttribute("representation", representation);
        model.addAttribute("date", representation.getScheduledAt().toLocalDate());
        model.addAttribute("heure", representation.getScheduledAt().toLocalTime());
        model.addAttribute("title", "Fiche d'une representation");

        return "representation/show";
    }

}
