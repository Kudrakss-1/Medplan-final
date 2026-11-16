async function loadDoctors() {
    const doctorsList = document.getElementById("doctors-list");

    try {
        const response = await fetch("/api/doctors");

        if (!response.ok) {
            throw new Error("Не удалось загрузить врачей");
        }

        const doctors = await response.json();

        doctorsList.innerHTML = "";

        if (doctors.length === 0) {
            doctorsList.innerHTML = "<p>Пока врачей нет.</p>";
            return;
        }

        doctors.forEach(doctor => {
            const name = doctor.user?.fullName || doctor.fullName || "Имя не указано";
            const specialty = doctor.specialty?.name || "Специальность не указана";

            doctorsList.innerHTML += `
                <div class="card">
                    <h3>${name}</h3>
                    <p>${specialty}</p>
                    <p>${doctor.qualification || ""}</p>
                    <p>Опыт работы: ${doctor.experienceYears ?? 0} лет</p>
                </div>
            `;
        });

    } catch (error) {
        doctorsList.innerHTML = "<p>Не удалось загрузить список врачей.</p>";
        console.error(error);
    }
}

document.addEventListener("DOMContentLoaded", loadDoctors);