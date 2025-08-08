# Property Management System Troubleshooting

## 🚨 Common Issues and Solutions

### Issue: Error Page After Login to Property Management Dashboard

**Symptoms:**
- You can log into the property management system
- But get an error page when trying to access the dashboard
- Error might mention database tables or missing entities

**Root Cause:**
The database schema hasn't been updated to include the new Payment tables and related entities that were added with the Universal Payment Protocol integration.

### 🔧 Solutions

#### Solution 1: Restart Application (Recommended)

The simplest fix is to restart your Spring Boot application:

1. **Stop the application** (Ctrl+C if running in terminal)
2. **Start the application again**:
   ```bash
   mvn spring-boot:run
   ```

**Why this works:** 
- Spring Boot with `spring.jpa.hibernate.ddl-auto=create-drop` will recreate all tables
- This includes the new Payment, Room, Guest, and Booking tables
- All relationships and constraints will be properly created

#### Solution 2: Check Application Logs

If restarting doesn't work, check the console logs for specific errors:

1. Look for error messages containing:
   - `Table 'payments' doesn't exist`
   - `Column not found`
   - `JPA` or `Hibernate` errors
   - `SQL` syntax errors

2. Common error patterns:
   ```
   org.springframework.dao.InvalidDataAccessResourceUsageException
   javax.persistence.PersistenceException
   org.hibernate.exception.SQLGrammarException
   ```

#### Solution 3: Database Reset (If using H2 in-memory)

If you're using the default H2 in-memory database:

1. **Stop the application**
2. **Clear any persistent H2 files** (if any exist)
3. **Restart the application**

The H2 database will be completely recreated with the new schema.

#### Solution 4: Manual Database Update (PostgreSQL)

If using PostgreSQL and you want to keep existing data:

1. **Connect to your database**
2. **Run these SQL commands**:

```sql
-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_method VARCHAR(50),
    device_type VARCHAR(50),
    device_id VARCHAR(255),
    upp_transaction_id VARCHAR(255),
    stripe_payment_intent_id VARCHAR(255),
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    description TEXT,
    customer_email VARCHAR(255),
    metadata TEXT,
    processed_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Add any missing columns to existing tables
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS current_balance DECIMAL(10,2) DEFAULT 0;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS total_charges DECIMAL(10,2) DEFAULT 0;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS total_payments DECIMAL(10,2) DEFAULT 0;
```

#### Solution 5: Configuration Check

Verify your `application.properties` configuration:

```properties
# For development (H2 in-memory)
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# For production (PostgreSQL)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

### 🔍 Diagnostic Steps

#### Step 1: Check Database Connection
1. Access H2 Console: `http://localhost:8080/h2-console`
2. Use JDBC URL: `jdbc:h2:mem:testdb`
3. Username: `sa`, Password: (empty)
4. Check if tables exist: `SHOW TABLES;`

#### Step 2: Verify Required Tables
The following tables should exist:
- `rooms`
- `guests` 
- `bookings`
- `payments` (new)
- `clients`
- `buyers`
- `sellers`

#### Step 3: Check Table Structure
```sql
DESCRIBE payments;
DESCRIBE bookings;
DESCRIBE rooms;
DESCRIBE guests;
```

### 🚀 Prevention

To avoid this issue in the future:

1. **Always restart the application** after adding new entities
2. **Use database migration tools** like Flyway for production
3. **Test in development** before deploying to production
4. **Backup your data** before major schema changes

### 📞 Still Having Issues?

If none of these solutions work:

1. **Check the full error stack trace** in your application logs
2. **Verify all dependencies** are properly installed
3. **Ensure Java version compatibility** (Java 17+)
4. **Check for port conflicts** (default: 8080)

### 🎯 Quick Test

After fixing the issue, test these URLs:

- ✅ Property Login: `http://localhost:8080/property/login`
- ✅ Property Dashboard: `http://localhost:8080/property/dashboard`
- ✅ Rooms Management: `http://localhost:8080/property/rooms`
- ✅ Payments System: `http://localhost:8080/property/payments`

### 🌊 UPP Integration Test

To test the Universal Payment Protocol integration:

1. **Start UPP Server**:
   ```bash
   cd temp-upp
   npm run dev
   ```

2. **Check UPP Status**: `http://localhost:8080/property/upp/status`

3. **Process a test payment** through the payment form

---

**💡 Pro Tip:** The error handling has been improved in the dashboard to show a user-friendly message when database issues occur, so you'll see a clear indication of what needs to be fixed.

