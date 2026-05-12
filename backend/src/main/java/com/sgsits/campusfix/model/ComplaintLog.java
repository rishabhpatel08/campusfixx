package com.sgsits.campusfix.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="complaint_logs")
public class ComplaintLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="complaint_id",nullable=false) private Long complaintId;
    @Column(nullable=false,length=120) private String action;
    @Column(name="action_hi",length=160) private String actionHi;
    @Column(name="performed_by") private Long performedBy;
    @Column(columnDefinition="TEXT") private String note;
    @Column(name="created_at") private LocalDateTime createdAt=LocalDateTime.now();

    public ComplaintLog(){}
    public static Builder builder(){return new Builder();}
    public static class Builder {
        private ComplaintLog l=new ComplaintLog();
        public Builder complaintId(Long v){l.complaintId=v;return this;}
        public Builder action(String v){l.action=v;return this;}
        public Builder actionHi(String v){l.actionHi=v;return this;}
        public Builder performedBy(Long v){l.performedBy=v;return this;}
        public Builder note(String v){l.note=v;return this;}
        public ComplaintLog build(){return l;}
    }
    public Long getId(){return id;} public Long getComplaintId(){return complaintId;} public String getAction(){return action;}
    public String getActionHi(){return actionHi;} public Long getPerformedBy(){return performedBy;} public String getNote(){return note;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
