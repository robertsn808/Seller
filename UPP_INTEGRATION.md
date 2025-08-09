# Universal Payment Protocol (UPP) Integration

## Overview

The Property Management System now includes full integration with the Universal Payment Protocol (UPP), allowing you to accept payments from any internet-connected device. This revolutionary payment system transforms smartphones, smart TVs, IoT devices, voice assistants, and more into payment terminals.

## 🌊 What is UPP?

Universal Payment Protocol is a middleware protocol that translates between any device and payment systems, similar to how Ivy allows different Python ML libraries to communicate seamlessly. UPP makes any internet-connected device into a payment terminal.

### Key Features

- **📱 Universal Device Support**: Smartphones, Smart TVs, IoT devices, Voice assistants, Gaming consoles
- **🌐 Internet-Only Requirement**: No special hardware needed - just an internet connection
- **💰 Lower Fees**: 2.5% vs industry standard 2.9%
- **🏝️ Hawaii-Based**: Serving underserved Pacific markets first
- **🔓 Open Source**: MIT licensed - make money and contribute back

## 🏗️ System Architecture

### Database Models

#### Payment Entity
```java
- id: Long (Primary Key)
- booking: Booking (ManyToOne)
- amount: BigDecimal
- currency: String (default: USD)
- paymentMethod: String (CARD, CASH, TRANSFER, UPP_DEVICE)
- deviceType: String (smartphone, smart_tv, iot_device, etc.)
- deviceId: String
- uppTransactionId: String
- stripePaymentIntentId: String
- paymentStatus: String (PENDING, COMPLETED, FAILED, REFUNDED)
- description: String
- customerEmail: String
- metadata: String (JSON)
- processedAt: LocalDateTime
- isActive: Boolean
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### Services

#### UniversalPaymentProtocolService
- **processPayment()**: Process payments through UPP API
- **registerDevice()**: Register devices with UPP system
- **getDeviceCapabilities()**: Get device capabilities
- **isServiceHealthy()**: Check UPP service health
- **getSupportedCurrencies()**: Get supported currencies

#### PaymentService
- **processPayment()**: Main payment processing logic
- **processUppPayment()**: Handle UPP-specific payments
- **processTraditionalPayment()**: Handle traditional payments
- **getPaymentStatistics()**: Get payment analytics
- **refundPayment()**: Process refunds

### Controllers

#### PropertyManagementController (New Endpoints)
- `GET /property/payments` - List all payments
- `GET /property/payments/{id}` - View payment details
- `GET /property/bookings/{bookingId}/payments/new` - New payment form
- `POST /property/bookings/{bookingId}/payments` - Process payment
- `POST /property/payments/{id}/refund` - Process refund
- `GET /property/upp/status` - UPP service status
- `POST /property/upp/register-device` - Register UPP device

## 🚀 Getting Started

### 1. UPP Server Setup

First, you need to set up the UPP server:

```bash
# Clone the UPP repository
git clone https://github.com/robertsn808/UniversalPaymentProtocol.git
cd UniversalPaymentProtocol

# Install dependencies
npm install

# Set up environment variables
cp .env.example .env
# Edit .env with your Stripe keys

