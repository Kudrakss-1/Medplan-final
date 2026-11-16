package medplan;

import medplan.entity.User;
import medplan.entity.Doctor;
import medplan.entity.Specialty;
import medplan.entity.Schedule;
import medplan.repository.UserRepository;
import medplan.repository.DoctorRepository;
import medplan.repository.SpecialtyRepository;
import medplan.repository.ScheduleRepository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@SpringBootApplication
public class MedplanApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedplanApplication.class, args);
    }

    @Bean
    CommandLineRunner createTestDoctorAndSchedule(
            UserRepository userRepository,
            DoctorRepository doctorRepository,
            SpecialtyRepository specialtyRepository,
            ScheduleRepository scheduleRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String email = "ivan.petrov.medplan@example.com";

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setFullName("Иван Петров");
                        newUser.setEmail(email);
                        newUser.setPassword(
                                passwordEncoder.encode("MedplanDoctor2026!")
                        );
                        newUser.setRole(User.Role.DOCTOR);
                        return userRepository.save(newUser);
                    });

            user.setRole(User.Role.DOCTOR);
            userRepository.save(user);

            Specialty specialty = specialtyRepository.findById(2L)
                    .orElseThrow(() ->
                            new RuntimeException("Специальность ID 2 не найдена"));

            Doctor doctor = doctorRepository.findAll().stream()
                    .filter(d -> d.getUser().getEmail().equals(email))
                    .findFirst()
                    .orElseGet(() -> {
                        Doctor newDoctor = new Doctor();
                        newDoctor.setUser(user);
                        newDoctor.setSpecialty(specialty);
                        newDoctor.setQualification("Врач-терапевт");
                        newDoctor.setExperienceYears(5);
                        newDoctor.setDescription(
                                "Опытный специалист по терапии."
                        );
                        return doctorRepository.save(newDoctor);
                    });

            if (scheduleRepository.findByDoctorId(doctor.getId()).isEmpty()) {
                List<DayOfWeek> workDays = List.of(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                );

                for (DayOfWeek day : workDays) {
                    Schedule schedule = new Schedule();
                    schedule.setDoctor(doctor);
                    schedule.setDayOfWeek(day);
                    schedule.setStartTime(LocalTime.of(9, 0));
                    schedule.setEndTime(LocalTime.of(17, 0));

                    scheduleRepository.save(schedule);
                }
            }

            System.out.println("Врач и расписание готовы!");
        };
    }
}



