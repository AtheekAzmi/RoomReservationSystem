# 🏨 Online Room Reservation System

![Java CI with Maven](https://github.com/AtheekAzmi/RoomReservationSystem/actions/workflows/maven-ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-30.6%25-orange)
![HTML](https://img.shields.io/badge/HTML-65.2%25-red)

A full-featured hotel room reservation management system built with **Java (Servlets)**, **HTML/CSS/JavaScript**, and **Maven**. The system supports staff authentication, guest management, room availability checking, reservation lifecycle management, and automated bill generation.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [System Architecture](#-system-architecture)
- [Getting Started](#-getting-started)
- [Project Structure](#-project-structure)
- [Version History](#-version-history)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Testing](#-testing)
- [Contributors](#-contributors)

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 **Authentication** | Secure login with SHA-256 password hashing and 3-attempt lockout |
| 👤 **Guest Management** | Add, search, and manage guest profiles with contact validation |
| 🛏️ **Room Management** | Supports Single, Double, Deluxe, and Suite room types |
| 📅 **Reservation System** | Create, update, cancel reservations with date-range availability checks |
| 💰 **Billing** | Automated bill calculation with 10% tax, subtotal, and total |
| 🖨️ **Print Service** | Generate physical or PDF receipts for guests |
| 👥 **Staff Management** | Admin can add, update, and delete staff accounts |
| 📊 **Reports** | Generate and view reservation reports |

---

## 🛠 Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Core backend logic, Servlets, business models |
| HTML / CSS / JS | — | Frontend UI (65.2% of codebase) |
| Maven | 3.6+ | Build tool, dependency management |
| JUnit 5 | 5.x | Unit and integration testing |
| Mockito | — | Mocking for service-layer tests |
| Tomcat (SmartTomcat) | — | Servlet container / application server |
| GitHub Actions | — | CI/CD pipeline (automated build & test) |
| Git | 2.43+ | Version control |

---

## 🏗 System Architecture

### Domain Model
The system is built around the following core entities:

- **User** → Receptionist / Admin (role-based access)
- **Guest** → guest_name, address, contact_number
- **Room** → room_number, floor_number, RoomType (enum), RoomStatus (enum)
- **RoomType** → `SINGLE` | `DOUBLE` | `DELUXE` | `SUITE`
- **Reservation** → reservation_number, checkin_date, checkout_date, ReservationStatus
- **ReservationStatus** → `CONFIRMED` | `CHECKED_OUT` | `CANCELLED`
- **Bill** → subtotal, tax_rate, tax_amount, total_amount, discount_amount
- **Payment** → payment_id, amount_paid, payment_method, payment_date

### Key Services
- **AuthenticationService** — SHA-256 hashing, credential verification, retry lockout
- **ReservationStore** — CRUD operations, auto reservation number generation
- **GuestStore** — Guest lookup and creation
- **RoomStore** — Room availability filtering by date range
- **PrintService** — PDF/physical receipt generation

### Authentication Flow
```
Launch App → Display Login Screen → Enter credentials
    → AuthenticationService.authenticate(username, password)
    → DataStore.fetchUser(username) → hashPassword(input)
    → Compare hashes → [true]  Display Main Menu
                     → [false] Show error, retry (max 3 attempts)
```

### Add Reservation Flow
```
Select "Add New Reservation" → Enter guest details
    → GuestStore.findOrCreateGuest(name, contact)
    → Enter room type + check-in/check-out dates
    → RoomStore.getAvailableRooms(roomType, checkIn, checkOut)
    → [Rooms found] Confirm selection
        → ReservationStore.addReservation(guest, room, dates)
        → generateReservationNumber() → saveReservation()
        → updateRoomStatus(OCCUPIED) → Display Confirmation
    → [No rooms] Suggest alternative dates or room types
```

### Bill Calculation Flow
```
Select "Calculate & Print Bill" → Enter reservation number
    → ReservationStore.findReservation(reservationNo)
    → Reservation.calculateNights()
    → new Bill(reservation, ratePerNight, nights)
    → Bill.calculateTotal():
        subtotal = nights × ratePerNight
        applyTax(taxRate = 10%)
        totalAmount = subtotal + taxAmount
    → PrintService.printBill(bill) → Physical/PDF receipt
    → updateReservationStatus(CHECKED_OUT)
```

---

## 🚀 Getting Started

### Prerequisites
- Java 11+
- Maven 3.6+
- Apache Tomcat (or IntelliJ SmartTomcat plugin)
- Git 2.23+

### Clone & Run

```bash
# Clone the repository
git clone https://github.com/AtheekAzmi/RoomReservationSystem.git

# Navigate into the project
cd RoomReservationSystem

# Build the project
mvn compile

# Run all tests
mvn test

# Package as WAR
mvn package

# Deploy target/RoomReservationSystem.war to your Tomcat server
```

### Expected Output After `mvn test`
```
[INFO] Tests run: 66, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 📁 Project Structure

```
RoomReservationSystem/
├── .github/
│   └── workflows/
│       └── maven-ci.yml          # GitHub Actions CI/CD pipeline
├── src/
│   └── main/
│       ├── java/
│       │   └── com/roomreservation/
│       │       ├── model/         # Guest, Room, Reservation, Bill, etc.
│       │       ├── service/       # AuthenticationService, PrintService
│       │       ├── store/         # GuestStore, RoomStore, ReservationStore
│       │       └── servlet/       # HTTP Servlet controllers
│       └── webapp/
│           ├── *.html             # Frontend pages
│           ├── css/               # Stylesheets
│           └── js/                # JavaScript files
├── .gitignore                     # Ignores build artifacts, IDE files
├── pom.xml                        # Maven build configuration
└── reservation_audit.log          # System audit log
```

---

## 📦 Version History

| Version | Milestone | Features |
|---|---|---|
| v0.1.0 | Project Init | Maven structure, .gitignore, base pom.xml |
| v0.2.0 | Domain Models | Guest, Room, RoomType, RoomStatus, Reservation |
| v0.3.0 | Authentication | AuthenticationService with SHA-256, 3-retry lockout |
| v0.4.0 | Reservation Flow | ReservationStore, availability check, reservation creation |
| v0.5.0 | Billing | Bill class, calculateTotal(), applyTax(), PrintService |
| v0.6.0 | Bug Fixes | State guard on cancel(), zero-rate validation in Bill |
| v0.7.0 | Test Suite | 66 JUnit 5 + Mockito test cases across 8 test classes |
| **v1.0.0** | **Final Release** | **CI/CD live, README complete, all 66 tests passing** |

---

## ⚙️ CI/CD Pipeline

The project uses **GitHub Actions** to automatically build and test on every push to `master` and on every pull request.

**Pipeline file: `.github/workflows/maven-ci.yml`**

```
git push → GitHub Actions triggered → ubuntu-latest runner
    → Checkout code       (actions/checkout@v4)
    → Setup JDK 11        (actions/setup-java@v4, Temurin distribution)
    → mvn compile         (fails fast on syntax errors)
    → mvn test            (runs all 66 JUnit 5 tests via Surefire)
    → Upload surefire-reports as downloadable artifact
    → Update CI status badge on README
```

**Branch Protection on `master`:**
- ✅ Require CI status checks to pass before merging
- ✅ Require branches to be up to date before merging
- ✅ Prevents broken code from entering master

---

## 🧪 Testing

The project includes **66 automated tests** across **8 test classes**:

| Test Class | Coverage |
|---|---|
| `BillTest` | `@ParameterizedTest` across all room types |
| `ReservationTest` | Status transitions (CONFIRMED → CHECKED_OUT / CANCELLED) |
| `AuthenticationServiceTest` | Mockito mocks for credential verification |
| `RoomStoreTest` | Room availability filtering |
| `ReservationStoreTest` | CRUD operations, number generation |
| `ReservationWorkflowIntegrationTest` | End-to-end reservation workflow |
| + 2 additional classes | Supporting model/service coverage |

**Run tests locally:**
```bash
mvn test
```

**Download test reports** from GitHub → Actions → Artifacts → `surefire-test-reports`

---

## 👥 Contributors

| Contributor | Role |
|---|---|
| [AtheekAzmi](https://github.com/AtheekAzmi) | Lead Developer |

---

## 📄 License

This is an academic project. No license has been applied.

---

*Repository: [github.com/AtheekAzmi/RoomReservationSystem](https://github.com/AtheekAzmi/RoomReservationSystem) · February 2026*
