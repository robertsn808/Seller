# Real Estate Connect CRM

A comprehensive Spring Boot web application designed as a lead generation and management system for real estate professionals. The application serves as a funnel for connecting buyers and sellers, allowing you to capture leads, manage campaigns, and bird-dog deals effectively.

## 🎯 Key Features

### Lead Management
- **Buyer Lead Collection**: Comprehensive forms capturing budget, preferences, and requirements
- **Seller Lead Collection**: Detailed property listing form for sellers
- **Creative Financing Support**: Seller financing, lease-to-own, rent-to-own options
- **Property Photo Upload**: Support for up to 10 photos, 10MB each
- **Lead Analytics**: Track sources, conversion rates, and performance

### Marketing Campaign Management
- **Multi-Platform Campaigns**: Facebook/Instagram, Google Ads integration
- **Campaign Types**: Lead generation, retargeting, brand awareness
- **Budget Management**: Track spending and ROI across platforms
- **Performance Analytics**: CTR, conversion rates, cost per lead
- **Content Generation**: AI-powered campaign content creation

### Professional Email System
- **DKIM Authentication**: Improved deliverability with domain authentication
- **Template Engine**: Professional email templates with personalization
- **Bulk Campaigns**: Rate-limited sending for large contact lists
- **Email Tracking**: Open rates, click-through tracking, bounce handling
- **Compliance Features**: Unsubscribe links, opt-in management

### Admin Dashboard
- **Lead Overview**: Recent buyers and sellers with contact information
- **Campaign Management**: Create, edit, and monitor marketing campaigns
- **Email Campaigns**: Compose and send professional email campaigns
- **Analytics Dashboard**: Performance metrics and insights
- **API Configuration**: Setup for Facebook, Google Ads, and email services

## ⚡ Quick Start

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Docker (optional, for PostgreSQL)

### Running the Application

#### Development Mode (H2 Database)
```bash
# Clone the repository
git clone <repository-url>
cd Seller

# Run the application
mvn spring-boot:run

# Access the application
open http://localhost:8080
```

#### Production Mode (PostgreSQL)
```bash
# Start PostgreSQL
docker-compose up -d

# Run with PostgreSQL profile
mvn spring-boot:run -Dspring.profiles.active=postgres
```

### Default Access
- **Public Site**: http://localhost:8080
- **Admin Portal**: http://localhost:8080/admin
- **H2 Console**: http://localhost:8080/h2-console (Development only)
- **Default Admin**: username: `admin`, password: `admin123`

## 📁 Project Structure

```
src/main/java/com/realestate/sellerfunnel/
├── controller/          # Web controllers (MVC)
├── model/              # Entity classes (JPA)
├── repository/         # Data access layer (Spring Data JPA)
├── service/            # Business logic layer
└── SellerFunnelApplication.java

src/main/resources/
├── application.properties    # Configuration
├── templates/               # Thymeleaf HTML templates
│   ├── admin/              # Admin dashboard templates
│   ├── buyer/              # Buyer-facing templates
│   └── seller/             # Seller-facing templates
└── static/                 # CSS, JS, images
```

## 🔧 Configuration

### Environment Variables

#### Database Configuration
```bash
# PostgreSQL (Production)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/seller_funnel
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
```

#### Email Configuration
```bash
# SMTP Settings
EMAIL_SMTP_HOST=smtp.gmail.com
EMAIL_SMTP_PORT=587
EMAIL_USERNAME=your-email@domain.com
EMAIL_PASSWORD=your-app-password

# Professional Email Settings
EMAIL_FROM_DOMAIN=yourdomain.com
EMAIL_REPLY_TO=support@yourdomain.com

# DKIM Authentication
DKIM_ENABLED=true
DKIM_DOMAIN=yourdomain.com
DKIM_SELECTOR=default
```

#### Marketing API Keys
```bash
# Facebook/Meta Ads
FACEBOOK_ACCESS_TOKEN=your_facebook_token
FACEBOOK_AD_ACCOUNT_ID=act_your_account_id
FACEBOOK_PAGE_ID=your_page_id

# Google Ads
GOOGLE_ADS_DEVELOPER_TOKEN=your_developer_token
GOOGLE_ADS_CLIENT_ID=your_client_id
GOOGLE_ADS_CLIENT_SECRET=your_client_secret
GOOGLE_ADS_REFRESH_TOKEN=your_refresh_token
GOOGLE_ADS_CUSTOMER_ID=your_customer_id

# OpenAI (Content Generation)
OPENAI_API_KEY=your_openai_key
```

### DKIM Email Setup

