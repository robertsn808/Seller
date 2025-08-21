package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.model.Room;
import com.realestate.sellerfunnel.model.Guest;
import com.realestate.sellerfunnel.model.Booking;
import com.realestate.sellerfunnel.repository.RoomRepository;
import com.realestate.sellerfunnel.repository.GuestRepository;
import com.realestate.sellerfunnel.repository.BookingRepository;
import com.realestate.sellerfunnel.repository.PaymentRepository;
import com.realestate.sellerfunnel.service.PaymentService;
import com.realestate.sellerfunnel.service.TransactionService;
import com.realestate.sellerfunnel.service.UniversalPaymentProtocolService;
import com.realestate.sellerfunnel.model.Payment;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/property")
public class PropertyManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(PropertyManagementController.class);

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

    @Autowired
    private TransactionService transactionService;

    // Helper method to check authentication
    private boolean isPropertyAuthenticated(HttpSession session) {
        Boolean authenticated = (Boolean) session.getAttribute("propertyAuthenticated");
        return authenticated != null && authenticated;
    }
    
    private String redirectToLoginIfNotAuthenticated(HttpSession session) {
        if (!isPropertyAuthenticated(session)) {
            return "redirect:/property/login";
        }
        return null;
    }

    // Property Management Login
    @GetMapping("/login")
    public String propertyLogin() {
        return "property/login";
    }
    
    @PostMapping("/login")
    public String processLogin(@RequestParam String username, 
                              @RequestParam String password,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        // Simple authentication for property management
        // In a real application, you would validate against a database
        if ("admin".equals(username) && "admin123".equals(password)) {
            session.setAttribute("propertyAuthenticated", true);
            return "redirect:/property/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/property/login?error";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("propertyAuthenticated");
        return "redirect:/property/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
        try {
            // Initialize sample data if database is empty
            initializeSampleDataIfNeeded();
            
            // Get statistics with safe fallbacks
            Long totalRooms = roomRepository.count();
            Long vacantRooms = roomRepository.countVacantRooms();
            Long occupiedRooms = roomRepository.countOccupiedRooms();
            Long activeBookings = bookingRepository.countActiveBookings();
            Long pendingPayments = bookingRepository.countPendingPayments();
            Long overduePayments = bookingRepository.countOverduePayments();
            BigDecimal totalOutstanding = bookingRepository.getTotalOutstandingBalance();
            
            // Get recent bookings
            List<Booking> recentBookings = bookingRepository.findByIsActiveTrueOrderByCreatedAtDesc();
            if (recentBookings != null && recentBookings.size() > 10) {
                recentBookings = recentBookings.subList(0, 10);
            }
            
            // Get overdue bookings
            List<Booking> overdueBookings = bookingRepository.findOverdueBookings();
            
            // Get rooms with outstanding balances
            List<Booking> outstandingBookings = bookingRepository.findBookingsWithOutstandingBalance();
            
            // Set attributes with safe defaults
            model.addAttribute("totalRooms", totalRooms != null ? totalRooms : 0L);
            model.addAttribute("vacantRooms", vacantRooms != null ? vacantRooms : 0L);
            model.addAttribute("occupiedRooms", occupiedRooms != null ? occupiedRooms : 0L);
            model.addAttribute("activeBookings", activeBookings != null ? activeBookings : 0L);
            model.addAttribute("pendingPayments", pendingPayments != null ? pendingPayments : 0L);
            model.addAttribute("overduePayments", overduePayments != null ? overduePayments : 0L);
            model.addAttribute("totalOutstanding", totalOutstanding != null ? totalOutstanding : BigDecimal.ZERO);
            model.addAttribute("recentBookings", recentBookings != null ? recentBookings : new java.util.ArrayList<>());
            model.addAttribute("overdueBookings", overdueBookings != null ? overdueBookings : new java.util.ArrayList<>());
            model.addAttribute("outstandingBookings", outstandingBookings != null ? outstandingBookings : new java.util.ArrayList<>());
            
        } catch (Exception e) {
            // Log the error and provide default values
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
            
            // Set default values
            model.addAttribute("totalRooms", 0L);
            model.addAttribute("vacantRooms", 0L);
            model.addAttribute("occupiedRooms", 0L);
            model.addAttribute("activeBookings", 0L);
            model.addAttribute("pendingPayments", 0L);
            model.addAttribute("overduePayments", 0L);
            model.addAttribute("totalOutstanding", BigDecimal.ZERO);
            model.addAttribute("recentBookings", new java.util.ArrayList<>());
            model.addAttribute("overdueBookings", new java.util.ArrayList<>());
            model.addAttribute("outstandingBookings", new java.util.ArrayList<>());
            model.addAttribute("error", "Database initialization required. Please check application logs.");
        }
        
        return "property/dashboard";
    }

    // Room Management
    @GetMapping("/rooms")
    public String listRooms(Model model, HttpSession session, @RequestParam(required = false) String search) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
    public String newRoom(Model model, HttpSession session) {
        logger.info("=== NEW ROOM GET REQUEST ===");
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) {
            logger.warn("Authentication failed for new room, redirecting to login");
            return authCheck;
        }
        
        try {
            Room room = new Room();
            // Initialize default values to prevent form binding issues
            room.setIsActive(true);
            room.setIsVacant(true);
            room.setGateKeyAssigned(false);
            // Set createdAt and updatedAt to current time to avoid null binding issues in form
            room.setCreatedAt(java.time.LocalDateTime.now());
            room.setUpdatedAt(java.time.LocalDateTime.now());
            logger.info("Created new room object with defaults and timestamps");
            
            model.addAttribute("room", room);
            logger.info("Added room object to model");
            
            addRoomFormData(model);
            logger.info("Added room form data to model");
            
            logger.info("Returning property/rooms/form template");
            return "property/rooms/form";
        } catch (Exception e) {
            logger.error("Error creating new room form: {}", e.getMessage(), e);
            logger.error("Full stack trace:", e);
            model.addAttribute("error", "Error loading new room form: " + e.getMessage());
            return "redirect:/property/rooms";
        }
    }

    @GetMapping("/rooms/{id}")
    public String viewRoom(@PathVariable Long id, Model model, HttpSession session) {
        logger.info("Viewing room with ID: {}", id);
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) {
            logger.warn("Authentication failed for room view, redirecting to login");
            return authCheck;
        }
        
        try {
            Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + id));
            
            logger.info("Found room: {} - {}", room.getRoomNumber(), room.getRoomName());
            
            // Get current booking if room is occupied
            Optional<Booking> currentBookingOpt = bookingRepository.findActiveBookingByRoom(room);
            Booking currentBooking = currentBookingOpt.orElse(null);
            
            // Get booking history
            List<Booking> bookingHistory = bookingRepository.findByRoomAndIsActiveTrueOrderByCreatedAtDesc(room);
            
            // Determine ledger booking (use current if exists, else most recent)
            Booking ledgerBooking = currentBooking != null ? currentBooking : (bookingHistory.isEmpty() ? null : bookingHistory.get(0));
            
            // Compute next due date and amount if we have a booking
            LocalDateTime nextDueDate = null;
            BigDecimal nextDueAmount = null;
            if (ledgerBooking != null) {
                String frequency = ledgerBooking.getPaymentFrequency();
                BigDecimal nightly = ledgerBooking.getNightlyRate();
                if (frequency != null && nightly != null) {
                    switch (frequency.toUpperCase()) {
                        case "DAILY":
                            nextDueDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
                            nextDueAmount = nightly;
                            break;
                        case "WEEKLY":
                            nextDueDate = LocalDateTime.now().plusWeeks(1);
                            nextDueAmount = nightly.multiply(new BigDecimal("7"));
                            break;
                        case "MONTHLY":
                            nextDueDate = LocalDateTime.now().plusMonths(1);
                            // Approximate month at 30 nights for simplicity
                            nextDueAmount = nightly.multiply(new BigDecimal("30"));
                            break;
                        default:
                            break;
                    }
                }
            }
            
            // Load payments for ledger booking
            List<Payment> ledgerPayments = java.util.Collections.emptyList();
            if (ledgerBooking != null) {
                ledgerPayments = paymentRepository.findByBookingIdAndIsActiveTrueOrderByCreatedAtDesc(ledgerBooking.getId());
            }
            
            model.addAttribute("room", room);
            model.addAttribute("currentBooking", currentBooking);
            model.addAttribute("bookingHistory", bookingHistory);
            return "property/rooms/view";
        } catch (Exception e) {
            logger.error("Error viewing room {}: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Error loading room details: " + e.getMessage());
            return "redirect:/property/rooms";
        }
    }

    // Add a transaction to a room (ledger entry)
    @PostMapping("/rooms/{id}/transactions")
    public String addRoomTransaction(@PathVariable Long id,
                                     @RequestParam String description,
                                     @RequestParam BigDecimal amount,
                                     @RequestParam String paidBy,
                                     @RequestParam(required = false) String collectedBy,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
        try {
            if (collectedBy == null || collectedBy.trim().isEmpty()) {
                // fallback to a simple user hint in session if ever used; else mark as Unknown
                collectedBy = "Unknown";
            }
            transactionService.addTransaction(id, description, amount, paidBy, collectedBy);
            redirectAttributes.addFlashAttribute("message", "Transaction added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add transaction: " + e.getMessage());
        }
        return "redirect:/property/rooms/" + id;
    }

    @GetMapping("/rooms/{id}/edit")
    public String editRoom(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        logger.info("=== EDIT ROOM GET REQUEST ===");
        logger.info("Room ID to edit: {}", id);
        logger.info("Session ID: {}", session.getId());
        logger.info("Is authenticated: {}", isPropertyAuthenticated(session));
        logger.info("=============================");
        
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) {
            logger.warn("Authentication failed for room edit, redirecting to login");
            return authCheck;
        }
        
        try {
            logger.info("Attempting to find room with ID: {}", id);
            
            // Check if room exists first
            if (!roomRepository.existsById(id)) {
                logger.warn("Room with ID {} does not exist", id);
                redirectAttributes.addFlashAttribute("error", "Room not found with ID: " + id);
                return "redirect:/property/rooms";
            }
            
            Room room = roomRepository.findById(id).orElse(null);
            if (room == null) {
                logger.warn("Room with ID {} returned null from findById", id);
                redirectAttributes.addFlashAttribute("error", "Room not found");
                return "redirect:/property/rooms";
            }
            
            logger.info("Found room for edit: {} - {}", room.getRoomNumber(), room.getRoomName());
            
            model.addAttribute("room", room);
            addRoomFormData(model);
            return "property/rooms/form";
        } catch (Exception e) {
            logger.error("Error editing room {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error loading room for edit: " + e.getMessage());
            return "redirect:/property/rooms";
        }
    }

    @PostMapping("/rooms")
    public String saveRoom(@Valid @ModelAttribute Room room, 
                          BindingResult result, 
                          Model model,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        return processRoomSave(room, result, model, session, redirectAttributes, null);
    }

    @PostMapping("/rooms/{id}")
    public String updateRoom(@PathVariable Long id,
                             @Valid @ModelAttribute Room room,
                             BindingResult result,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        logger.info("=== UPDATE ROOM POST REQUEST ===");
        logger.info("Path ID: {} Form ID: {}", id, room.getId());
        if (room.getId() == null || !room.getId().equals(id)) {
            logger.warn("Mismatch or null form ID. Forcing room ID to {}", id);
            room.setId(id);
        }
        return processRoomSave(room, result, model, session, redirectAttributes, id);
    }

    private String processRoomSave(Room room,
                                   BindingResult result,
                                   Model model,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes,
                                   Long pathId) {
        logger.info("=== SAVE ROOM DEBUG ===");
        logger.info("(Path ID:{}) Room ID:{} Number:{} Name:{} Type:{}", pathId, room.getId(), room.getRoomNumber(), room.getRoomName(), room.getRoomType());
        boolean isNew = (room.getId() == null);
        logger.info("Is new room: {}", isNew);
        logger.info("========================");

        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) {
            logger.warn("Authentication failed during room save");
            return authCheck;
        }

        if (result.hasErrors()) {
            logger.warn("Validation errors for room: {}", result.getAllErrors());
            try {
                addRoomFormData(model);
                return "property/rooms/form";
            } catch (Exception e) {
                logger.error("Error adding form data after validation failure: {}", e.getMessage(), e);
                redirectAttributes.addFlashAttribute("error", "Validation failed: " + result.getAllErrors());
                return "redirect:/property/rooms";
            }
        }

        try {
            if (room.getIsVacant() == null) room.setIsVacant(true);
            if (room.getGateKeyAssigned() == null) room.setGateKeyAssigned(false);
            if (room.getIsActive() == null) room.setIsActive(true);

            // Duplicate checks
            if (isNew) {
                logger.info("Checking duplicate for new room: {}", room.getRoomNumber());
                if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
                    result.rejectValue("roomNumber", "error.roomNumber", "A room with this number already exists");
                }
            } else {
                logger.info("Checking duplicate for existing room: {} (ID:{})", room.getRoomNumber(), room.getId());
                if (roomRepository.existsByRoomNumberAndIdNot(room.getRoomNumber(), room.getId())) {
                    result.rejectValue("roomNumber", "error.roomNumber", "A room with this number already exists");
                }
            }

            if (result.hasErrors()) {
                logger.warn("Validation failed after duplicate checks: {}", result.getAllErrors());
                try {
                    addRoomFormData(model);
                    return "property/rooms/form";
                } catch (Exception e) {
                    logger.error("Error adding form data after duplicate check failure: {}", e.getMessage(), e);
                    redirectAttributes.addFlashAttribute("error", "Duplicate room number: " + room.getRoomNumber());
                    return "redirect:/property/rooms";
                }
            }

            logger.info("Attempting to save room: ID={}, Number={}, Type={}, Rate={}", room.getId(), room.getRoomNumber(), room.getRoomType(), room.getBaseRate());
            Room savedRoom = roomRepository.save(room);
            logger.info("Saved room ID {} (was new? {})", savedRoom.getId(), isNew);

            redirectAttributes.addFlashAttribute("message", isNew ? "Room created successfully!" : "Room updated successfully!");
            return "redirect:/property/rooms";
        } catch (Exception e) {
            logger.error("Error saving room: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error saving room: " + e.getMessage());
            return "redirect:/property/rooms";
        }
    }
    

    @PostMapping("/rooms/{id}/delete")
    public String deleteRoom(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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

    @PostMapping("/rooms/{id}/transactions")
    public String addTransaction(@PathVariable Long id,
                                 @RequestParam String description,
                                 @RequestParam BigDecimal amount,
                                 @RequestParam String paidBy,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;

        try {
            transactionService.addTransaction(id, description, amount, paidBy);
            redirectAttributes.addFlashAttribute("message", "Transaction added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding transaction: " + e.getMessage());
        }

        return "redirect:/property/rooms/" + id;
    }

    // Guest Management
    @GetMapping("/guests")
    public String listGuests(Model model, HttpSession session, @RequestParam(required = false) String search) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
    public String newGuest(Model model, HttpSession session) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
        model.addAttribute("guest", new Guest());
        addGuestFormData(model);
        return "property/guests/form";
    }

    @GetMapping("/guests/{id}")
    public String viewGuest(@PathVariable Long id, Model model, HttpSession session) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
    public String editGuest(@PathVariable Long id, Model model, HttpSession session) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        return processGuestSave(guest, result, model, session, redirectAttributes, null);
    }

    @PostMapping("/guests/{id}")
    public String updateGuest(@PathVariable Long id,
                              @Valid @ModelAttribute Guest guest,
                              BindingResult result,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        logger.info("=== UPDATE GUEST POST REQUEST === Path ID:{} Form ID:{} Email:{}", id, guest.getId(), guest.getEmail());
        if (guest.getId() == null || !guest.getId().equals(id)) {
            guest.setId(id);
        }
        return processGuestSave(guest, result, model, session, redirectAttributes, id);
    }

    private String processGuestSave(Guest guest,
                                    BindingResult result,
                                    Model model,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes,
                                    Long pathId) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
        boolean isNew = (guest.getId() == null);
        logger.info("=== SAVE GUEST DEBUG === (Path ID:{}) ID:{} Email:{} isNew:{}", pathId, guest.getId(), guest.getEmail(), isNew);

        if (result.hasErrors()) {
            addGuestFormData(model);
            return "property/guests/form";
        }
        // Duplicate email checks
        if (isNew) {
            if (guestRepository.existsByEmail(guest.getEmail())) {
                result.rejectValue("email", "error.email", "A guest with this email already exists");
            }
        } else {
            if (guestRepository.existsByEmailAndIdNot(guest.getEmail(), guest.getId())) {
                result.rejectValue("email", "error.email", "A guest with this email already exists");
            }
        }
        if (result.hasErrors()) {
            addGuestFormData(model);
            return "property/guests/form";
        }
        guestRepository.save(guest);
        redirectAttributes.addFlashAttribute("message", isNew ? "Guest created successfully!" : "Guest updated successfully!");
        return "redirect:/property/guests";
    }

    // Booking Management
    @GetMapping("/bookings")
    public String listBookings(Model model, HttpSession session,
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) String status) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
    public String newBooking(Model model, HttpSession session) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
        model.addAttribute("booking", new Booking());
        addBookingFormData(model);
        return "property/bookings/form";
    }

    @GetMapping("/bookings/{id}")
    public String viewBooking(@PathVariable Long id, Model model, HttpSession session) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        model.addAttribute("booking", booking);
        return "property/bookings/view";
    }

    @GetMapping("/bookings/{id}/edit")
    public String editBooking(@PathVariable Long id, Model model, HttpSession session) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        return processBookingSave(booking, result, model, session, redirectAttributes, null);
    }

    @PostMapping("/bookings/{id}")
    public String updateBooking(@PathVariable Long id,
                                @Valid @ModelAttribute Booking booking,
                                BindingResult result,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        logger.info("=== UPDATE BOOKING POST REQUEST === Path ID:{} Form ID:{} Status:{}", id, booking.getId(), booking.getBookingStatus());
        if (booking.getId() == null || !booking.getId().equals(id)) {
            booking.setId(id);
        }
        return processBookingSave(booking, result, model, session, redirectAttributes, id);
    }

    private String processBookingSave(Booking booking,
                                      BindingResult result,
                                      Model model,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes,
                                      Long pathId) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
        boolean isNew = (booking.getId() == null);
        logger.info("=== SAVE BOOKING DEBUG === (Path ID:{}) ID:{} Room:{} Guest:{} isNew:{} Status:{}", pathId, booking.getId(), booking.getRoom()!=null?booking.getRoom().getId():null, booking.getGuest()!=null?booking.getGuest().getId():null, isNew, booking.getBookingStatus());

        if (result.hasErrors()) {
            addBookingFormData(model);
            return "property/bookings/form";
        }
        if (isNew) {
            Optional<Booking> existingBooking = bookingRepository.findActiveBookingByRoom(booking.getRoom());
            if (existingBooking.isPresent()) {
                result.rejectValue("room", "error.room", "This room is already occupied");
            }
        }
        if (result.hasErrors()) {
            addBookingFormData(model);
            return "property/bookings/form";
        }
        if (booking.getTotalCharges() == null || booking.getTotalCharges().compareTo(BigDecimal.ZERO) == 0) {
            booking.setTotalCharges(booking.getNightlyRate());
        }
        // Update room vacancy only when new booking created
        if (isNew && booking.getRoom() != null) {
            Room room = booking.getRoom();
            room.setIsVacant(false);
            roomRepository.save(room);
        }
        bookingRepository.save(booking);
        redirectAttributes.addFlashAttribute("message", isNew ? "Booking created successfully!" : "Booking updated successfully!");
        return "redirect:/property/bookings";
    }

    @PostMapping("/bookings/{id}/checkout")
    public String checkoutBooking(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
    public String listPayments(Model model, HttpSession session,
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String method) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
    public String viewPayment(@PathVariable Long id, Model model, HttpSession session) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        model.addAttribute("payment", payment);
        return "property/payments/view";
    }

    @GetMapping("/bookings/{bookingId}/payments/new")
    public String newPayment(@PathVariable Long bookingId, Model model, HttpSession session) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
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
    
    @GetMapping("/diagnostic")
    @ResponseBody
    public Map<String, Object> diagnostic(HttpSession session) {
        Map<String, Object> diagnostic = new HashMap<>();
        
        // Authentication status
        diagnostic.put("authenticated", isPropertyAuthenticated(session));
        diagnostic.put("sessionId", session.getId());
        
        // Database status
        diagnostic.put("totalRooms", roomRepository.count());
        diagnostic.put("totalGuests", guestRepository.count());
        diagnostic.put("totalBookings", bookingRepository.count());
        
        // Sample room data
        List<Room> rooms = roomRepository.findByIsActiveTrueOrderByRoomNumberAsc();
        diagnostic.put("sampleRoom", rooms.isEmpty() ? null : rooms.get(0));
        
        // UPP service status
        diagnostic.put("uppHealthy", paymentService.isUppServiceHealthy());
        
        return diagnostic;
    }
    
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        logger.info("=== TEST ENDPOINT HIT ===");
        return "PropertyManagementController is working! Current time: " + System.currentTimeMillis();
    }
    
    @GetMapping("/test-form")
    public String testForm(Model model, HttpSession session) {
        logger.info("=== TEST FORM ENDPOINT ===");
        String authCheck = redirectToLoginIfNotAuthenticated(session);
        if (authCheck != null) return authCheck;
        
        Room room = new Room();
        room.setIsActive(true);
        room.setIsVacant(true);
        room.setGateKeyAssigned(false);
        
        model.addAttribute("room", room);
        addRoomFormData(model);
        
        logger.info("Attempting to return property/rooms/form template");
        return "property/rooms/form";
    }
    
    @GetMapping("/rooms-debug")
    @ResponseBody
    public Map<String, Object> roomsDebug() {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            long totalRooms = roomRepository.count();
            debug.put("totalRooms", totalRooms);
            
            List<Room> allRooms = roomRepository.findAll();
            debug.put("allRoomIds", allRooms.stream().map(Room::getId).toList());
            debug.put("allRooms", allRooms.stream().map(r -> 
                Map.of("id", r.getId(), "number", r.getRoomNumber(), "name", r.getRoomName() != null ? r.getRoomName() : "null")
            ).toList());
            
            // Check if room ID 1 exists
            boolean exists1 = roomRepository.existsById(1L);
            debug.put("room1Exists", exists1);
            
            if (exists1) {
                Room room1 = roomRepository.findById(1L).orElse(null);
                debug.put("room1Details", room1 != null ? Map.of(
                    "id", room1.getId(),
                    "roomNumber", room1.getRoomNumber(),
                    "roomName", room1.getRoomName(),
                    "isActive", room1.getIsActive()
                ) : "null");
            }
            
        } catch (Exception e) {
            debug.put("error", e.getMessage());
            debug.put("exception", e.getClass().getSimpleName());
        }
        
        return debug;
    }
    
    @GetMapping("/guests-debug")
    @ResponseBody
    public Map<String, Object> guestsDebug() {
        Map<String, Object> debug = new HashMap<>();
        try {
            long totalGuests = guestRepository.count();
            debug.put("totalGuests", totalGuests);
            List<Guest> allGuests = guestRepository.findByIsActiveTrueOrderByLastNameAscFirstNameAsc();
            debug.put("activeGuestIds", allGuests.stream().map(Guest::getId).toList());
            debug.put("sampleGuest", allGuests.isEmpty() ? null : Map.of(
                "id", allGuests.get(0).getId(),
                "name", allGuests.get(0).getFirstName() + " " + allGuests.get(0).getLastName(),
                "email", allGuests.get(0).getEmail()
            ));
            // Simple duplicate email detection
            Map<String, Long> emailCounts = allGuests.stream()
                .filter(g -> g.getEmail() != null)
                .collect(java.util.stream.Collectors.groupingBy(Guest::getEmail, java.util.stream.Collectors.counting()));
            List<String> duplicateEmails = emailCounts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();
            debug.put("duplicateEmails", duplicateEmails);
        } catch (Exception e) {
            debug.put("error", e.getMessage());
        }
        return debug;
    }

    @GetMapping("/bookings-debug")
    @ResponseBody
    public Map<String, Object> bookingsDebug() {
        Map<String, Object> debug = new HashMap<>();
        try {
            long totalBookings = bookingRepository.count();
            debug.put("totalBookings", totalBookings);
            List<Booking> recent = bookingRepository.findByIsActiveTrueOrderByCreatedAtDesc();
            debug.put("recentBookingIds", recent.stream().limit(10).map(Booking::getId).toList());
            // Active room occupancy snapshot
            List<Room> rooms = roomRepository.findByIsActiveTrueOrderByRoomNumberAsc();
            long vacant = rooms.stream().filter(r -> Boolean.TRUE.equals(r.getIsVacant())).count();
            long occupied = rooms.size() - vacant;
            debug.put("roomVacant", vacant);
            debug.put("roomOccupied", occupied);
            // Active bookings by status
            Map<String, Long> statusCounts = recent.stream()
                .collect(java.util.stream.Collectors.groupingBy(Booking::getBookingStatus, java.util.stream.Collectors.counting()));
            debug.put("statusCounts", statusCounts);
        } catch (Exception e) {
            debug.put("error", e.getMessage());
        }
        return debug;
    }

    @GetMapping("/payments-debug")
    @ResponseBody
    public Map<String, Object> paymentsDebug() {
        Map<String, Object> debug = new HashMap<>();
        try {
            long totalPayments = paymentRepository.count();
            debug.put("totalPayments", totalPayments);
            List<Payment> recent = paymentRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc();
            debug.put("recentPaymentIds", recent.stream().map(Payment::getId).toList());
            BigDecimal totalUpp = paymentRepository.sumCompletedUppPayments();
            debug.put("totalUppCompleted", totalUpp);
            // Payment method summary
            Map<String, Long> methodCounts = recent.stream()
                .filter(p -> p.getPaymentMethod() != null)
                .collect(java.util.stream.Collectors.groupingBy(Payment::getPaymentMethod, java.util.stream.Collectors.counting()));
            debug.put("recentMethodCounts", methodCounts);
            debug.put("uppHealthy", paymentService.isUppServiceHealthy());
        } catch (Exception e) {
            debug.put("error", e.getMessage());
        }
        return debug;
    }

    /**
     * Initialize sample data if the database is empty
     */
    private void initializeSampleDataIfNeeded() {
        try {
            // Check if we have any rooms
            if (roomRepository.count() == 0) {
                System.out.println("=== INITIALIZING SAMPLE PROPERTY DATA ===");
                
                // Create sample rooms
                Room room1 = new Room("101", "Ocean View", "Single", new BigDecimal("150.00"));
                room1.setCurrentCode("1234");
                room1.setResetCode("0000");
                roomRepository.save(room1);
                
                Room room2 = new Room("102", "Garden Suite", "Double", new BigDecimal("200.00"));
                room2.setCurrentCode("5678");
                room2.setResetCode("0000");
                roomRepository.save(room2);
                
                Room room3 = new Room("103", "Mountain View", "Single", new BigDecimal("140.00"));
                room3.setCurrentCode("9012");
                room3.setResetCode("0000");
                roomRepository.save(room3);
                
                Room room4 = new Room("104", "Premium Suite", "Suite", new BigDecimal("300.00"));
                room4.setCurrentCode("3456");
                room4.setResetCode("0000");
                roomRepository.save(room4);
                
                Room room5 = new Room("105", "Standard Room", "Single", new BigDecimal("120.00"));
                room5.setCurrentCode("7890");
                room5.setResetCode("0000");
                roomRepository.save(room5);
                
                Room room6 = new Room("106", "Family Room", "Double", new BigDecimal("180.00"));
                room6.setCurrentCode("2345");
                room6.setResetCode("0000");
                roomRepository.save(room6);
                
                Room room7 = new Room("107", "Deluxe Room", "Single", new BigDecimal("160.00"));
                room7.setCurrentCode("6789");
                room7.setResetCode("0000");
                roomRepository.save(room7);
                
                Room room8 = new Room("108", "Executive Suite", "Suite", new BigDecimal("350.00"));
                room8.setCurrentCode("0123");
                room8.setResetCode("0000");
                roomRepository.save(room8);
                
                System.out.println("=== SAMPLE ROOMS CREATED ===");
            }
            
            // Check if we have any guests
            if (guestRepository.count() == 0) {
                // Create sample guests
                Guest guest1 = new Guest();
                guest1.setFirstName("John");
                guest1.setLastName("Doe");
                guest1.setEmail("john.doe@example.com");
                guest1.setPhoneNumber("555-0101");
                guestRepository.save(guest1);
                
                Guest guest2 = new Guest();
                guest2.setFirstName("Jane");
                guest2.setLastName("Smith");
                guest2.setEmail("jane.smith@example.com");
                guest2.setPhoneNumber("555-0102");
                guestRepository.save(guest2);
                
                System.out.println("=== SAMPLE GUESTS CREATED ===");
            }
            
        } catch (Exception e) {
            System.err.println("Error initializing sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
