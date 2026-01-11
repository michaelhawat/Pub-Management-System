# 🍺 Pub Management System

A comprehensive pub management system built with Spring Boot backend and JavaFX frontend, following the same architecture patterns as the original authentication system.

## 🏗️ Architecture

### Backend (Spring Boot API)
- **Framework**: Spring Boot 3.5.5 with Java 17
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: JWT authentication (inherited from original system)
- **Architecture**: Clean separation with Controllers → Services → Repositories

### Frontend (JavaFX Desktop Client)
- **Framework**: JavaFX 17 with FXML
- **Architecture**: MVC pattern with controllers, models, and views
- **Communication**: RESTful API with JSON payloads

## 📊 Data Model

### Core Entities
- **User**: Staff members (Manager, Waiter, Bartender)
- **Customer**: Pub customers with contact information
- **Product**: Menu items with pricing and stock management
- **Table**: Dining tables with capacity and status tracking
- **Order**: Customer orders with status tracking
- **OrderItem**: Individual items within orders
- **Reservation**: Table reservations with time slots

### Key Relationships
- Orders belong to Customers and are served by Users
- Orders are assigned to Tables
- Orders contain multiple OrderItems
- OrderItems reference Products
- Reservations link Customers to Tables

## 🚀 Features

### Backend Services
- **User Management**: CRUD operations for staff
- **Customer Management**: Customer registration and management
- **Product Management**: Menu items with stock tracking
- **Table Management**: Table status and capacity management
- **Order Management**: Order creation, modification, and closure
- **Order Item Management**: Adding/removing items from orders
- **Reservation Management**: Table booking system

### Frontend Views
1. **Dashboard**: Overview of open orders, table status, and statistics
2. **Product Management**: CRUD operations for menu items
3. **Order Management**: Create orders, add items, close orders
4. **Table Management**: Monitor table status and capacity
5. **Customer Management**: Customer database management
6. **Reservation Management**: Table booking system

## 🎮 3D Visualization Features 

### Customer Insights (3D)
- Interactive 3D customer visualization
- Real-time data refresh
- Smooth animations and camera movement
- Implemented using:
  - `CustomerVisualizationController`
  - `CustomerVisualization.fxml`

### Table Reservation 3D View
- 3D floor layout representing tables
- Color-coded table status:
  - Available
  - Reserved
  - Occupied
- View rotation, reset, and refresh
- Filtering by table status
- Implemented using:
  - `TableVisualizationController`
  - `TableVisualization.fxml`

### Reservation Creation Dialog
- Modal popup reservation creation
- Customer selection
- Date and time selection
- Hour and minute spinners
- Implemented using:
  - `ReservationDialogController`
  - `ReservationDialog.fxml`

---

## 🛠️ Technical Implementation

### Backend Components
- **Entities**: JPA entities with proper relationships and validation
- **Repositories**: Spring Data JPA repositories with custom queries
- **Services**: Business logic with transaction management
- **Controllers**: RESTful API endpoints with error handling
- **DTOs**: Data transfer objects for API communication

### Frontend Components
- **Models**: DTOs matching backend API structure
- **API Clients**: HTTP client classes for each service
- **Controllers**: JavaFX controllers for each view
- **FXML**: Modern UI layouts with responsive design
- **Styling**: CSS with modern pub-themed design

## 🔧 Configuration

### Database Setup
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mfx
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
```

### API Endpoints
- `/api/users` - User management
- `/api/customers` - Customer management
- `/api/products` - Product management
- `/api/tables` - Table management
- `/api/orders` - Order management
- `/api/reservations` - Reservation management

## 🚀 Running the Application

### Backend (Spring Boot)
```bash
cd csis231-api
mvn spring-boot:run
```

### Frontend (JavaFX)
```bash
cd demo
mvn javafx:run
```

## 📱 User Interface

### Dashboard
- Real-time statistics (total orders, open orders, occupied tables)
- Quick navigation to all management modules
- Live tables showing open orders and table status

### Product Management
- Complete CRUD operations for menu items
- Stock quantity tracking
- Category-based organization
- Price management

### Order Management
- Create new orders with customer and table assignment
- Add/remove items from orders
- Real-time order status tracking
- Order closure and billing

### Table Management
- Monitor table availability
- Update table status (Available, Occupied, Reserved)
- Capacity management

### Customer Management
- Customer database with contact information
- Customer lookup for order creation
- Contact information management

### Reservation Management
- Table booking system
- Date and time scheduling
- Reservation status tracking
- Conflict prevention

## 🎨 Design Features

### Modern UI
- Clean, professional interface
- Pub-themed color scheme
- Responsive layouts
- Intuitive navigation

### User Experience
- Real-time data updates
- Contextual actions
- Error handling with userEntity-friendly messages
- Confirmation dialogs for destructive actions

## 🔒 Security

- Inherits JWT authentication from original system
- Role-based access control
- Secure API communication
- Input validation and sanitization

## 📈 Business Logic

### Order Management
- Stock validation before adding items
- Automatic stock updates
- Order status enforcement
- Table status management

### Reservation System
- Table availability checking
- Time conflict prevention
- Automatic table status updates

### Inventory Management
- Real-time stock tracking
- Low stock alerts
- Product availability checking

## 🚀 Future Enhancements

- Reporting and analytics
- Payment processing integration
- Inventory alerts and notifications
- Advanced reservation features
- Multi-location support
- Mobile app integration

## 📋 Requirements

- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- JavaFX 17+

This pub management system provides a complete solution for managing a pub's daily operations, from order taking to table management, with a modern, intuitive interface and robust backend architecture.
