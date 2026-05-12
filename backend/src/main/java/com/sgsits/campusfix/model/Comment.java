package com.sgsits.campusfix.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="comments")
public class Comment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="complaint_id",nullable=false) private Long complaintId;
    @Column(name="user_id") private Long userId;
    @Column(nullable=false,columnDefinition="TEXT") private String message;
    @Column(name="is_admin") private Boolean adminComment=false;
    @Column(name="created_at") private LocalDateTime createdAt=LocalDateTime.now();

    public Comment(){}
    public static Builder builder(){return new Builder();}
    public static class Builder {
        private Comment c=new Comment();
        public Builder complaintId(Long v){c.complaintId=v;return this;}
        public Builder userId(Long v){c.userId=v;return this;}
        public Builder message(String v){c.message=v;return this;}
        public Builder adminComment(Boolean v){c.adminComment=v;return this;}
        public Comment build(){return c;}
    }
    public Long getId(){return id;} public Long getComplaintId(){return complaintId;} public Long getUserId(){return userId;}
    public String getMessage(){return message;} public Boolean getAdminComment(){return adminComment;} public LocalDateTime getCreatedAt(){return createdAt;}
}
