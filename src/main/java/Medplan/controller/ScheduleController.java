package medplan.controller;

import medplan.entity.Schedule;
import medplan.repository.ScheduleRepository;
import medplan.service.ScheduleService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

private final ScheduleService scheduleService;
public ScheduleController(ScheduleService scheduleService) {
    this.scheduleService = scheduleService;
}

@GetMapping("/doctor/{doctorId}")
public List<Schedule> getDoctorSchedule(@PathVariable Long doctorId) {
    return scheduleService.getDoctorSchedule(doctorId);
}

@PostMapping
public Schedule createSchedule(@RequestBody Schedule schedule) {
    return scheduleService.createSchedule(schedule);
}
}