# Property Management Portal

## Overview

The Property Management Portal is a comprehensive system designed to manage 8 rooms with full guest tracking, booking management, payment processing, and access control. This system operates independently from the main real estate application with its own login and dedicated functionality.

## Features

### 🏠 Room Management
- **8 Room Support**: Manage exactly 8 rooms with individual configurations
- **Vacancy Tracking**: Real-time status of vacant vs. occupied rooms
- **Room Types**: Single, Double, Suite, Studio, Deluxe
- **Base Rate Management**: Set and track nightly rates for each room
- **Door Code Management**: 
  - Current door code for guest access
  - Reset code for administrative access
- **Gate Key Assignment**: Track which rooms have gate keys assigned and key numbers

### 👥 Guest Management
- **Comprehensive Guest Profiles**:
  - Personal information (name, email, phone)
  - Identification (ID type, ID number)
  - Vehicle information (make, model, license plate)
  - Emergency contact details
  - Special requests and notes
- **Guest Search**: Find guests by name, email, phone, or vehicle
- **Duplicate Prevention**: Email-based duplicate detection

### 📅 Booking Management
- **Check-in/Check-out Tracking**: Automatic date tracking
- **Payment Frequency**: Daily, Weekly, or Monthly payment schedules
- **Balance Management**: 
  - Running balance calculation
  - Total charges vs. total payments
  - Outstanding balance tracking
- **Payment Status**: Pending, Paid, Partial, Overdue
- **Booking Status**: Active, Completed, Cancelled
- **Night Count**: Automatic calculation of nights stayed

### 💰 Financial Management
- **Real-time Balance Tracking**: Live updates of guest balances
- **Payment Processing**: Add payments and track history
- **Charge Management**: Add additional charges with descriptions
- **Outstanding Balance Reports**: Identify overdue accounts
- **Revenue Tracking**: Total charges and payments by period

### 🔐 Access Control
- **Door Codes**: Individual codes for each room
- **Reset Codes**: Administrative override codes
- **Gate Key Management**: Track key assignments and numbers
- **Security Logging**: All access changes are timestamped

## System Architecture

### Database Models

