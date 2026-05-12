package com.sgsits.campusfix.model;
import jakarta.persistence.*;

@Entity @Table(name="departments")
public class Department {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="name_en") private String nameEn;
    @Column(name="name_hi") private String nameHi;
    @Column(name="category_key") private String categoryKey;
    @Column(name="sla_hours") private Integer slaHours;
    public Department(){}
    public Long getId(){return id;} public String getNameEn(){return nameEn;} public Integer getSlaHours(){return slaHours;}
}
