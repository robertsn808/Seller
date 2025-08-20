# Overview

This is a comprehensive AI-powered business management system for a Hawaiian poke shop called "Allii Fish Market" that leverages Grok AI for marketing insights and business optimization. The application combines traditional restaurant management features (orders, inventory, customers, menu) with advanced AI-driven marketing automation, social media analysis, and business intelligence capabilities.

The system is designed to help the poke shop position itself as an authentic Hawaiian food experience while using modern technology to optimize operations, predict trends, and enhance customer engagement through data-driven marketing campaigns.

# User Preferences

Preferred communication style: Simple, everyday language.

# System Architecture

## Frontend Architecture
- **Framework**: React 18 with TypeScript using Vite as the build tool
- **UI Library**: Shadcn/ui components built on Radix UI primitives with Tailwind CSS
- **State Management**: TanStack Query (React Query) for server state management with real-time updates
- **Routing**: Wouter for lightweight client-side routing
- **Forms**: React Hook Form with Zod validation for type-safe form handling
- **Real-time Updates**: WebSocket connection for live data synchronization across dashboard components

## Backend Architecture
- **Runtime**: Node.js with Express.js framework
- **Language**: TypeScript with ES modules
- **API Pattern**: RESTful API endpoints with WebSocket support for real-time features
- **Build System**: ESBuild for production bundling with development hot-reload via Vite

## Database Architecture
- **Database**: PostgreSQL with Drizzle ORM for type-safe database operations
- **Schema Design**: Unified business data table approach combining customer, order, inventory, and marketing data in a single comprehensive record structure
- **Migration Strategy**: Drizzle Kit for schema migrations and database management
- **Connection**: Neon Database serverless PostgreSQL for cloud hosting

## AI Integration Architecture
- **AI Provider**: Grok AI (xAI) integration for business intelligence and marketing insights
- **Capabilities**: 
  - Social media trend analysis for ingredient and menu optimization
  - Competitor monitoring and pricing analysis
  - Customer sentiment tracking from reviews and social media
  - Automated marketing campaign suggestions based on real-time data
- **API Integration**: Custom service layer wrapping Grok AI API calls with structured response handling

## Authentication & Authorization
- **Session Management**: Express sessions with PostgreSQL session store (connect-pg-simple)
- **Security**: Environment-based configuration for API keys and database credentials

## Real-time Features
- **WebSocket Server**: Custom WebSocket implementation for live dashboard updates
- **Event Broadcasting**: Real-time notifications for orders, inventory alerts, and AI insights
- **Client Reconnection**: Automatic reconnection logic with exponential backoff

# External Dependencies

## Core Infrastructure
- **Database**: PostgreSQL (via Neon Database serverless)
- **AI Services**: Grok AI (xAI) for business intelligence and marketing automation
- **Session Storage**: PostgreSQL-based session management

## Development & Build Tools
- **Package Manager**: NPM with lockfile version 3
- **Build System**: Vite for frontend development and ESBuild for backend production builds
- **Development Environment**: Replit integration with custom cartographer and error overlay plugins

## UI & Styling
- **Design System**: Shadcn/ui component library with Radix UI primitives
- **Styling**: Tailwind CSS with PostCSS processing
- **Icons**: Lucide React icon library
- **Charts**: Recharts for data visualization and analytics dashboards
- **Fonts**: Google Fonts integration (Roboto, Inter, Fira Code, Geist Mono, Architects Daughter, DM Sans)

## Payment Processing
- **Stripe Integration**: React Stripe.js for payment processing capabilities

## Data Validation & Type Safety
- **Schema Validation**: Zod for runtime type checking and form validation
- **ORM**: Drizzle ORM with PostgreSQL dialect for type-safe database operations
- **TypeScript**: Strict TypeScript configuration with path mapping for clean imports

## Real-time Communication
- **WebSocket**: Native WebSocket implementation with custom connection management
- **Query Synchronization**: TanStack Query for optimistic updates and cache invalidation

## Monitoring & Development
- **Error Handling**: Custom error overlay for development environment
- **Logging**: Structured logging with timestamp formatting for API requests
- **Hot Reload**: Vite middleware integration for seamless development experience