# Start the UPP server
npm run dev
```

### 2. Configuration

Add UPP configuration to your `application.properties`:

```properties
# Universal Payment Protocol Configuration
upp.api.base-url=${UPP_API_BASE_URL:http://localhost:3000}
upp.api.device-id=${UPP_DEVICE_ID:property_management_system}
upp.api.device-type=${UPP_DEVICE_TYPE:smartphone}
```

### 3. Environment Variables

Set these environment variables for production:

```bash
UPP_API_BASE_URL=https://your-upp-server.com
UPP_DEVICE_ID=your_property_management_system
UPP_DEVICE_TYPE=smartphone
```

## 💳 Payment Processing Workflow

### 1. Traditional Payment Flow
```
Guest → Property Manager → Payment Form → Traditional Payment → Booking Updated
```

### 2. UPP Payment Flow
```
Guest → Property Manager → UPP Payment Form → Device Selection → UPP API → Stripe → Booking Updated
```

### 3. Device Registration Flow
```
Device → UPP Registration → Device Attestation → Trust Score → Device ID → Payment Processing
```

## 📱 Supported Device Types

| Device Type | Status | Input Methods | Use Cases |
|-------------|--------|---------------|-----------|
| 📱 Smartphones | ✅ Live | NFC, QR, Voice, Touch, Biometric | Guest check-in, mobile payments |
| 📺 Smart TVs | ✅ Live | QR Display, Remote Control | Hotel room payments, entertainment |
| 🏠 IoT Devices | ✅ Live | Sensors, Buttons, Automation | Automated payments, smart rooms |
| 🎤 Voice Assistants | ✅ Live | Natural Language | Hands-free payments |
| 🎮 Gaming Consoles | ✅ Live | Controller Navigation | Gaming room payments |
| ⌚ Smartwatches | 🚧 Coming Soon | Touch, Voice, Haptic | Quick payments |
| 🚗 Car Systems | 🚧 Coming Soon | Voice, Touch, Integration | Drive-through payments |

## 🎯 Usage Examples

### Processing a Smartphone Payment

1. **Navigate to Booking**: Go to `/property/bookings/{id}`
2. **Click "Process Payment"**: Select UPP Device payment method
3. **Choose Device**: Select "Smartphone" from device options
4. **Enter Amount**: Set payment amount
5. **Process**: Payment is sent to UPP API, processed through Stripe
6. **Confirmation**: Payment status updated in booking

### Processing a Smart TV Payment

1. **Guest in Room**: Guest wants to pay for additional services
2. **TV Interface**: Smart TV displays payment QR code
3. **Guest Scans**: Guest scans QR with smartphone
4. **Payment Processed**: UPP handles device translation
5. **Confirmation**: TV displays payment confirmation

### IoT Device Payment

1. **Automated Trigger**: IoT sensor detects service usage
2. **Payment Request**: Device automatically requests payment
3. **UPP Processing**: Payment processed through IoT adapter
4. **Confirmation**: Device LED confirms payment

## 🔧 API Integration

### UPP API Endpoints

The system integrates with these UPP API endpoints:

```typescript
// Process Payment
POST /api/v1/payments/process
{
  "amount": 25.99,
  "deviceType": "smartphone",
  "deviceId": "phone_123",
  "description": "Room payment",
  "customerEmail": "guest@example.com"
}

// Register Device
POST /api/v1/devices/register
{
  "deviceType": "smartphone",
  "capabilities": ["nfc", "camera", "biometric"],
  "fingerprint": "device_unique_id"
}

// Get Device Capabilities
GET /api/v1/devices/{deviceType}/capabilities

// Health Check
GET /health
```

### Response Handling

```java
// Success Response
{
  "success": true,
  "transaction_id": "upp_txn_123",
  "payment_intent_id": "pi_stripe_456",
  "amount": 25.99,
  "currency": "USD",
  "status": "completed",
  "deviceType": "smartphone",
  "riskScore": 85
}

// Error Response
{
  "success": false,
  "error": "Payment failed",
  "message": "Insufficient funds"
}
```

## 📊 Payment Analytics

### Dashboard Statistics

The payment system provides comprehensive analytics:

- **Completed Payments**: Total successful payments
- **UPP Payments**: Payments processed through UPP
- **Pending Payments**: Payments awaiting processing
- **Failed Payments**: Failed payment attempts
- **Total UPP Revenue**: Revenue from UPP payments

### Payment Methods Breakdown

- **Universal Payment Device**: UPP-enabled devices
- **Credit/Debit Card**: Traditional card payments
- **Cash**: Cash payments
- **Bank Transfer**: Transfer payments

### Device Type Analytics

- **Smartphone**: Most common UPP device
- **Smart TV**: Hotel room payments
- **IoT Device**: Automated payments
- **Voice Assistant**: Hands-free payments

## 🔒 Security Features

### Device Attestation

- **Device Fingerprinting**: Unique device identification
- **Trust Scoring**: Device reliability assessment
- **Capability Validation**: Device feature verification

### Fraud Detection

- **Risk Scoring**: Transaction risk assessment
- **IP Validation**: Geographic location verification
- **Device History**: Previous transaction analysis

### Payment Security

- **Stripe Integration**: PCI-compliant payment processing
- **Encryption**: End-to-end data encryption
- **Tokenization**: Secure payment token handling

## 🛠️ Troubleshooting

### Common Issues

#### UPP Service Unavailable
```
Error: UPP API connection failed
Solution: Check UPP server status and network connectivity
```

#### Device Registration Failed
```
Error: Device attestation failed
Solution: Verify device capabilities and trust score
```

#### Payment Processing Failed
```
Error: Payment confirmation failed
Solution: Check Stripe configuration and payment method
```

### Health Checks

```bash
# Check UPP service health
curl http://localhost:3000/health

# Check device capabilities
curl http://localhost:3000/api/v1/devices/smartphone/capabilities

# Test payment processing
curl -X POST http://localhost:3000/api/v1/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 10.00,
    "deviceType": "smartphone",
    "deviceId": "test_device",
    "description": "Test payment"
  }'
