package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.model.Room;
import com.realestate.sellerfunnel.model.Guest;
import com.realestate.sellerfunnel.model.Booking;
import com.realestate.sellerfunnel.repository.RoomRepository;
import com.realestate.sellerfunnel.repository.GuestRepository;
import com.realestate.sellerfunnel.repository.BookingRepository;
import com.realestate.sellerfunnel.repository.PaymentRepository;
import com.realestate.sellerfunnel.service.PaymentService;
import com.realestate.sellerfunnel.service.UniversalPaymentProtocolService;
import com.realestate.sellerfunnel.model.Payment;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/property")
public class PropertyManagementController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    // Property Management Login
    @GetMapping("/login")
    public String propertyLogin() {
        return "property/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get statistics
        Long totalRooms = roomRepository.count();
        Long vacantRooms = roomRepository.countVacantRooms();
        Long occupiedRooms = roomRepository.countOccupiedRooms();
        Long activeBookings = bookingRepository.countActiveBookings();
        Long pendingPayments = bookingRepository.countPendingPayments();
        Long overduePayments = bookingRepository.countOverduePayments();
        BigDecimal totalOutstanding = bookingRepository.getTotalOutstandingBalance();
        
        // Get recent bookings
        List<Booking> recentBookings = bookingRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        if (recentBookings.size() > 10) {
            recentBookings = recentBookings.subList(0, 10);
        }
        
        // Get overdue bookings
        List<Booking> overdueBookings = bookingRepository.findOverdueBookings();
        
        // Get rooms with outstanding balances
        List<Booking> outstandingBookings = bookingRepository.findBookingsWithOutstandingBalance();
        
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("vacantRooms", vacantRooms);
        model.addAttribute("occupiedRooms", occupiedRooms);
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("pendingPayments", pendingPayments);
        model.addAttribute("overduePayments", overduePayments);
        model.addAttribute("totalOutstanding", totalOutstanding != null ? totalOutstanding : BigDecimal.ZERO);
        model.addAttribute("recentBookings", recentBookings);
        model.addAttribute("overdueBookings", overdueBookings);
        model.addAttribute("outstandingBookings", outstandingBookings);
        
        return "property/dashboard";
    }

    // Room Management
    @GetMapping("/rooms")
    public String listRooms(Model model, @RequestParam(required = false) String search) {
        List<Room> rooms;
        if (search != null && !search.trim().isEmpty()) {
            rooms = roomRepository.findBySearchTerm(search.trim());
        } else {
            rooms = roomRepository.findByIsActiveTrueOrderByRoomNumberAsc();
        }
        
        model.addAttribute("rooms", rooms);
        model.addAttribute("search", search);
        return "property/rooms/list";
    }

    @GetMapping("/rooms/new")
    public String newRoom(Model model) {
        model.addAttribute("room", new Room());
        addRoomFormData(model);
        return "property/rooms/form";
    }

    @GetMapping("/rooms/{id}")
    public String viewRoom(@PathVariable Long id, Model model) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        
        // Get current booking if room is occupied
        Optional<Booking> currentBooking = bookingRepository.findActiveBookingByRoom(room);
        
        // Get booking history
        List<Booking> bookingHistory = bookingRepository.findByRoomAndIsActiveTrueOrderByCreatedAtDesc(room);
        
        model.addAttribute("room", room);
        model.addAttribute("currentBooking", currentBooking.orElse(null));
        model.addAttribute("bookingHistory", bookingHistory);
        return "property/rooms/view";
    }

    @GetMapping("/rooms/{id}/edit")
    public String editRoom(@PathVariable Long id, Model model) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        
        model.addAttribute("room", room);
        addRoomFormData(model);
        return "property/rooms/form";
    }

    @PostMapping("/rooms")
    public String saveRoom(@Valid @ModelAttribute Room room, 
                          BindingResult result, 
                          Model model,
                          RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            addRoomFormData(model);
            return "property/rooms/form";
        }
        
        // Check for duplicate room number
        if (room.getId() == null) { // New room
            if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
                result.rejectValue("roomNumber", "error.roomNumber", "A room with this number already exists");
            }
        } else { // Existing room
            if (roomRepository.existsByRoomNumberAndIdNot(room.getRoomNumber(), room.getId())) {
                result.rejectValue("roomNumber", "error.roomNumber", "A room with this number already exists");
            }
        }
        
        if (result.hasErrors()) {
            addRoomFormData(model);
            return "property/rooms/form";
        }
        
        roomRepository.save(room);
        
        String message = room.getId() == null ? "Room created successfully!" : "Room updated successfully!";
        redirectAttributes.addFlashAttribute("message", message);
        
        return "redirect:/property/rooms";
    }

    @PostMapping("/rooms/{id}/delete")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        
        // Check if room has active bookings
        Optional<Booking> activeBooking = bookingRepository.findActiveBookingByRoom(room);
        if (activeBooking.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete room with active booking");
            return "redirect:/property/rooms/" + id;
        }
        
        room.setIsActive(false);
        roomRepository.save(room);
        
        redirectAttributes.addFlashAttribute("message", "Room deactivated successfully!");
        return "redirect:/property/rooms";
    }

    // Guest Management
    @GetMapping("/guests")
    public String listGuests(Model model, @RequestParam(required = false) String search) {
        List<Guest> guests;
        if (search != null && !search.trim().isEmpty()) {
            guests = guestRepository.findBySearchTerm(search.trim());
        } else {
            guests = guestRepository.findByIsActiveTrueOrderByLastNameAscFirstNameAsc();
        }
        
        model.addAttribute("guests", guests);
        model.addAttribute("search", search);
        return "property/guests/list";
    }

    @GetMapping("/guests/new")
    public String newGuest(Model model) {
        model.addAttribute("guest", new Guest());
        addGuestFormData(model);
        return "property/guests/form";
    }

    @GetMapping("/guests/{id}")
    public String viewGuest(@PathVariable Long id, Model model) {
        Guest guest = guestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Guest not found"));
        
        // Get current bookings
        List<Booking> currentBookings = bookingRepository.findActiveBookingsByGuest(guest);
        
        // Get booking history
        List<Booking> bookingHistory = bookingRepository.findByGuestAndIsActiveTrueOrderByCreatedAtDesc(guest);
        
        model.addAttribute("guest", guest);
        model.addAttribute("currentBookings", currentBookings);
        model.addAttribute("bookingHistory", bookingHistory);
        return "property/guests/view";
    }

    @GetMapping("/guests/{id}/edit")
    public String editGuest(@PathVariable Long id, Model model) {
        Guest guest = guestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Guest not found"));
        
        model.addAttribute("guest", guest);
        addGuestFormData(model);
        return "property/guests/form";
    }

    @PostMapping("/guests")
    public String saveGuest(@Valid @ModelAttribute Guest guest, 
                           BindingResult result, 
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            addGuestFormData(model);
            return "property/guests/form";
        }
        
        // Check for duplicate email
        if (guest.getId() == null) { // New guest
            if (guestRepository.existsByEmail(guest.getEmail())) {
                result.rejectValue("email", "error.email", "A guest with this email already exists");
            }
        } else { // Existing guest
            if (guestRepository.existsByEmailAndIdNot(guest.getEmail(), guest.getId())) {
                result.rejectValue("email", "error.email", "A guest with this email already exists");
            }
        }
        
        if (result.hasErrors()) {
            addGuestFormData(model);
            return "property/guests/form";
        }
        
        guestRepository.save(guest);
        
        String message = guest.getId() == null ? "Guest created successfully!" : "Guest updated successfully!";
        redirectAttributes.addFlashAttribute("message", message);
        
        return "redirect:/property/guests";
    }

    @PostMapping("/guests/{id}/delete")
    public String deleteGuest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Guest guest = guestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Guest not found"));
        
        // Check if guest has active bookings
        List<Booking> activeBookings = bookingRepository.findActiveBookingsByGuest(guest);
        if (!activeBookings.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete guest with active bookings");
            return "redirect:/property/guests/" + id;
        }
        
        guest.setIsActive(false);
        guestRepository.save(guest);
        
        redirectAttributes.addFlashAttribute("message", "Guest deactivated successfully!");
        return "redirect:/property/guests";
    }

    // Booking Management
    @GetMapping("/bookings")
    public String listBookings(Model model, 
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) String status) {
        List<Booking> bookings;
        
        if (search != null && !search.trim().isEmpty()) {
            bookings = bookingRepository.findBySearchTerm(search.trim());
        } else if (status != null && !status.trim().isEmpty()) {
            bookings = bookingRepository.findByBookingStatusAndIsActiveTrue(status);
        } else {
            bookings = bookingRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        }
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        return "property/bookings/list";
    }

    @GetMapping("/bookings/new")
    public String newBooking(Model model) {
        model.addAttribute("booking", new Booking());
        addBookingFormData(model);
        return "property/bookings/form";
    }

    @GetMapping("/bookings/{id}")
    public String viewBooking(@PathVariable Long id, Model model) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        model.addAttribute("booking", booking);
        return "property/bookings/view";
    }

    @GetMapping("/bookings/{id}/edit")
    public String editBooking(@PathVariable Long id, Model model) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        model.addAttribute("booking", booking);
        addBookingFormData(model);
        return "property/bookings/form";
    }

    @PostMapping("/bookings")
    public String saveBooking(@Valid @ModelAttribute Booking booking, 
                             BindingResult result, 
                             Model model,
                             RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            addBookingFormData(model);
            return "property/bookings/form";
        }
        
        // Validate room availability for new bookings
        if (booking.getId() == null) {
            Optional<Booking> existingBooking = bookingRepository.findActiveBookingByRoom(booking.getRoom());
            if (existingBooking.isPresent()) {
                result.rejectValue("room", "error.room", "This room is already occupied");
            }
        }
        
        if (result.hasErrors()) {
            addBookingFormData(model);
            return "property/bookings/form";
        }
        
        // Set initial charges based on nightly rate
        if (booking.getTotalCharges() == null || booking.getTotalCharges().compareTo(BigDecimal.ZERO) == 0) {
            booking.setTotalCharges(booking.getNightlyRate());
        }
        
        // Update room vacancy status
        Room room = booking.getRoom();
        room.setIsVacant(false);
        roomRepository.save(room);
        
        bookingRepository.save(booking);
        
        String message = booking.getId() == null ? "Booking created successfully!" : "Booking updated successfully!";
        redirectAttributes.addFlashAttribute("message", message);
        
        return "redirect:/property/bookings";
    }

    @PostMapping("/bookings/{id}/checkout")
    public String checkoutBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setCheckOutDate(LocalDateTime.now());
        booking.setBookingStatus("COMPLETED");
        booking.calculateNights();
        bookingRepository.save(booking);
        
        // Update room vacancy status
        Room room = booking.getRoom();
        room.setIsVacant(true);
        roomRepository.save(room);
        
        redirectAttributes.addFlashAttribute("message", "Guest checked out successfully!");
        return "redirect:/property/bookings/" + id;
    }

    @PostMapping("/bookings/{id}/add-payment")
    public String addPayment(@PathVariable Long id, 
                            @RequestParam BigDecimal amount,
                            RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.addPayment(amount);
        bookingRepository.save(booking);
        
        redirectAttributes.addFlashAttribute("message", "Payment of $" + amount + " added successfully!");
        return "redirect:/property/bookings/" + id;
    }

    @PostMapping("/bookings/{id}/add-charge")
    public String addCharge(@PathVariable Long id, 
                           @RequestParam BigDecimal amount,
                           @RequestParam(required = false) String description,
                           RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.addCharge(amount);
        
        // Add description to notes
        if (description != null && !description.trim().isEmpty()) {
            String currentNotes = booking.getNotes() != null ? booking.getNotes() : "";
            String newNote = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")) + 
                           " - Added charge: $" + amount + " - " + description;
            booking.setNotes(currentNotes + "\n" + newNote);
        }
        
        bookingRepository.save(booking);
        
        redirectAttributes.addFlashAttribute("message", "Charge of $" + amount + " added successfully!");
        return "redirect:/property/bookings/" + id;
    }

    // Helper methods
    private void addRoomFormData(Model model) {
        Map<String, String> roomTypes = new HashMap<>();
        roomTypes.put("Single", "Single");
        roomTypes.put("Double", "Double");
        roomTypes.put("Suite", "Suite");
        roomTypes.put("Studio", "Studio");
        roomTypes.put("Deluxe", "Deluxe");
        
        model.addAttribute("roomTypes", roomTypes);
    }

    private void addGuestFormData(Model model) {
        Map<String, String> idTypes = new HashMap<>();
        idTypes.put("Driver's License", "Driver's License");
        idTypes.put("Passport", "Passport");
        idTypes.put("State ID", "State ID");
        idTypes.put("Military ID", "Military ID");
        idTypes.put("Other", "Other");
        
        Map<String, String> relationships = new HashMap<>();
        relationships.put("Spouse", "Spouse");
        relationships.put("Parent", "Parent");
        relationships.put("Child", "Child");
        relationships.put("Sibling", "Sibling");
        relationships.put("Friend", "Friend");
        relationships.put("Other", "Other");
        
        model.addAttribute("idTypes", idTypes);
        model.addAttribute("relationships", relationships);
    }

    private void addBookingFormData(Model model) {
        List<Room> availableRooms = roomRepository.findByIsVacantTrueAndIsActiveTrueOrderByRoomNumberAsc();
        List<Guest> activeGuests = guestRepository.findByIsActiveTrueOrderByLastNameAscFirstNameAsc();
        
        Map<String, String> paymentFrequencies = new HashMap<>();
        paymentFrequencies.put("DAILY", "Daily");
        paymentFrequencies.put("WEEKLY", "Weekly");
        paymentFrequencies.put("MONTHLY", "Monthly");
        
        Map<String, String> bookingStatuses = new HashMap<>();
        bookingStatuses.put("ACTIVE", "Active");
        bookingStatuses.put("COMPLETED", "Completed");
        bookingStatuses.put("CANCELLED", "Cancelled");
        
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("activeGuests", activeGuests);
        model.addAttribute("paymentFrequencies", paymentFrequencies);
        model.addAttribute("bookingStatuses", bookingStatuses);
    }

    // Payment Processing Endpoints
    @GetMapping("/payments")
    public String listPayments(Model model, 
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String method) {
        List<Payment> payments;
        
        if (search != null && !search.trim().isEmpty()) {
            payments = paymentRepository.searchByDescription(search.trim());
        } else if (status != null && !status.trim().isEmpty()) {
            payments = paymentRepository.findByPaymentStatusAndIsActiveTrue(status);
        } else if (method != null && !method.trim().isEmpty()) {
            payments = paymentRepository.findByPaymentMethodAndIsActiveTrue(method);
        } else {
            payments = paymentRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc();
        }
        
        // Get payment statistics
        PaymentService.PaymentStatistics stats = paymentService.getPaymentStatistics();
        
        model.addAttribute("payments", payments);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("method", method);
        model.addAttribute("stats", stats);
        model.addAttribute("uppHealthy", paymentService.isUppServiceHealthy());
        
        return "property/payments/list";
    }

    @GetMapping("/payments/{id}")
    public String viewPayment(@PathVariable Long id, Model model) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        model.addAttribute("payment", payment);
        return "property/payments/view";
    }

    @GetMapping("/bookings/{bookingId}/payments/new")
    public String newPayment(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        Payment payment = new Payment();
        payment.setBooking(booking);
        
        // Get UPP device capabilities
        Map<String, Object> smartphoneCapabilities = paymentService.getUppDeviceCapabilities("smartphone");
        Map<String, Object> smartTvCapabilities = paymentService.getUppDeviceCapabilities("smart_tv");
        Map<String, Object> iotCapabilities = paymentService.getUppDeviceCapabilities("iot_device");
        
        model.addAttribute("payment", payment);
        model.addAttribute("booking", booking);
        model.addAttribute("uppHealthy", paymentService.isUppServiceHealthy());
        model.addAttribute("smartphoneCapabilities", smartphoneCapabilities);
        model.addAttribute("smartTvCapabilities", smartTvCapabilities);
        model.addAttribute("iotCapabilities", iotCapabilities);
        
        return "property/payments/form";
    }

    @PostMapping("/bookings/{bookingId}/payments")
    public String processPayment(@PathVariable Long bookingId,
                                @RequestParam BigDecimal amount,
                                @RequestParam String paymentMethod,
                                @RequestParam(required = false) String deviceType,
                                @RequestParam(required = false) String deviceId,
                                @RequestParam(required = false) String customerEmail,
                                RedirectAttributes redirectAttributes) {
        try {
            PaymentService.PaymentResult result = paymentService.processPayment(
                bookingId, amount, paymentMethod, deviceType, deviceId, customerEmail
            );
            
            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("message", "Payment processed successfully: " + result.getMessage());
            } else {
                redirectAttributes.addFlashAttribute("error", "Payment failed: " + result.getMessage());
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Payment processing error: " + e.getMessage());
        }
        
        return "redirect:/property/bookings/" + bookingId;
    }

    @PostMapping("/payments/{id}/refund")
    public String refundPayment(@PathVariable Long id,
                               @RequestParam BigDecimal refundAmount,
                               RedirectAttributes redirectAttributes) {
        try {
            PaymentService.PaymentResult result = paymentService.refundPayment(id, refundAmount);
            
            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("message", "Refund processed successfully: " + result.getMessage());
            } else {
                redirectAttributes.addFlashAttribute("error", "Refund failed: " + result.getMessage());
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Refund processing error: " + e.getMessage());
        }
        
        return "redirect:/property/payments/" + id;
    }

    @GetMapping("/upp/status")
    @ResponseBody
    public Map<String, Object> getUppStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("healthy", paymentService.isUppServiceHealthy());
        status.put("supportedCurrencies", paymentService.getSupportedCurrencies());
        return status;
    }

    @PostMapping("/upp/register-device")
    @ResponseBody
    public Map<String, Object> registerUppDevice(@RequestParam String deviceType,
                                                @RequestParam String deviceId,
                                                @RequestParam String[] capabilities) {
        try {
            UniversalPaymentProtocolService.UppDeviceRegistration registration = 
                paymentService.registerUppDevice(deviceType, deviceId, capabilities);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deviceId", registration.getDeviceId());
            response.put("trustScore", registration.getTrustScore());
            response.put("expiresAt", registration.getExpiresAt());
            return response;
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }
}
