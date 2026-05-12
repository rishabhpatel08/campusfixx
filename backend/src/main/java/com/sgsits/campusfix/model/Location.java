package com.sgsits.campusfix.model;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="locations")
public class Location {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="name_en") private String nameEn;
    @Column(name="name_hi") private String nameHi;
    private String block,floor,zone;
    private BigDecimal latitude,longitude;
    public Location(){}
    public Long getId(){return id;} public String getNameEn(){return nameEn;} public String getNameHi(){return nameHi;}
    public String getBlock(){return block;} public String getFloor(){return floor;} public String getZone(){return zone;}
    public BigDecimal getLatitude(){return latitude;} public BigDecimal getLongitude(){return longitude;}
}
