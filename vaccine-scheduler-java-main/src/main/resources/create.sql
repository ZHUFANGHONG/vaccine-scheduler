CREATE TABLE Caregivers (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Availabilities (
    Time date,
    Username varchar(255) REFERENCES Caregivers,
    PRIMARY KEY (Time, Username)
);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);

CREATE TABLE Patients (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

--CREATE TABLE PatientAvailabilities (
--    Time date,
--    Username varchar(255) REFERENCES Patients,
--    PRIMARY KEY (Time, Username)
--);

CREATE TABLE Appointment (
    appointmentID int,
    Time date,
    caregiverName varchar(255) REFERENCES Caregivers(Username),
    patientName varchar(255) REFERENCES Patients(Username),
    vaccineName varchar(255) REFERENCES Vaccines(Name),
    PRIMARY KEY (appointmentID)
);