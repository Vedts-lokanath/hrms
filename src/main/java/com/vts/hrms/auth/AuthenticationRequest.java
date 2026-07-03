package com.vts.hrms.auth;


public class AuthenticationRequest {

    private String username;
    private String password;
//    private Long timestamp;

//    public AuthenticationRequest(String username, String password, Long timestamp) {
//        super();
//        this.username = username;
//        this.password = password;
//        this.timestamp = timestamp;
//    }


    public AuthenticationRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public AuthenticationRequest()
    {

    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
//    public Long getTimestamp() { return timestamp; }
//    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

}
