# Unified CRM Architecture

## Overview

This document describes the architecture of the Unified CRM platform that combines real estate management capabilities with multi-tenant business support.

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React/TypeScript)              │
├─────────────────────────────────────────────────────────────┤
│  • Business Switcher    • Dashboard       • Forms          │
│  • Real Estate Pages    • Restaurant UI   • Admin Panel    │
│  • Marketing Tools      • Analytics       • User Management│
└─────────────────┬───────────────────────────────────────────┘
                  │ HTTP/REST API
                  │ CORS Enabled
┌─────────────────▼───────────────────────────────────────────┐
│                Backend (Spring Boot API)                    │
├─────────────────────────────────────────────────────────────┤
│  • REST Controllers     • JWT Auth        • Business Logic │
│  • Multi-tenant Routing • File Upload     • Email/SMS      │
│  • AI Integration      • Campaign Mgmt    • Real-time APIs │
└─────────────────┬───────────────────────────────────────────┘
                  │ JPA/Hibernate
                  │ Connection Pooling
┌─────────────────▼───────────────────────────────────────────┐
│                 Database (PostgreSQL)                       │
├─────────────────────────────────────────────────────────────┤
│  • Multi-tenant Schema  • Real Estate Data • Business Data │
│  • Unified Client Table • Order Management • AI Content    │
│  • Campaign Data        • Analytics Data   • Audit Logs    │
└─────────────────────────────────────────────────────────────┘
```

## Multi-Tenant Architecture

### Business Isolation

The platform uses a multi-tenant architecture where each business has isolated data:

```sql
-- Core business table
CREATE TABLE businesses (
    id VARCHAR(255) PRIMARY KEY,     -- 'real-estate', 'restaurant-1', etc.
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,      -- 'real-estate', 'restaurant', 'retail'
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- All tenant data references business_id
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    business_id VARCHAR(255) REFERENCES businesses(id),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    -- ... other client fields
);
```

### Business Types Supported

1. **Real Estate (`real-estate`)**
   - Buyer/Seller management
   - Property listings with photos
   - Commission tracking
   - Marketing campaigns

2. **Restaurant (`restaurant`)**
   - Order management
   - Menu/inventory tracking
   - Customer analytics
   - Table management

3. **Retail/General (`retail`)**
   - Product catalog
   - Customer management
   - Sales tracking
   - Marketing automation

## Database Schema

### Core Entities

#### Business Entity
- Manages business information and tenant isolation
- Links to all other entities via `business_id`

#### Unified Client Table
```java
@Entity
public class Client {
    private Long id;
    private String businessId;        // Tenant isolation
    private String firstName;
    private String lastName;
    private String email;
    private String clientType;        // BUYER, SELLER, CUSTOMER, etc.
    private String clientStatus;      // LEAD, PROSPECT, DEAL, etc.
    // ... contact tracking fields
}
```

#### Universal Order Table
```java
@Entity
public class Order {
    private Long id;
    private String businessId;        // Tenant isolation
    private String orderType;         // PROPERTY_SALE, RESTAURANT_ORDER, etc.
    private String items;             // JSON string for flexibility
    private BigDecimal total;
    private String status;
    // Real estate specific fields
    private String propertyAddress;
    private BigDecimal commissionAmount;
    private LocalDateTime closingDate;
    // ... other fields
}
```

### Legacy Entity Support

Original entities are preserved and enhanced:

- **Buyer/Seller**: Enhanced with `businessId` for multi-tenant support
- **Campaign**: Enhanced with business context
- **AIGeneratedContent**: Business-aware content generation

## API Architecture

### REST API Structure

```
/api/
├── /buyers                 # Real estate buyer management
├── /sellers                # Real estate seller management  
├── /clients                # Unified client management
├── /orders                 # Universal order management
├── /campaigns              # Marketing campaigns
├── /businesses             # Business management
├── /analytics              # Cross-business analytics
└── /auth                   # Authentication
```

### Authentication Flow

1. **Login**: POST `/api/auth/login` with credentials
2. **JWT Token**: Server returns JWT token with business context
3. **Business Context**: Token includes current business ID
4. **API Requests**: All API calls include business filtering
5. **Business Switching**: Update token with new business context

### CORS Configuration

Frontend development servers are whitelisted:
- `http://localhost:3000`
- `http://localhost:5173`
- `https://*.vercel.app`
- `https://*.netlify.app`

