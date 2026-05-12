package com.sgsits.campusfix.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="notifications")
public class Notification {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="user_id") private Long userId;
    @Column(name="complaint_id") private Long complaintId;
    @Column(nullable=false,length=60) private String type;
    @Column(name="title_en",length=200) private String titleEn;
    @Column(name="title_hi",length=200) private String titleHi;
    @Column(name="message_en",columnDefinition="TEXT") private String messageEn;
    @Column(name="message_hi",columnDefinition="TEXT") private String messageHi;
    @Column(name="is_read") private Boolean read=false;
    @Column(name="created_at") private LocalDateTime createdAt=LocalDateTime.now();

    public Notification(){}
    public static Builder builder(){return new Builder();}
    public static class Builder {
        private Notification n=new Notification();
        public Builder userId(Long v){n.userId=v;return this;}
        public Builder complaintId(Long v){n.complaintId=v;return this;}
        public Builder type(String v){n.type=v;return this;}
        public Builder titleEn(String v){n.titleEn=v;return this;}
        public Builder titleHi(String v){n.titleHi=v;return this;}
        public Builder messageEn(String v){n.messageEn=v;return this;}
        public Builder messageHi(String v){n.messageHi=v;return this;}
        public Notification build(){return n;}
    }
    public Long getId(){return id;} public Long getUserId(){return userId;} public Long getComplaintId(){return complaintId;}
    public String getType(){return type;} public String getTitleEn(){return titleEn;} public String getTitleHi(){return titleHi;}
    public String getMessageEn(){return messageEn;} public String getMessageHi(){return messageHi;} public Boolean getRead(){return read;}
    public void setRead(Boolean v){read=v;}
}
