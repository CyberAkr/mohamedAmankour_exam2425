package be.iccbxl.pid.reservationsspringboot.controller;

import be.iccbxl.pid.reservationsspringboot.model.Show;
import be.iccbxl.pid.reservationsspringboot.model.Video;
import be.iccbxl.pid.reservationsspringboot.repository.ShowRepository;
import be.iccbxl.pid.reservationsspringboot.repository.VideoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/videos")
public class VideoController {

    private final VideoRepository videoRepository;
    private final ShowRepository showRepository;

    public VideoController(VideoRepository videoRepository, ShowRepository showRepository) {
        this.videoRepository = videoRepository;
        this.showRepository = showRepository;
    }

    // Affichage des vidéos d’un artiste donné
    @GetMapping("/artist/{name}")
    public String getVideosByArtist(@PathVariable String name, Model model) {
        List<Video> videos = videoRepository.findVideosByArtistFirstname(name);
        model.addAttribute("videos", videos);
        model.addAttribute("artistName", name);
        return "videos/byArtist";
    }

    // Formulaire d’ajout de vidéo
    @GetMapping("/add")
    public String showAddForm(Model model) {
        if (!isAdmin()) return "redirect:/";

        model.addAttribute("video", new Video());
        model.addAttribute("shows", showRepository.findAll());
        return "videos/add";
    }

    // Enregistrement de la vidéo
    @PostMapping("/add")
    public String addVideo(@ModelAttribute Video video) {
        if (!isAdmin()) return "redirect:/";

        videoRepository.save(video);
        return "redirect:/shows/" + video.getShow().getSlug();
    }

    // Vérifie si l'utilisateur courant a le rôle ADMIN via Spring Security
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
    }
}
