package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Client;
import com.realestate.sellerfunnel.model.Buyer;
import com.realestate.sellerfunnel.model.Seller;
import com.realestate.sellerfunnel.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PersonalFollowUpService {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonalFollowUpService.class);
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SMSService smsService;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Value("${app.personal.name:Robert}")
    private String personalName;
    
    @Value("${app.personal.phone:(808) 555-0123}")
    private String personalPhone;
    
    @Value("${app.personal.email:robert@realconnect.online}")
    private String personalEmail;
    
    @Value("${app.personal.company:Real Connect}")
    private String companyName;
    
    /**
     * Send personal follow-up for buyer leads
     */
    public void sendBuyerFollowUp(Buyer buyer) {
        try {
            // Find the client record
            Optional<Client> clientOpt = clientRepository.findByEmail(buyer.getEmail());
            if (clientOpt.isEmpty()) {
                logger.warn("No client record found for buyer email: {}", buyer.getEmail());
                return;
            }
            
            Client client = clientOpt.get();
            String firstName = extractFirstName(buyer.getName());
            
            // Send personal email
            String emailSubject = "Welcome to our investor community, " + firstName + "!";
            String emailContent = createBuyerEmailContent(buyer, firstName);
            
            boolean emailSent = emailService.sendEmail(client, emailSubject, emailContent, personalName, personalEmail);
            
            // Send personal SMS
            String smsContent = createBuyerSMSContent(buyer, firstName);
            List<Client> singleClientList = List.of(client);
            smsService.sendBulkSMS(singleClientList, smsContent);
            
            logger.info("Personal follow-up sent to buyer {}: Email={}", buyer.getName(), emailSent);
            
        } catch (Exception e) {
            logger.error("Error sending buyer follow-up for {}: {}", buyer.getName(), e.getMessage());
        }
    }
    
    /**
     * Send personal follow-up for seller leads
     */
    public void sendSellerFollowUp(Seller seller) {
        try {
            // Find the client record
            Optional<Client> clientOpt = clientRepository.findByEmail(seller.getEmail());
            if (clientOpt.isEmpty()) {
                logger.warn("No client record found for seller email: {}", seller.getEmail());
                return;
            }
            
            Client client = clientOpt.get();
            String firstName = extractFirstName(seller.getName());
            
            // Send personal email
            String emailSubject = "Thank you for trusting us with your property, " + firstName;
            String emailContent = createSellerEmailContent(seller, firstName);
            
            boolean emailSent = emailService.sendEmail(client, emailSubject, emailContent, personalName, personalEmail);
            
            // Send personal SMS
            String smsContent = createSellerSMSContent(seller, firstName);
            List<Client> singleClientList = List.of(client);
            smsService.sendBulkSMS(singleClientList, smsContent);
            
            logger.info("Personal follow-up sent to seller {}: Email={}", seller.getName(), emailSent);
            
        } catch (Exception e) {
            logger.error("Error sending seller follow-up for {}: {}", seller.getName(), e.getMessage());
        }
    }
    
    /**
     * Create heartfelt email content for buyer leads
     */
    private String createBuyerEmailContent(Buyer buyer, String firstName) {
        String budgetRange = formatBudgetRange(buyer.getMinBudget(), buyer.getMaxBudget());
        String timeOfDay = getTimeOfDayGreeting();
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Georgia', serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; border-radius: 10px; text-align: center; margin-bottom: 30px; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 10px; margin-bottom: 20px; }
                    .personal-note { background: #fff; border-left: 4px solid #667eea; padding: 20px; margin: 20px 0; font-style: italic; }
                    .contact-info { background: #667eea; color: white; padding: 20px; border-radius: 8px; text-align: center; }
                    .warm-regards { margin-top: 30px; font-style: italic; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Welcome to Our Investor Family!</h1>
                    <p>Your Real Estate Journey Starts Here</p>
                </div>
                
                <div class="content">
                    <p>Good %s, %s!</p>
                    
                    <p>I just received your investment criteria, and I wanted to personally reach out to welcome you to our community. Your interest in finding the perfect investment property with a budget of %s truly excites me!</p>
                    
                    <div class="personal-note">
                        <p>"I believe that real estate isn't just about properties—it's about people, dreams, and building lasting relationships. When you shared your investment goals with us, you became more than just a lead in our system. You became part of our extended family."</p>
                    </div>
                    
                    <p>Here's what I want you to know about working with me:</p>
                    <ul>
                        <li><strong>🏡 Personal Attention:</strong> Your investment goals matter to me personally. I'll hand-select opportunities that match your criteria.</li>
                        <li><strong>📱 Direct Access:</strong> You have my personal phone number below. Call or text me anytime—I mean it!</li>
                        <li><strong>🤝 No Pressure:</strong> I'm here to serve you, not pressure you. We move at your pace, always.</li>
                        <li><strong>💎 Exclusive Opportunities:</strong> You'll get first access to off-market deals before they hit the public market.</li>
                    </ul>
                    
                    <p>%s, I know you have choices when it comes to real estate professionals. The fact that you chose to share your investment criteria with us means the world to me, and I'm committed to earning your trust every single day.</p>
                    
                    <p><strong>What happens next?</strong></p>
                    <p>I'm already reviewing properties that match your criteria. Expect a call from me within the next 24 hours (probably sooner—I'm excited to connect with you!). We'll discuss your goals in detail and start building a relationship that I hope will last for years to come.</p>
                </div>
                
                <div class="contact-info">
                    <h3>Ready to Connect? Here's How to Reach Me:</h3>
                    <p><strong>📞 Call/Text:</strong> %s</p>
                    <p><strong>📧 Email:</strong> %s</p>
                    <p><em>Seriously—use these anytime. I'm here for you!</em></p>
                </div>
                
                <div class="warm-regards">
                    <p>Looking forward to our journey together,</p>
                    <p><strong>%s</strong><br>
                    <em>Your Personal Real Estate Partner</em><br>
                    %s</p>
                    
                    <p><small>P.S. I'll be sending you a quick text message shortly so you have my number saved. That way, reaching out is just a tap away! 😊</small></p>
                </div>
            </body>
            </html>
            """, timeOfDay, firstName, budgetRange, firstName, personalPhone, personalEmail, personalName, companyName);
    }
    
    /**
     * Create heartfelt email content for seller leads
     */
    private String createSellerEmailContent(Seller seller, String firstName) {
        String timeOfDay = getTimeOfDayGreeting();
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Georgia', serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; border-radius: 10px; text-align: center; margin-bottom: 30px; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 10px; margin-bottom: 20px; }
                    .personal-note { background: #fff; border-left: 4px solid #667eea; padding: 20px; margin: 20px 0; font-style: italic; }
                    .contact-info { background: #667eea; color: white; padding: 20px; border-radius: 8px; text-align: center; }
                    .warm-regards { margin-top: 30px; font-style: italic; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Thank You for Trusting Us!</h1>
                    <p>Your Property Deserves Special Attention</p>
                </div>
                
                <div class="content">
                    <p>Good %s, %s!</p>
                    
                    <p>I just received the details about your property at <strong>%s</strong>, and I wanted to personally thank you for considering us to help with this important decision. Selling your home is one of life's biggest decisions, and the fact that you trusted us with your information means everything to me.</p>
                    
                    <div class="personal-note">
                        <p>"I understand that your property isn't just a house—it's your home, filled with memories and meaning. My job isn't just to sell it; it's to honor what it represents to you while helping you achieve your goals."</p>
                    </div>
                    
                    <p>Here's my personal commitment to you, %s:</p>
                    <ul>
                        <li><strong>🏡 Personalized Strategy:</strong> Every property is unique, just like every seller. I'll create a custom plan specifically for your situation.</li>
                        <li><strong>📱 Always Available:</strong> You have my direct contact information. No phone trees, no waiting—just direct access to me.</li>
                        <li><strong>💰 Maximum Value:</strong> I'm committed to getting you the best possible outcome, whether that's the highest price, fastest sale, or whatever matters most to you.</li>
                        <li><strong>🤝 Complete Transparency:</strong> You'll know exactly what's happening every step of the way. No surprises, ever.</li>
                    </ul>
                    
                    <p>Whether you're looking to sell quickly, maximize your return, or explore creative options, I'm here to make this process as smooth and successful as possible. Your goals become my goals.</p>
                    
                    <p><strong>What's next?</strong></p>
                    <p>I'll be calling you within the next 24 hours to discuss your situation in detail and answer any questions you might have. We'll explore all your options together and create a plan that feels right for you.</p>
                </div>
                
                <div class="contact-info">
                    <h3>Let's Connect! Here's How to Reach Me:</h3>
                    <p><strong>📞 Call/Text:</strong> %s</p>
                    <p><strong>📧 Email:</strong> %s</p>
                    <p><em>Don't hesitate to reach out anytime—I'm here to help!</em></p>
                </div>
                
                <div class="warm-regards">
                    <p>Honored to be part of your journey,</p>
                    <p><strong>%s</strong><br>
                    <em>Your Trusted Real Estate Partner</em><br>
                    %s</p>
                    
                    <p><small>P.S. I'll send you a quick text so you have my number handy. Feel free to call or text me anytime—even with small questions! 🏡</small></p>
                </div>
            </body>
            </html>
            """, timeOfDay, firstName, seller.getPropertyAddress() != null ? seller.getPropertyAddress() : "your property", firstName, personalPhone, personalEmail, personalName, companyName);
    }
    
    /**
     * Create warm SMS content for buyer leads
     */
    private String createBuyerSMSContent(Buyer buyer, String firstName) {
        String budgetRange = formatBudgetRange(buyer.getMinBudget(), buyer.getMaxBudget());
        
        return String.format(
            "Hi %s! 👋 This is %s from %s. Just sent you a welcome email about your %s investment criteria. " +
            "I'm personally excited to help you find the perfect property! I'll call you tomorrow to connect. " +
            "Feel free to call/text me anytime at this number! 🏡",
            firstName, personalName, companyName, budgetRange
        );
    }
    
    /**
     * Create warm SMS content for seller leads
     */
    private String createSellerSMSContent(Seller seller, String firstName) {
        return String.format(
            "Hi %s! 👋 This is %s from %s. Thank you for trusting us with your property information. " +
            "I just sent you a detailed email and will call you within 24 hours to discuss your goals. " +
            "Please save this number—I'm here whenever you need me! 🏡",
            firstName, personalName, companyName
        );
    }
    
    // Helper methods
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Friend";
        }
        return fullName.split(" ")[0];
    }
    
    private String formatBudgetRange(Object minBudget, Object maxBudget) {
        if (minBudget == null || maxBudget == null) {
            return "your investment";
        }
        return String.format("$%,.0f - $%,.0f", 
            Double.parseDouble(minBudget.toString()), 
            Double.parseDouble(maxBudget.toString()));
    }
    
    private String getTimeOfDayGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "morning";
        if (hour < 17) return "afternoon";
        return "evening";
    }
}
