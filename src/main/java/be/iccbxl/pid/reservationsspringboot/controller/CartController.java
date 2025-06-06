package be.iccbxl.pid.reservationsspringboot.controller;

import be.iccbxl.pid.reservationsspringboot.model.*;
import be.iccbxl.pid.reservationsspringboot.repository.PriceRepository;
import be.iccbxl.pid.reservationsspringboot.repository.RepresentationRepository;
import be.iccbxl.pid.reservationsspringboot.repository.UserRepository;
import be.iccbxl.pid.reservationsspringboot.service.ReservationService;
import be.iccbxl.pid.reservationsspringboot.service.StripeService;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@SessionAttributes("cart")
@RequestMapping("/cart") // ✅ Regroupe toutes les routes sous /cart
public class CartController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private RepresentationRepository representationRepo;

    @Autowired
    private PriceRepository priceRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationService reservationService;

    @ModelAttribute("cart")
    public Cart cart() {
        return new Cart();
    }

    // ✅ Affichage panier
    @GetMapping("/view")
    public String viewCart(Model model, @ModelAttribute("cart") Cart cart) {
        model.addAttribute("items", cart.getItems());
        model.addAttribute("total", cart.getTotal());
        return "cart/view";
    }

    // ✅ Ajout item dans panier
    @PostMapping("/add")
    public String addToCart(@RequestParam Long representationId,
                            @RequestParam Long priceId,
                            @RequestParam(defaultValue = "1") int quantity,
                            @ModelAttribute("cart") Cart cart) {

        Representation representation = representationRepo.findById(representationId).orElseThrow();
        Price price = priceRepo.findById(priceId).orElseThrow();

        CartItem item = new CartItem();
        item.setRepresentationId(representationId);
        item.setPriceId(priceId);
        item.setQuantity(quantity);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        item.setLabel(representation.getScheduledAt().format(formatter));
        item.setUnitPrice(price.getPrice());

        cart.addItem(item);
        return "redirect:/cart/view";
    }

    // ✅ Suppression d’un item
    @PostMapping("/delete/{index}")
    public String deleteItem(@PathVariable int index, @ModelAttribute("cart") Cart cart) {
        cart.removeItem(index);
        return "redirect:/cart/view";
    }

    // ✅ Checkout Stripe
    @PostMapping("/checkout")
    public String checkout(@ModelAttribute("cart") Cart cart, Principal principal, HttpSession session) {
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart/view?empty=true";
        }

        User user = userRepository.findByLogin(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }

        // Création réservation
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBookingDate(LocalDateTime.now());
        reservation.setStatus("PENDING");

        for (CartItem item : cart.getItems()) {
            Representation rep = representationRepo.findById(item.getRepresentationId()).orElse(null);
            Price price = priceRepo.findById(item.getPriceId()).orElse(null);

            if (rep != null && price != null) {
                RepresentationReservation rr = new RepresentationReservation();
                rr.setRepresentation(rep);
                rr.setPrice(price);
                rr.setQuantity(item.getQuantity());
                reservation.addItem(rr);
            }
        }

        reservationService.save(reservation);
        session.setAttribute("lastReservationId", reservation.getId());

        // Stripe
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            lineItems.add(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) item.getQuantity())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("eur")
                                            .setUnitAmount((long) (item.getUnitPrice() * 100))
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(item.getLabel())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }

        try {
            Session sessionStripe = stripeService.createCheckoutSession(lineItems);
            return "redirect:" + sessionStripe.getUrl();
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/cart/view?error=stripe";
        }
    }

    // ✅ Confirmation de réservation
    @GetMapping("/confirmation")
    public String confirmation(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("lastReservationId");
        if (id == null) {
            return "redirect:/shows";
        }

        Reservation reservation = reservationService.getReservation(id);
        model.addAttribute("reservation", reservation);
        return "reservation/confirmation";
    }
}
