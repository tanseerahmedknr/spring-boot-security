package com.tanseer.springsecurityclient.controller;

import com.tanseer.springsecurityclient.entity.User;
import com.tanseer.springsecurityclient.entity.VerificationToken;
import com.tanseer.springsecurityclient.event.RegistrationCompleteEvent;
import com.tanseer.springsecurityclient.model.PasswordModel;
import com.tanseer.springsecurityclient.model.UserModel;
import com.tanseer.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    //    For creating url and to get the context of url we need http request , so we are using final htt
    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
        User user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(
                user,
                applicationUrl(request)
        ));
        return "Success";
    }


    //    For checking the authenticity of user and validity of token within the time limit
    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {
        String result = userService.validateVerificationToken(token);
        if (result.equalsIgnoreCase("Valid")) {
            return "User Verified Successfully";
        }
        return "Bad User";
    }

    //    Taking old token to verify and generate new token
    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken,
                                          HttpServletRequest request) {

//  A new token would be generated
        VerificationToken verificationToken =
                userService.generateNewVerificationToken(oldToken);

//  Sending email back to the user
        User user = verificationToken.getUser();
        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
        return "Verification Link Sent";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request) {
        User user = userService.findUserByEmail(passwordModel.getEmail());
//  We need url to reset the token
        String url = "";
        if (user != null) {
            String token = UUID.randomUUID().toString();
//  At this point a new token is created
            userService.createPasswordResetTokenForUser(user, token);
            url = passwordResetTokenMail(user, applicationUrl(request), token);
        }
        return url;
    }

    //    Checking the user with email and if old password matched then return successful
    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel) {
        User user = userService.findUserByEmail(passwordModel.getEmail());
        if (!userService.checkIfValidOldPassword(user, passwordModel.getOldPassword())) {
            return "Invalid Old Password";
        }
//  Save new Password
        userService.changePassword(user, passwordModel.getNewPassword());
        return "Password Changed Successfully";
    }


    //  Reset Password
    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token,
                               @RequestBody PasswordModel passwordModel) {

        //  Verify the token and save it
        String result = userService.validatePasswordResetToken(token);
        if (!result.equalsIgnoreCase("valid")) {
            return "Invalid Token";
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if (user.isPresent()) {
            userService.changePassword(user.get(), passwordModel.getNewPassword());
            return "Password Reset Successfully";
        } else {
            return "Invalid Token";
        }

    }

    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url =
                applicationUrl
                        + "/savePassword?token="
                        + token;

//        The actual email should be here, but we are mimicking it here
//        creating sendVerificationEmail()

        log.info("Click the link to reset your password : {}", url);
        return url;
    }

    private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
        String url =
                applicationUrl
                        + "/verifyRegistration?token="
                        + verificationToken.getToken();

//        The actual email should be here, but we are mimicking it here
//        creating sendVerificationEmail()

        log.info("Click the link to verify your account : {}", url);
    }

    //    Method returning the path
    private String applicationUrl(HttpServletRequest request) {
        return "http://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                request.getContextPath();
    }
}