#### Room Entity
```java
- id: Long (Primary Key)
- roomNumber: String (Unique)
- roomName: String (Optional friendly name)
- roomType: String (Single, Double, Suite, etc.)
- baseRate: BigDecimal
- isVacant: Boolean
- currentCode: String (Door code)
- resetCode: String (Admin reset code)
- gateKeyAssigned: Boolean
- gateKeyNumber: String
- notes: String
- isActive: Boolean
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

#### Guest Entity
```java
- id: Long (Primary Key)
- firstName: String
- lastName: String
- email: String (Unique)
- phoneNumber: String
- idNumber: String
- idType: String (Driver's License, Passport, etc.)
- vehicleLicensePlate: String
- vehicleMakeModel: String
- emergencyContactName: String
- emergencyContactPhone: String
- emergencyContactRelationship: String
- specialRequests: String
- isActive: Boolean
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

#### Booking Entity
```java
- id: Long (Primary Key)
- room: Room (ManyToOne)
- guest: Guest (ManyToOne)
- checkInDate: LocalDateTime
- checkOutDate: LocalDateTime
- expectedCheckOutDate: LocalDateTime
- nightlyRate: BigDecimal
- paymentFrequency: String (DAILY, WEEKLY, MONTHLY)
- totalCharges: BigDecimal
- totalPayments: BigDecimal
- currentBalance: BigDecimal
- numberOfNights: Integer
- bookingStatus: String (ACTIVE, COMPLETED, CANCELLED)
- paymentStatus: String (PENDING, PAID, PARTIAL, OVERDUE)
- specialInstructions: String
- notes: String
- isActive: Boolean
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### Key Features

#### Automatic Calculations
- **Balance Updates**: Automatically recalculates when payments or charges are added
- **Night Counting**: Calculates nights based on check-in and check-out dates
- **Payment Status**: Updates based on balance (Paid when balance ≤ 0)
- **Room Vacancy**: Automatically updates when bookings are created/completed

#### Smart Validation
- **Room Availability**: Prevents double-booking of rooms
- **Guest Duplicates**: Prevents duplicate guest records by email
- **Room Number Uniqueness**: Ensures unique room numbers
- **Data Integrity**: Validates all required fields and relationships

#### Search and Filtering
- **Room Search**: By room number, name, or type
- **Guest Search**: By name, email, phone, or vehicle
- **Booking Search**: By guest, room, or status
- **Advanced Filtering**: By payment status, booking status, date ranges

## User Interface

### Dashboard
- **Statistics Overview**: Vacant/occupied rooms, active bookings, overdue payments
- **Recent Bookings**: Latest booking activity
- **Overdue Payments**: Alerts for outstanding balances
- **Quick Actions**: Direct links to add rooms, guests, or bookings

### Room Management
- **Room Grid**: Visual cards showing room status and key information
- **Room Details**: Complete information including codes and keys
- **Status Indicators**: Color-coded vacant/occupied status
- **Quick Actions**: View, edit, or deactivate rooms

### Guest Management
- **Guest List**: Searchable list of all guests
- **Guest Profiles**: Complete guest information
- **Booking History**: All bookings for each guest
- **Contact Information**: Easy access to guest contact details

### Booking Management
- **Booking List**: All bookings with status and balance information
- **Booking Details**: Complete booking information
- **Payment Management**: Add payments and charges
- **Check-out Process**: Streamlined check-out with automatic room vacancy update

## Security

### Authentication
- **Separate Login**: Independent from main application
- **Session Management**: Secure session handling
- **Access Control**: All property management routes require authentication

### Data Protection
- **Input Validation**: All user inputs are validated
- **SQL Injection Prevention**: Parameterized queries
- **XSS Protection**: Output encoding
- **CSRF Protection**: Form token validation

## Usage Workflow

### Setting Up Rooms
1. **Add Rooms**: Create room records with numbers, types, and base rates
2. **Set Codes**: Assign current door codes and reset codes
3. **Assign Keys**: Track gate key assignments if applicable
4. **Configure Rates**: Set appropriate nightly rates for each room type

### Guest Check-in Process
1. **Create Guest**: Add guest information to the system
2. **Create Booking**: Assign guest to available room
3. **Set Payment Schedule**: Choose daily, weekly, or monthly payments
4. **Provide Access**: Give guest room code and gate key if needed
5. **Track Stay**: Monitor nights and balance

### Payment Management
1. **Record Payments**: Add payments as they are received
2. **Add Charges**: Include any additional charges (damages, late fees, etc.)
3. **Monitor Balance**: Track outstanding balances
4. **Follow Up**: Contact guests with overdue payments

### Check-out Process
1. **Final Payment**: Ensure all charges are paid
2. **Check-out**: Mark booking as completed
3. **Room Reset**: Room automatically becomes vacant
4. **Code Reset**: Change door code for next guest
5. **Key Return**: Update gate key status

## Technical Implementation

### Controllers
- **PropertyManagementController**: Main controller handling all property management operations
- **Room Management**: CRUD operations for rooms
- **Guest Management**: CRUD operations for guests
- **Booking Management**: CRUD operations for bookings with payment processing

### Repositories
- **RoomRepository**: Custom queries for room status and availability
- **GuestRepository**: Search and duplicate detection queries
- **BookingRepository**: Complex queries for financial reporting and status tracking

### Services
- **Automatic Calculations**: Balance updates and night counting
- **Validation Services**: Business rule validation
- **Status Management**: Automatic status updates based on data changes

## Configuration

### Database Setup
The system automatically creates the necessary tables:
- `rooms`: Room information and status
- `guests`: Guest profiles and contact information
- `bookings`: Booking records and financial data

### Security Configuration
- Property management routes require authentication
- Separate from main application security
- Session-based authentication

### Default Settings
- **Payment Status**: New bookings start as "PENDING"
- **Booking Status**: New bookings are "ACTIVE"
- **Room Status**: New rooms are "VACANT"
- **Guest Status**: New guests are "ACTIVE"

## Reporting and Analytics

### Financial Reports
- **Outstanding Balances**: Total money owed
- **Payment Status**: Breakdown by payment status
- **Revenue Tracking**: Charges and payments by period
- **Overdue Accounts**: Guests with outstanding balances

### Operational Reports
- **Room Occupancy**: Vacancy rates and trends
- **Booking Activity**: Recent bookings and patterns
- **Guest Statistics**: Guest demographics and preferences
- **Payment Frequency**: Distribution of payment schedules

## Maintenance and Support

### Data Backup
- Regular database backups recommended
- Export functionality for financial records
- Audit trail for all changes

### System Updates
- Automatic balance calculations
- Real-time status updates
- Automatic night counting

### Troubleshooting
- **Common Issues**: 
  - Duplicate room numbers
  - Double-booked rooms
  - Negative balances
  - Missing guest information
- **Solutions**: Built-in validation prevents most issues

## Future Enhancements

### Potential Features
- **Email Notifications**: Automated payment reminders
- **Mobile App**: Guest check-in/check-out app
- **Integration**: Connect with payment processors
- **Reporting**: Advanced analytics and reporting
- **Multi-property**: Support for multiple properties
- **Inventory**: Track room supplies and maintenance

### Scalability
- **Database Optimization**: Indexed queries for performance
- **Caching**: Session and data caching
- **Load Balancing**: Support for multiple users
- **API Development**: REST API for external integrations

This property management system provides a complete solution for managing 8 rooms with comprehensive guest tracking, financial management, and access control features.
