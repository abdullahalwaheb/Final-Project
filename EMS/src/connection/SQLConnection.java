/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author abdulla
 */
public class SQLConnection {
    //establish connection to database
    public static Connection DbConnector() {
        try {
            Connection conn = null;
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:EMS.sqlite");
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            return null;
        }
    }
}
