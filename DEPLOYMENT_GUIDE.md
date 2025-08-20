# Deployment Guide

This guide walks you through deploying the Unified CRM platform to production.

## Quick Deploy Options

### Option 1: GitHub + Render + Vercel (Recommended)

1. **Push to GitHub**:
   ```bash
   # Create new repository on GitHub first, then:
   git remote add origin https://github.com/your-username/unified-crm.git
   git branch -M main
   git push -u origin main
   ```

2. **Deploy Backend to Render**:
   - Connect your GitHub repository to Render
   - Create a new Web Service
   - Set build command: `cd backend && ./mvnw clean package -DskipTests`
   - Set start command: `cd backend && java -jar target/*.jar`
   - Add environment variables (see below)

3. **Deploy Frontend to Vercel**:
   - Connect your GitHub repository to Vercel
   - Set root directory to `frontend`
   - Add environment variable: `VITE_API_BASE_URL=https://your-render-app.onrender.com`

### Option 2: Docker Compose (Local/VPS)

```bash
# Clone your repository
git clone https://github.com/your-username/unified-crm.git
cd unified-crm

# Start all services
docker-compose up -d

# Services will be available at:
# Frontend: http://localhost:3000
# Backend: http://localhost:8080
# Database: localhost:5432
```

### Option 3: Manual Deployment

#### Backend Setup
```bash
cd backend
./mvnw clean package -DskipTests
java -jar target/seller-funnel-*.jar
```

#### Frontend Setup
```bash
cd frontend
npm install
npm run build
# Serve the dist/ folder with nginx or any static server
```

## Environment Variables

### Backend Environment Variables

```bash
# Database
SPRING_PROFILES_ACTIVE=postgres
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/unified_crm
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

# Security
APP_ADMIN_USERNAME=admin
APP_ADMIN_PASSWORD=change_this_password

# AI Services
OPENAI_API_KEY=sk-your-openai-api-key

# Email (Optional)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password

# SMS (Optional)
TWILIO_ACCOUNT_SID=your-twilio-sid
TWILIO_AUTH_TOKEN=your-twilio-token
TWILIO_PHONE_NUMBER=+1234567890

# Marketing APIs (Optional)
FACEBOOK_APP_ID=your-facebook-app-id
FACEBOOK_APP_SECRET=your-facebook-app-secret
GOOGLE_ADS_CLIENT_ID=your-google-ads-client-id
GOOGLE_ADS_CLIENT_SECRET=your-google-ads-client-secret
```

### Frontend Environment Variables

```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080

# For production:
VITE_API_BASE_URL=https://your-backend-domain.com
```

## Database Setup

### PostgreSQL Setup

1. **Create Database**:
   ```sql
   CREATE DATABASE unified_crm;
   CREATE USER crm_user WITH PASSWORD 'secure_password';
   GRANT ALL PRIVILEGES ON DATABASE unified_crm TO crm_user;
   ```

2. **Configure Connection**:
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/unified_crm
   export SPRING_DATASOURCE_USERNAME=crm_user
   export SPRING_DATASOURCE_PASSWORD=secure_password
   ```

### Development Database (H2)

For development, the application uses H2 by default:
- No setup required
- Data is reset on restart
- Access H2 console at: http://localhost:8080/h2-console

## Production Checklist

### Security
- [ ] Change default admin password
- [ ] Configure HTTPS/SSL certificates
- [ ] Set up firewall rules
- [ ] Enable CSRF protection in production
- [ ] Configure CORS for production domains
- [ ] Set secure JWT secret key

### Performance
- [ ] Configure database connection pooling
- [ ] Set up CDN for static assets
- [ ] Enable gzip compression
- [ ] Configure database indexes
- [ ] Set up Redis for session storage (optional)

### Monitoring
- [ ] Set up application logs
- [ ] Configure health checks
- [ ] Set up error tracking (Sentry, etc.)
- [ ] Configure metrics collection
- [ ] Set up backup strategy

### Deployment
- [ ] Configure CI/CD pipeline
- [ ] Set up staging environment
- [ ] Test database migrations
- [ ] Configure environment variables
- [ ] Test all integrations

## Render Deployment Configuration

### render.yaml
```yaml
services:
  - type: web
    name: unified-crm-api
    runtime: java
    buildCommand: cd backend && ./mvnw clean package -DskipTests
    startCommand: cd backend && java -jar target/seller-funnel-*.jar
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: postgres
      - key: JAVA_TOOL_OPTIONS
        value: -Xmx512m
    autoDeploy: true

databases:
  - name: unified-crm-db
    databaseName: unified_crm
    user: unified_crm_user
    region: oregon
```

### Vercel Configuration

#### vercel.json
```json
{
  "version": 2,
  "builds": [
    {
      "src": "package.json",
      "use": "@vercel/static-build",
      "config": {
        "distDir": "dist"
      }
    }
  ],
  "routes": [
    {
      "src": "/assets/(.*)",
      "headers": {
        "cache-control": "max-age=31536000, immutable"
      }
    },
    {
      "src": "/(.*)",
      "dest": "/index.html"
    }
  ]
}
```

## Troubleshooting

### Common Issues

1. **Backend won't start**:
   - Check Java version (requires Java 21)
   - Verify database connection
   - Check environment variables

2. **Frontend build fails**:
   - Run `npm install` to ensure dependencies
   - Check Node.js version (requires 18+)
   - Verify API URL configuration

3. **Database connection issues**:
   - Verify PostgreSQL is running
   - Check connection string format
   - Ensure user has proper permissions

4. **CORS errors**:
   - Verify frontend URL is in CORS configuration
   - Check that API URL is correct in frontend
   - Ensure both HTTP and HTTPS variants are configured

### Performance Issues

1. **Slow API responses**:
   - Check database indexes
   - Enable connection pooling
   - Monitor database queries

2. **High memory usage**:
   - Configure JVM heap size
   - Check for memory leaks
   - Optimize database queries

## Scaling Considerations

### Horizontal Scaling
- Use load balancer for multiple backend instances
- Implement Redis for session sharing
- Configure database read replicas

### Vertical Scaling
- Increase server resources
- Optimize JVM parameters
- Tune database configuration

## Backup Strategy

### Database Backups
```bash
# Daily backup script
pg_dump unified_crm > backup_$(date +%Y%m%d).sql

# Restore from backup
psql unified_crm < backup_20241201.sql
```

### File Uploads Backup
```bash
# Backup uploaded files
tar -czf uploads_backup.tar.gz backend/uploads/
```

## Support

For deployment issues:
1. Check application logs
2. Verify environment variables
3. Test database connectivity
4. Review this deployment guide
5. Open issue on GitHub repository