# 🏨 Room Reservation System

A web-based **Room Reservation Management System** built with Java (Servlets/JSP) and deployed on Apache Tomcat. This system enables hotel staff to manage guest reservations, room availability, billing, and staff accounts through a structured and role-based interface.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [System Architecture](#system-architecture)
- [Tech Stack](#tech-stack)
- [Database Design](#database-design)
- [UML Diagrams](#uml-diagrams)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Contributors](#contributors)

---

## Overview

The Room Reservation System is designed to streamline hotel operations by providing a centralised platform for:

- **Receptionists** to handle guest check-ins, reservations, and billing.
- **Admins** to manage staff accounts, generate reports, and oversee system configuration.
- **Guests** to be registered and tracked across reservations.

Authentication is handled via a secure login flow with hashed password comparison and a maximum of 3 retry attempts.

---

## Features

### Receptionist
- 🔐 Secure login with credential validation
- ➕ Add new reservations with guest details
- 📋 Display reservation details
- 🧾 Calculate and print itemised bills (with tax)
- 🔍 Check room availability by type and date range
- 🔄 Update or cancel existing reservations

### Admin
- 👥 Manage staff (Add / Update / Delete)
- 📊 Generate system reports
- 🏠 Administer the full reservation system

### System
- Room types: **Single**, **Double**, **Deluxe**, **Suite**
- Room statuses: Available / Occupied
- Reservation statuses: Active / Checked Out / Cancelled
- Automatic reservation number generation
- Bill calculation: `subtotal = nights × ratePerNight`, `totalAmount = subtotal + taxAmount (10%)`

---

## System Architecture

The system follows a layered architecture with clearly separated concerns:

| Layer | Components |
|---|---|
| **UI** | HTML, CSS, JavaScript (JSP-based console) |
| **Business Logic** | `ReservationSystem`, `AuthenticationService` |
| **Data Stores** | `GuestStore`, `RoomStore`, `ReservationStore`, `DataStore` |
| **Domain Models** | `Reservation`, `Bill`, `Guest`, `Room`, `Staff` |
| **Services** | `PrintService` |

### Key Workflows

**Login Flow:** User enters credentials → `AuthenticationService` fetches user from `DataStore` → hashes input password → compares hashes → grants access or prompts retry (max 3 attempts).

**Add Reservation Flow:** Staff selects "Add New Reservation" → enters guest details → system finds or creates guest → enters room type and dates → system checks availability → confirms room selection → saves reservation and updates room status to OCCUPIED.

**Calculate & Print Bill Flow:** Staff enters reservation number → system fetches reservation → calculates nights → creates Bill with subtotal and tax → displays itemised bill → prints receipt → updates reservation status to CHECKED_OUT.

---

## Tech Stack

| Technology | Purpose |
|---|---|
| **Java** | Core application logic (Servlets) |
| **HTML / CSS / JavaScript** | Frontend UI |
| **Apache Tomcat** | Web server / servlet container |
| **Maven** | Dependency and build management |
| **JSP** | Dynamic page rendering |

---

## Database Design

The system is modelled using the following entities:

- **STAFF** — `staff_id`, `username`, `password`, `role` (admin / receptionist), `full_name`, `email`
- **GUEST** — `guest_id`, `guest_name`, `address`, `contact_number`
- **RESERVATION** — `reservation_id`, `reservation_number`, `checkin_date`, `checkout_date`, `status`
- **ROOM** — `room_id`, `room_number`, `room_status`, `floor_number`
- **ROOM_TYPE** — `room_type_id`, `type_name`, `description`, `max_occupancy`
- **ROOM_RATE** — `room_rate_id`, `rate_per_night`, `max_occupancy`, `effective_from`, `effective_to`, `description`
- **BILL** — `bill_id`, `bill_number`, `subtotal`, `tax_rate`, `tax_amount`, `total_amount`, `payment_status`, `discount_amount`
- **PAYMENT** — `payment_id`, `amount_paid`, `payment_method`, `payment_date`, `payment_status`

### Key Relationships

- A **STAFF** member CREATES many **RESERVATION**s (1:N)
- A **GUEST** MAKES many **RESERVATION**s (1:N)
- A **RESERVATION** is ASSIGNED_TO a **ROOM** (N:1)
- A **RESERVATION** GENERATES a **BILL** (1:1)
- A **BILL** is SETTLED_BY a **PAYMENT** (1:N)
- A **ROOM** is CLASSIFIED by a **ROOM_TYPE** (N:1)
- A **ROOM_TYPE** HAS_RATE via **ROOM_RATE** (1:N)

---

## UML Diagrams

The following UML diagrams are included in the project documentation:

| Diagram | Description |
|---|---|
| **Class Diagram** | Shows all system classes, attributes, relationships, and enumerations |
| **Use Case Diagram** | Illustrates interactions between Receptionist, Admin, and the system |
| **Sequence Diagram – Login** | Details the authentication flow |
| **Sequence Diagram – Add New Reservation** | Covers guest lookup/creation and room booking |
| **Sequence Diagram – Calculate & Print Bill** | Covers billing calculation and checkout |
| **ER Diagram** | Entity-relationship model of the database |

---

## Getting Started

### Prerequisites

- Java JDK 11 or higher
- Apache Maven 3.6+
- Apache Tomcat 9+
- A supported IDE (IntelliJ IDEA recommended)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/AtheekAzmi/RoomReservationSystem.git
   cd RoomReservationSystem
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Deploy to Tomcat**
    - Copy the generated `.war` file from `target/` to your Tomcat `webapps/` directory, **or**
    - Use the SmartTomcat plugin in IntelliJ IDEA (configuration already included in `.smarttomcat/`)

4. **Start the server**
   ```bash
   # From Tomcat bin directory
   ./startup.sh   # Linux/macOS
   startup.bat    # Windows
   ```

5. **Access the application**
   ```
   http://localhost:8080/RoomReservationSystem
   ```

---

## Usage

### Logging In

Use your assigned credentials (username and password). The system supports two roles:
- **Admin** — Full system access including staff management and reporting
- **Receptionist** — Guest and reservation management, billing

### Making a Reservation

1. Select **"Add New Reservation"** from the main menu
2. Enter guest name, address, and contact number
3. Select room type (Single / Double / Deluxe / Suite) and dates
4. Confirm available room from the list
5. System generates a reservation number and confirms the booking

### Calculating and Printing a Bill

1. Select **"Calculate and Print Bill"**
2. Enter the reservation number
3. Review the reservation summary
4. Confirm **"Generate Bill"** to compute subtotal and tax
5. Select **"Print Bill"** to output a physical or PDF receipt
6. Reservation status is automatically updated to **CHECKED_OUT**

---

## Project Structure

```
RoomReservationSystem/
├── src/
│   └── main/
│       ├── java/           # Java Servlets and business logic
│       ├── webapp/         # HTML, CSS, JS, JSP files
│       └── resources/      # Configuration files
├── .smarttomcat/           # Tomcat plugin configuration
├── .idea/                  # IntelliJ IDEA project settings
├── pom.xml                 # Maven build configuration
├── reservation_audit.log   # System audit log
└── .gitignore
```

---

## Contributors

| Name | Role |
|---|---|
| [AtheekAzmi](https://github.com/AtheekAzmi) | Developer |

---

## License

This project is for academic/educational purposes. Please refer to the repository for licensing details.
