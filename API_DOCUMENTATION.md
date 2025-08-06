# API Documentation

Real Estate Connect CRM provides both internal APIs for the web application and external APIs for integrations.

## 🌐 Web API Endpoints

### Authentication
All admin endpoints require authentication. Login at `/admin` with default credentials:
- Username: `admin`
- Password: `admin123`

### Lead Management APIs

#### Buyer Endpoints
```http
POST /submit-buyer
Content-Type: application/x-www-form-urlencoded

firstName=John&lastName=Doe&email=john@example.com&phone=555-1234&budget=250000&...
```

**Response:**
```json
{
  "status": "success",
  "message": "Thank you for your submission!",
  "buyerId": 123
}
```

#### Seller Endpoints
```http
POST /submit-seller
Content-Type: multipart/form-data

firstName=Jane&lastName=Smith&email=jane@example.com&propertyPhotos=@photo1.jpg&...
```

**Response:**
```json
{
  "status": "success", 
  "message": "Property submitted successfully!",
  "sellerId": 456,
  "photosUploaded": 3
}
```

### Campaign Management APIs

#### Create Campaign
```http
POST /admin/marketing/campaigns
Content-Type: application/x-www-form-urlencoded

name=Spring Campaign&type=FACEBOOK&targetAudience=BUYERS&budget=1000&...
```

#### Update Campaign Status
```http
POST /admin/marketing/campaigns/{id}/status
Content-Type: application/x-www-form-urlencoded

status=ACTIVE
```

#### Get Campaign Analytics
```http
GET /admin/marketing/campaigns/{id}/analytics
```

**Response:**
```json
{
  "campaignId": 123,
  "impressions": 10000,
  "clicks": 250,
  "leads": 15,
  "cost": 75.50,
  "ctr": 2.5,
  "costPerLead": 5.03
}
```

### Email Campaign APIs

#### Send Email Campaign
```http
POST /admin/email-campaigns/{id}/send
Content-Type: application/json

{
  "recipientIds": [1, 2, 3],
  "sendImmediately": true
}
```

#### Email Tracking Pixel
```http
GET /api/email-tracking/pixel?c=123&u=456&t=tracking_id
```

#### Unsubscribe
```http
GET /unsubscribe?t=tracking_id
```

## 🔗 External API Integrations

### Facebook Ads API Integration

#### Create Facebook Campaign
The system automatically creates Facebook campaigns when a campaign is set to "ACTIVE":

**Internal Flow:**
1. Campaign status changed to ACTIVE
2. `FacebookAdsService.createCampaign()` called
3. Facebook API creates campaign, ad set, and ads
4. Campaign metrics synced back to database

**Facebook API Calls Made:**
```http
# Create Campaign
POST https://graph.facebook.com/v18.0/act_{ad-account-id}/campaigns

# Create Ad Set  
POST https://graph.facebook.com/v18.0/act_{ad-account-id}/adsets

# Create Ad
POST https://graph.facebook.com/v18.0/act_{ad-account-id}/ads
```

#### Facebook Credentials Management
```http
POST /admin/marketing/facebook-credentials
Content-Type: application/x-www-form-urlencoded

accessToken=your_token&pageId=your_page_id&adAccountId=act_123456
```

### Google Ads API Integration

#### Create Google Ads Campaign
Similar to Facebook, Google Ads campaigns are created automatically:

**Google API Calls Made:**
```http
# Create Campaign
POST https://googleads.googleapis.com/v14/customers/{customer-id}/campaigns:mutate

# Create Ad Group
POST https://googleads.googleapis.com/v14/customers/{customer-id}/adGroups:mutate

# Create Keywords and Ads
POST https://googleads.googleapis.com/v14/customers/{customer-id}/adGroupAds:mutate
```

### Email Service APIs

#### Professional Email Sending
```http
POST /admin/marketing/email-setup/test
Content-Type: application/x-www-form-urlencoded

testEmail=test@example.com
```

**Response:**
```json
{
  "success": true,
  "message": "Test email sent successfully with DKIM authentication!"
}
```

#### DKIM Configuration Check
```http
GET /admin/marketing/email-setup/configuration-guide
```

**Response:**
```json
{
  "dkim": {
    "record_name": "default._domainkey",
    "record_type": "TXT", 
    "record_value": "v=DKIM1;k=rsa;p=MIIBIjANBgkqhkiG..."
  },
  "configured": true,
  "status": "Email service configured with DKIM authentication"
}
```

## 📊 Analytics APIs

### Marketing Dashboard Data
```http
GET /admin/marketing/dashboard
```

Returns dashboard with:
- Total campaigns count
- Active campaigns count  
- Monthly leads and spend
- Campaign performance metrics

### Lead Analytics
```http
GET /admin/analytics/leads?startDate=2024-01-01&endDate=2024-12-31
```

**Response:**
```json
{
  "totalLeads": 150,
  "buyerLeads": 90,
  "sellerLeads": 60,
  "conversionRate": 12.5,
  "leadSources": [
    {"source": "Facebook", "count": 75},
    {"source": "Google", "count": 45},
    {"source": "Direct", "count": 30}
  ]
}
```

## 🔐 Authentication & Security

### Admin Authentication
The application uses Spring Security with session-based authentication:

```http
POST /login
Content-Type: application/x-www-form-urlencoded

username=admin&password=admin123
```