1. **Add DNS Record**: Add the following TXT record to your domain:
   ```
   Record Name: default._domainkey
   Record Type: TXT
   Record Value: v=DKIM1;k=rsa;p=MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvgn5VVvCnMnAHOk7TBQ1Fq3auIa+CaZeHMz3gHOwkiIA5IZPXmho3BHuxCXzo7I3PnLsiZA18TOQQqVvhVbNU7aRAdaKDsz5q4KmPuHAQkHqPj6aSRmGUtYzeRUxzuc8ys8w9Eff2QpCICF1ArRlVdPIJPgJIftk8ByrKao+qwB+Cjemb5K7cya4i/ssVf9Hm2VH7cGOlmRluBY1VTvUeNA5Gr9d7alGrlYBZkmYCX2g/gZ9FEpPNamlS4n/t/SiKtHACZW3i9QaGnglo616+KakVn9kGeWaQ8m3Wxxo43IdPd5CZMBVP8Ji9mbXRNbRhY3E/ptnMD1eE9maCisNoQIDAQAB
   ```

2. **Configure Environment Variables**: Set the DKIM variables as shown above

3. **Test Configuration**: Use the Email Setup dashboard at `/admin/marketing/email-setup`

## 🚀 Deployment

### Render Deployment
The application is configured for deployment on Render using the included `render.yaml` blueprint.

1. **Connect Repository**: Link your GitHub repository to Render
2. **Environment Variables**: Set all required environment variables in Render dashboard
3. **Deploy**: Render will automatically build and deploy from the `master` branch

### Manual Deployment
```bash
# Build the application
mvn clean package

# Run the JAR file
java -jar target/seller-funnel-0.0.1-SNAPSHOT.jar
```

## 🗃️ Database Schema

### Key Entities

#### Buyer
- Personal information (name, email, phone)
- Budget range and financing preferences
- Location preferences and property requirements
- Creative financing interests
- Lead source tracking

#### Seller
- Property details and address information
- Property photos (file storage)
- Price and condition information
- Selling timeline and motivation
- Creative financing options

#### Campaign
- Campaign name, type, and description
- Target audience and budget information
- Platform-specific settings (Facebook, Google)
- Performance metrics and analytics
- Start/end dates and status

#### EmailCampaign
- Email subject and HTML content
- Sender information and settings
- Recipient targeting and segmentation
- Delivery statistics and tracking

#### Client
- Unified contact management
- Email opt-in/opt-out preferences
- Lead source and conversion tracking
- Communication history and notes

## 🛠️ Development

### Build Commands
```bash
# Clean build
mvn clean package

# Run tests
mvn test

# Start development server
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring.profiles.active=postgres
```

### Testing
- **Unit Tests**: Service layer testing with JUnit 5
- **Integration Tests**: Web layer testing with MockMvc
- **Database Tests**: Repository testing with @DataJpaTest

### Code Style
- Follow Spring Boot conventions
- Use service layer for business logic
- Repository pattern for data access
- Thymeleaf for view templates

## 📊 Marketing Features

### Campaign Types
- **Facebook/Instagram Ads**: Automated campaign creation and management
- **Google Ads**: Search and display campaign setup
- **Email Campaigns**: Professional email marketing with templates
- **Content Generation**: AI-powered ad copy and email content

### Analytics & Reporting
- **Lead Metrics**: Conversion rates, source tracking, lead quality
- **Campaign Performance**: CTR, CPC, conversion rates, ROI
- **Email Analytics**: Open rates, click rates, bounce rates
- **Financial Tracking**: Budget vs. spend, cost per lead, revenue attribution

### Lead Generation Strategy
- **Buyer Targeting**: Investors, first-time buyers, cash buyers
- **Seller Targeting**: Distressed properties, tired landlords, FSBO
- **Creative Financing**: Appeals to unique financing situations
- **Local Marketing**: Geo-targeted campaigns for specific markets

## 🔐 Security Features

- **Admin Authentication**: Secure admin portal access
- **DKIM Email Authentication**: Verified email sending
- **File Upload Security**: Validated file types and size limits
- **SQL Injection Protection**: JPA parameterized queries
- **XSS Protection**: Thymeleaf template escaping

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support & Troubleshooting

### Common Issues

**Email Not Sending**
- Check SMTP configuration and credentials
- Verify DKIM DNS record is properly configured
- Test with the Email Setup dashboard

**Campaign Creation Issues**
- Ensure API credentials are properly set
- Check network connectivity to marketing platforms
- Verify account permissions and limits

**Database Connection Issues**
- Confirm PostgreSQL is running (production mode)
- Check connection string and credentials
- Verify database exists and is accessible

### Logs & Debugging
- Application logs available in console output
- Enable SQL logging with `spring.jpa.show-sql=true`
- Use H2 console for database debugging in development

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is proprietary software for real estate lead generation and management.

---

## 🎯 Getting Started Checklist

- [ ] Clone repository and run `mvn spring-boot:run`
- [ ] Access admin portal at http://localhost:8080/admin
- [ ] Configure email settings at `/admin/marketing/email-setup`
- [ ] Set up marketing API credentials at `/admin/marketing/api-config`
- [ ] Create your first marketing campaign
- [ ] Test lead capture forms at buyer and seller pages
- [ ] Review analytics dashboard for performance insights

**Ready to start generating real estate leads!** 🏡
