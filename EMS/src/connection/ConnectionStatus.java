/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author abdulla
 */
public class ConnectionStatus {

    Connection conn;
    //constructor
    public ConnectionStatus() {
        conn = SQLConnection.DbConnector();
        if (conn == null) {
            System.exit(1);
        }
    }
    //method to validate connection status
    public boolean isDbConnected() {
        try {
            return !conn.isClosed();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

}

