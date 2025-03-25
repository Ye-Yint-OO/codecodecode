package com.prj.LoneHPManagement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String userCode;
    private String name;
    private String email;
    private String phoneNumber;
    private String roleName;
    private String branchName;
    private String status;
    private LocalDateTime createdDate;

}
