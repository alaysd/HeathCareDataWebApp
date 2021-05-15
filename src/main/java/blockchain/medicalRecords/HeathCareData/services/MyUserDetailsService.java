package blockchain.medicalRecords.HeathCareData.services;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import java.sql.*;

@Service
public class MyUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/HeathCareData?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC","alay","password");
            String sql = "Select password from user_details where user_name = " + "\'"+userName+"\'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            return new User(userName, rs.getString("password"), new ArrayList<>());
        } catch (Exception e) {
            System.out.println("loadUserByUsername exception " + e);
            return null;
        }

    }
}
