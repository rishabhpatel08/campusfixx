package com.sgsits.campusfix.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="users")
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="enrollment_id",unique=true,nullable=false,length=60) private String enrollmentId;
    @Column(nullable=false,length=120) private String name;
    @Column(unique=true,nullable=false,length=140) private String email;
    @Column(nullable=false,length=20) private String role;
    @Column(length=100) private String department;
    @Column(length=50) private String branch;
    @Column(name="year_section",length=20) private String yearSection;
    @Column(length=20) private String phone;
    @Column(name="whatsapp_number",length=20) private String whatsappNumber;
    private Integer points=0;
    @Column(name="streak_days") private Integer streakDays=0;
    @Column(length=50) private String badge="Newcomer";
    @Column(name="preferred_lang",length=5) private String preferredLang="en";
    @Column(name="is_active") private Boolean active=true;
    @Column(name="created_at") private LocalDateTime createdAt=LocalDateTime.now();
    @Column(name="updated_at") private LocalDateTime updatedAt=LocalDateTime.now();
    @Column(name="last_login") private LocalDateTime lastLogin;
    @Column(name="data_source",length=30) private String dataSource="csv_import";
    @PreUpdate public void onUpdate(){ updatedAt=LocalDateTime.now(); }

    public User(){}
    public static Builder builder(){return new Builder();}
    public static class Builder {
        private User u=new User();
        public Builder enrollmentId(String v){u.enrollmentId=v;return this;}
        public Builder name(String v){u.name=v;return this;}
        public Builder email(String v){u.email=v;return this;}
        public Builder role(String v){u.role=v;return this;}
        public Builder department(String v){u.department=v;return this;}
        public Builder branch(String v){u.branch=v;return this;}
        public Builder yearSection(String v){u.yearSection=v;return this;}
        public Builder phone(String v){u.phone=v;return this;}
        public Builder whatsappNumber(String v){u.whatsappNumber=v;return this;}
        public Builder points(Integer v){u.points=v;return this;}
        public Builder badge(String v){u.badge=v;return this;}
        public Builder active(Boolean v){u.active=v;return this;}
        public Builder dataSource(String v){u.dataSource=v;return this;}
        public User build(){return u;}
    }
    public void addPoints(int pts){points=(points==null?0:points)+pts;updateBadge();}
    private void updateBadge(){if(points>=500)badge="Champion";else if(points>=200)badge="Contributor";else if(points>=50)badge="Reporter";else badge="Newcomer";}
    public Long getId(){return id;} public String getEnrollmentId(){return enrollmentId;} public String getName(){return name;}
    public String getEmail(){return email;} public String getRole(){return role;} public String getDepartment(){return department;}
    public String getBranch(){return branch;} public String getYearSection(){return yearSection;} public String getPhone(){return phone;}
    public String getWhatsappNumber(){return whatsappNumber;} public Integer getPoints(){return points;} public String getBadge(){return badge;}
    public String getPreferredLang(){return preferredLang;} public Boolean getActive(){return active;} public LocalDateTime getLastLogin(){return lastLogin;}
    public LocalDateTime getCreatedAt(){return createdAt;} public String getDataSource(){return dataSource;}
    public void setId(Long v){id=v;} public void setName(String v){name=v;} public void setEmail(String v){email=v;}
    public void setRole(String v){role=v;} public void setWhatsappNumber(String v){whatsappNumber=v;} public void setActive(Boolean v){active=v;}
    public void setLastLogin(LocalDateTime v){lastLogin=v;} public void setPoints(Integer v){points=v;} public void setBadge(String v){badge=v;}
}
