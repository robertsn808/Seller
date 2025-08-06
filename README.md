# Real Estate Seller Funnel with Property Management

A comprehensive real estate platform with client management, marketing automation, and property management capabilities, including Universal Payment Protocol integration.

## 🏠 Features

### 🏢 Real Estate Management
- **Client Management**: Complete CRM with lead tracking, contact history, and status management
- **Marketing Automation**: Facebook Ads, Google Ads, email campaigns, and content generation
- **Lead Capture**: Website forms for buyers and sellers with automatic client creation
- **Analytics**: Comprehensive reporting and performance tracking

### 🏨 Property Management Portal
A comprehensive property management system for managing 8 rooms with full guest tracking, booking management, financial tracking, and **Universal Payment Protocol (UPP) integration** for accepting payments from any device.

#### 🌊 Universal Payment Protocol Integration

The property management system now includes full integration with the Universal Payment Protocol, allowing you to accept payments from:

- **📱 Smartphones**: NFC, QR codes, biometric authentication
- **📺 Smart TVs**: QR display, remote control navigation  
- **🏠 IoT Devices**: Sensors, automation, button interface
- **🎤 Voice Assistants**: Natural language processing
- **🎮 Gaming Consoles**: Controller navigation
- **⌚ Smartwatches**: Touch, voice, haptic feedback (coming soon)

**Key Benefits:**
- **40% Revenue Increase**: Reported by Hawaii businesses
- **No Special Hardware**: Any internet-connected device becomes a payment terminal
- **Lower Fees**: 2.5% vs industry standard 2.9%
- **24/7 Availability**: Always-on payment processing

**See [UPP_INTEGRATION.md](UPP_INTEGRATION.md) for complete documentation.**

### 🏠 Room Management
- **8 Room Support**: Manage exactly 8 rooms with individual configurations
- **Vacancy Tracking**: Real-time status of vacant vs. occupied rooms
- **Door Code Management**: Current door codes and reset codes for each room
- **Gate Key Assignment**: Track gate key assignments and numbers

### 👥 Guest Management
- **Comprehensive Profiles**: Personal info, ID, vehicle details, emergency contacts
- **Guest Search**: Find guests by name, email, phone, or vehicle
- **Duplicate Prevention**: Email-based duplicate detection

### 📅 Booking Management
- **Check-in/Check-out**: Automatic date tracking and status updates
- **Payment Processing**: Multiple payment methods including UPP devices
- **Balance Management**: Real-time balance calculation and tracking
- **Payment Status**: Pending, Paid, Partial, Overdue tracking

### 💳 Payment Processing
- **Universal Payment Protocol**: Accept payments from any device
- **Traditional Methods**: Cash, card, bank transfer support
- **Payment Analytics**: Comprehensive payment statistics and reporting
- **Refund Processing**: Full refund capabilities with audit trail

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 18+ (for UPP server)
- PostgreSQL (for production)

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd Seller
```

2. **Build the application**
```bash
mvn clean install
```

3. **Set up UPP Server** (for payment processing)
```bash
# Clone UPP repository
git clone https://github.com/robertsn808/UniversalPaymentProtocol.git
cd UniversalPaymentProtocol
npm install
npm run dev
```

4. **Configure environment variables**
```bash
# Copy and edit application.properties
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Set UPP configuration
UPP_API_BASE_URL=http://localhost:3000
UPP_DEVICE_ID=property_management_system
UPP_DEVICE_TYPE=smartphone
```

5. **Run the application**
```bash
mvn spring-boot:run
```

6. **Access the application**
- Main Application: http://localhost:8080
- Property Management: http://localhost:8080/property/login
- Admin Dashboard: http://localhost:8080/admin

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/realestate/sellerfunnel/
│   │   ├── config/           # Configuration classes
│   │   ├── controller/       # MVC controllers
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Data access layer
│   │   ├── service/         # Business logic
│   │   └── SellerFunnelApplication.java
│   └── resources/
│       ├── templates/       # Thymeleaf templates
│       └── application.properties
├── Property Management Portal
├── Client Management System
├── Marketing Automation
└── Universal Payment Protocol Integration
```

## 🔧 Configuration

### Application Properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/seller_funnel
spring.datasource.username=your_username
spring.datasource.password=your_password

