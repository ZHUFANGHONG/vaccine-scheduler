package scheduler.model;
import scheduler.db.ConnectionManager;
//import scheduler.model.Caregiver;

import java.sql.*;

public class Appointment {
    private String vaccineName;
    private int appointmentId;
    private String patientName;
    private String caregiverName;
    private int availableDoses;
    private Date date;

    public Appointment(int appointmentId, Date date, String patientName, String caregiverName, String vaccineName) {
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.caregiverName = caregiverName;
        this.vaccineName = vaccineName;
        this.date = date;
    }

    // Getters
    public String getVaccineName() {
        return vaccineName;
    }

    public void updateAvailableDoses() {
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            System.out.println("There is no this vaccine");
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.decreaseAvailableDoses(1);
            } catch (SQLException e) {
                System.out.println("Error occurred when decreasing doses");
                e.printStackTrace();
            }
        }
    }

    public void DeleteCaregiverAvailability() throws SQLException {
//        Caregiver caregiver = null;
//        Vaccine vaccine = null;
//        try {
//            caregiver = new Caregiver.CaregiverGetter(caregiverName, "gawk").get();
//        } catch (SQLException e) {
//            System.out.println("Error occurred when getting caregiver availability");
//            e.printStackTrace();
//        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table

//        if (caregiver == null) {
//            System.out.println("There is no corresponding caregiver");
//        } else {
        // if the vaccine is not null, meaning that the vaccine already exists in our table
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String cancelCaregiverDate = "DELETE FROM Availabilities AS A WHERE A.Time = ? AND A.Username = ? ";
        try {
            PreparedStatement statement = con.prepareStatement(cancelCaregiverDate);
            statement.setDate(1, date);
            statement.setString(2, caregiverName);
            ResultSet resultSet = statement.executeQuery();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
//    }
    }

    public void saveToDB() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String setAppointments = "INSERT INTO Appointment VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = con.prepareStatement(setAppointments);
            statement.setInt(1, appointmentId);
            statement.setDate(2, date);
            statement.setString(3, caregiverName);
            statement.setString(4, patientName);
            statement.setString(5, vaccineName);
            statement.executeUpdate();
            this.updateAvailableDoses();
            //this.DeleteCaregiverAvailability();
        } catch (SQLException e) {
            System.out.println("Error occurred when saving a appointment");
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
//        this.updateAvailableDoses();
//        this.DeleteCaregiverAvailability();
    }

    // Increment the available doses
//    public void increaseAvailableDoses(int num) throws SQLException {
//        if (num <= 0) {
//            throw new IllegalArgumentException("Argument cannot be negative!");
//        }
//        this.availableDoses += num;
//
//        ConnectionManager cm = new ConnectionManager();
//        Connection con = cm.createConnection();
//
//        String removeAvailability  = "UPDATE vaccines SET Doses = ? WHERE Name = ?;";
//        try {
//            PreparedStatement statement = con.prepareStatement(removeAvailability);
//            statement.setInt(1, this.availableDoses);
//            statement.setString(2, this.vaccineName);
//            statement.executeUpdate();
//        } catch (SQLException e) {
//            throw new SQLException();
//        } finally {
//            cm.closeConnection();
//        }
//    }

}
