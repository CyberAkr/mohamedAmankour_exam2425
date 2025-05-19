package be.iccbxl.pid.reservationsspringboot.controller;

import be.iccbxl.pid.reservationsspringboot.model.*;
import be.iccbxl.pid.reservationsspringboot.repository.RepresentationRepository;
import be.iccbxl.pid.reservationsspringboot.service.ReviewService;
import be.iccbxl.pid.reservationsspringboot.service.ShowService;
import be.iccbxl.pid.reservationsspringboot.service.TagService;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@SessionAttributes("cart")
@RequestMapping("/shows")
public class ShowController {

    @ModelAttribute("cart")
    public Cart cart() {
        return new Cart();
    }

    @Autowired
    private ShowService showService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private RepresentationRepository representationRepo;

    // === Liste des spectacles ===
    @GetMapping
    public String index(@RequestParam(value = "tag", required = false) String tagLabel, Model model) {
        List<Show> shows;
        String title = "Liste des spectacles";

        if (tagLabel != null && !tagLabel.isBlank()) {
            Tag tag = tagService.findByTag(tagLabel).orElse(null);
            if (tag != null) {
                shows = showService.getByTag(tag);
                model.addAttribute("resultCount", shows.size());
                title += " – Mots-clés : " + tagLabel;
            } else {
                shows = new ArrayList<>();
                model.addAttribute("errorMessage", "Mot-clé introuvable");
            }
        } else {
            shows = showService.getAll();
        }

        model.addAttribute("shows", shows);
        model.addAttribute("title", title);
        model.addAttribute("availableTags", tagService.findAll());

        return "show/index";
    }

    // === Fiche spectacle (par ID ou slug) ===
    @GetMapping("/{param}")
    @Transactional
    public String show(@PathVariable("param") String param, Model model) {
        Show show = showService.getBySlugOrId(param);
        if (show == null) {
            model.addAttribute("errorMessage", "Spectacle introuvable.");
            return "error/404";
        }

        Hibernate.initialize(show.getTags());
        show.setTags(new HashSet<>(show.getTags()));

        Set<ArtistType> uniqueArtistTypes = new HashSet<>(show.getArtistTypes());
        Map<String, ArrayList<Artist>> collaborateurs = new TreeMap<>();
        for (ArtistType at : uniqueArtistTypes) {
            String type = at.getType().getType();
            ArrayList<Artist> artistes = collaborateurs.computeIfAbsent(type, k -> new ArrayList<>());
            if (!artistes.contains(at.getArtist())) {
                artistes.add(at.getArtist());
            }
        }

        show.getRepresentations().forEach(rep -> Hibernate.initialize(rep.getItems()));
        boolean canBook = show.getRepresentations().stream().anyMatch(r -> r.getAvailableSeats() > 0);

        model.addAttribute("show", show);
        model.addAttribute("canBook", canBook);
        model.addAttribute("collaborateurs", collaborateurs);
        model.addAttribute("availableTags", tagService.findAll());
        model.addAttribute("reviews", reviewService.getReviewsByShowId(show.getId()));
        model.addAttribute("title", "Fiche d'un spectacle");

        return "show/show";
    }

    // === Ajouter un mot-clé (ADMIN uniquement) ===
    @PostMapping("/{param}/tags")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public String addTagToShow(@PathVariable("param") String param,
                               @RequestParam("tagId") Long tagId,
                               RedirectAttributes redirectAttributes) {
        Show show = showService.getBySlugOrId(param);
        Tag tag = tagService.find(tagId).orElse(null);

        if (show != null && tag != null) {
            Hibernate.initialize(show.getTags());
            Set<Tag> updatedTags = new HashSet<>(show.getTags());

            if (!updatedTags.contains(tag)) {
                updatedTags.add(tag);
                show.setTags(updatedTags);
                showService.save(show);
                redirectAttributes.addFlashAttribute("successMessage", "Mot-clé ajouté !");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Ce mot-clé est déjà associé.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Spectacle ou mot-clé introuvable.");
        }

        return "redirect:/shows/" + show.getSlug();
    }

    // === Exclure un mot-clé de la liste ===
    @GetMapping("/exclude-tag/{tag}")
    public String showsWithoutTag(@PathVariable("tag") String tagLabel, Model model) {
        Tag tag = tagService.findByTag(tagLabel).orElse(null);
        if (tag == null) {
            model.addAttribute("errorMessage", "Mot-clé non trouvé.");
            return "redirect:/shows";
        }

        List<Show> shows = showService.getWithoutTag(tag);
        model.addAttribute("shows", shows);
        model.addAttribute("title", "Spectacles sans le mot-clé : " + tagLabel);
        model.addAttribute("availableTags", tagService.findAll());

        return "show/index";
    }

    // === Réserver une représentation ===
    @PostMapping("/{param}/reserve")
    public String reserveToCart(@PathVariable("param") String param,
                                @RequestParam Long representationId,
                                @RequestParam Long priceId,
                                @RequestParam int quantity,
                                @ModelAttribute("cart") Cart cart) {

        Representation rep = representationRepo.findById(representationId).orElse(null);
        Price price = rep != null ? rep.getShow().getPrices().stream()
                .filter(p -> p.getId().equals(priceId))
                .findFirst()
                .orElse(null) : null;

        if (rep == null || price == null) {
            return "redirect:/shows/" + param + "?error";
        }

        CartItem item = new CartItem();
        item.setRepresentationId(rep.getId());
        item.setPriceId(price.getId());
        item.setQuantity(quantity);
        item.setLabel(rep.getScheduledAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        item.setUnitPrice(price.getPrice());

        cart.addItem(item);
        return "redirect:/cart/view";
    }
}
