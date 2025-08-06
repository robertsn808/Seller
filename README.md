# Real Estate Seller Funnel

A Spring Boot web application that connects real estate buyers and sellers by collecting detailed lead information and facilitating deal matching. This platform serves as a comprehensive funnel for real estate professionals to capture qualified leads and facilitate property transactions.

## 🏠 Overview

Real Estate Connect is designed to help real estate professionals, investors, and wholesalers by:

- **Collecting buyer requirements** - Capture detailed "buy box" criteria from potential property buyers
- **Gathering seller information** - Collect comprehensive property and seller details  
- **Facilitating connections** - Match qualified buyers with suitable properties
- **Supporting creative financing** - Handle various financing scenarios beyond traditional mortgages

## ✨ Features

### 🛒 Buyer Lead Collection
- Comprehensive buyer questionnaire capturing:
  - Budget range and financing preferences
  - Preferred locations and property types  
  - Bedroom/bathroom requirements
  - Timeline and urgency
  - Creative financing options (seller financing, lease-to-own, etc.)
- Form validation for data quality assurance
- Contact information capture for follow-up

### 🏡 Seller Lead Collection  
- Detailed property listing forms including:
  - Complete property details and specifications
  - Property condition and repair needs
  - Pricing expectations and flexibility
  - Selling motivation and timeline
  - Photo upload support (up to 10 photos, 10MB each)
  - Creative financing willingness (owner financing, rent-to-own, etc.)

### 📊 Admin Dashboard
- Centralized lead management at `/admin`
- View all buyer and seller submissions
- Sort by submission date (most recent first)
- Quick access to contact information
- Property photo thumbnails for seller listings
- Creative financing indicators
- Lead statistics and analytics

### 🔒 Security & Validation
- Spring Security integration
- Form validation using Bean Validation
- Secure file upload handling
- Data sanitization and protection

## 🛠 Technology Stack

- **Backend**: Spring Boot 3.2.0
- **Frontend**: Thymeleaf templating engine
- **Database**: H2 (development) / PostgreSQL (production)
- **Security**: Spring Security
- **Validation**: Bean Validation (JSR-380)
- **File Storage**: Local file system
- **Build Tool**: Maven
- **Java Version**: 17

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker (optional, for PostgreSQL)
- Git

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd seller-funnel
   ```

2. **Build the project**
   ```bash
   mvn clean package
   ```

3. **Run with H2 (Development)**
   ```bash
   mvn spring-boot:run
   ```
   Access the application at: http://localhost:8080
   
   H2 Console available at: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: (leave blank)

### Running with PostgreSQL (Production-like)

1. **Start PostgreSQL**
   ```bash
   docker-compose up -d
   ```

2. **Run with PostgreSQL profile**
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=postgres
   ```

3. **Stop PostgreSQL**
   ```bash
   docker-compose down
   ```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/realestate/sellerfunnel/
│   │   ├── SellerFunnelApplication.java    # Main Spring Boot application
│   │   ├── controller/                     # Web controllers
│   │   ├── model/                         # JPA entities (Buyer, Seller)
│   │   ├── repository/                    # Data access layer
│   │   └── service/                       # Business logic
│   └── resources/
│       ├── application*.properties        # Configuration files
│       ├── templates/                     # Thymeleaf HTML templates
│       └── static/                       # CSS, JS, images
├── docker-compose.yml                     # PostgreSQL development setup
├── Dockerfile                            # Container configuration
├── init.sql                              # Database initialization
├── pom.xml                               # Maven dependencies
└── render.yaml                           # Render deployment config
```

## 🌐 Usage

### For Buyers
1. Navigate to the buyer form
2. Fill out your property requirements:
   - Budget and financing details
   - Location preferences
   - Property specifications
   - Timeline requirements
3. Submit the form to be added to the buyer database

### For Sellers
1. Access the seller form
2. Provide detailed property information:
   - Property details and condition
   - Photos (optional, up to 10)
   - Pricing and flexibility
   - Selling timeline and motivation
3. Submit to be included in seller leads

### For Administrators
1. Access the admin dashboard at `/admin`
2. Review buyer and seller leads
3. Contact leads directly using provided information
4. Match buyers with suitable properties

## 🚢 Deployment

### Local Development
- Uses H2 in-memory database
- File uploads stored in `uploads/property-photos/`
- Automatic restart with Spring Boot DevTools

### Production Deployment

#### Render (Recommended)
The application is configured for easy deployment on Render:

1. Connect your GitHub repository to Render
2. Use the provided `render.yaml` configuration
3. Set up a PostgreSQL database
4. Deploy automatically via Git pushes

See `DEPLOYMENT.md` for detailed Render deployment instructions.

#### Docker
```bash
# Build the image
docker build -t seller-funnel .

# Run with PostgreSQL
docker-compose up -d
```

### Environment Configuration

The application supports multiple profiles:
- `default` - H2 database for development
- `postgres` - PostgreSQL for local production testing  
- `render` - Optimized for Render deployment

## 🔧 Configuration

Key configuration files:
- `application.properties` - Default H2 configuration
- `application-postgres.properties` - PostgreSQL settings
- `application-render.properties` - Render-specific configuration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Spring Boot best practices
- Add appropriate validation for new form fields
- Include unit tests for new functionality
- Update documentation for new features

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For questions, issues, or feature requests:
- Create an issue in the GitHub repository
- Review existing documentation in `CLAUDE.md` and `DEPLOYMENT.md`

---

**Built with ❤️ for real estate professionals**