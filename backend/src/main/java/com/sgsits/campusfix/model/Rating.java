package com.sgsits.campusfix.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="ratings")
public class Rating {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="complaint_id") private Long complaintId;
    @Column(name="user_id") private Long userId;
    @Column(nullable=false) private Integer stars;
    @Column(columnDefinition="TEXT") private String comment;
    @Column(name="created_at") private LocalDateTime createdAt=LocalDateTime.now();
    public Rating(){}
    public static Builder builder(){return new Builder();}
    public static class Builder {
        private Rating r=new Rating();
        public Builder complaintId(Long v){r.complaintId=v;return this;}
        public Builder userId(Long v){r.userId=v;return this;}
        public Builder stars(Integer v){r.stars=v;return this;}
        public Builder comment(String v){r.comment=v;return this;}
        public Rating build(){return r;}
    }
    public Long getId(){return id;} public Long getComplaintId(){return complaintId;} public Long getUserId(){return userId;}
    public Integer getStars(){return stars;} public String getComment(){return comment;}
}
