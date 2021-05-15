package blockchain.medicalRecords.HeathCareData.util;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MiscUtil {

    public ArrayList<ArrayList<String>> getMenu(String user_type) {
        ArrayList<ArrayList<String>> haha = new ArrayList<ArrayList<String>>();
        if(user_type.equals("doctor")) {

        } else {
            haha.add(new ArrayList<String>(Arrays.asList("View Profile", "/userProfile")));
            haha.add(new ArrayList<String>(Arrays.asList("View Data", "/viewPatientData")));
            haha.add(new ArrayList<String>(Arrays.asList("Book Doctor", "/bookDoctor")));
            haha.add(new ArrayList<String>(Arrays.asList("Give / Revoke Doctor Permission", "/doctorPermissions")));
            haha.add(new ArrayList<String>(Arrays.asList("View Appointments", "/viewAppointments")));

        }

        return haha;

    }

}
