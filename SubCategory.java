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
@Table(name = "sub_category")
public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "category", nullable = false, unique = true, length = 50)
    private String category;
    @ManyToOne
    @JoinColumn(name = "main_category_id", nullable = false)
    private MainCategory mainCategory;

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

    public MainCategory getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(MainCategory mainCategory) {
        this.mainCategory = mainCategory;
    }

    @JsonProperty("status")
    public String getStatusDescription() {
        ConstraintEnum constraint = ConstraintEnum.fromCode(status);
        return constraint != null ? constraint.getDescription() : "unknown";
    }

    @PrePersist
    protected void prePersist() {
        if (status == 0) status = ConstraintEnum.ACTIVE.getCode(); // Default to ACTIVE (13)
    }
}
