package com.example.myapplication.data;

public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    public UserDto(Long id, String fn, String ln, String email) {
        this.id = id;
        this.firstName = fn;
        this.lastName = ln;
        this.email = email;
    }

    public Long getId(){
        return this.id;
    }

    public String getFirstName() {
        return firstName;
    }


    public String getLastName() {
        return lastName;
    }


    public String getEmail() {
        return email;
    }

}

