/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import connection.SQLConnection;
import entities.EmpPerformance;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 *
 * @author abdulla
 */
public class ManagerController implements Initializable {

    //declare connection and statement
    Connection conn;
    PreparedStatement ps = null;
    ResultSet rs = null;
    //declare controls
    @FXML
    private JFXButton signoutBtn, findBtn, runBtn, confirmBtn, clearBtn, saveBtn, cancelBtn, evalBtn;
    @FXML
    private JFXPasswordField managerPass;
    @FXML
    private LineChart<String, Number> perChart;
    @FXML
    private TableView<EmpPerformance> empTable;
    @FXML
    private TableColumn<?, ?> idCol, nameCol, scoreCol, commentsCol, posCol;
    @FXML
    private Label managerName;
    @FXML
    private JFXTextField scoreTxtField, posTxtField;
    @FXML
    private VBox editVBox;
    //declare variables
    private ObservableList<EmpPerformance> empList;
    private int tempID;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //connection
        conn = SQLConnection.DbConnector();
        setCellTable();
        empList = FXCollections.observableArrayList(); //all employees table
        scoreTxtField.addEventFilter(KeyEvent.KEY_TYPED, maxLength(2));
        //force the phone extention field to be numeric only
        scoreTxtField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                scoreTxtField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        //-------------------------------events---------------------------------
        //sign out event (go back to login page)
        signoutBtn.setOnAction(e -> {
            try {
                Parent homeParent = FXMLLoader.load(getClass().getResource("/userInterface/Login.fxml"));
                Scene loginScene = new Scene(homeParent);
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.setScene(loginScene);
                stage.show();
            } catch (IOException ex) {
                Logger.getLogger(HRAssistantController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        //confirm manager password event
        confirmBtn.setOnAction(e -> {
            try {
                if (findManager(managerPass.getText())) {
                    JOptionPane.showMessageDialog(null, "Welcome " + managerName.getText());
                    managerPass.setDisable(true);
                    findBtn.setDisable(false);
                    confirmBtn.setDisable(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Please check your password!");
                }
            } catch (SQLException ex) {
                Logger.getLogger(ManagerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        //find employees per manager event
        findBtn.setOnAction(e -> {
            try {
                if (!"".equals(managerName.getText())) {
                    empList.clear();
                    findEmp(managerName.getText());
                    runBtn.setDisable(false);
                    evalBtn.setDisable(false);
                } else {
                    JOptionPane.showMessageDialog(null, "Manager must be identified!");
                }
            } catch (SQLException ex) {
                Logger.getLogger(ManagerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        //run performance graph event
        runBtn.setOnAction(e -> {
            perChart.getData().clear();
            perChart.setDisable(false);
            XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
            for (int i = 0; i < empList.size(); i++) {
                series.getData().add(new XYChart.Data<String, Number>(empTable.getItems().get(i).getName(), empTable.getItems().get(i).getScore()));
            }
            perChart.getData().add(series);
        });
        //clear results event
        clearBtn.setOnAction(e -> {
            clearResults();
            findBtn.setDisable(true);
            managerPass.setDisable(false);
            runBtn.setDisable(true);
            evalBtn.setDisable(true);
            editVBox.setVisible(false);
            confirmBtn.setDisable(false);
            scoreTxtField.clear();
            posTxtField.clear();
            perChart.getData().clear();
        });
        //evaluate employee event
        evalBtn.setOnAction(e -> {
            editVBox.setVisible(true);
            scoreTxtField.setText(String.valueOf(empTable.getSelectionModel().getSelectedItem().getScore()));
            posTxtField.setText(empTable.getSelectionModel().getSelectedItem().getPosition());
            tempID = empTable.getSelectionModel().getSelectedItem().getId();

        });
        //save changes event
        saveBtn.setOnAction(e -> {
            try {
                updateEmp(Integer.parseInt(scoreTxtField.getText()), posTxtField.getText(), tempID);
                editVBox.setVisible(false);
                scoreTxtField.clear();
                posTxtField.clear();
            } catch (SQLException ex) {
                Logger.getLogger(ManagerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        //cancel changes event
        cancelBtn.setOnAction(e -> {
            editVBox.setVisible(false);
            scoreTxtField.clear();
            posTxtField.clear();
        });

    } //end initialize

    //----------------------------------methods---------------------------------
    //set table
    private void setCellTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        commentsCol.setCellValueFactory(new PropertyValueFactory<>("comments"));
        posCol.setCellValueFactory(new PropertyValueFactory<>("position"));
    }

    //find employees per manager
    private void findEmp(String manager) throws SQLException {
        try {
            ps = conn.prepareStatement("SELECT  id, score, (lastName || ', ' || firstName) as Name, comments, position FROM employee WHERE manager = ?");
            ps.setString(1, manager);
            rs = ps.executeQuery();
            while (rs.next()) {
                empList.add(new EmpPerformance(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5)));
            }
        } catch (SQLException ex) {
        }
        empTable.setItems(empList);
        ps.close();
    }

    //identify manager
    private boolean findManager(String password) throws SQLException {
        try {
            ps = conn.prepareStatement("select name from mngerAcct where password = ?");
            ps.setString(1, password);
            rs = ps.executeQuery();
            managerName.setText(rs.getString(1));
            return rs.next();
        } catch (Exception e) {
            return false;
        } finally {
            ps.close();
            rs.close();
        }
    }

    //update score and position
    private void updateEmp(int score, String position, int id) throws SQLException {
        try {
            ps = conn.prepareStatement("update employee set score = ?, position = ? where id = ?");
            ps.setInt(1, score);
            ps.setString(2, position);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ManagerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //clear fields
    private void clearResults() {
        managerPass.clear();
        managerName.setText("");
        empList.clear();
    }

    //limit the characters entered for score
    public EventHandler<KeyEvent> maxLength(final Integer i) {
        return (KeyEvent arg0) -> {
            JFXTextField tx = (JFXTextField) arg0.getSource();
            if (tx.getText().length() >= i) {
                arg0.consume();
            }
        };
    }

}//end class
