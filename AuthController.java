package com.prj.LoneHPManagement.Controller;

import com.prj.LoneHPManagement.Service.UserService;
import com.prj.LoneHPManagement.model.dto.ApiResponse;
import com.prj.LoneHPManagement.model.dto.LoginRequest;
import com.prj.LoneHPManagement.model.entity.User;
import com.prj.LoneHPManagement.model.repo.UserRepository;
import com.prj.LoneHPManagement.security.CustomUserDetailsService;
import com.prj.LoneHPManagement.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    @Autowired
    private UserRepository userRepository;
    public AuthController(AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest loginRequest) {

        System.out.println("Revice obj"+loginRequest);
        try {
            System.out.println("Attempting authentication for user: " + loginRequest.getUserCode());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUserCode(), loginRequest.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUserCode());
            List<String> permissions = userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toList());
            String token = jwtUtil.generateToken(userDetails.getUsername(),permissions);
            return ResponseEntity.ok(ApiResponse.success(200, "Login successful", "token : " + token));
        } catch (BadCredentialsException e) {
            System.out.println("will 401");
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Wrong password"));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, e.getMessage()));
        }
    }



    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        User user = userRepository.findByUserCodeWithBranch(authentication.getName());

        if (user == null || user.getBranch() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", "User or branch not found")
            );
        }

        System.out.println("user role level: " + user.getRole().getAuthority());

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "userCode", user.getUserCode(),
                "name", user.getName(),
                "email", user.getEmail(),
                "branchName", user.getBranch().getBranchName(),
                "roleLevel", user.getRole().getAuthority(),
                "branch", user.getBranch(),
                "role", user.getRole()
        ));
    }

    // Logout endpoint
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Extract token
            jwtUtil.blacklistToken(token); // Blacklist the token
            return ResponseEntity.ok(ApiResponse.success(200, "Logout successful", null));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid or missing token"));
    }
}