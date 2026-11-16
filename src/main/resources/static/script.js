function openRegister() {
    document.getElementById("register-modal").style.display = "flex";
}

function closeRegister() {
    document.getElementById("register-modal").style.display = "none";
}

function openLogin() {
    document.getElementById("login-modal").style.display = "flex";
}

function closeLogin() {
    document.getElementById("login-modal").style.display = "none";
}


document.getElementById("register-form").addEventListener("submit", async function(event) {
    event.preventDefault();

    const message = document.getElementById("register-message");
    const fullName = document.getElementById("reg-name").value;
    const email = document.getElementById("reg-email").value;
    const password = document.getElementById("reg-password").value;

    message.textContent = "Регистрация...";

    try {
        const response = await fetch("/api/auth/register", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({fullName, email, password})
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || result.detail || "Ошибка регистрации");
        }

        message.style.color = "green";
        message.textContent = "Регистрация успешна! Добро пожаловать, " + result.fullName + "!";
        document.getElementById("register-form").reset();

    } catch (error) {
        message.style.color = "red";
        message.textContent = error.message;
    }
});


document.getElementById("login-form").addEventListener("submit", async function(event) {
    event.preventDefault();

    const message = document.getElementById("login-message");
    const email = document.getElementById("login-email").value;
    const password = document.getElementById("login-password").value;

    message.textContent = "Выполняется вход...";

    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({email, password})
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || result.detail || "Неверный логин или пароль");
        }

        if (result.token) {
            sessionStorage.setItem("medplan-token", result.token);
        }

        sessionStorage.setItem("medplan-user-id", result.id);

        message.style.color = "green";
        message.textContent = "Вы успешно вошли! Ваш ID: " + result.id;
        document.getElementById("login-form").reset();

    } catch (error) {
        message.style.color = "red";
        message.textContent = error.message;
    }
});


async function loadDoctors() {
    const doctorsList = document.getElementById("doctors-list");

    try {
        const response = await fetch("/api/doctors");
        if (!response.ok) throw new Error("Ошибка загрузки врачей");

        const doctors = await response.json();
        doctorsList.innerHTML = "";

        if (doctors.length === 0) {
            doctorsList.textContent = "Пока врачей нет.";
            return;
        }

        doctors.forEach(doctor => {
            const card = document.createElement("div");
            card.className = "card";

            const name = document.createElement("h3");
            name.textContent = doctor.user?.fullName || "Имя не указано";

            const specialty = document.createElement("p");
            specialty.textContent = doctor.specialty?.name || "Специальность не указана";

            const experience = document.createElement("p");
            experience.textContent = "Опыт работы: " + (doctor.experienceYears ?? 0) + " лет";

            card.append(name, specialty, experience);
            doctorsList.appendChild(card);
        });

    } catch (error) {
        doctorsList.textContent = "Не удалось загрузить список врачей.";
    }
}

async function loadBookingDoctors() {


const select = document.getElementById("booking-doctor");

    try {
        const response = await fetch("/api/doctors");
        if (!response.ok) throw new Error("Ошибка загрузки врачей");

        const doctors = await response.json();

        select.innerHTML = '<option value="">Выберите врача</option>';

        doctors.forEach(doctor => {
            const option = document.createElement("option");
            option.value = doctor.id;
            option.textContent =
                (doctor.user?.fullName || "Врач") + " — " +
                (doctor.specialty?.name || "Специалист");
            select.appendChild(option);
        });

        if (doctors.length === 0) {
            select.innerHTML = '<option value="">Врачей пока нет</option>';
        }

    } catch (error) {
        select.innerHTML = '<option value="">Не удалось загрузить врачей</option>';
    }
}


