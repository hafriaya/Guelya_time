# Guelya Time 🎬

A comprehensive movie recommendation platform with multiple interfaces - featuring a modern web registration system and a powerful JavaFX desktop application for personalized film recommendations.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

Guelya Time is a multi-platform movie recommendation system that helps users discover personalized film recommendations. The application consists of three main components:

1. *Web Registration Frontend* - Next.js application for user registration and authentication
2. *REST API Backend* - Spring Boot API for user management and authentication
3. *Desktop Application* - JavaFX desktop app for browsing movies, getting recommendations, and managing watchlists

## 🏗️ Architecture


Guelya Time
├── register/              # Next.js Web Frontend
│   ├── Registration & Login UI
│   └── User Authentication
│
├── register-api/          # Spring Boot REST API
│   ├── User Management
│   ├── JWT Authentication
│   └── Neo4j Integration
│
└── desktop-app/           # JavaFX Desktop Application
    ├── Movie Search & Discovery
    ├── Personalized Recommendations
    ├── Watchlist Management
    └── User Dashboard


## ✨ Features

### Web Application (Next.js)
- ✅ Modern, responsive registration and login pages
- ✅ Real-time form validation
- ✅ Password strength indicator
- ✅ Toast notifications for user feedback
- ✅ Mobile-first design
- ✅ Seamless API integration

### Desktop Application (JavaFX)
- ✅ User authentication
- ✅ Movie search functionality
- ✅ Personalized movie recommendations
- ✅ Popular films browsing
- ✅ Watchlist management
- ✅ Movie and actor details
- ✅ Modern, intuitive UI

### Backend API (Spring Boot)
- ✅ User registration and authentication
- ✅ JWT-based security
- ✅ Neo4j graph database integration
- ✅ Password encryption (BCrypt)
- ✅ RESTful API design
- ✅ Comprehensive error handling

## 🛠️ Tech Stack

### Frontend (Web)
- *Next.js 14* - React framework with App Router
- *TypeScript* - Type-safe development
- *Tailwind CSS* - Utility-first styling
- *React Hook Form* - Form management
- *Axios* - HTTP client

### Backend API
- *Spring Boot 3.2* - Java framework
- *Spring Security* - Authentication & authorization
- *Spring Data Neo4j* - Graph database integration
- *JWT (jjwt 0.12.3)* - Token-based authentication
- *Bean Validation* - Input validation

### Desktop Application
- *JavaFX 17* - Desktop UI framework
- *Java 17* - Programming language
- *Neo4j Java Driver 5.17.0* - Database connectivity
- *BCrypt* - Password hashing
- *Maven* - Build tool

### Database
- *Neo4j* - Graph database for user and movie relationships

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- *Node.js* (v18 or higher) and npm
- *Java JDK 17* or higher
- *Maven 3.6+*
- *Neo4j* (running on default port 7687)
- *Git*

## 🚀 Installation

### 1. Clone the Repository

bash
git clone <repository-url>
cd Guelya_time


### 2. Setup Neo4j Database

1. Install and start Neo4j (Docker recommended):
   bash
   docker run -p 7474:7474 -p 7687:7687 -e NEO4J_AUTH=neo4j/password neo4j:latest
   

2. Access Neo4j Browser at http://localhost:7474
3. Default credentials: neo4j / password (change on first login)

### 3. Setup Backend API (register-api)

bash
cd register-api

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run


The API will be available at http://localhost:8080

*Configuration* (src/main/resources/application.properties):
properties
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=your_password


### 4. Setup Web Frontend (register)

bash
cd register

# Install dependencies
npm install

# Create environment file (if needed)
# cp .env.example .env.local

# Start development server
npm run dev


The web app will be available at http://localhost:3000

*Note*: The Next.js app is configured to proxy API requests to http://localhost:8080 (see next.config.js)

### 5. Setup Desktop Application (desktop-app)

bash
cd desktop-app

# Build the project
mvn clean install

# Run the application
mvn javafx:run


Or run the JAR file:
bash
java -jar target/desktop-app-1.0-SNAPSHOT.jar


*Configuration* (src/main/resources/application.properties):
properties
neo4j.uri=bolt://localhost:7687
neo4j.username=neo4j
neo4j.password=your_password


## ⚙️ Configuration

### Environment Variables

#### Web Frontend (.env.local)
env
NEXT_PUBLIC_API_URL=http://localhost:8080/api


#### Backend API (application.properties)
properties
# Neo4j Configuration
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=your_password

# JWT Configuration
jwt.secret=your-secret-key-change-in-production
jwt.expiration=86400000

