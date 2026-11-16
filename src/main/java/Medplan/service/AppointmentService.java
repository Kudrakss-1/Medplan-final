package medplan.service;

import medplan.entity.Appointment;
import medplan.entity.Doctor;
import medplan.entity.User;
import medplan.repository.AppointmentRepository;
import medplan.repository.DoctorRepository;
import medplan.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository,
            UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    public Appointment createAppointment(
            Long patientId, Long doctorId, LocalDateTime date) {

        User patient = userRepository.findById(patientId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Patient not found"));

        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Doctor not found"));

        if (date == null || !date.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Invalid appointment date");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(date);
        appointment.setStatus(Appointment.Status.PENDING);

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getPatientAppointments(Long patientId) {
        return appointmentRepository
            .findByPatient_IdOrderByAppointmentDateDesc(patientId);
    }

    public Appointment cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appointment.getStatus() == Appointment.Status.COMPLETED) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Appointment already completed");
        }

        appointment.setStatus(Appointment.Status.CANCELLED);

        return appointmentRepository.save(appointment);
    }
}