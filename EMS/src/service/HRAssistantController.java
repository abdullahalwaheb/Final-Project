/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import connection.SQLConnection;
import entities.Employee;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 *
 * @author abdulla
 */
public class HRAssistantController implements Initializable {

    //declare connection and statement
    Connection conn;
    PreparedStatement ps = null;
    ResultSet rs = null;
    //declare variables
    private ObservableList<Employee> employeeList;
    private ObservableList<Employee> employeeList2;
    private ObservableList<String> departmentList;
    private ObservableList<String> filterList;
    private ObservableList<String> managerList;
    //declare interface components and controls
    //main frame components and controls
    @FXML
    private JFXButton signoutBtn;
    //--------------employees index pane controls--------------
    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<?, ?> idCol, firstNameCol, lastNameCol, managerCol, deptCol, phoneCol, commentsCol;
    @FXML
    private JFXComboBox filterComboBox;
    @FXML
    private JFXButton refreshBtn;
    ////--------------add employee pane controls--------------        
    @FXML
    private JFXButton addBtn, clearBtn;
    @FXML
    private JFXComboBox deptComboBox, managerComboBox;
    @FXML
    private JFXTextField lastNameTxtField, firstNameTxtField, phoneTxtField;
    @FXML
    private JFXTextArea commentsTxtArea;
    ////--------------remove employee pane controls--------------
    @FXML
    private JFXCheckBox checkBox;
    @FXML
    private JFXButton searchBtn, deleteBtn, clearBtn1;
    @FXML
    private TableView<Employee> employeeTable2;
    @FXML
    private TableColumn<?, ?> idCol2, firstNameCol2, lastNameCol2, managerCol2, deptCol2, phoneCol2, commentsCol2;
    @FXML
    private JFXTextField searchIDTxtField;
    ////--------------edit employee pane controls--------------
    @FXML
    private JFXButton updateBtn, clearBtn2, searchBtn2;
    @FXML
    private JFXTextField searchIDTxtField2, new_lastNameTxtField, new_firstNameTxtField, new_phoneTxtField;
    @FXML
    private JFXComboBox new_deptComboBox, new_managerComboBox;
    @FXML
    private JFXTextArea new_commentsTxtArea;
    @FXML
    private JFXCheckBox checkBox2;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //connection
        conn = SQLConnection.DbConnector();
        //load data from database into tables
        setCellTable();
        employeeList = FXCollections.observableArrayList(); //all employees table
        loadTableData();
        departmentList = FXCollections.observableArrayList(); //departments combobox
        loadDepartmentList();
        filterList = FXCollections.observableArrayList(); //department filter combobox
        filterListComboBox();
        filterComboBox.getSelectionModel().selectLast();
        managerList = FXCollections.observableArrayList(); //managers combobox
        loadManagerList();
        //limit the characters entered for entry fields - method below
        lastNameTxtField.addEventFilter(KeyEvent.KEY_TYPED, maxLength(15));
        firstNameTxtField.addEventFilter(KeyEvent.KEY_TYPED, maxLength(15));
        phoneTxtField.addEventFilter(KeyEvent.KEY_TYPED, maxLength(4));
        new_lastNameTxtField.addEventFilter(KeyEvent.KEY_TYPED, maxLength(15));
        new_firstNameTxtField.addEventFilter(KeyEvent.KEY_TYPED, maxLength(15));
        new_phoneTxtField.addEventFilter(KeyEvent.KEY_TYPED, maxLength(4));
        //force the phone extention field to be numeric only
        phoneTxtField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                phoneTxtField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        new_phoneTxtField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                new_phoneTxtField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        //-------------------------button mouse click events-------------------------
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
        //refresh table event
        refreshBtn.setOnAction(e -> {
            if ("All".equals(filterComboBox.getSelectionModel().getSelectedItem().toString())) {
                loadTableData();
            } else {
                filteredTable(filterComboBox.getSelectionModel().getSelectedItem().toString());
            }
        });
        //add employee event
        addBtn.setOnAction(e -> {
            if (isFilled()) {
                try {
                    addEmployee(lastNameTxtField.getText(), firstNameTxtField.getText(),
                            managerComboBox.getSelectionModel().getSelectedItem().toString(),
                            deptComboBox.getSelectionModel().getSelectedItem().toString(),
                            Integer.parseInt(phoneTxtField.getText()), commentsTxtArea.getText());
                    clearEntry(); //clear Entry
                } catch (SQLException ex) {
                    Logger.getLogger(HRAssistantController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                JOptionPane.showMessageDialog(null, "All information have to be entered");
            }
        });
        //clear entry event (for add employee form)
        clearBtn.setOnAction(e -> {
            clearEntry();
        });
        //find employee event
        searchBtn.setOnAction(e -> {
            if (!"".equals(searchIDTxtField.getText())) {
                try {
                    findEmployee(Integer.parseInt(searchIDTxtField.getText()));
                } catch (SQLException ex) {
                    Logger.getLogger(HRAssistantController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please enter the employee ID");
            }
        });
        //delete employee event
        deleteBtn.setOnAction(e -> {
            try {
                if (checkBox.isSelected()) {
                    deleteEmployee(employeeTable2.getSelectionModel().getSelectedItem().getId());
                    employeeList2.clear(); //to refresh the table's data
                    JOptionPane.showMessageDialog(null, "Deleted!");
                } else {
                    JOptionPane.showMessageDialog(null, "Please confirm your selection!");
                }
            } catch (Exception ex) {
            }
            checkBox.setSelected(false);
        });
        //clear found id text field at delete pane
        clearBtn1.setOnAction(e -> {
            searchIDTxtField.clear();
            employeeList2.clear();
        });
        //find employee by ID for edit event
        searchBtn2.setOnAction(e -> {
            if (!"".equals(searchIDTxtField2.getText())) {
                try {
                    ps = conn.prepareStatement("select * from employee where id = ?");
                    ps.setInt(1, Integer.parseInt(searchIDTxtField2.getText()));
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        new_lastNameTxtField.setText(rs.getString(2));
                        new_firstNameTxtField.setText(rs.getString(3));
                        new_managerComboBox.getSelectionModel().select(rs.getString(4));
                        new_deptComboBox.getSelectionModel().select(rs.getString(5));
                        new_phoneTxtField.setText(rs.getString(6));
                        new_commentsTxtArea.setText(rs.getString(7));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(HRAssistantController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please enter the employee ID");
            }
        });
        //update employee event
        updateBtn.setOnAction(e -> {
            if (checkBox2.isSelected() && !"".equals(searchIDTxtField2.getText())) {
                editEmployee(
                        new_lastNameTxtField.getText(),
                        new_firstNameTxtField.getText(),
                        new_managerComboBox.getSelectionModel().getSelectedItem().toString(),
                        new_deptComboBox.getSelectionModel().getSelectedItem().toString(),
                        Integer.parseInt(new_phoneTxtField.getText()),
                        new_commentsTxtArea.getText(),
                        Integer.parseInt(searchIDTxtField2.getText()));
                clearEdit();
                JOptionPane.showMessageDialog(null, "Updated!");
            } else {
                JOptionPane.showMessageDialog(null, "Please confirm your selection or check your ID entry");
            }
            checkBox2.setSelected(false);
        });
        //clear button at edit pane event
        clearBtn2.setOnAction(e -> {
        });
    } //end initialize

    //-------------------------methods-------------------------
    //setting employees tables
    private void setCellTable() {
        //table 1
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        managerCol.setCellValueFactory(new PropertyValueFactory<>("manager"));
        deptCol.setCellValueFactory(new PropertyValueFactory<>("dept"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        commentsCol.setCellValueFactory(new PropertyValueFactory<>("comments"));
        //table 2
        idCol2.setCellValueFactory(new PropertyValueFactory<>("id"));
        lastNameCol2.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        firstNameCol2.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        managerCol2.setCellValueFactory(new PropertyValueFactory<>("manager"));
        deptCol2.setCellValueFactory(new PropertyValueFactory<>("dept"));
        phoneCol2.setCellValueFactory(new PropertyValueFactory<>("phone"));
        commentsCol2.setCellValueFactory(new PropertyValueFactory<>("comments"));
    }

    //loading data into all employees table
    private void loadTableData() {
        employeeList.clear(); //to avoid loading duplicate date when adding an employee
        try {
            ps = conn.prepareStatement("select * from employee");
            rs = ps.executeQuery();
            while (rs.next()) {
                employeeList.add(new Employee(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getInt(6),
                        rs.getString(7)));
            }
        } catch (SQLException ex) {
        }
        employeeTable.setItems(employeeList);
    }

    //
    private void filteredTable(String dept) {
        employeeList.clear();
        try {
            ps = conn.prepareStatement("select * from employee where dept = ?");
            ps.setString(1, dept);
            rs = ps.executeQuery();
            while (rs.next()) {
                employeeList.add(new Employee(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getInt(6),
                        rs.getString(7)));
            }
        } catch (SQLException ex) {
        }
        employeeTable.setItems(employeeList);
    }

    //load data into filter combokBox (drop down list)
    private void filterListComboBox() {
        try {
            ps = conn.prepareStatement("select * from department");
            rs = ps.executeQuery();
            while (rs.next()) {
                filterList.add(rs.getString(1));
            }
        } catch (SQLException ex) {
        }
        filterList.add("All");
        filterComboBox.getItems().addAll(filterList);
    }

    //load data into department comboBox (drop down list)
    private void loadDepartmentList() {
        try {
            ps = conn.prepareStatement("select * from department");
            rs = ps.executeQuery();
            while (rs.next()) {
                departmentList.add(rs.getString(1));
            }
        } catch (SQLException ex) {
        }
        deptComboBox.getItems().addAll(departmentList); //for add employee pane
        new_deptComboBox.getItems().addAll(departmentList); //for edit employee pane
    }

    //load data into manager comboBox (drop down list)
    private void loadManagerList() {
        try {
            ps = conn.prepareStatement("SELECT (\"lastName\" || ', ' || \"firstName\") FROM employee");
            rs = ps.executeQuery();
            while (rs.next()) {
                managerList.add(rs.getString(1));
            }
        } catch (SQLException ex) {
        }
        managerList.add("N/A");
        managerComboBox.getItems().addAll(managerList); //for add employee pane
        new_managerComboBox.getItems().addAll(managerList); //for edit employee pane
    }

    //add employee
    private void addEmployee(String lastName, String firstName, String manager, String dept, int phone, String comments) throws SQLException {
        try {
            ps = conn.prepareStatement("Insert into employee (lastName, firstName, manager, dept, phone, comments) values (?,?,?,?,?,?)");
            ps.setString(1, lastName);
            ps.setString(2, firstName);
            ps.setString(3, manager);
            ps.setString(4, dept);
            ps.setInt(5, phone);
            ps.setString(6, comments);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(HRAssistantController.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            ps.close();
        }
    }

    //restrictions for add form to force use all entries
    private boolean isFilled() {
        return ((!"".equals(lastNameTxtField.getText()))
                && (!"".equals(firstNameTxtField.getText()))
                && (!"".equals(managerComboBox.getSelectionModel().getSelectedItem().toString()))
                && (!"".equals(deptComboBox.getSelectionModel().getSelectedItem().toString()))
                && (!"".equals(Integer.parseInt(phoneTxtField.getText())))
                && (!"".equals(commentsTxtArea.getText())));
    }

    //clear entry at add pane
    private void clearEntry() {
        lastNameTxtField.clear();
        firstNameTxtField.clear();
        phoneTxtField.clear();
        managerComboBox.getSelectionModel().clearSelection();
        deptComboBox.getSelectionModel().clearSelection();
    }

    //find employee by id for delete event
    private void findEmployee(int id) throws SQLException {
        employeeList2 = FXCollections.observableArrayList();
        try {
            ps = conn.prepareStatement("select * from employee where id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                employeeList2.add(new Employee(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getInt(6),
                        rs.getString(7)));
            }
        } catch (SQLException ex) {
        }
        employeeTable2.setItems(employeeList2);
        ps.close();
    }

    //delete employee
    private void deleteEmployee(int id) {
        try {
            ps = conn.prepareStatement("delete from employee where id =?");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(HRAssistantController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //edit employee
    private void editEmployee(String lastName, String firstName, String manager, String dept, int phone, String comments, int id) {
        try {
            ps = conn.prepareStatement("update employee set lastName = ?, firstName = ?, manager = ?, dept = ?, phone = ?, comments = ? where id =?");
            ps.setString(1, lastName);
            ps.setString(2, firstName);
            ps.setString(3, manager);
            ps.setString(4, dept);
            ps.setInt(5, phone);
            ps.setString(6, comments);
            ps.setInt(7, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(HRAssistantController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //clear all entries at edit pane
    private void clearEdit() {
        searchIDTxtField2.clear();
        new_lastNameTxtField.clear();
        new_firstNameTxtField.clear();
        new_phoneTxtField.clear();
        new_commentsTxtArea.clear();
        new_managerComboBox.getSelectionModel().clearSelection();
        new_deptComboBox.getSelectionModel().clearSelection();
    }

    //limit the characters entered for entry fields
    public EventHandler<KeyEvent> maxLength(final Integer i) {
        return (KeyEvent arg0) -> {
            JFXTextField tx = (JFXTextField) arg0.getSource();
            if (tx.getText().length() >= i) {
                arg0.consume();
            }
        };
    }

} //end class

