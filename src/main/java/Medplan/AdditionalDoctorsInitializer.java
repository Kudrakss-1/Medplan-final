package medplan;

import medplan.entity.User;
import medplan.entity.Doctor;
import medplan.entity.Specialty;
import medplan.entity.Schedule;

import medplan.repository.UserRepository;
import medplan.repository.DoctorRepository;
import medplan.repository.SpecialtyRepository;
import medplan.repository.ScheduleRepository;

import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Component
public class AdditionalDoctorsInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final ScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdditionalDoctorsInitializer(
            UserRepository userRepository,
            DoctorRepository doctorRepository,
            SpecialtyRepository specialtyRepository,
            ScheduleRepository scheduleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
        this.scheduleRepository = scheduleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        createDoctor("Анна Смирнова",
                "anna.cardio@example.com",
                "Кардиология",
                "Врач-кардиолог", 8);

        createDoctor("Мария Соколова",
                "maria.neuro@example.com",
                "Неврология",
                "Врач-невролог", 6);

        createDoctor("Дмитрий Орлов",
                "dmitry.derma@example.com",
                "Дерматология",
                "Врач-дерматолог", 10);

        System.out.println("Дополнительные врачи созданы!");
    }

    private void createDoctor(
            String name,
            String email,
            String specialtyName,
            String qualification,
            int experience) {

        Specialty specialty = specialtyRepository.findAll()
                .stream()
                .filter(s -> specialtyName.equals(s.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Specialty newSpecialty = new Specialty();
                    newSpecialty.setName(specialtyName);
                    newSpecialty.setDescription(
                            "Консультации и лечение по направлению " + specialtyName
                    );
                    return specialtyRepository.save(newSpecialty);
                });

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setFullName(name);
                    newUser.setEmail(email);
                    newUser.setPassword(
                            passwordEncoder.encode("MedplanDoctor2026!")
                    );
                    newUser.setRole(User.Role.DOCTOR);
                    return userRepository.save(newUser);
                });

        user.setRole(User.Role.DOCTOR);
        userRepository.save(user);

        Doctor doctor = doctorRepository.findAll()
                .stream()
                .filter(d -> d.getUser().getEmail().equals(email))
                .findFirst()
                .orElseGet(() -> {
                    Doctor newDoctor = new Doctor();
                    newDoctor.setUser(user);
                    newDoctor.setSpecialty(specialty);
                    newDoctor.setQualification(qualification);
                    newDoctor.setExperienceYears(experience);
                    newDoctor.setDescription(
                            "Опытный специалист медицинского центра МедПлан."
                    );
                    return doctorRepository.save(newDoctor);
                });


if (scheduleRepository.findByDoctorId(doctor.getId()).isEmpty()) {
            for (DayOfWeek day : List.of(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY)) {

                Schedule schedule = new Schedule();
                schedule.setDoctor(doctor);
                schedule.setDayOfWeek(day);
                schedule.setStartTime(LocalTime.of(9, 0));
                schedule.setEndTime(LocalTime.of(17, 0));

                scheduleRepository.save(schedule);
            }
        }
    }
};


