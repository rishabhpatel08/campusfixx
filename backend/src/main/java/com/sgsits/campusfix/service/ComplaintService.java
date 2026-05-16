package com.sgsits.campusfix.service;

import com.sgsits.campusfix.model.*;
import com.sgsits.campusfix.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ComplaintService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ComplaintService.class);

    private final ComplaintRepository    complaintRepo;
    private final CoReporterRepository   coRepo;
    private final ComplaintLogRepository logRepo;
    private final CommentRepository      commentRepo;
    private final RatingRepository       ratingRepo;
    private final DepartmentRepository   deptRepo;
    private final OtpTokenRepository     otpRepo;
    private final NotificationRepository notifRepo;
    private final UserRepository         userRepo;
    private final AuthService            auth;
    private final BCryptPasswordEncoder  encoder;
    private final JavaMailSender         mailer;

    @Value("${app.upload.dir}")                        private String uploadDir;
    @Value("${app.upload.base-url}")                   private String baseUrl;
    @Value("${app.mail.from}")                         private String mailFrom;
    @Value("${app.escalation.reporter-threshold:5}")   private int escThreshold;
    @Value("${app.escalation.remind-auto-escalate:3}") private int remindEscThreshold;
    @Value("${app.points.submit:10}")                  private int ptsSubmit;
    @Value("${app.points.photo:5}")                    private int ptsPhoto;
    @Value("${app.points.gps:5}")                      private int ptsGps;
    @Value("${app.points.comment:2}")                  private int ptsComment;
    @Value("${app.points.rate:5}")                     private int ptsRate;
    @Value("${app.points.co-report:5}")                private int ptsCo;
    @Value("${app.points.closure-confirm:5}")          private int ptsClosure;
    @Value("${app.points.reminder:1}")                 private int ptsReminder;
    @Value("${app.points.faculty-bonus:5}")            private int ptsFacultyBonus;
    @Value("${app.whatsapp.wati.api-url:https://live-server-XXXX.wati.io}") private String watiUrl;
    @Value("${app.whatsapp.wati.access-token:YOUR_TOKEN}")                  private String watiToken;
    @Value("${app.whatsapp.staff.electrical:919876543210}")  private String waElectrical;
    @Value("${app.whatsapp.staff.plumbing:919876543211}")    private String waPlumbing;
    @Value("${app.whatsapp.staff.technical:919876543212}")   private String waTechnical;
    @Value("${app.whatsapp.staff.sanitation:919876543214}")  private String waSanitation;
    @Value("${app.whatsapp.staff.other:919000000001}")       private String waOther;

    private static final Map<String,Long> CAT_DEPT = Map.of(
        "electrical",1L,"plumbing",2L,"technical",3L,"furniture",4L,"sanitation",5L,"other",6L);
    // AUTO-ROUTE: directly WhatsApp technician. Others go to admin.
    private static final Set<String> AUTO_ROUTE = Set.of("electrical","plumbing","technical","sanitation");

    // READ
    public List<Complaint> all(String cat,String stat,String pri){ return complaintRepo.findFiltered(cat,stat,pri); }
    public List<Complaint> mine(){ return complaintRepo.findByReporterIdOrderByCreatedAtDesc(auth.myId()); }
    public Complaint get(Long id){ return complaintRepo.findById(id).orElseThrow(()->new RuntimeException("Complaint not found: "+id)); }
    public Map<String,Object> stats(){ List<Map<String,Object>> r=complaintRepo.getStats(); return r.isEmpty()?Map.of():r.get(0); }
    public List<Map<String,Object>> leaderboard(){ return complaintRepo.getLeaderboard(); }
    public List<Map<String,Object>> deptLoad(){ return complaintRepo.getDeptLoad(); }
    public List<Map<String,Object>> hotspots(){ return complaintRepo.getHotspots(); }
    public List<ComplaintLog> logs(Long id){ return logRepo.findByComplaintIdOrderByCreatedAtAsc(id); }
    public List<Comment> comments(Long id){ return commentRepo.findByComplaintIdOrderByCreatedAtAsc(id); }

    // SUBMIT
    @Transactional
    public Map<String,Object> submit(String title,String description,String category,
        String subcategory,String priority,Long locationId,String locationText,
        Double lat,Double lng,String aiCat,Double aiConf,MultipartFile photo){

        User reporter=auth.me();
        String photoUrl=(photo!=null&&!photo.isEmpty())?saveFile(photo):null;

        // Backend classify if frontend didn't send aiCategory
        String fCat=(aiCat!=null&&!aiCat.isBlank())?aiCat:classifyCategory(title+" "+description);
        Double fConf=(aiConf!=null)?aiConf:(fCat!=null?75.0:null);
        if((category==null||"other".equals(category))&&fCat!=null) category=fCat;

        var dup=findDuplicate(category,locationId,lat,lng);
        if(dup.isPresent()){
            addCoReporter(dup.get().getId(),reporter);
            return Map.of("duplicate",true,"complaintNo",dup.get().getComplaintNo(),
                "id",dup.get().getId(),"reporters",dup.get().getReporterCount(),"points",ptsCo,
                "message","Similar complaint already filed. Added as co-reporter. Priority boosted.");
        }

        String pri=(priority!=null)?priority:"medium";
        if("faculty".equals(reporter.getRole())&&!"critical".equals(pri)) pri="high";

        Long deptId=CAT_DEPT.getOrDefault(category,6L);
        Department dept=deptRepo.findById(deptId).orElse(null);
        int slaH=(dept!=null&&dept.getSlaHours()!=null)?dept.getSlaHours():48;
        boolean autoRoute=AUTO_ROUTE.contains(category);

        String code=generateCode(category);
        Complaint c=Complaint.builder()
            .complaintNo(code).title(title).description(description)
            .category(category).subcategory(subcategory).priority(pri).status("registered")
            .locationId(locationId).locationText(locationText)
            .gpsLat(lat!=null?BigDecimal.valueOf(lat):null)
            .gpsLng(lng!=null?BigDecimal.valueOf(lng):null)
            .gpsVerified(lat!=null).photoUrl(photoUrl)
            .aiCategory(fCat).aiConfidence(fConf!=null?BigDecimal.valueOf(fConf):null)
            .reporterId(reporter.getId()).departmentId(deptId)
            .reporterCount(1).escalated(false).remindCount(0)
            .slaDeadline(LocalDateTime.now().plusHours(slaH))
            .autoRouted(autoRoute).build();
        c.recalculateScore();
        Complaint saved=complaintRepo.save(c);

        addLog(saved.getId(),"Complaint Registered","शिकायत दर्ज",reporter.getId(),
            "Filed by "+reporter.getRole()+(autoRoute?" — Auto-routed via WhatsApp":" — Admin routing required"));

        int pts=ptsSubmit+(photoUrl!=null?ptsPhoto:0)+(lat!=null?ptsGps:0)
               +("faculty".equals(reporter.getRole())?ptsFacultyBonus:0);
        auth.addPoints(reporter.getId(),pts);

        if(autoRoute){ sendStaffWhatsApp(saved); }
        else{ notifyAdmins(saved,reporter); }

        return Map.of("duplicate",false,"complaintNo",code,"id",saved.getId(),
            "points",pts,"autoRouted",autoRoute,"message","Complaint filed! Reference: "+code);
    }

    // STATUS UPDATE — FIX: actor passed explicitly, no auth.me() called here
    @Transactional
    public Complaint updateStatus(Long id,String newStatus,String reason,User actor){
        Complaint c=get(id); String old=c.getStatus();
        c.setStatus(newStatus);
        if("resolved".equals(newStatus)){
            String rawOtp=String.format("%06d",new Random().nextInt(999999));
            c.setClosureOtpHash(encoder.encode(rawOtp));
            c.setClosureOtpSentAt(LocalDateTime.now());
            c.setResolvedAt(LocalDateTime.now());
            sendClosureOtp(c,rawOtp);
        }
        if("rejected".equals(newStatus)) c.setRejectionReason(reason);
        c.recalculateScore(); Complaint saved=complaintRepo.save(c);
        if(!old.equals(newStatus)){
            Long actorId=(actor!=null)?actor.getId():c.getReporterId();
            addLog(id,"Status → "+cap(newStatus),"स्थिति → "+statusHi(newStatus),actorId,reason);
            notifyReporter(saved);
        }
        return saved;
    }
    // Overload for authenticated controllers (uses auth.me())
    @Transactional
    public Complaint updateStatus(Long id,String newStatus,String reason){ return updateStatus(id,newStatus,reason,auth.me()); }

    // UPLOAD REPAIR PROOF
    @Transactional
    public Map<String,Object> uploadProof(Long id,MultipartFile proofFile){
        User actor=auth.me();
        if(!List.of("staff","faculty","admin","super_admin").contains(actor.getRole()))
            throw new RuntimeException("Only staff or admin can upload proof.");
        Complaint c=get(id);
        if("closed".equals(c.getStatus())) throw new RuntimeException("Complaint already closed.");
        String url=saveFile(proofFile);
        if(url==null) throw new RuntimeException("File upload failed.");
        c.setProofUrl(url); c.setProofUploadedAt(LocalDateTime.now());
        complaintRepo.save(c);
        addLog(id,"Repair Proof Uploaded","मरम्मत प्रमाण अपलोड",actor.getId(),"Proof: "+url);
        notifyReporter(c);
        return Map.of("message","Proof uploaded. Reporter notified to verify.","proofUrl",url);
    }

    // REOPEN
    @Transactional
    public Map<String,Object> reopenComplaint(Long id,String reason){
        User u=auth.me(); Complaint c=get(id);
        if(!u.getId().equals(c.getReporterId())) throw new RuntimeException("Only original reporter can reopen.");
        if(!"resolved".equals(c.getStatus())) throw new RuntimeException("Only resolved complaints can be reopened.");
        int reopens=(c.getReopenCount()==null?0:c.getReopenCount())+1;
        if(reopens>3) throw new RuntimeException("Max 3 reopens allowed. Contact admin directly.");
        c.setStatus("in_progress"); c.setReopenCount(reopens);
        c.setClosureOtpHash(null); c.setClosureOtpSentAt(null); c.setResolvedAt(null);
        c.setSlaDeadline(LocalDateTime.now().plusHours(24));
        complaintRepo.save(c);
        addLog(id,"Reopened by Reporter (#"+reopens+")","रिपोर्टर ने पुनः खोला (#"+reopens+")",
            u.getId(),"Reason: "+(reason!=null?reason:"Not satisfied"));
        notifyAdmins(c,u);
        return Map.of("message","Complaint reopened. Admin notified.","reopenCount",reopens,"status","in_progress");
    }

    // CONFIRM CLOSURE (OTP)
    @Transactional
    public Map<String,Object> confirmClosure(Long id,String otp){
        Complaint c=get(id); User u=auth.me();
        if(!u.getId().equals(c.getReporterId())) throw new RuntimeException("Only original reporter can confirm closure.");
        if(c.getClosureOtpHash()==null) throw new RuntimeException("No OTP pending.");
        if(c.getClosureOtpSentAt().isBefore(LocalDateTime.now().minusMinutes(15))) throw new RuntimeException("OTP expired.");
        if(!encoder.matches(otp.trim(),c.getClosureOtpHash())) throw new RuntimeException("Invalid OTP.");
        c.setClosureConfirmed(true); c.setStatus("closed"); complaintRepo.save(c);
        addLog(id,"Closure Confirmed (OTP)","OTP से बंद",u.getId(),"Reporter confirmed via OTP");
        auth.addPoints(u.getId(),ptsClosure);
        return Map.of("message","Complaint closed. +"+ptsClosure+" points!","points",ptsClosure);
    }

    // REMIND
    @Transactional
    public Map<String,Object> sendReminder(Long id){
        Complaint c=get(id); User u=auth.me();
        if(c.isTerminal()) throw new RuntimeException("Already resolved/closed.");
        c.setRemindCount((c.getRemindCount()==null?0:c.getRemindCount())+1);
        c.setLastRemindedAt(LocalDateTime.now());
        boolean autoEsc=false;
        if(c.getRemindCount()>=remindEscThreshold&&!Boolean.TRUE.equals(c.getEscalated())){
            c.setEscalated(true); c.setEscalatedAt(LocalDateTime.now());
            c.setEscalationReason("Auto-escalated after "+remindEscThreshold+" reminders");
            addLog(id,"Auto-Escalated","स्वतः एस्केलेट",u.getId(),null); autoEsc=true;
        }
        complaintRepo.save(c); auth.addPoints(u.getId(),ptsReminder); notifyAdmins(c,u);
        return Map.of("message","Reminder sent.","remindCount",c.getRemindCount(),"autoEscalated",autoEsc);
    }

    // ESCALATE
    @Transactional
    public Complaint escalate(Long id,String reason){
        Complaint c=get(id); c.setEscalated(true); c.setEscalatedAt(LocalDateTime.now()); c.setEscalationReason(reason);
        c.recalculateScore(); Complaint saved=complaintRepo.save(c);
        addLog(id,"Escalated","एस्केलेट",auth.myId(),reason); notifyAdmins(saved,auth.me()); return saved;
    }

    // ASSIGN — sends WhatsApp to assigned staff member
    @Transactional
    public Complaint assign(Long id,Long staffId){
        Complaint c=get(id); c.setAssignedTo(staffId);
        if("registered".equals(c.getStatus())) c.setStatus("forwarded");
        addLog(id,"Forwarded / Assigned","अग्रेषित/आवंटित",auth.myId(),null);
        Complaint saved=complaintRepo.save(c);
        userRepo.findById(staffId).ifPresent(staff->{
            if(staff.getWhatsappNumber()!=null&&!staff.getWhatsappNumber().isBlank()){
                sendWhatsAppToNumber(staff.getWhatsappNumber(),saved);
                addLog(id,"WhatsApp Sent to Staff","WhatsApp भेजा",auth.myId(),"To: "+staff.getName());
            } else {
                log.warn("Staff {} has no WhatsApp number — admin must notify manually",staff.getName());
            }
        });
        return saved;
    }

    // CO-REPORTER
    @Transactional
    public void addCoReporter(Long id,User user){
        if(coRepo.existsByComplaintIdAndUserId(id,user.getId())) return;
        coRepo.save(CoReporter.builder().complaintId(id).userId(user.getId()).build());
        Complaint c=get(id); c.setReporterCount(c.getReporterCount()+1);
        if(c.getReporterCount()>=escThreshold&&!Boolean.TRUE.equals(c.getEscalated())){
            c.setEscalated(true); c.setEscalatedAt(LocalDateTime.now());
            c.setEscalationReason("Auto-escalated: "+escThreshold+"+ reporters");
            addLog(id,"Auto-Escalated: "+escThreshold+"+ reporters","स्वतः एस्केलेट",user.getId(),null);
        }
        c.recalculateScore(); complaintRepo.save(c); auth.addPoints(user.getId(),ptsCo);
    }

    // COMMENT
    @Transactional
    public Comment postComment(Long id,String message){
        User u=auth.me(); boolean isAdmin=!List.of("student").contains(u.getRole());
        Comment saved=commentRepo.save(Comment.builder().complaintId(id).userId(u.getId()).message(message).adminComment(isAdmin).build());
        auth.addPoints(u.getId(),ptsComment); return saved;
    }

    // RATE
    @Transactional
    public Map<String,Object> rate(Long id,int stars,String comment){
        User u=auth.me();
        if(ratingRepo.existsByComplaintIdAndUserId(id,u.getId())) throw new RuntimeException("Already rated.");
        ratingRepo.save(Rating.builder().complaintId(id).userId(u.getId()).stars(stars).comment(comment).build());
        auth.addPoints(u.getId(),ptsRate);
        return Map.of("message","Rating submitted! +"+ptsRate+" pts","points",ptsRate);
    }

    // BACKEND AI CLASSIFIER (mirrors frontend simulateAI)
    private String classifyCategory(String text){
        if(text==null||text.isBlank()) return null;
        String t=text.toLowerCase();
        Map<String,List<String>> rules=new LinkedHashMap<>();
        rules.put("electrical",List.of("wire","wiring","switch","light","fan","power","spark","sparking","ac","electricity","short circuit","mcb","breaker","socket","plug","voltage","bulb","tube light","inverter","generator"));
        rules.put("plumbing",List.of("water","leak","pipe","drain","tap","flood","waterlog","blockage","sewage","toilet","washroom","basin","bathroom","flush","geyser","leakage"));
        rules.put("technical",List.of("wifi","computer","projector","server","network","screen","laptop","printer","internet","system","software","hardware","pc","monitor","keyboard","mouse","router","access point"));
        rules.put("sanitation",List.of("garbage","trash","dirty","waste","smell","cleaning","sweeping","dustbin","hygiene","pest","rat","cockroach","mosquito"));
        rules.put("furniture",List.of("chair","table","window","door","bench","board","blackboard","whiteboard","desk","shelf","cupboard","rack","broken","damaged","ceiling","roof","wall","floor"));
        for(var e:rules.entrySet()) for(String kw:e.getValue()) if(t.contains(kw)) return e.getKey();
        return null;
    }

    // WHATSAPP — auto-route to category default number
    @Async void sendStaffWhatsApp(Complaint c){
        String n=switch(c.getCategory()){
            case"electrical"->waElectrical; case"plumbing"->waPlumbing;
            case"technical"->waTechnical;   case"sanitation"->waSanitation;
            default->waOther;
        };
        sendWhatsAppToNumber(n,c);
    }

    // WHATSAPP — send to specific normalized number
    @Async void sendWhatsAppToNumber(String number,Complaint c){
        String norm=number.replaceAll("[^0-9]","");
        if(norm.length()==10) norm="91"+norm;
        String msg=String.format(
            "🔔 CampusFix — New Complaint\n\nRef: %s\nCategory: %s\nPriority: %s\nLocation: %s\nIssue: %s\n\n" +
            "Reply *YES %s* to accept\nReply *DONE %s* when resolved\n(Include Ref# to update correct ticket)\n\n— CampusFix | SGSITS",
            c.getComplaintNo(),c.getCategory(),c.getPriority(),
            c.getLocationText()!=null?c.getLocationText():"See dashboard",c.getTitle(),
            c.getComplaintNo(),c.getComplaintNo());

        if(watiToken.equals("YOUR_TOKEN")||watiUrl.contains("XXXX")){
            log.info("📱 [DEV MODE — WhatsApp] To:{} | {}",norm,msg.replace("\n"," | "));
            return;
        }
        try{
            String body=String.format("{\"whatsappNumber\":\"%s\",\"message\":\"%s\"}",
                norm,msg.replace("\"","'").replace("\n","\\n"));
            HttpResponse<String> resp=HttpClient.newHttpClient().send(
                HttpRequest.newBuilder().uri(URI.create(watiUrl+"/api/v1/sendSessionMessage/"+norm))
                    .header("Authorization","Bearer "+watiToken)
                    .header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
            log.info("WhatsApp sent to {} for {} — status:{}",norm,c.getComplaintNo(),resp.statusCode());
        }catch(Exception e){ log.warn("WhatsApp failed for {}: {}",c.getComplaintNo(),e.getMessage()); }
    }

    // HELPERS
    private Optional<Complaint> findDuplicate(String cat,Long locId,Double lat,Double lng){
        LocalDateTime since=LocalDateTime.now().minusHours(24);
        if(locId!=null) return complaintRepo.findDupByLocation(cat,locId,since);
        if(lat!=null&&lng!=null){
            BigDecimal d=BigDecimal.valueOf(0.0005),la=BigDecimal.valueOf(lat),ln=BigDecimal.valueOf(lng);
            return complaintRepo.findDupByGps(cat,la.subtract(d),la.add(d),ln.subtract(d),ln.add(d),since);
        }
        return Optional.empty();
    }

    private String generateCode(String cat){
        String pfx=switch(cat){case"electrical"->"EL";case"plumbing"->"PL";case"technical"->"IT";case"furniture"->"FU";case"sanitation"->"SN";default->"OT";};
        return String.format("#%s-%d",pfx,1001+complaintRepo.countByCategory(cat));
    }

    private void addLog(Long cId,String a,String ah,Long uid,String note){
        logRepo.save(ComplaintLog.builder().complaintId(cId).action(a).actionHi(ah).performedBy(uid).note(note).build());
    }

    String saveFile(MultipartFile f){
        try{Path dir=Paths.get(uploadDir);Files.createDirectories(dir);
            String name=UUID.randomUUID()+"_"+Objects.requireNonNull(f.getOriginalFilename()).replaceAll("[^a-zA-Z0-9._-]","_");
            Files.write(dir.resolve(name),f.getBytes());return baseUrl+"/"+name;
        }catch(IOException e){log.error("File save: {}",e.getMessage());return null;}
    }

    private String cap(String s){return s==null?"":s.substring(0,1).toUpperCase()+s.substring(1);}
    private String statusHi(String s){return switch(s){case"registered"->"दर्ज";case"forwarded"->"अग्रेषित";case"in_progress"->"प्रगति में";case"resolved"->"हल";case"closed"->"बंद";case"rejected"->"अस्वीकृत";default->s;};}

    @Async void notifyAdmins(Complaint c,User r){
        userRepo.findByRoleIn(List.of("admin","super_admin")).forEach(a->
            notifRepo.save(Notification.builder().userId(a.getId()).complaintId(c.getId()).type("new_complaint")
                .titleEn("New: "+c.getComplaintNo()).titleHi("नई शिकायत: "+c.getComplaintNo())
                .messageEn(r.getName()+" filed: "+c.getTitle()).messageHi(r.getName()+" ने दर्ज किया").build()));
    }

    @Async void notifyReporter(Complaint c){
        if(c.getReporterId()==null) return;
        String m=switch(c.getStatus()){
            case"resolved"->"Complaint "+c.getComplaintNo()+" resolved!"+(c.getProofUrl()!=null?" Proof uploaded.":"")+" Enter OTP to close, or Reopen if not satisfied.";
            case"in_progress"->"Work started on "+c.getComplaintNo();
            case"rejected"->"Complaint "+c.getComplaintNo()+" rejected.";
            default->"Status updated: "+c.getComplaintNo();};
        notifRepo.save(Notification.builder().userId(c.getReporterId()).complaintId(c.getId()).type("status_change")
            .titleEn("Status Updated").titleHi("स्थिति अपडेट").messageEn(m).messageHi(m).build());
    }

    @Async void sendClosureOtp(Complaint c,String otp){
        userRepo.findById(c.getReporterId()).ifPresent(u->{
            try{SimpleMailMessage msg=new SimpleMailMessage();msg.setFrom(mailFrom);msg.setTo(u.getEmail());
                msg.setSubject("CampusFix — Complaint Resolved: "+c.getComplaintNo());
                msg.setText("Dear "+u.getName()+",\n\nYour complaint "+c.getComplaintNo()+" has been resolved.\n"+
                    (c.getProofUrl()!=null?"Repair proof has been uploaded — check the complaint details.\n":"")+
                    "\nOTP to confirm closure: "+otp+"\n\n✅ Satisfied? Enter OTP to close (+"+ptsClosure+" points).\n"+
                    "❌ Not satisfied? Click 'Reopen' in the app.\n\nOTP valid 15 min.\n\n— CampusFix | SGSITS Indore");
                mailer.send(msg);
            }catch(Exception e){log.warn("Closure OTP email failed for {}: {}",c.getComplaintNo(),e.getMessage());}
        });
    }
}
