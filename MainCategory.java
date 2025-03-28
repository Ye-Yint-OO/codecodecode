package com.prj.LoneHPManagement.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "main_category")
public class MainCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "category", nullable = false, unique = true, length = 50)
    private String category;

    @Column(name = "status", nullable = false)
    @JsonIgnore
    private int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty("status")
    public String getStatusDescription() {
        ConstraintEnum constraint = ConstraintEnum.fromCode(status);
        return constraint != null ? constraint.getDescription() : "unknown";
    }
    @PrePersist
    protected void prePersist() {
        if (status == 0) status = ConstraintEnum.ACTIVE.getCode(); // Default status
    }

}
