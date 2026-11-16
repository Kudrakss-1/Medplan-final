package medplan.controller;

import medplan.entity.Appointment;
import medplan.entity.Doctor;
import medplan.entity.User;
import medplan.repository.AppointmentRepository;
import medplan.repository.DoctorRepository;
import medplan.repository.UserRepository;
import medplan.service.AppointmentService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AppointmentService appointmentService;

    public AppointmentController(
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            AppointmentService appointmentService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.appointmentService = appointmentService;
    }

    public record CreateAppointmentRequest(
        Long patientId,
        Long doctorId,
        LocalDateTime appointmentDate
    ) {}

    public record AppointmentResponse(
        Long id,
        Long patientId,
        Long doctorId,
        LocalDateTime appointmentDate,
        String status
    ) {}

    @PostMapping
public AppointmentResponse createAppointment(
        @RequestBody CreateAppointmentRequest request) {
    return toResponse(appointmentService.createAppointment(
        request.patientId(),
        request.doctorId(),
        request.appointmentDate()
    ));
}

    @GetMapping("/patient/{patientId}")
    public List<AppointmentResponse> getPatientAppointments(
        @PathVariable Long patientId) {
    return appointmentService.getPatientAppointments(patientId)
            .stream()
            .map(this::toResponse)
            .toList();
}

    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancelAppointment(
        @PathVariable Long id) {
    return toResponse(appointmentService.cancelAppointment(id));
}
    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
            appointment.getId(),
            appointment.getPatient().getId(),
            appointment.getDoctor().getId(),
            appointment.getAppointmentDate(),
            appointment.getStatus().name()
        );
    }
}