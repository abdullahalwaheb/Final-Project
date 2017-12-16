/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import connection.ConnectionStatus;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import connection.SQLConnection;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 *
 * @author abdulla
 */
public class LoginController implements Initializable {

    //declare connection and statement
    Connection conn;
    ConnectionStatus cs = new ConnectionStatus();
    PreparedStatement ps = null;
    ResultSet rs = null;
    //declare variables
    private ObservableList<String> rolesList;
    //declare components and controls
    @FXML
    private Label connResultLbl;
    @FXML
    private JFXButton exitBtn, signinBtn;
    @FXML
    private JFXTextField usernameTxt;
    @FXML
    private JFXPasswordField passwordTxt;
    @FXML
    private JFXComboBox roleComboBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //check status connection
        conn = SQLConnection.DbConnector();
        if (cs.isDbConnected()) {
            connResultLbl.setText("Connected");
        } else {
            connResultLbl.setText("Failed to connect");
        }
        //load roles comboBox with data
        rolesList = FXCollections.observableArrayList();
        loadRolesList();
        roleComboBox.getSelectionModel().selectFirst(); //set a default value
        //sing in events
        signinBtn.setOnAction((ActionEvent e) -> {
            try {
                if (login(usernameTxt.getText(), passwordTxt.getText())) {
                    if (roleComboBox.getSelectionModel().getSelectedIndex() == 0) {
                        Parent homeParent = FXMLLoader.load(getClass().getResource("/userInterface/HRAssistant.fxml"));
                        Scene homeScene = new Scene(homeParent);
                        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                        stage.setScene(homeScene);
                        stage.show();
                    } else if (roleComboBox.getSelectionModel().getSelectedIndex() == 1) {
                        Parent homeParent = FXMLLoader.load(getClass().getResource("/userInterface/Manager.fxml"));
                        Scene homeScene = new Scene(homeParent);
                        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                        stage.setScene(homeScene);
                        stage.show();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "username or password are wrong! ");
                    usernameTxt.clear();
                    passwordTxt.clear();
                }
            } catch (IOException | SQLException ex) {
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        //exit event
        exitBtn.setOnAction((ActionEvent e) -> {
            System.exit(1);
        });

    } //end initialize

    //---------------methods---------------
    //validating login credentials
    private boolean login(String username, String password) throws SQLException {
        try {
            ps = conn.prepareStatement("SELECT * FROM user WHERE username = (?) AND password = (?)");
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            return false;
        } finally {
            ps.close();
            rs.close();
        }
    }

    //load data into role comboBox (drop down list)
    private void loadRolesList() {
        try {
            ps = conn.prepareStatement("select * from roles");
            rs = ps.executeQuery();
            while (rs.next()) {
                rolesList.add(rs.getString(1));
            }
        } catch (SQLException ex) {
        }
        roleComboBox.getItems().addAll(rolesList);
    }

}//end class
