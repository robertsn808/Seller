# Unified CRM Platform

A modern, multi-business CRM platform that combines real estate management with multi-tenant business capabilities. Built with React/TypeScript frontend and Spring Boot API backend.

## 🏗️ Architecture

- **Frontend**: React + TypeScript + Tailwind CSS + Radix UI
- **Backend**: Spring Boot + PostgreSQL + JWT Authentication
- **Database**: Multi-tenant PostgreSQL with business isolation
- **Deployment**: Docker + Render/Vercel

## 🚀 Features

### Real Estate CRM
- Buyer and seller lead capture forms
- Property management with photo uploads
- Marketing campaign automation (Facebook/Google Ads)
- Email and SMS campaign management
- AI-powered content generation
- Commission tracking and analytics

### Multi-Business Support
- Restaurant order management
- Inventory tracking
- Customer analytics
- AI-powered insights and content creation
- Multi-tenant business switching

### Unified Features
- Cross-business customer management
- Unified analytics dashboard
- AI content generation for all business types
- Comprehensive campaign management
- Real-time notifications and updates

## 🛠️ Technology Stack

### Frontend
- **React 18** with TypeScript
- **Tailwind CSS** for styling
- **Radix UI** for accessible components
- **React Query** for data fetching
- **Wouter** for routing
- **Framer Motion** for animations

### Backend
- **Spring Boot 3.2** with Java 21
- **Spring Security** with JWT authentication
- **Spring Data JPA** with PostgreSQL
- **OpenAI Integration** for AI features
- **Twilio** for SMS capabilities
- **Email integration** for campaigns

### Database
- **PostgreSQL** with multi-tenant architecture
- **H2** for development/testing
- **Drizzle ORM** support for TypeScript queries

## 🏃‍♂️ Quick Start

### Prerequisites
- Java 21
- Node.js 18+
- PostgreSQL (optional - H2 used by default)

### Backend Setup
```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at http://localhost:8080

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at http://localhost:5173

### Database Configuration

#### Development (H2)
Default configuration uses H2 in-memory database. Access H2 console at:
http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

#### Production (PostgreSQL)
```bash
# Start PostgreSQL with Docker
cd backend
docker-compose up -d

# Run with PostgreSQL profile
./mvnw spring-boot:run -Dspring.profiles.active=postgres
```

## 🏢 Multi-Business Architecture

The platform supports multiple business types:

- **real-estate**: Real estate buyer/seller management
- **restaurant**: Restaurant order and inventory management
- **retail**: General retail business support

Each business has isolated data while sharing common CRM features.

## 📱 API Endpoints

### Public Endpoints
- `POST /api/buyer` - Submit buyer form
- `POST /api/seller` - Submit seller form
- `GET /api/photos/{fileName}` - Get property photos
- `GET /api/dashboard-stats` - Public dashboard stats

### Authenticated Endpoints
- `GET /api/buyers` - List all buyers
- `GET /api/sellers` - List all sellers
- `GET /api/clients` - List all clients
- `GET /api/orders` - List orders
- `POST /api/campaigns` - Create marketing campaigns

## 🔐 Authentication

The platform uses JWT-based authentication with session support. Default admin credentials:
- Username: `admin`
- Password: `admin123`

## 🎨 Frontend Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── dashboard/     # Dashboard components
│   │   ├── layout/        # Layout components
│   │   └── ui/            # Reusable UI components
│   ├── hooks/             # Custom React hooks
│   ├── lib/               # Utility libraries
│   └── pages/             # Page components
├── package.json
└── vite.config.ts
```

## 🗄️ Backend Structure

```
backend/
├── src/main/java/com/realestate/sellerfunnel/
│   ├── controller/        # REST API controllers
│   ├── model/             # JPA entities
│   ├── repository/        # Data repositories
│   ├── service/           # Business logic
│   └── config/            # Configuration classes
└── pom.xml
```

## 🚀 Deployment

### Backend (Render)
The backend is configured for Render deployment with `render.yaml`.

### Frontend (Vercel/Netlify)
The frontend can be deployed to Vercel, Netlify, or any static hosting provider.

### Environment Variables

Backend `.env`:
```
SPRING_PROFILES_ACTIVE=postgres
DATABASE_URL=your_postgres_url
OPENAI_API_KEY=your_openai_key
TWILIO_ACCOUNT_SID=your_twilio_sid
TWILIO_AUTH_TOKEN=your_twilio_token
```

Frontend `.env`:
```
VITE_API_BASE_URL=https://your-backend-url.com
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License.

## 🔗 Related Projects

- Original Real Estate CRM: [Real Estate Connect]
- Original Restaurant Platform: [AliiBiz](https://github.com/gorjessbbyx3/AliiBiz)

## 📞 Support

For support, please open an issue on GitHub or contact the development team.