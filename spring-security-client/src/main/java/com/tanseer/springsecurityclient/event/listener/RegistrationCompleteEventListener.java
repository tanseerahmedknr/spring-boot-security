package com.tanseer.springsecurityclient.event.listener;

import com.tanseer.springsecurityclient.entity.User;
import com.tanseer.springsecurityclient.event.RegistrationCompleteEvent;
import com.tanseer.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent>
{

    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {

//      Create verification token for the user with link
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.saveVerificationTokenForUser(token,user);

//        verifyRegistration is an api and as a parameter we are passing enter token
//        getApplicationUrl is the context which we are getting
//
//        Send mail to user
        String url =
                event.getApplicationUrl()
                + "/verifyRegistration?token="
                +token;

//        The actual email should be here, but we are mimicking it here
//        creating sendVerificationEmail()

        log.info("Click the link to verify your account : {}",url);
    }
}
