package com.myolnir.model;

import javax.persistence.*;


@Entity
@Table(name = "User")
public class UserAccountDO {

    @Id
    @GeneratedValue
    private Long id;

    private String password;

    private String email;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
