# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Real Estate Connect is a Spring Boot web application that serves as a funnel for connecting real estate buyers and sellers. The application allows buyers to submit their "buy box" requirements and sellers to list their properties, enabling you to bird-dog deals by matching qualified leads.

## Development Commands

### Running with H2 (Development)
- **Start the application**: `mvn spring-boot:run`
- **Access H2 console**: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:testdb)

### Running with PostgreSQL (Local Production)
- **Start PostgreSQL**: `docker-compose up -d`
- **Start with PostgreSQL**: `mvn spring-boot:run -Dspring.profiles.active=postgres`
- **Stop PostgreSQL**: `docker-compose down`

### Deployment (Render)
- **Deploy Guide**: See `DEPLOYMENT.md` for complete Render deployment instructions
- **Live Demo**: Deploy to Render for public access
- **Auto-deploy**: Configured via `render.yaml` blueprint

### General Commands
- **Build the project**: `mvn clean package`
- **Run tests**: `mvn test`
- **Access the application**: http://localhost:8080

### Project Structure
- `src/main/java/com/realestate/sellerfunnel/` - Main application package
  - `SellerFunnelApplication.java` - Spring Boot main class
  - `model/` - Entity classes (Buyer, Seller)
  - `repository/` - JPA repositories
  - `controller/` - Web controllers
- `src/main/resources/` - Configuration and templates
  - `application.properties` - Spring Boot configuration
  - `templates/` - Thymeleaf HTML templates
- `pom.xml` - Maven configuration with Spring Boot dependencies

## Key Features

### Buyer Lead Collection
- Comprehensive buyer form capturing budget, preferences, and requirements
- Fields include: budget range, preferred areas, bedrooms/bathrooms, property type, financing needs, timeframe
- **NEW**: Creative financing options (seller financing, lease-to-own, etc.)
- Validation ensures data quality

### Seller Lead Collection  
- Detailed property listing form for sellers
- Fields include: property details, address, price, condition, selling reason, timeframe
- **NEW**: Property photo upload support (up to 10 photos, 10MB each)
- **NEW**: Creative financing options (owner financing, rent-to-own, etc.)
- Support for repair details and property condition

### Admin Dashboard
- View all buyer and seller leads at `/admin`
- Sort by submission date (most recent first)
- Contact information readily accessible
- **NEW**: Property photo thumbnails in seller listings
- **NEW**: Creative financing indicators for both buyers and sellers
- Lead statistics and overview

## Database Configuration

### Development (H2)
The application uses H2 in-memory database by default for development. Data is recreated on each restart.

### Production (PostgreSQL)
For persistent data storage, use PostgreSQL:
1. Start PostgreSQL: `docker-compose up -d`
2. Run application: `mvn spring-boot:run -Dspring.profiles.active=postgres`
3. Database: `seller_funnel` with user `postgres` and password `password`

### File Storage
- Property photos are stored in `uploads/property-photos/` directory
- Access photos via `/photos/{fileName}` endpoint
- Supports JPG, PNG, GIF, WebP formats

## Architecture Notes

This is a Spring MVC web application using Thymeleaf for templating. The architecture follows standard Spring Boot patterns with clearly separated layers (Controller → Service → Repository → Entity). Form validation uses Bean Validation annotations, and the H2 console is available for development database access.

## Git Practices
- Do not put your signature in the commit messages

## Marketing Strategy

### 🎯 Target Audiences

SELLERS

Primary Targets:
- Distressed property owners (foreclosure, divorce, job loss)
- Tired landlords wanting to exit rental business
- Inherited property owners who don't want the hassle
- Elderly homeowners looking to downsize quickly
- Out-of-state owners with local properties

Secondary Targets:
- FSBO (For Sale By Owner) sellers
- Properties that have been on market 90+ days
- Property flippers looking for quick sales

BUYERS

Primary Targets:
- Real estate investors (fix & flip, buy & hold)
- First-time homebuyers priced out of traditional market
- Cash buyers looking for deals
- Landlords expanding portfolios

Secondary Targets:
- People relocating to your area
- Buyers with unique financing situations

### 📱 Digital Marketing Strategy

1. Facebook/Meta Advertising

Seller Campaigns:
- "We Buy Houses Fast - Any Condition"
- Target: Age 35-65, homeowners, life event triggers
- Creative: Before/after photos, testimonials

Buyer Campaigns:
- "Off-Market Property Deals"
- Target: Real estate investor groups, business owners
- Creative: Property photos, ROI calculators

2. Google Ads

Keywords for Sellers:
- "Sell house fast [city]"
- "We buy houses [city]"
- "Cash for houses"
- "Sell inherited property"

Keywords for Buyers:
- "Investment properties [city]"
- "Off market deals"
- "Real estate opportunities"
- "Fix and flip properties"

3. Social Media Organic

Content Ideas:
- Success stories and testimonials
- Market updates and trends
- Property transformation videos
- Educational content about creative financing
- Behind-the-scenes of deal process

### 🏠 Local Marketing Tactics

