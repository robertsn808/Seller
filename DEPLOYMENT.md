# Deployment Guide - Render

This guide will help you deploy your Real Estate Connect application to Render.

## Prerequisites

- GitHub repository with your code (✅ Already done)
- Render account (free tier available)

## Step-by-Step Deployment

### 1. Sign Up for Render
- Go to [render.com](https://render.com)
- Sign up using your GitHub account

### 2. Connect Your Repository
1. Click "New +" in Render dashboard
2. Select "Web Service"
3. Connect your GitHub account if not already connected
4. Select your `Seller` repository

### 3. Configure the Web Service
Use these settings when creating your web service:

**Basic Settings:**
- **Name**: `seller-funnel` (or any name you prefer)
- **Runtime**: `Java`
- **Branch**: `master`
- **Root Directory**: Leave blank
- **Build Command**: `mvn clean package -DskipTests`
- **Start Command**: `java -Dserver.port=$PORT -jar target/seller-funnel-1.0-SNAPSHOT.jar --spring.profiles.active=render`

**Advanced Settings:**
- **Plan**: Free (or upgrade as needed)
- **Auto-Deploy**: Yes (recommended)

### 4. Create PostgreSQL Database
1. In Render dashboard, click "New +" again
2. Select "PostgreSQL"
3. Use these settings:
   - **Name**: `seller-funnel-db`
   - **Database**: `seller_funnel`
   - **User**: `seller_user`
   - **Plan**: Free

### 5. Configure Environment Variables
After creating both services, go to your web service settings and add these environment variables:

- `SPRING_DATASOURCE_URL`: Copy from your PostgreSQL database "External Database URL"
- `SPRING_DATASOURCE_USERNAME`: Copy from your PostgreSQL database
- `SPRING_DATASOURCE_PASSWORD`: Copy from your PostgreSQL database
- `SPRING_PROFILES_ACTIVE`: `render`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: `update`

### 6. Deploy
1. Your app will automatically deploy after configuration
2. Wait for build to complete (5-10 minutes first time)
3. Your app will be available at `https://your-app-name.onrender.com`

## Alternative: Use render.yaml (Recommended)

The repository includes a `render.yaml` file that automates this process:

1. In Render dashboard, click "New +" 
2. Select "Blueprint"
3. Connect your repository
4. Render will automatically create both the web service and database using the configuration in `render.yaml`

## Post-Deployment

### Test Your Application
1. Visit your Render URL
2. Test buyer form submission
3. Test seller form with photo upload
4. Check admin dashboard at `/admin`

### Monitor Logs
- View logs in Render dashboard under your web service
- Check for any database connection issues

### Custom Domain (Optional)
- In Render dashboard, go to Settings > Custom Domains
- Add your own domain name

## Important Notes

- **File Uploads**: Photos are stored in `/tmp/` on Render's ephemeral filesystem. Consider upgrading to a cloud storage solution for production.
- **Database**: Free PostgreSQL has limits. Monitor usage in Render dashboard.
- **SSL**: Render provides free SSL certificates automatically.
- **Scaling**: Free tier sleeps after 15 minutes of inactivity. Paid plans stay always on.

## Troubleshooting

### Build Issues
- Check Java version is 17 in logs
- Ensure Maven dependencies download correctly
- Review build command in service settings

### Database Connection Issues
- Verify environment variables are set correctly
- Check PostgreSQL database is running
- Review database logs in Render dashboard

### Application Issues
- Check application logs for Spring Boot startup errors
- Verify all required tables are created
- Test database connectivity

## Support

If you encounter issues:
1. Check Render documentation
2. Review application logs
3. Verify environment variables
4. Contact Render support (they're very helpful!)

Your Real Estate Connect funnel will be live and ready for users! 🚀