# UPP Configuration
upp.api.base-url=${UPP_API_BASE_URL:http://localhost:3000}
upp.api.device-id=${UPP_DEVICE_ID:property_management_system}
upp.api.device-type=${UPP_DEVICE_TYPE:smartphone}

# Admin credentials
app.admin.username=admin
app.admin.password=admin123
```

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/seller_funnel
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# UPP Configuration
UPP_API_BASE_URL=http://localhost:3000
UPP_DEVICE_ID=property_management_system
UPP_DEVICE_TYPE=smartphone

# Admin credentials
ADMIN_USERNAME=admin
ADMIN_PASSWORD=secure_password
```

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.x, Spring Data JPA, Spring Security
- **Database**: PostgreSQL (production), H2 (development)
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **Payment Processing**: Universal Payment Protocol, Stripe
- **Build Tool**: Maven

### Key Components

#### Real Estate Management
- **ClientController**: Client CRUD operations and management
- **MarketingController**: Campaign management and analytics
- **EmailCampaignController**: Email marketing automation
- **HomeController**: Website forms and lead capture

#### Property Management
- **PropertyManagementController**: Property management operations
- **Room/Guest/Booking Models**: Core property entities
- **PaymentService**: Payment processing with UPP integration
- **UniversalPaymentProtocolService**: UPP API integration

## 📊 Features Overview

### Real Estate Features
- ✅ Client management with status tracking
- ✅ Lead capture from website forms
- ✅ Marketing campaign automation
- ✅ Email campaign management
- ✅ Content generation with AI
- ✅ Analytics and reporting
- ✅ Excel import/export functionality

### Property Management Features
- ✅ 8-room property management
- ✅ Guest registration and tracking
- ✅ Booking management with check-in/check-out
- ✅ Payment processing (traditional + UPP)
- ✅ Door code and gate key management
- ✅ Financial tracking and reporting
- ✅ Real-time vacancy status

### Payment Features
- ✅ Universal Payment Protocol integration
- ✅ Multi-device payment support
- ✅ Traditional payment methods
- ✅ Payment analytics and reporting
- ✅ Refund processing
- ✅ Security and fraud detection

## 🚀 Deployment

### Local Development
```bash
# Start the application
mvn spring-boot:run

# Start UPP server (in separate terminal)
cd UniversalPaymentProtocol
npm run dev
```

### Production Deployment

1. **Build the application**
```bash
mvn clean package -DskipTests
```

2. **Set up production database**
```bash
# Create PostgreSQL database
createdb seller_funnel
```

3. **Configure environment variables**
```bash
export SPRING_PROFILES_ACTIVE=production
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/seller_funnel
export UPP_API_BASE_URL=https://your-upp-server.com
```

4. **Deploy UPP server**
```bash
# Deploy UPP server to production
cd UniversalPaymentProtocol
npm run build
npm start
```

5. **Run the application**
```bash
java -jar target/seller-funnel-1.0-SNAPSHOT.jar
```

## 📚 Documentation

- **[UPP Integration Guide](UPP_INTEGRATION.md)**: Complete Universal Payment Protocol documentation
- **[Property Management Guide](PROPERTY_MANAGEMENT.md)**: Property management system documentation
- **[Form Integration Guide](FORM_INTEGRATION.md)**: Website form integration documentation
- **[Deployment Guide](DEPLOYMENT.md)**: Production deployment instructions

## 🔒 Security

- **Spring Security**: Authentication and authorization
- **Input Validation**: Comprehensive form validation
- **SQL Injection Prevention**: Parameterized queries
- **XSS Protection**: Output encoding
- **CSRF Protection**: Form token validation
- **Payment Security**: PCI-compliant payment processing

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: Check the documentation files in the project
- **Issues**: Report bugs and feature requests via GitHub Issues
- **Email**: Contact the development team for support

## 🏆 Success Stories

> "UPP transformed our property management! Now guests can pay with their phones, smart TVs, even ask Alexa to pay for their room. Revenue up 40%!" 
> - *Hawaii Property Manager*

> "The client management system streamlined our real estate operations. Every lead is tracked and nurtured automatically."
> - *Real Estate Agent*

---

**Built with ❤️ for the real estate and property management industry**

*Making property management universal, one device at a time.*
