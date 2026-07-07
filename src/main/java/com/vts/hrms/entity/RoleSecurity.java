package com.vts.hrms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Table(name = "role_security")
@Data
@Entity
public class RoleSecurity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name")
    private String roleName;

}
