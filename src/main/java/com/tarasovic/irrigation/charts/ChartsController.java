package com.tarasovic.irrigation.charts;

import com.tarasovic.irrigation.measurement.DeviceStatusChecker;
import com.tarasovic.irrigation.measurement.MeasurementService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/charts")
@AllArgsConstructor
public class ChartsController {


    private MeasurementService measurementService;
    private DeviceStatusChecker deviceStatusChecker;


    @GetMapping
    public String index(Model model) {
        model.addAttribute("basilData", measurementService.findFirst300OrderByTimeAtDesc("basil"));
        model.addAttribute("basilLatestValue", measurementService.getLatestMoisture("basil"));
        model.addAttribute("parsleyChartData", measurementService.findFirst300OrderByTimeAtDesc("parsley"));
        model.addAttribute("parsleyLatestValue", measurementService.getLatestMoisture("parsley"));
        model.addAttribute("emptyChartData", measurementService.findFirst300OrderByTimeAtDesc("empty"));
        model.addAttribute("emptyLatestValue", measurementService.getLatestMoisture("empty"));
        model.addAttribute("waterPumpOff", deviceStatusChecker.isWatterPumpOffline());
        model.addAttribute("deviceOff", deviceStatusChecker.isDeviceOffline());
        return "charts.html";
    }
}
