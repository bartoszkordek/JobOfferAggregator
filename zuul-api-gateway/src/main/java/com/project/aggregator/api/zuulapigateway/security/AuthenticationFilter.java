package com.project.aggregator.api.zuulapigateway.security;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class AuthenticationFilter extends BasicAuthenticationFilter {

    private final Environment environment;

    @Autowired
    public AuthenticationFilter(AuthenticationManager authenticationManager, Environment environment) {
        super(authenticationManager);
        this.environment=environment;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        String token=request.getHeader(environment.getProperty("authorization.token.header.name"));

        if(token==null){
            chain.doFilter(request,response);
            return;
        }

        UsernamePasswordAuthenticationToken authenticationToken=getAuthentication(token);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        chain.doFilter(request,response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) {

        String userId= Jwts.parser()
                .setSigningKey(environment.getProperty("token.secret"))
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        if(userId==null){
            return null;
        }

        return new UsernamePasswordAuthenticationToken(userId,null,new ArrayList<>());
    }
}
