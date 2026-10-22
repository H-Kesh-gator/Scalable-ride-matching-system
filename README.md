> **New: interactive Rideflow UI** — see [DEMO.md](DEMO.md) for the runnable Java demo, setup, tests, attribution, and limitations. Run `mvn -Pdemo spring-boot:run -Dspring-boot.run.profiles=demo`.

# Ride Hailing Platform

A comprehensive ride-hailing platform application built with Spring Boot.

## Project Structure

```
ride-hailing-platform/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── platform/
│   │   │           ├── Application.java
│   │   │           ├── controller/
│   │   │           │   ├── UserController.java
│   │   │           │   ├── BookingController.java
│   │   │           │   ├── TripController.java
│   │   │           │   ├── PaymentController.java
│   │   │           │   └── AdminController.java
│   │   │           ├── service/
│   │   │           │   ├── MatchingEngine.java
│   │   │           │   ├── PricingEngine.java
│   │   │           │   ├── NotificationService.java
│   │   │           │   ├── EmergencyService.java
│   │   │           │   ├── OTPManager.java
│   │   │           │   ├── CustomerCareService.java
│   │   │           │   └── impl/
│   │   │           ├── model/
│   │   │           │   ├── core/
│   │   │           │   │   ├── Rateable.java
│   │   │           │   │   ├── Matchable.java
│   │   │           │   │   └── Payable.java
│   │   │           │   ├── enums/
│   │   │           │   │   ├── ServiceType.java
│   │   │           │   │   ├── VehicleCategory.java
│   │   │           │   │   ├── DocType.java
│   │   │           │   │   ├── VerificationStatus.java
│   │   │           │   │   ├── Gender.java
│   │   │           │   │   ├── MessageType.java
│   │   │           │   │   ├── TicketStatus.java
│   │   │           │   │   ├── EarningSource.java
│   │   │           │   │   ├── AccessLevel.java
│   │   │           │   │   ├── TripStatus.java
│   │   │           │   │   ├── ParcelStatus.java
│   │   │           │   │   └── DriverStatus.java
│   │   │           │   ├── user/
│   │   │           │   │   ├── User.java
│   │   │           │   │   ├── Customer.java
│   │   │           │   │   ├── Driver.java
│   │   │           │   │   └── Administrator.java
│   │   │           │   ├── ride/
│   │   │           │   │   ├── Trip.java
│   │   │           │   │   ├── BookingRequest.java
│   │   │           │   │   ├── ServiceZone.java
│   │   │           │   │   ├── Vehicle.java
│   │   │           │   │   └── Parcel.java
│   │   │           │   ├── payment/
│   │   │           │   │   ├── Invoice.java
│   │   │           │   │   └── DriverPayoutModel.java
│   │   │           │   └── support/
│   │   │           │       ├── Document.java
│   │   │           │       ├── Rating.java
│   │   │           │       └── Message.java
│   │   │           ├── strategy/
│   │   │           │   ├── MatchStrategy.java
│   │   │           │   ├── PaymentStrategy.java
│   │   │           │   └── impl/
│   │   │           │       ├── UPIPayment.java
│   │   │           │       ├── CashPayment.java
│   │   │           │       └── WalletPayment.java
│   │   │           ├── repository/
│   │   │           │   ├── UserRepository.java
│   │   │           │   ├── TripRepository.java
│   │   │           │   └── InvoiceRepository.java
│   │   │           ├── dto/
│   │   │           │   ├── request/
│   │   │           │   └── response/
│   │   │           └── view/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       └── java/
│           └── com/
│               └── platform/
└── pom.xml
```

## Technology Stack

- Java 8
- Spring Boot 2.7.18
- Spring Data JPA
- H2 Database
- Maven

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6 or higher

### Installation

1. Clone the repository
2. Navigate to the project directory
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### IDE Configuration

If you're seeing errors like "The declared package 'com.platform' does not match the expected package 'main.java.com.platform'", you need to configure your IDE to recognize this as a Maven project:

#### For IntelliJ IDEA:
1. Open the project
2. Go to File > Project Structure > Modules
3. Set the source root to `src/main/java`
4. Mark `src/main/java` as Sources Root
5. Reload Maven dependencies: Right-click pom.xml > Reload Maven Project

#### For Eclipse:
1. Import as Maven project: File > Import > Existing Maven Projects
2. Select the root directory
3. Complete the import process

#### For VS Code:
1. Install the Java Extension Pack
2. Install the Maven for Java extension
3. Open the command palette (Ctrl+Shift+P)
4. Run "Java: Import Maven Project"

## Fixing Common Issues

### Package Declaration Errors
The IDE might show package declaration errors because it's not recognizing the Maven source structure. The correct package declaration is `package com.platform.xxx;` and the IDE should be configured to recognize `src/main/java` as the source root.

### Dependency Resolution
If you see "cannot be resolved" errors for Spring classes:
1. Ensure Maven dependencies are downloaded: `mvn clean install`
2. Refresh your IDE's Maven dependencies
3. Check that your IDE is recognizing this as a Maven project

## API Endpoints

Once running, the application will be available at `http://localhost:8080`

## Database

The application uses an in-memory H2 database. You can access the H2 console at `http://localhost:8080/h2-console`

## Testing

Run tests with:
```bash
mvn test
```
# Scalable-ride-matching-system
