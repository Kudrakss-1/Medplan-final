package medplan.controller;

import medplan.entity.Specialty;
import medplan.repository.SpecialtyRepository;
import medplan.service.SpecialtyService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/specialties")
public class SpecialtyController {

private final SpecialtyService specialtyService;
public SpecialtyController(SpecialtyService specialtyService) {
    this.specialtyService = specialtyService;
}
    @GetMapping
public List<Specialty> getAllSpecialties() {
    return specialtyService.getAllSpecialties();
}

@PostMapping
public Specialty createSpecialty(@RequestBody Specialty specialty) {
    return specialtyService.createSpecialty(specialty);
}
}