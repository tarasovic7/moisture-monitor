package com.tarasovic.irrigation.battery;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@AllArgsConstructor
public class BatteryLifeController {

    private final BatteryLifeService batteryLifeService;

    @GetMapping("/battery")
    public String displayBatteryLifeTable(Model model) {
        List<BatteryLife> batteryLifeList = batteryLifeService.findBatteryLives();
        model.addAttribute("batteryLifeList", batteryLifeList);
        return "battery.html";
    }
}
