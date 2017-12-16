/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

/**
 *
 * @author abdulla
 */
public class Employee {
    //declare variables
    private int id;
    private String lastName;
    private String firstName;
    private String manager;
    private String dept;
    private int phone;
    private String comments;
    //constructor
    public Employee(int id, String lastName, String firstName, String manager, String dept, int phone, String comments) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.manager = manager;
        this.dept = dept;
        this.phone = phone;
        this.comments = comments;
    }
    
    //getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    
    
}