### API Security Features
- **CSRF Protection**: All POST requests require CSRF tokens
- **Input Validation**: Form data validated on server-side
- **File Upload Security**: Image files only, size limits enforced
- **SQL Injection Prevention**: All queries use JPA parameters

### Rate Limiting
- Email sending: 100ms delay between emails
- API calls: Respect platform rate limits (Facebook, Google)
- File uploads: 10MB per file, 50MB total request size

## 📝 Error Handling

### Standard Error Response Format
```json
{
  "error": true,
  "message": "Description of the error",
  "code": "ERROR_CODE",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Common Error Codes
- `INVALID_INPUT` - Form validation failed
- `AUTHENTICATION_REQUIRED` - User not logged in
- `PERMISSION_DENIED` - Insufficient permissions
- `API_LIMIT_EXCEEDED` - Rate limit reached
- `EXTERNAL_API_ERROR` - Facebook/Google API error
- `EMAIL_SEND_FAILED` - Email delivery failed

## 🧪 Testing APIs

### Using curl

#### Test Buyer Form Submission
```bash
curl -X POST http://localhost:8080/submit-buyer \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "firstName=John&lastName=Doe&email=john@test.com&phone=555-1234&budget=250000&budgetMax=300000&preferredAreas=Downtown&bedrooms=3&bathrooms=2&propertyType=Single Family&needsFinancing=true&timeframe=3-6 months&additionalInfo=Looking for move-in ready home"
```

#### Test Seller Form with Photo
```bash
curl -X POST http://localhost:8080/submit-seller \
  -H "Content-Type: multipart/form-data" \
  -F "firstName=Jane" \
  -F "lastName=Smith" \
  -F "email=jane@test.com" \
  -F "phone=555-5678" \
  -F "propertyAddress=123 Main St" \
  -F "propertyCity=Anytown" \
  -F "propertyState=CA" \
  -F "propertyZipCode=90210" \
  -F "propertyType=Single Family" \
  -F "bedrooms=4" \
  -F "bathrooms=3" \
  -F "propertyPrice=450000" \
  -F "propertyCondition=Good" \
  -F "reasonForSelling=Relocating" \
  -F "timeframe=1-3 months" \
  -F "propertyPhotos=@photo1.jpg"
```

#### Test Email Configuration
```bash
curl -X POST http://localhost:8080/admin/marketing/email-setup/test \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "testEmail=your-email@example.com" \
  --cookie-jar cookies.txt \
  --cookie cookies.txt
```

### Using Postman

1. **Import Collection**: Create a Postman collection with the endpoints above
2. **Set Environment Variables**: 
   - `base_url`: `http://localhost:8080`
   - `admin_username`: `admin`
   - `admin_password`: `admin123`
3. **Authentication**: Use session cookies or login before API calls

## 🔌 Webhook Support

### Email Event Webhooks
The system supports webhooks for email events:

```http
POST /webhooks/email-events
Content-Type: application/json

{
  "event": "delivered|opened|clicked|bounced",
  "trackingId": "unique_tracking_id",
  "timestamp": "2024-01-15T10:30:00Z",
  "recipientEmail": "recipient@example.com"
}
```

### Campaign Performance Webhooks
For real-time campaign updates:

```http
POST /webhooks/campaign-performance  
Content-Type: application/json

{
  "campaignId": 123,
  "platform": "facebook|google",
  "metrics": {
    "impressions": 1000,
    "clicks": 25,
    "cost": 12.50
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 📈 API Usage Examples

### Complete Lead-to-Campaign Workflow

1. **Capture Lead**:
```bash
# Buyer submits form
curl -X POST /submit-buyer -d "firstName=John&email=john@test.com&budget=250000&..."
```

2. **Create Campaign**:
```bash  
# Admin creates targeted campaign
curl -X POST /admin/marketing/campaigns -d "name=Buyer Campaign&targetAudience=BUYERS&..."
```

3. **Activate Campaign**:
```bash
# Campaign goes live on Facebook/Google
curl -X POST /admin/marketing/campaigns/123/status -d "status=ACTIVE"
```

4. **Send Follow-up Email**:
```bash
# Send personalized email to lead
curl -X POST /admin/email-campaigns/456/send -d "recipientIds=[123]"
```

5. **Track Performance**:
```bash
# Monitor campaign results
curl -X GET /admin/marketing/campaigns/123/analytics
```

## 🔧 API Configuration

### Required Environment Variables
```bash
# Marketing APIs
FACEBOOK_ACCESS_TOKEN=your_token
GOOGLE_ADS_DEVELOPER_TOKEN=your_token
OPENAI_API_KEY=your_key

# Email Configuration  
EMAIL_SMTP_HOST=smtp.gmail.com
EMAIL_USERNAME=your_email
EMAIL_PASSWORD=your_password
DKIM_ENABLED=true
DKIM_DOMAIN=yourdomain.com
```

### API Rate Limits
- **Facebook API**: 200 calls per hour per user
- **Google Ads API**: 10,000 operations per day
- **Email Sending**: 100ms delay between emails
- **File Uploads**: 10MB per file, 10 files max per request

---

## 📚 Additional Resources

- **Postman Collection**: Available in `/docs/postman/` directory
- **API Testing Scripts**: See `/scripts/api-tests/` directory  
- **Integration Examples**: Check `/examples/integrations/` directory

For questions about API usage, check the application logs or contact the development team.