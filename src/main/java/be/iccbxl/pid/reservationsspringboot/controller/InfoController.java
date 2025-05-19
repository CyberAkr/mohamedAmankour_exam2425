package be.iccbxl.pid.reservationsspringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InfoController {
    @GetMapping("/infos-pratiques")
    public String infosPratiques() {
        return "info"; // affiche templates/info.html
    }
}
