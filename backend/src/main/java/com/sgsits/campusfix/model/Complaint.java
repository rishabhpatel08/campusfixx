package com.sgsits.campusfix.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="complaints")
public class Complaint {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="complaint_no",unique=true,nullable=false,length=20) private String complaintNo;
    @Column(nullable=false,length=200) private String title;
    @Column(nullable=false,columnDefinition="TEXT") private String description;
    @Column(nullable=false,length=50) private String category;
    @Column(length=80) private String subcategory;
    @Column(nullable=false,length=20) private String priority="medium";
    @Column(nullable=false,length=30) private String status="registered";
    @Column(name="location_id") private Long locationId;
    @Column(name="location_text",length=255) private String locationText;
    @Column(name="gps_lat",precision=10,scale=7) private BigDecimal gpsLat;
    @Column(name="gps_lng",precision=10,scale=7) private BigDecimal gpsLng;
    @Column(name="gps_verified") private Boolean gpsVerified=false;
    @Column(name="photo_url",length=500) private String photoUrl;
    @Column(name="ai_category",length=80) private String aiCategory;
    @Column(name="ai_confidence",precision=5,scale=2) private BigDecimal aiConfidence;
    @Column(name="reporter_id") private Long reporterId;
    @Column(name="assigned_to") private Long assignedTo;
    @Column(name="department_id") private Long departmentId;
    @Column(name="importance_score") private Integer importanceScore=0;
    @Column(name="reporter_count") private Integer reporterCount=1;
    @Column(name="is_escalated") private Boolean escalated=false;
    @Column(name="escalated_at") private LocalDateTime escalatedAt;
    @Column(name="escalation_reason",columnDefinition="TEXT") private String escalationReason;
    @Column(name="remind_count") private Integer remindCount=0;
    @Column(name="last_reminded_at") private LocalDateTime lastRemindedAt;
    @Column(name="closure_otp_hash",length=255) private String closureOtpHash;
    @Column(name="closure_otp_sent_at") private LocalDateTime closureOtpSentAt;
    @Column(name="closure_confirmed") private Boolean closureConfirmed=false;
    @Column(name="rejection_reason",columnDefinition="TEXT") private String rejectionReason;
    @Column(name="resolved_at") private LocalDateTime resolvedAt;
    @Column(name="sla_deadline") private LocalDateTime slaDeadline;
    @Column(name="sla_breached") private Boolean slaBreached=false;
    @Column(name="proof_url",length=500) private String proofUrl;
    @Column(name="proof_uploaded_at") private LocalDateTime proofUploadedAt;
    @Column(name="reopen_count") private Integer reopenCount=0;
    @Column(name="auto_routed") private Boolean autoRouted=false;
    @Column(name="created_at") private LocalDateTime createdAt=LocalDateTime.now();
    @Column(name="updated_at") private LocalDateTime updatedAt=LocalDateTime.now();
    @PreUpdate public void onUpdate(){ updatedAt=LocalDateTime.now(); }

    public Complaint(){}

    // Builder pattern
    public static Builder builder(){ return new Builder(); }
    public static class Builder {
        private Complaint c = new Complaint();
        public Builder complaintNo(String v){c.complaintNo=v;return this;}
        public Builder title(String v){c.title=v;return this;}
        public Builder description(String v){c.description=v;return this;}
        public Builder category(String v){c.category=v;return this;}
        public Builder subcategory(String v){c.subcategory=v;return this;}
        public Builder priority(String v){c.priority=v;return this;}
        public Builder status(String v){c.status=v;return this;}
        public Builder locationId(Long v){c.locationId=v;return this;}
        public Builder locationText(String v){c.locationText=v;return this;}
        public Builder gpsLat(BigDecimal v){c.gpsLat=v;return this;}
        public Builder gpsLng(BigDecimal v){c.gpsLng=v;return this;}
        public Builder gpsVerified(Boolean v){c.gpsVerified=v;return this;}
        public Builder photoUrl(String v){c.photoUrl=v;return this;}
        public Builder aiCategory(String v){c.aiCategory=v;return this;}
        public Builder aiConfidence(BigDecimal v){c.aiConfidence=v;return this;}
        public Builder reporterId(Long v){c.reporterId=v;return this;}
        public Builder assignedTo(Long v){c.assignedTo=v;return this;}
        public Builder departmentId(Long v){c.departmentId=v;return this;}
        public Builder importanceScore(Integer v){c.importanceScore=v;return this;}
        public Builder reporterCount(Integer v){c.reporterCount=v;return this;}
        public Builder escalated(Boolean v){c.escalated=v;return this;}
        public Builder remindCount(Integer v){c.remindCount=v;return this;}
        public Builder slaDeadline(LocalDateTime v){c.slaDeadline=v;return this;}
        public Builder autoRouted(Boolean v){c.autoRouted=v;return this;}
        public Complaint build(){return c;}
    }

    public void recalculateScore(){
        int s=switch(priority==null?"medium":priority){case"critical"->40;case"high"->28;case"medium"->16;default->5;};
        s+=Math.min((reporterCount==null?1:reporterCount)*5,30);
        if(Boolean.TRUE.equals(escalated))s+=20;
        if(createdAt!=null){long h=java.time.Duration.between(createdAt,LocalDateTime.now()).toHours();s+=Math.min((int)(h/6),10);}
        importanceScore=Math.min(s,100);
    }
    public boolean isSlaBreached(){if(slaDeadline==null)return false;return LocalDateTime.now().isAfter(slaDeadline)&&!isTerminal();}
    public boolean isTerminal(){return "resolved".equals(status)||"closed".equals(status)||"rejected".equals(status);}

    public Long getId(){return id;} public String getComplaintNo(){return complaintNo;} public String getTitle(){return title;}
    public String getDescription(){return description;} public String getCategory(){return category;} public String getSubcategory(){return subcategory;}
    public String getPriority(){return priority;} public String getStatus(){return status;} public Long getLocationId(){return locationId;}
    public String getLocationText(){return locationText;} public BigDecimal getGpsLat(){return gpsLat;} public BigDecimal getGpsLng(){return gpsLng;}
    public Boolean getGpsVerified(){return gpsVerified;} public String getPhotoUrl(){return photoUrl;} public String getAiCategory(){return aiCategory;}
    public BigDecimal getAiConfidence(){return aiConfidence;} public Long getReporterId(){return reporterId;} public Long getAssignedTo(){return assignedTo;}
    public Long getDepartmentId(){return departmentId;} public Integer getImportanceScore(){return importanceScore;} public Integer getReporterCount(){return reporterCount;}
    public Boolean getEscalated(){return escalated;} public LocalDateTime getEscalatedAt(){return escalatedAt;} public String getEscalationReason(){return escalationReason;}
    public Integer getRemindCount(){return remindCount;} public LocalDateTime getLastRemindedAt(){return lastRemindedAt;} public String getClosureOtpHash(){return closureOtpHash;}
    public LocalDateTime getClosureOtpSentAt(){return closureOtpSentAt;} public Boolean getClosureConfirmed(){return closureConfirmed;} public String getRejectionReason(){return rejectionReason;}
    public LocalDateTime getResolvedAt(){return resolvedAt;} public LocalDateTime getSlaDeadline(){return slaDeadline;} public Boolean getSlaBreached(){return slaBreached;}
    public String getProofUrl(){return proofUrl;} public LocalDateTime getProofUploadedAt(){return proofUploadedAt;} public Integer getReopenCount(){return reopenCount;}
    public Boolean getAutoRouted(){return autoRouted;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
    public void setId(Long v){id=v;} public void setComplaintNo(String v){complaintNo=v;} public void setTitle(String v){title=v;}
    public void setDescription(String v){description=v;} public void setCategory(String v){category=v;} public void setSubcategory(String v){subcategory=v;}
    public void setPriority(String v){priority=v;} public void setStatus(String v){status=v;} public void setLocationId(Long v){locationId=v;}
    public void setLocationText(String v){locationText=v;} public void setGpsLat(BigDecimal v){gpsLat=v;} public void setGpsLng(BigDecimal v){gpsLng=v;}
    public void setGpsVerified(Boolean v){gpsVerified=v;} public void setPhotoUrl(String v){photoUrl=v;} public void setAiCategory(String v){aiCategory=v;}
    public void setAiConfidence(BigDecimal v){aiConfidence=v;} public void setReporterId(Long v){reporterId=v;} public void setAssignedTo(Long v){assignedTo=v;}
    public void setDepartmentId(Long v){departmentId=v;} public void setImportanceScore(Integer v){importanceScore=v;} public void setReporterCount(Integer v){reporterCount=v;}
    public void setEscalated(Boolean v){escalated=v;} public void setEscalatedAt(LocalDateTime v){escalatedAt=v;} public void setEscalationReason(String v){escalationReason=v;}
    public void setRemindCount(Integer v){remindCount=v;} public void setLastRemindedAt(LocalDateTime v){lastRemindedAt=v;} public void setClosureOtpHash(String v){closureOtpHash=v;}
    public void setClosureOtpSentAt(LocalDateTime v){closureOtpSentAt=v;} public void setClosureConfirmed(Boolean v){closureConfirmed=v;} public void setRejectionReason(String v){rejectionReason=v;}
    public void setResolvedAt(LocalDateTime v){resolvedAt=v;} public void setSlaDeadline(LocalDateTime v){slaDeadline=v;} public void setSlaBreached(Boolean v){slaBreached=v;}
    public void setProofUrl(String v){proofUrl=v;} public void setProofUploadedAt(LocalDateTime v){proofUploadedAt=v;} public void setReopenCount(Integer v){reopenCount=v;}
    public void setAutoRouted(Boolean v){autoRouted=v;} public void setCreatedAt(LocalDateTime v){createdAt=v;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
