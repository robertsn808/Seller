# Deployment Guide

This guide covers deploying Real Estate Connect CRM to production environments.

## 🚀 Production Deployment Options

### Option 1: Render (Recommended)
Render provides easy deployment with automatic builds and SSL certificates.

#### Prerequisites
- GitHub account with repository
- Render account (free tier available)

#### Steps
1. **Push to GitHub**
   ```bash
   git add .
   git commit -m "Ready for deployment"
   git push origin master
   ```

2. **Connect to Render**
   - Log in to [Render Dashboard](https://dashboard.render.com)
   - Click "New Web Service"
   - Connect your GitHub repository
   - Select the "Seller" repository

3. **Configure Build Settings**
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/seller-funnel-0.0.1-SNAPSHOT.jar`
   - **Environment**: Select "Java"

4. **Environment Variables**
   Set these in Render Dashboard:
   ```
   SPRING_PROFILES_ACTIVE=production
   SPRING_DATASOURCE_URL=jdbc:postgresql://[DB_HOST]:5432/seller_funnel
   SPRING_DATASOURCE_USERNAME=[DB_USER]
   SPRING_DATASOURCE_PASSWORD=[DB_PASSWORD]
   
   EMAIL_SMTP_HOST=smtp.gmail.com
   EMAIL_SMTP_PORT=587
   EMAIL_USERNAME=[YOUR_EMAIL]
   EMAIL_PASSWORD=[APP_PASSWORD]
   
   DKIM_ENABLED=true
   DKIM_DOMAIN=[YOUR_DOMAIN]
   DKIM_SELECTOR=default
   
   FACEBOOK_ACCESS_TOKEN=[FB_TOKEN]
   GOOGLE_ADS_DEVELOPER_TOKEN=[GOOGLE_TOKEN]
   OPENAI_API_KEY=[OPENAI_KEY]
   ```

5. **Database Setup**
   - Add PostgreSQL database in Render
   - Use the provided connection string
   - Database will auto-initialize on first run

6. **Deploy**
   - Click "Create Web Service"
   - Render will build and deploy automatically
   - Your app will be available at `https://your-app-name.onrender.com`

### Option 2: Heroku

#### Prerequisites
- Heroku CLI installed
- Heroku account

#### Steps
1. **Create Heroku App**
   ```bash
   heroku create your-app-name
   ```

2. **Add PostgreSQL**
   ```bash
   heroku addons:create heroku-postgresql:mini
   ```

3. **Set Environment Variables**
   ```bash
   heroku config:set SPRING_PROFILES_ACTIVE=production
   heroku config:set EMAIL_USERNAME=your-email@domain.com
   heroku config:set EMAIL_PASSWORD=your-app-password
   heroku config:set DKIM_ENABLED=true
   heroku config:set DKIM_DOMAIN=yourdomain.com
   # ... add other variables
   ```

4. **Deploy**
   ```bash
   git push heroku master
   ```

### Option 3: AWS/DigitalOcean/VPS

#### Prerequisites
- VPS with Java 11+ installed
- PostgreSQL database
- Domain name with DNS access

#### Steps
1. **Build Application**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Transfer Files**
   ```bash
   scp target/seller-funnel-0.0.1-SNAPSHOT.jar user@server:/opt/realestate/
   ```

3. **Create Systemd Service**
   ```bash
   sudo nano /etc/systemd/system/realestate-crm.service
   ```

   ```ini
   [Unit]
   Description=Real Estate CRM
   After=syslog.target

   [Service]
   User=realestate
   ExecStart=/usr/bin/java -jar /opt/realestate/seller-funnel-0.0.1-SNAPSHOT.jar
   SuccessExitStatus=143
   Environment=SPRING_PROFILES_ACTIVE=production
   Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/seller_funnel
   # ... other environment variables

   [Install]
   WantedBy=multi-user.target
   ```

4. **Start Service**
   ```bash
   sudo systemctl enable realestate-crm
   sudo systemctl start realestate-crm
   ```

## 🗄️ Database Setup

### PostgreSQL Installation
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# Create database and user
sudo -u postgres psql
CREATE DATABASE seller_funnel;
CREATE USER realestate WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE seller_funnel TO realestate;
\q
```

### Database Migration
The application uses Hibernate DDL auto-generation in production:
- First run: Creates all tables automatically
- Subsequent runs: Updates schema as needed
- Data is persisted between deployments

## 📧 Email Configuration

### Gmail Setup
1. **Enable 2FA** on your Google account
2. **Generate App Password**:
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Generate password for "Mail"
   - Use this password in `EMAIL_PASSWORD`

### Custom SMTP
```bash
# For custom email providers
EMAIL_SMTP_HOST=mail.yourdomain.com
EMAIL_SMTP_PORT=587
EMAIL_USERNAME=noreply@yourdomain.com
EMAIL_PASSWORD=your_password
```

### DKIM Setup
1. **Add DNS TXT Record**:
   ```
   Name: default._domainkey
   Type: TXT
   Value: v=DKIM1;k=rsa;p=MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvgn5VVvCnMnAHOk7TBQ1Fq3auIa+CaZeHMz3gHOwkiIA5IZPXmho3BHuxCXzo7I3PnLsiZA18TOQQqVvhVbNU7aRAdaKDsz5q4KmPuHAQkHqPj6aSRmGUtYzeRUxzuc8ys8w9Eff2QpCICF1ArRlVdPIJPgJIftk8ByrKao+qwB+Cjemb5K7cya4i/ssVf9Hm2VH7cGOlmRluBY1VTvUeNA5Gr9d7alGrlYBZkmYCX2g/gZ9FEpPNamlS4n/t/SiKtHACZW3i9QaGnglo616+KakVn9kGeWaQ8m3Wxxo43IdPd5CZMBVP8Ji9mbXRNbRhY3E/ptnMD1eE9maCisNoQIDAQAB
   ```

2. **Enable DKIM**:
   ```bash
   DKIM_ENABLED=true
   DKIM_DOMAIN=yourdomain.com
   DKIM_SELECTOR=default
   ```

## 🔧 Marketing API Setup

### Facebook/Meta Ads API
1. **Create Facebook App**:
   - Go to [Facebook Developers](https://developers.facebook.com)
   - Create new app → Business → Marketing API
   - Get App ID and App Secret

2. **Get Access Token**:
   - Use Facebook Graph API Explorer
   - Select your app and get user access token
   - Convert to long-lived token

3. **Set Variables**:
   ```bash
   FACEBOOK_ACCESS_TOKEN=your_long_lived_token
   FACEBOOK_AD_ACCOUNT_ID=act_1234567890
   FACEBOOK_PAGE_ID=your_page_id
   ```

### Google Ads API
1. **Enable Google Ads API**:
   - Go to [Google Cloud Console](https://console.cloud.google.com)
   - Enable Google Ads API
   - Create OAuth 2.0 credentials

2. **Get Developer Token**:
   - Apply for developer token in Google Ads account
   - Usually approved instantly for test accounts

3. **Set Variables**:
   ```bash
   GOOGLE_ADS_DEVELOPER_TOKEN=your_dev_token
   GOOGLE_ADS_CLIENT_ID=your_client_id
   GOOGLE_ADS_CLIENT_SECRET=your_client_secret
   GOOGLE_ADS_REFRESH_TOKEN=your_refresh_token
   GOOGLE_ADS_CUSTOMER_ID=123-456-7890
   ```

## 🔐 Security Checklist

### Application Security
- [ ] Change default admin password
- [ ] Use strong database passwords
- [ ] Enable HTTPS (handled by Render/Heroku)
- [ ] Set up DKIM for email authentication
- [ ] Configure CORS if needed
- [ ] Review file upload restrictions

### Infrastructure Security
- [ ] Use environment variables for secrets
- [ ] Enable database backups
- [ ] Set up monitoring and alerts
- [ ] Configure firewall rules (if using VPS)
- [ ] Keep Java and dependencies updated

## 📊 Monitoring & Maintenance

### Health Checks
The application provides health check endpoints:
- `/actuator/health` - Application health status
- `/actuator/info` - Application information

### Logging
Application logs include:
- Startup and shutdown events
- Email sending status
- Campaign creation and publishing
- Error stack traces

### Backups
- **Database**: Set up automated PostgreSQL backups
- **File Uploads**: Back up `uploads/` directory
- **Configuration**: Keep environment variables documented

## 😨 Troubleshooting

### Common Deployment Issues

**Build Fails**
```bash
# Check Java version
java -version
# Should be 11+

# Check Maven version
mvn -version
# Should be 3.6+

# Clean and rebuild
mvn clean package -U
```

**Database Connection**
```bash
# Test connection
psql -h hostname -U username -d seller_funnel
# Should connect successfully

# Check application.properties
grep datasource src/main/resources/application.properties
```

**Email Not Sending**
- Verify SMTP credentials are correct
- Check if 2FA and app passwords are set up
- Test DKIM record with online tools
- Check application logs for email errors

**Marketing APIs Not Working**
- Verify API tokens are not expired
- Check API rate limits and quotas
- Ensure proper permissions are granted
- Test API calls manually with curl

### Log Analysis
```bash
# View application logs (if using systemd)
sudo journalctl -u realestate-crm -f

# Check database logs
sudo tail -f /var/log/postgresql/postgresql-*.log

# Monitor resource usage
htop
```

## 📈 Scaling Considerations

### Performance Optimization
- Use connection pooling for database
- Enable gzip compression
- Implement caching for static resources
- Consider CDN for file uploads

### High Availability
- Use managed database services
- Implement health checks and auto-restart
- Consider load balancing for high traffic
- Set up monitoring and alerting

### Cost Optimization
- Use free tiers when possible
- Monitor API usage and costs
- Implement rate limiting
- Optimize database queries

---

## ✅ Post-Deployment Checklist

- [ ] Application starts successfully
- [ ] Database connection works
- [ ] Admin login works
- [ ] Email sending works (test with Email Setup)
- [ ] Campaign creation works
- [ ] Lead forms work
- [ ] File uploads work
- [ ] Marketing APIs connected (if configured)
- [ ] HTTPS certificate valid
- [ ] Domain name configured
- [ ] Backups configured
- [ ] Monitoring set up

**Your Real Estate CRM is now live and ready to generate leads!** 🎉