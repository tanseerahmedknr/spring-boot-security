package com.tanseer.springsecurityclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModel {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
//    This attribute would check if the password entered was same
    private String matchingPassword;
}
