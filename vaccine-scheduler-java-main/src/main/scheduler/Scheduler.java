package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Appointment;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
//import java.util.Arrays;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;
    private static int appointmentId = 0;
    //private static String usernameLogin;


    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }
    /*******888*****8*888888*/
    private static void createPatient(String[] tokens) {
        // TODO: Part 1
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsPatient(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            currentPatient = new Patient.PatientBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            currentPatient.saveToDB();
            System.out.println(" *** Account created successfully *** ");
        } catch (SQLException e) {
            System.out.println("Create failed");
            e.printStackTrace();
        }
    }
    /*******888*****8*888888*/
    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            currentCaregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            currentCaregiver.saveToDB();
            System.out.println(" *** Account created successfully *** ");
        } catch (SQLException e) {
            System.out.println("Create failed");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }
    /*******888*****8*888888*/
    private static void loginPatient(String[] tokens) {
        // TODO: Part 1
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("Already logged-in!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when logging in");
            e.printStackTrace();
        }
        // check if the login was successful
        if (patient == null) {
            System.out.println("Please try again!");
        } else {
            System.out.println("Patient logged in as: " + username);
            currentPatient = patient;

        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("Already logged-in!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when logging in");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Please try again!");
        } else {
            System.out.println("Caregiver logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        // TODO: Part 2
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please logged-in!");
            return;
        }
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String str1 = "";
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String searchCaregiver = "SELECT Username FROM Availabilities WHERE Time = ?";
        String searchVaccine = "SELECT Name, Doses FROM Vaccines";
//        try {
//            PreparedStatement statement1 = con.prepareStatement(searchVaccine);
//            ResultSet resultSet1 = statement1.executeQuery();
//
//            PreparedStatement statement = con.prepareStatement(searchCaregiver);
//            statement.setDate(1, Date.valueOf(tokens[1]));
//            ResultSet resultSet = statement.executeQuery();
//            //String str1 = "";
//            while (resultSet1.next()) {
//                String vaccineName = resultSet1.getString("Name");
//                int dose = resultSet1.getInt("Doses");
//                str1 = str1+" "+vaccineName + ":" + dose;
//            }
//            String str2 = "";
//            while (resultSet.next()) {
//                String caregiverName = resultSet.getString("Username");
//                System.out.println(caregiverName+str1);
//            }
//        } catch (SQLException e) {
//            System.out.println("Error occurred when searching Caregiver Schedule");
//            e.printStackTrace();
//        }

        try {
            PreparedStatement statement1 = con.prepareStatement(searchVaccine);
            ResultSet resultSet1 = statement1.executeQuery();

            //String str1 = "";
            while (resultSet1.next()) {
                String vaccineName = resultSet1.getString("Name");
                int doses = resultSet1.getInt("Doses");
                str1 = str1+" "+vaccineName + ":" + doses;
            }
        } catch (SQLException e) {
            System.out.println("Error occurred when searching Caregiver Schedule");
            e.printStackTrace();
        }

        try {
            PreparedStatement statement = con.prepareStatement(searchCaregiver);
            statement.setDate(1, Date.valueOf(tokens[1]));
            ResultSet resultSet = statement.executeQuery();
            //String str1 = "";
            String str2 = "";
            while (resultSet.next()) {
                String caregiverName = resultSet.getString("Username");
                System.out.println(caregiverName+str1);
            }
        } catch (SQLException e) {
            System.out.println("Error occurred when searching Caregiver Schedule");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
    }

    private static void reserve(String[] tokens){
        // TODO: Part 2
        if (currentPatient == null) {
            System.out.println("Please logged-in! Only patients can log in reserve section");
            return;
        }
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String get_patient_appointment_num = "SELECT Count(*) FROM Appointment as A WHERE A.Time = ? AND A.patientName = ?";
        try {
            PreparedStatement statement = con.prepareStatement(get_patient_appointment_num);
            statement.setDate(1, Date.valueOf(tokens[1]));
            statement.setString(2, tokens[2]);
            ResultSet resultSet = statement.executeQuery();
            int num = 0;
            while (resultSet.next()) {
                num = resultSet.getInt(1);
            }
            if(num != 0){
                System.out.println("You already have made an appointment on" + tokens[1]);
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error occurred when making an appointment");
            e.printStackTrace();
        }

        String caregiverAvailable = "SELECT Count(*) FROM Availabilities as A WHERE A.Time = ?";
        try {
            PreparedStatement statement = con.prepareStatement(caregiverAvailable);
            statement.setDate(1, Date.valueOf(tokens[1]));
            ResultSet resultSet = statement.executeQuery();
            int num = 0;
            while (resultSet.next()) {
                num = resultSet.getInt(1);
            }
            if(num == 0){
                System.out.println("There is no caregivers in the date");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String caregiverName = "";
        String caregiverReserve = "SELECT Username FROM Availabilities WHERE Time = ? ";
        try {
            PreparedStatement statement = con.prepareStatement(caregiverReserve);
            statement.setDate(1, Date.valueOf(tokens[1]));
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                caregiverName = resultSet.getString("Username");
                //System.out.println(caregiverName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String appointmentCount = "SELECT Count(*) FROM Appointment";
        try {
            PreparedStatement statement = con.prepareStatement(appointmentCount);
            //statement.setDate(1, Date.valueOf(tokens[1]));
            ResultSet resultSet = statement.executeQuery();
            int num = 0;
            while (resultSet.next()) {
                num = resultSet.getInt(1);
                appointmentId = num+1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            cm.closeConnection();
        }
        //appointmentId++;
        Appointment appointment = new Appointment(appointmentId, Date.valueOf(tokens[1]), currentPatient.getUsername(), caregiverName, tokens[2]);

        try {
            appointment.saveToDB();
            System.out.println("Appointment save successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit
        if(currentCaregiver == null && currentPatient == null){
            System.out.println("Please login first!");
            return;
        }
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        int appointmentID = Integer.parseInt(tokens[1]);
        String caregiverName = "";
        String vaccineName = "";
       // String patientNameK = "";
        Date date = null;

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String caregiverAppointment = "SELECT Time, patientName, vaccineName, caregiverName FROM Appointment WHERE appointmentID = ? ";
        try {
            PreparedStatement statement = con.prepareStatement(caregiverAppointment);
            statement.setInt(1, appointmentID);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                caregiverName = resultSet.getString("caregiverName");
                vaccineName = resultSet.getString("vaccineName");
                //patientNameK = resultSet.getString("patientName");
                date = resultSet.getDate("Time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String cancelAppointment = "DELETE FROM Appointment As A WHERE A.appointmentID = ?";
        String updateCaregiverAvailability = "INSERT INTO Availabilities VALUES (? , ?)";
        try {
            PreparedStatement statement = con.prepareStatement(cancelAppointment);
            statement.setInt(1, appointmentID);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("Delete successfully!");

            PreparedStatement statement1 = con.prepareStatement(updateCaregiverAvailability);
            statement1.setDate(1, date);
            statement1.setString(2, caregiverName);
            statement1.executeUpdate();
            Vaccine vaccine = new Vaccine.VaccineGetter(vaccineName).get();
            vaccine.increaseAvailableDoses(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }

    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
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
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        // TODO: Part 2
        if (currentCaregiver != null) {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();
            String caregiverAppointment = "SELECT appointmentID, Time, patientName, vaccineName FROM Appointment As A WHERE A.caregiverName = ?";
            try {
                PreparedStatement statement = con.prepareStatement(caregiverAppointment);
                statement.setString(1, currentCaregiver.getUsername());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    int appointmentID = resultSet.getInt("appointmentID");
                    String vaccineName = resultSet.getString("vaccineName");
                    String patientName = resultSet.getString("patientName");
                    Date date = resultSet.getDate("Time");
                    System.out.println(appointmentID +" - "+ vaccineName +" - "+ patientName +" - "+ date);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cm.closeConnection();
            }
        }

        if (currentPatient != null) {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();
            String patientAppointment = "SELECT appointmentID, Time, caregiverName, vaccineName FROM Appointment As A WHERE A.patientName = ?";
            try {
                PreparedStatement statement = con.prepareStatement(patientAppointment);
                statement.setString(1, currentPatient.getUsername());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    int appointmentID = resultSet.getInt("appointmentID");
                    String vaccineName = resultSet.getString("vaccineName");
                    String caregiverName = resultSet.getString("caregiverName");
                    Date date = resultSet.getDate("Time");
                    System.out.println(appointmentID +" - "+ vaccineName +" - "+ caregiverName +" - "+ date);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cm.closeConnection();
            }
        }
    }

    private static void logout(String[] tokens) {
        // TODO: Part 2
        if(tokens[0].equals("logout")) {
            currentPatient = null;
            currentCaregiver = null;
        }
    }
}
