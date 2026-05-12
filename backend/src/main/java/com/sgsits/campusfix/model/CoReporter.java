package com.sgsits.campusfix.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="co_reporters")
public class CoReporter {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="complaint_id",nullable=false) private Long complaintId;
    @Column(name="user_id",nullable=false) private Long userId;
    @Column(name="reported_at") private LocalDateTime reportedAt=LocalDateTime.now();
    public CoReporter(){}
    public static Builder builder(){return new Builder();}
    public static class Builder {
        private CoReporter c=new CoReporter();
        public Builder complaintId(Long v){c.complaintId=v;return this;}
        public Builder userId(Long v){c.userId=v;return this;}
        public CoReporter build(){return c;}
    }
    public Long getId(){return id;} public Long getComplaintId(){return complaintId;} public Long getUserId(){return userId;}
}