Seller Outreach:
- Direct mail to distressed neighborhoods
- Bandit signs (where legal): "We Buy Houses"
- Door hangers in target areas
- Craigslist "Real Estate Wanted" ads
- Local newspaper classified ads

Buyer Outreach:
- Local real estate investor meetups (REIA groups)
- Wholesaler networking events
- Real estate agent partnerships
- BiggerPockets community engagement
- LinkedIn outreach to local investors

### 📧 Content Marketing

Blog Content:
- "5 Ways to Sell Your House Fast Without a Realtor"
- "Creative Financing Options for Real Estate Investors"
- "What to Expect When Selling to a Cash Buyer"
- "Investment Property ROI Calculator Guide"
- Local market analysis reports

Lead Magnets:
- "Homeowner's Guide to Quick Sales" (PDF)
- "Investor's Market Analysis Report"
- "Creative Financing Strategies Checklist"
- Property valuation calculator

### 🤝 Partnership Strategy

Strategic Partners:
- Real estate attorneys (referrals for probate)
- Property management companies (tired landlords)
- Contractors (distressed property leads)
- Financial planners (clients needing liquidity)
- Moving companies (relocating sellers)

Referral Program:
- $500 referral fee for successful seller leads
- $250 referral fee for successful buyer leads
- Partner with local businesses for mutual referrals

### 📊 Campaign Implementation Plan

Week 1-2: Foundation
- Set up Facebook Business Page & Google My Business
- Create landing page variations for different audiences
- Design initial ad creatives and copy
- Set up tracking pixels and analytics

Week 3-4: Launch Digital
- Launch Facebook ads for both buyers and sellers
- Start Google Ads campaigns
- Begin organic social media posting
- Launch email sequences

Month 2: Scale & Optimize
- Analyze campaign performance
- A/B test ad creatives and copy
- Expand successful campaigns
- Add retargeting campaigns

Month 3+: Local & Partnerships
- Implement direct mail campaigns
- Attend networking events
- Build partner referral network
- Launch local newspaper ads

### 💰 Budget Allocation (Monthly)

Digital Advertising (60%):
- Facebook/Meta Ads: $1,000
- Google Ads: $800

Content Creation (20%):
- Graphic design: $200
- Video production: $100
- Copywriting: $100

Local Marketing (15%):
- Direct mail: $200
- Print ads: $50
- Networking events: $50

Tools & Software (5%):
- CRM software: $50
- Design tools: $50

### 📈 Success Metrics

Lead Generation KPIs:
- Cost per lead (seller vs buyer)
- Lead-to-conversion rate
- Website traffic and conversion rate
- Email open/click rates

Campaign KPIs:
- Return on Ad Spend (ROAS)
- Click-through rates
- Cost per acquisition
- Lifetime value of customers

### 🎯 Quick Win Tactics (Start This Week)

1. Create Facebook Business Page with compelling content
2. Join local real estate Facebook groups and provide value
3. Post on Craigslist in "Real Estate Wanted" section
4. Create simple business cards with your website URL
5. Set up Google My Business for local SEO

## Marketing Technology Updates

### Campaign Management Enhancements

✅ Fixed & Enhanced:

Campaign Creation Fixed:
- Fixed form field mapping issues
- Added missing keywords field to Campaign model
- Campaign creation now works properly

API Integrations Added:
- FacebookAdsService: Create campaigns, ad sets, and ads on Facebook/Instagram
- GoogleAdsService: Create campaigns, ad groups, keywords, and ads on Google Ads
- CampaignPublishingService: Orchestrates publishing across platforms

New Features:
- "Publish to Platform" buttons in campaign detail pages
- API Configuration Guide at /admin/marketing/api-config
- Automated campaign publishing when status changes to "ACTIVE"
- Graceful fallback to manual publishing when APIs aren't configured

🎯 How to Use:

Immediate Use (No API Setup):
1. Create campaigns - Plan your marketing campaigns
2. Generate content - Use ready-made templates
3. Copy & paste - Manually post to Facebook, Google, etc.
4. Track manually - Update metrics in the dashboard

Full Automation (With API Setup):
1. Visit /admin/marketing/api-config for detailed setup instructions
2. Configure API credentials in application.properties
3. Create campaigns and they'll automatically publish to platforms
4. Real-time sync of campaign metrics and performance

🔧 API Configuration:

The system supports:
- Facebook/Meta Ads API - For Facebook and Instagram campaigns
- Google Ads API - For Google search and display campaigns
- Automatic Publishing - Campaigns publish when set to "ACTIVE"
- Performance Syncing - Real-time metrics from platforms

🚀 Next Steps:
1. Test the campaign creation - Should work now without errors
2. Try manual publishing first to test your content
3. Set up APIs later when ready to fully automate (guide provided)
4. Scale your marketing with automated campaign management

Your marketing dashboard is now a complete campaign management system that can grow with your business - start simple and add automation as needed!
- do not include your signature to anything including commits and pull requests
- ptg means push to github. so you know what that means when i say that