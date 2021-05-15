package blockchain.medicalRecords.HeathCareData.controller;


import blockchain.medicalRecords.HeathCareData.model.*;
import blockchain.medicalRecords.HeathCareData.services.MyUserDetailsService;
import blockchain.medicalRecords.HeathCareData.util.DbUtil;
import blockchain.medicalRecords.HeathCareData.util.JwtUtil;
import blockchain.medicalRecords.HeathCareData.util.MiscUtil;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.json.*;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;


@Controller
public class RestController {

    @Autowired
    private AuthenticationManager authManager;


    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private DbUtil dbUtil;

    @Autowired
    private MiscUtil miscUtil;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/login")
    public String login(Model theModel) {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerNew(HttpServletRequest req, Model theModel) {
        System.out.println(req.getParameter("first_name"));
        System.out.println(req.getParameter("last_name"));
        System.out.println(req.getParameter("user_name"));
        System.out.println(req.getParameter("email_id"));
        System.out.println(req.getParameter("user_type"));
        System.out.println(req.getParameter("password"));

        User_details curr = new User_details(0
                , req.getParameter("first_name")
                , req.getParameter("last_name")
                , req.getParameter("user_name")
                , req.getParameter("email_id")
                , req.getParameter("user_type")
                , req.getParameter("password"));


        if(dbUtil.registerNewUser(curr) == 1) {
            return "login";
        } else {
            theModel.addAttribute("isError", "Error creating new user. User Name already exists");
            return "register";
        }


    }

    @GetMapping("/publicDNSEntry")
    public String publicDNSEntry() { return "publicDNSEntry"; }

    @PostMapping("/publicDNSEntry")
    public String enterPublicDNS(HttpServletRequest req) throws Exception {
        dbUtil.enterIp(req.getParameter("ipaddress"));
        return "login";
    }



    @GetMapping("/userProfile")
    public String userProfilePage(HttpServletRequest req, Model theModel) throws Exception {

        MenuDetails(req, theModel);

        return "userProfile";
    }

    void MenuDetails(HttpServletRequest req, Model theModel) throws Exception {
        Cookie[] cookies = req.getCookies();
        ArrayList<ArrayList<String>> ops;

        if(cookies != null) {
            for(Cookie c : cookies) {
                if(c.getName().equals("abcHospitalAppUser")) {
                    theModel.addAttribute("userName", c.getValue());
                    //Get the details to be displayed on userProfile
                    User_details curr = dbUtil.getUserDetails(c.getValue());
                    theModel.addAttribute("user_details", curr);
                } else if(c.getName().equals("abcHospitalAppUserType")) {
                    ops = miscUtil.getMenu(c.getValue());
                    theModel.addAttribute("ops", ops);
                }
            }
        }
    }



    @GetMapping("/viewPatientData")
    public String viewPatientData(HttpServletRequest req, Model theModel) throws Exception {
        MenuDetails(req, theModel);

        User_details curr = (User_details)theModel.getAttribute("user_details");

        String targetNode = dbUtil.getAvailableIp();

        String uri = "http://"+targetNode+"/record/id/"+ String.valueOf(curr.getUser_id());
        String result = restTemplate.getForObject(uri, String.class);

        JSONObject responseData = new JSONObject(result);
        JSONObject medicalHistory = responseData.getJSONObject("medicalHistory");
        JSONArray records = medicalHistory.getJSONArray("Records");

        ArrayList<Record_data> data = new ArrayList<Record_data>();

        for (int i=0; i < records.length(); i++) {
            data.add(new Record_data(records.getJSONObject(i).getString("doctor")
                , records.getJSONObject(i).getString("patient")
                , records.getJSONObject(i).getString("description")
                , records.getJSONObject(i).getString("prescription")
                , records.getJSONObject(i).getString("RecordId"))
            );
        }

        theModel.addAttribute("blockchain_data",data);
        theModel.addAttribute("nodeUrl","http://"+targetNode);

        return "viewPatientData";

    }

    @GetMapping("/bookDoctor")
    public String bookDoctor(HttpServletRequest req, Model theModel) throws Exception {
        MenuDetails(req, theModel);

        return "bookDoctor";
    }

    @PostMapping("/bookDoctor")
    public String bookDocAppointment(HttpServletRequest req, Model theModel) throws Exception {
        MenuDetails(req, theModel);
        User_details temp = (User_details) theModel.getAttribute("user_details");
        UUID aid = UUID.randomUUID();
        String appointment_id = aid.toString();

        Appointment curr = new Appointment(
                appointment_id
                , temp.getUser_id()
                , Integer.parseInt(req.getParameter("doctorid"))
                , null
                , "Active"
                , req.getParameter("description")
        );

        if(dbUtil.bookAppoit(curr) == -1) {
            theModel.addAttribute("isError", "Error booking doctor");
            return "bookDoctor";
        }

        AppointmentDetails(temp.getUser_id(),theModel);

        return "viewPatientAppointment";
    }

    void AppointmentDetails(int user_id, Model theModel) throws Exception {
        ArrayList<Appointment> data = dbUtil.getAppointments(user_id);
        theModel.addAttribute("appointmentDetails", data);
    }

    @GetMapping("/viewAppointments")
    public String viewPatientAppointmentPage(HttpServletRequest req, Model theModel) throws Exception {
        MenuDetails(req, theModel);
        User_details temp = (User_details) theModel.getAttribute("user_details");
        AppointmentDetails(temp.getUser_id(),theModel);
        return "viewPatientAppointment";
    }

    @GetMapping("/cancelAppointment")
    public String cancelAppointment(HttpServletRequest req, Model theModel) throws Exception {
        MenuDetails(req, theModel);
        User_details temp = (User_details) theModel.getAttribute("user_details");
        dbUtil.cancelAppoint(req.getParameter("aid"));
        AppointmentDetails(temp.getUser_id(),theModel);
        return "viewPatientAppointment";
    }

    @GetMapping("/doctorPermissions")
    public String doctorPermissionsPage(HttpServletRequest req, Model theModel) throws Exception {
        MenuDetails(req, theModel);
        User_details temp = (User_details) theModel.getAttribute("user_details");
        PermissionsGiven(temp.getUser_id(), theModel);
        return "doctorPermissions";
    }

    @PostMapping("/givePermission")
    public String givePermissionMethod(HttpServletRequest req, Model theModel) throws Exception {
        MenuDetails(req, theModel);
        User_details temp = (User_details) theModel.getAttribute("user_details");
        if(dbUtil.giveDoctorPermission(temp.getUser_id(), Integer.parseInt(req.getParameter("did"))) == -1) {
            theModel.addAttribute("isError", "Invalid Doctor ID");
        }
        PermissionsGiven(temp.getUser_id(), theModel);
        return "doctorPermissions";
    }

    void PermissionsGiven(int user_id, Model theModel) throws Exception {
        ArrayList<User_details> data = dbUtil.getDoctorPermissions(user_id);
        theModel.addAttribute("givenPermissions", data);
    }

    @GetMapping("/revokePermission")
    public String revokePermissionAction(HttpServletRequest req, Model theModel) throws Exception {
        MenuDetails(req, theModel);
        User_details temp = (User_details) theModel.getAttribute("user_details");
        dbUtil.revokePermissionAction(temp.getUser_id(), Integer.parseInt(req.getParameter("did")));
        PermissionsGiven(temp.getUser_id(), theModel);
        return "doctorPermissions";
    }


    //...

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authReq) throws Exception {
        System.out.println(authReq.getUsername() +" :: "+ authReq.getPassword());
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authReq.getUsername(), authReq.getPassword())
            );

        } catch (BadCredentialsException be) {
            throw new Exception("Incorrect username / password : ", be);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authReq.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);
        final String userName = jwtTokenUtil.extractUsername(jwt);
        return  ResponseEntity.ok(new AuthenticationResponse(jwt));

    }

}