# Server Configuration
server.port=8080


#### Desktop App (application.properties)
properties
neo4j.uri=bolt://localhost:7687
neo4j.username=neo4j
neo4j.password=your_password


## 📖 Usage

### Web Application

1. Navigate to http://localhost:3000
2. Click "Commencer gratuitement" to register a new account
3. Fill in the registration form with:
   - Username (3-20 characters)
   - Email (valid format)
   - Password (min 8 chars, 1 uppercase, 1 lowercase, 1 number)
   - First and last name (optional)
4. After registration, you'll be redirected to the login page
5. Log in with your credentials

### Desktop Application

1. Launch the desktop application
2. Log in with your registered credentials
3. Explore features:
   - *Search*: Use the search bar to find movies and actors
   - *Popular Films*: Browse trending movies
   - *Recommendations*: Get personalized movie suggestions
   - *Watchlist*: Save movies you want to watch later
   - *Details*: Click on movies/actors for detailed information

## 📁 Project Structure


Guelya_time/
├── register/                      # Next.js Web Frontend
│   ├── app/                       # Next.js App Router pages
│   │   ├── layout.tsx
│   │   ├── page.tsx              # Homepage
│   │   ├── register/
│   │   │   └── page.tsx          # Registration page
│   │   └── login/
│   │       └── page.tsx          # Login page
│   ├── components/               # React components
│   │   ├── RegisterForm.tsx
│   │   ├── LoginForm.tsx
│   │   └── ui/                   # UI components
│   ├── lib/                      # Utilities
│   │   ├── api.ts               # API client
│   │   └── validations.ts       # Form validations
│   ├── types/                    # TypeScript types
│   └── package.json
│
├── register-api/                 # Spring Boot Backend
│   ├── src/main/java/com/guelyatime/
│   │   ├── controller/          # REST controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # Data access
│   │   ├── model/               # Domain models
│   │   ├── dto/                 # Data transfer objects
│   │   ├── security/            # Security configuration
│   │   └── exception/           # Exception handling
│   └── pom.xml
│
├── desktop-app/                  # JavaFX Desktop App
│   ├── src/main/java/
│   │   ├── app/                 # Application entry point
│   │   ├── controller/          # FXML controllers
│   │   ├── model/               # Domain models
│   │   ├── service/             # Business logic
│   │   ├── repository/          # Data access
│   │   └── config/              # Configuration
│   ├── src/main/resources/
│   │   ├── fxml/                # FXML UI files
│   │   └── css/                 # Stylesheets
│   └── pom.xml
│
└── pom.xml                       # Parent POM


## 📡 API Documentation

### Authentication Endpoints

#### POST /api/auth/register
Register a new user.

*Request Body:*
json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "Password123",
  "firstName": "John",
  "lastName": "Doe"
}


*Responses:*
- 201 Created: User registered successfully
- 400 Bad Request: Validation error or user already exists

#### POST /api/auth/login
Authenticate a user.

*Request Body:*
json
{
  "email": "john@example.com",
  "password": "Password123"
}


*Response:*
json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe"
}


*Responses:*
- 200 OK: Login successful
- 401 Unauthorized: Invalid credentials

### Validation Rules

| Field | Rules |
|-------|-------|
| username | 3-20 characters, alphanumeric and underscores only |
| email | Valid email format |
| password | Min 8 characters, 1 uppercase, 1 lowercase, 1 number |
| firstName | Optional, max 50 characters |
| lastName | Optional, max 50 characters |

## 🧪 Development

### Running Tests

#### Backend API
bash
cd register-api
mvn test


#### Desktop App
bash
cd desktop-app
mvn test


### Building for Production

#### Web Frontend
bash
cd register
npm run build
npm start


#### Backend API
bash
cd register-api
mvn clean package
java -jar target/register-api-1.0-SNAPSHOT.jar


#### Desktop App
bash
cd desktop-app
mvn clean package
java -jar target/desktop-app-1.0-SNAPSHOT.jar


## 🔐 Security

- Passwords are hashed using BCrypt
- JWT tokens for stateless authentication
- CORS protection configured
- Input validation on both client and server
- Secure password requirements enforced

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (git checkout -b feature/amazing-feature)
3. Commit your changes (git commit -m 'Add some amazing feature')
4. Push to the branch (git push origin feature/amazing-feature)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- *Your Name* - Initial work

## 🙏 Acknowledgments

- Neo4j for graph database technology
- Spring Boot team for the excellent framework
- Next.js team for the React framework
- JavaFX community for desktop UI components
