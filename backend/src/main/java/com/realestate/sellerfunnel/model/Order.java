package com.realestate.sellerfunnel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import org.hibernate.annotations.Type;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "orders")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "business_id", nullable = false)
    private String businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "client_id")
    private Long clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Client client;

    @NotBlank
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @NotBlank
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "order_type")
    private String orderType; // 'PROPERTY_SALE', 'PROPERTY_PURCHASE', 'RESTAURANT_ORDER', 'SERVICE', etc.

    // Generic items field to store order details as JSON
    @Column(name = "items", columnDefinition = "TEXT")
    private String items; // JSON string of order items

    @NotNull
    @DecimalMin("0.0")
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "order_status")
    private String status = "PENDING"; // PENDING, PROCESSING, COMPLETED, CANCELLED, REFUNDED

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "estimated_completion_time")
    private Integer estimatedTime; // in minutes for restaurant orders, days for real estate

    @Column(length = 1000)
    private String notes;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_status")
    private String paymentStatus = "PENDING"; // PENDING, PAID, FAILED, REFUNDED

    @Column(name = "delivery_method")
    private String deliveryMethod; // 'PICKUP', 'DELIVERY', 'IN_PERSON', 'DIGITAL'

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "property_address", length = 500)
    private String propertyAddress; // For real estate transactions

    @Column(name = "contract_terms", columnDefinition = "TEXT")
    private String contractTerms; // For real estate transactions

    @Column(name = "commission_amount", precision = 10, scale = 2)
    private BigDecimal commissionAmount; // For real estate transactions

    @Column(name = "commission_percentage", precision = 5, scale = 2)
    private BigDecimal commissionPercentage; // For real estate transactions

    @Column(name = "closing_date")
    private LocalDateTime closingDate; // For real estate transactions

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Order() {}

    public Order(String businessId, String customerName, String customerEmail, 
                 BigDecimal total, String orderType) {
        this.businessId = businessId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.total = total;
        this.orderType = orderType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public Integer getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(Integer estimatedTime) { this.estimatedTime = estimatedTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPropertyAddress() { return propertyAddress; }
    public void setPropertyAddress(String propertyAddress) { this.propertyAddress = propertyAddress; }

    public String getContractTerms() { return contractTerms; }
    public void setContractTerms(String contractTerms) { this.contractTerms = contractTerms; }

    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }

    public BigDecimal getCommissionPercentage() { return commissionPercentage; }
    public void setCommissionPercentage(BigDecimal commissionPercentage) { this.commissionPercentage = commissionPercentage; }

    public LocalDateTime getClosingDate() { return closingDate; }
    public void setClosingDate(LocalDateTime closingDate) { this.closingDate = closingDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isRealEstateTransaction() {
        return "PROPERTY_SALE".equals(orderType) || "PROPERTY_PURCHASE".equals(orderType);
    }

    public boolean isRestaurantOrder() {
        return "RESTAURANT_ORDER".equals(orderType) || "FOOD_ORDER".equals(orderType);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
}