async function loadBookingTimes() {
    const doctorId = document.getElementById("booking-doctor").value;
    const date = document.getElementById("booking-date").value;
    const timeSelect = document.getElementById("booking-time");

    timeSelect.innerHTML = '<option value="">Выберите время</option>';

    if (!doctorId || !date) return;

    try {
        const response = await fetch("/api/schedules/doctor/" + doctorId);
        if (!response.ok) throw new Error("Ошибка расписания");

        const schedules = await response.json();
        const selectedDay = new Date(date + "T12:00:00").getDay();

        const dayMap = {
            0: "SUNDAY",
            1: "MONDAY",
            2: "TUESDAY",
            3: "WEDNESDAY",
            4: "THURSDAY",
            5: "FRIDAY",
            6: "SATURDAY"
        };

        const schedule = schedules.find(item => item.dayOfWeek === dayMap[selectedDay]);

        if (!schedule) {
            timeSelect.innerHTML = '<option value="">В этот день приёма нет</option>';
            return;
        }

        const [startHour, startMinute] = schedule.startTime.split(":").map(Number);
        const [endHour, endMinute] = schedule.endTime.split(":").map(Number);

        let current = startHour * 60 + startMinute;
        const end = endHour * 60 + endMinute;

        while (current + 30 <= end) {
            const hour = String(Math.floor(current / 60)).padStart(2, "0");
            const minute = String(current % 60).padStart(2, "0");
            const time = hour + ":" + minute;

            const option = document.createElement("option");
            option.value = time;
            option.textContent = time;
            timeSelect.appendChild(option);

            current += 30;
        }

    } catch (error) {
        timeSelect.innerHTML = '<option value="">Не удалось загрузить время</option>';
    }
}


document.getElementById("booking-doctor").addEventListener("change", loadBookingTimes);
document.getElementById("booking-date").addEventListener("change", loadBookingTimes);


document.getElementById("appointment-form").addEventListener("submit", async function(event) {
    event.preventDefault();

    const message = document.getElementById("appointment-message");
    const doctorId = Number(document.getElementById("booking-doctor").value);
    const date = document.getElementById("booking-date").value;
    const time = document.getElementById("booking-time").value;
    const patientId = Number(sessionStorage.getItem("medplan-user-id"));

    if (!patientId) {
        message.style.color = "red";
        message.textContent = "Сначала войдите в аккаунт.";
        return;
    }

    if (!doctorId || !date || !time) {
        message.style.color = "red";
        message.textContent = "Выберите врача, дату и время.";
        return;
    }

    try {
        const response = await fetch("/api/appointments", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                patientId: patientId,
                doctorId: doctorId,
                appointmentDate: date + "T" + time + ":00"
            })
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || result.detail || "Ошибка записи");
        }

        message.style.color = "green";
        message.textContent = "Вы успешно записаны! Номер записи: " + result.id;
        loadMyAppointments();

        document.getElementById("appointment-form").reset();
        document.getElementById("booking-time").innerHTML =
            '<option value="">Выберите время</option>';

    } catch (error) {
        message.style.color = "red";
        message.textContent = error.message;
    }
});


document.addEventListener("DOMContentLoaded", function() {
    loadDoctors();
    loadBookingDoctors();
    loadMyAppointments();
});


async function loadMyAppointments() {
    const container = document.getElementById("appointments-list");
    if (!container) return;

    const patientId = sessionStorage.getItem("medplan-user-id");

    if (!patientId) {
        container.textContent = "Войдите в аккаунт, чтобы увидеть свои записи.";
        return;
    }

    try {
        const response = await fetch("/api/appointments/patient/" + patientId);
        if (!response.ok) throw new Error("Не удалось загрузить записи");

        const appointments = await response.json();
        container.innerHTML = "";

        if (!appointments.length) {
            container.textContent = "У вас пока нет записей.";
            return;
        }

        appointments.forEach(appointment => {
            const card = document.createElement("div");
            card.className = "card";
            card.style.margin = "15px 0";
            card.style.padding = "20px";

            const doctorName = appointment.doctor?.user?.fullName || "Врач";
            const date = appointment.appointmentDate
                ? appointment.appointmentDate.replace("T", " ")
                : "Дата не указана";

            const info = document.createElement("p");
            info.textContent = "Врач: " + doctorName + " | Дата: " + date +
                " | Статус: " + appointment.status;
            card.appendChild(info);

            if (appointment.status !== "CANCELLED" &&
                appointment.status !== "COMPLETED") {
                const button = document.createElement("button");
                button.textContent = "Отменить запись";
                button.addEventListener("click", async () => {
                    if (!confirm("Вы действительно хотите отменить запись?")) return;

                    try {
                        const cancelResponse = await fetch(
                            "/api/appointments/" + appointment.id + "/cancel",
                            { method: "PATCH" }
                        );

                        if (!cancelResponse.ok) {
                            throw new Error("Не удалось отменить запись");
                        }

                        await loadMyAppointments();
                    } catch (error) {
                        alert(error.message);
                    }
                });
                card.appendChild(button);
            }

            container.appendChild(card);
        });
    } catch (error) {
        container.textContent = error.message;
    }
}