```

## 🚀 Deployment

### Production Setup

1. **UPP Server**: Deploy UPP server to production
2. **Environment Variables**: Configure production UPP settings
3. **SSL Certificates**: Ensure secure communication
4. **Monitoring**: Set up health monitoring
5. **Backup**: Configure payment data backup

### Docker Deployment

```dockerfile
# UPP Server
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 3000
CMD ["npm", "start"]
```

### Environment Configuration

```bash
# Production UPP Configuration
UPP_API_BASE_URL=https://upp.yourdomain.com
UPP_DEVICE_ID=production_property_system
UPP_DEVICE_TYPE=smartphone
STRIPE_SECRET_KEY=sk_live_your_stripe_key
```

## 📈 Business Benefits

### Revenue Growth

- **40% Revenue Increase**: Reported by Hawaii coffee shop
- **Faster Payments**: Reduced payment processing time
- **Higher Conversion**: Multiple payment options

### Operational Efficiency

- **Reduced Hardware Costs**: No special payment terminals needed
- **Automated Processing**: IoT device automation
- **24/7 Availability**: Always-on payment processing

### Customer Experience

- **Multiple Payment Options**: Any device, anywhere
- **Faster Check-in**: Quick mobile payments
- **Convenient Experience**: Voice and touch payments

## 🔮 Future Enhancements

### Planned Features

- **AI Fraud Detection**: Machine learning fraud prevention
- **Blockchain Integration**: Decentralized payment processing
- **Advanced IoT Support**: More device types
- **Real-time Analytics**: Live payment monitoring

### Device Expansion

- **Smartwatches**: Wearable payment processing
- **Car Systems**: Automotive payment integration
- **AR/VR Devices**: Immersive payment experiences
- **Smart Appliances**: Home automation payments

## 📞 Support

### Documentation

- **UPP White Paper**: [WHITEPAPER.md](https://github.com/robertsn808/UniversalPaymentProtocol/blob/main/WHITEPAPER.md)
- **API Documentation**: [API Reference](https://github.com/robertsn808/UniversalPaymentProtocol#api-reference)
- **Demo Examples**: [Live Demos](https://github.com/robertsn808/UniversalPaymentProtocol#live-demo-examples)

### Community

- **GitHub Issues**: [Report Issues](https://github.com/robertsn808/UniversalPaymentProtocol/issues)
- **Discussions**: [Community Forum](https://github.com/robertsn808/UniversalPaymentProtocol/discussions)
- **Email Support**: contact@universalpaymentprotocol.com

### Location

- **Hawaii, USA** 🏝️
- **Pacific Markets Focus**
- **Global Availability**

---

**Built with ❤️ by Kai 🌊**

*Making payments universal, one device at a time.*
