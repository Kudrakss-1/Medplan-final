package medplan.controller;

import medplan.entity.Doctor;
import medplan.entity.User;
import medplan.entity.Specialty;
import medplan.repository.DoctorRepository;
import medplan.repository.UserRepository;
import medplan.repository.SpecialtyRepository;
import medplan.service.DoctorService;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;

    public DoctorController(
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            SpecialtyRepository specialtyRepository, DoctorService doctorService) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.specialtyRepository = specialtyRepository;
        this.doctorService = doctorService;
    }

    public record DoctorRequest(
            Long userId,
            Long specialtyId,
            String qualification,
            Integer experienceYears,
            String description
    ) {}

    @GetMapping
public List<Doctor> getAllDoctors() {
    return doctorService.getAllDoctors();
}

    @GetMapping("/{id}")
    public Doctor getDoctorById(@PathVariable Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Doctor not found"));
    }

    @PostMapping
    public Doctor createDoctor(@RequestBody DoctorRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() ->
                    new RuntimeException("User not found"));

        Specialty specialty = specialtyRepository.findById(request.specialtyId())
                .orElseThrow(() ->
                    new RuntimeException("Specialty not found"));

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialty(specialty);
        doctor.setQualification(request.qualification());
        doctor.setExperienceYears(request.experienceYears());
        doctor.setDescription(request.description());

        user.setRole(User.Role.DOCTOR);
        userRepository.save(user);

        return doctorRepository.save(doctor);
    }
}


