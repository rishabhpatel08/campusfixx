package com.sgsits.campusfix.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="otp_tokens")
public class OtpToken {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="user_id") private Long userId;
    @Column(nullable=false,length=140) private String email;
    @Column(name="token_hash",nullable=false,length=255) private String tokenHash;
    @Column(nullable=false,length=30) private String purpose="login";
    @Column(name="expires_at",nullable=false) private LocalDateTime expiresAt;
    private Boolean used=false;
    @Column(name="created_at") private LocalDateTime createdAt=LocalDateTime.now();

    public OtpToken(){}
    public static Builder builder(){return new Builder();}
    public static class Builder {
        private OtpToken o=new OtpToken();
        public Builder userId(Long v){o.userId=v;return this;}
        public Builder email(String v){o.email=v;return this;}
        public Builder tokenHash(String v){o.tokenHash=v;return this;}
        public Builder purpose(String v){o.purpose=v;return this;}
        public Builder expiresAt(LocalDateTime v){o.expiresAt=v;return this;}
        public Builder used(Boolean v){o.used=v;return this;}
        public OtpToken build(){return o;}
    }
    public Long getId(){return id;} public Long getUserId(){return userId;} public String getEmail(){return email;}
    public String getTokenHash(){return tokenHash;} public String getPurpose(){return purpose;}
    public LocalDateTime getExpiresAt(){return expiresAt;} public Boolean getUsed(){return used;}
    public void setUsed(Boolean v){used=v;} public void setExpiresAt(LocalDateTime v){expiresAt=v;}
}