## Frontend Architecture

### Component Structure

```
src/
├── components/
│   ├── layout/
│   │   ├── business-switcher.tsx    # Multi-tenant switcher
│   │   ├── sidebar.tsx              # Navigation
│   │   └── topbar.tsx               # Header with notifications
│   ├── dashboard/
│   │   ├── metrics-cards.tsx        # Key metrics display
│   │   ├── sales-chart.tsx          # Sales analytics
│   │   └── recent-orders.tsx        # Recent activity
│   └── ui/                          # Reusable Radix UI components
├── pages/
│   ├── dashboard.tsx                # Main dashboard
│   ├── customers.tsx                # Customer management
│   ├── orders.tsx                   # Order management
│   └── marketing.tsx                # Campaign management
├── hooks/
│   ├── use-business-context.tsx     # Business switching logic
│   └── use-websocket.tsx            # Real-time updates
└── lib/
    ├── queryClient.ts               # React Query setup
    └── utils.ts                     # Utility functions
```

### State Management

- **React Query**: Server state management and caching
- **Context API**: Business switching and user context
- **Local State**: Component-specific state with useState/useReducer

### Business Context Management

```typescript
const BusinessContext = createContext({
  currentBusiness: 'real-estate',
  businesses: [],
  switchBusiness: (id: string) => void,
  isLoading: false
});

export const useBusiness = () => useContext(BusinessContext);
```

## Integration Points

### Real Estate CRM Integration

The platform maintains full compatibility with existing real estate features:

- Buyer/Seller forms continue to work
- Marketing campaigns with Facebook/Google Ads
- Property photo management
- Commission tracking
- Email/SMS campaigns

### Restaurant Platform Integration

AliiBiz features are integrated as a business type:

- Order management system
- Menu and inventory tracking
- Customer analytics
- AI-powered insights
- Payment processing

### AI Content Generation

Unified AI content system supports:

- Real estate marketing copy
- Restaurant menu descriptions
- Social media posts
- Email campaigns
- Ad copy for different platforms

## Deployment Architecture

### Backend Deployment (Render)

```yaml
# render.yaml
services:
- type: web
  name: unified-crm-api
  runtime: java
  buildCommand: ./mvnw clean package -DskipTests
  startCommand: java -jar target/seller-funnel-*.jar
  envVars:
  - key: SPRING_PROFILES_ACTIVE
    value: postgres
```

### Frontend Deployment (Vercel)

```json
{
  "builds": [
    {
      "src": "package.json",
      "use": "@vercel/node"
    }
  ],
  "routes": [
    { "src": "/api/(.*)", "dest": "https://api-url.com/api/$1" },
    { "src": "/(.*)", "dest": "/index.html" }
  ]
}
```

### Database Migration Strategy

1. **Phase 1**: Add business_id columns to existing tables
2. **Phase 2**: Populate with default 'real-estate' business
3. **Phase 3**: Create new unified entities
4. **Phase 4**: Migrate data to unified schema
5. **Phase 5**: Remove redundant legacy tables

## Security Architecture

### Authentication

- JWT tokens with configurable expiration
- Secure password hashing with BCrypt
- Session management with Redis (optional)

### Authorization

- Role-based access control (ADMIN, USER, VIEWER)
- Business-level data isolation
- API endpoint protection

### Data Protection

- SQL injection prevention via JPA
- XSS protection with OWASP sanitizer
- CORS configuration for secure frontend access
- File upload validation and sanitization

## Monitoring and Observability

### Metrics

- Business-specific analytics
- API performance monitoring
- Database query optimization
- User activity tracking

### Logging

- Structured logging with Logback
- Business context in all log entries
- Error tracking and alerting
- Performance monitoring

## Scalability Considerations

### Horizontal Scaling

- Stateless API design
- Database connection pooling
- CDN for static assets
- Load balancer ready

### Vertical Scaling

- JVM optimization
- Database indexing strategy
- Query optimization
- Caching layers

### Multi-Region Support

- Database replication
- CDN distribution
- Regional API endpoints
- Data sovereignty compliance