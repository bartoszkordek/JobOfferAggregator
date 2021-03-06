package com.project.aggregator.api.useraccountservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.aggregator.api.useraccountservice.service.UserService;
import com.project.aggregator.api.useraccountservice.shared.UserDTO;
import com.project.aggregator.api.useraccountservice.ui.model.LoginRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UserService userService;
    private Environment environment;

    @Autowired
    public AuthenticationFilter(UserService userService, Environment environment) {
        this.userService = userService;
        this.environment = environment;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {

        try {
            LoginRequest credentials=new ObjectMapper().readValue(request.getInputStream(),LoginRequest.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.getEmail(),
                            credentials.getPassword(),
                            new ArrayList<>()
                    )
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) throws IOException, ServletException {

        String userEmail=((User) authResult.getPrincipal()).getUsername();
        UserDTO userDetails=userService.getUserDetailsByEmail(userEmail);

        String token= Jwts.builder()
                .setSubject(userDetails.getUserId())
                .setExpiration(new Date(System.currentTimeMillis()+Long.parseLong(environment.getProperty("token.expiration-time"))))
                .signWith(SignatureAlgorithm.HS512,environment.getProperty("token.secret"))
                .compact();

        response.addHeader("token",token);
        response.addHeader("userId",userDetails.getUserId());
    }
}
