# Face Detection Microservice 📸

This project is a compact yet complete face detection system built with a polyglot microservice architecture — ideal for demoing production-ready backend skills, from authentication to gRPC integration and containerized deployment.

## 💡 Tech Stack

- Python (Flask) — REST API for photo upload & UI/API gateway
- Webcam integration (OpenCV + Flask) — capture photo directly from browser
- Java (Spring Boot) — business logic and user management
- Python (gRPC) — face detection service (OpenCV-based)
- PostgreSQL — persistent storage
- JWT — secure authentication for photo management
- 🐳 Docker & Docker Compose — seamless multi-service setup

## Project Showcase

- [UI walkthrough video](./UI_presentation.mp4)
- [Swagger API demo](./SWAGGER_doc.mp4)
- [Example validation image](./PhotoForValidationExample/Valid_Example.jpg)

## Key Engineering Challenges

- Bridging Java + Python through HTTP and gRPC with clean interface definitions
- Handling image validation efficiently with OpenCV-based face detection
- Implementing secure JWT authentication from scratch
- Building a minimal admin panel UI with Flask that also acts as an API Gateway
- Designing a clean Docker Compose setup for instant deployment

## ✅ Testing

The Java backend is covered with integration and unit tests:

- Controller layer: validated responses, error handling (400, 403, 404, 500)
- Service layer: business logic, multithreading cases, transactional behavior
- Test tools: JUnit 5, Mockito, Testcontainers (PostgreSQL)

## 🐳 How to Run

1. Copy `.env.example` → `.env` and configure environment variables
2. Run everything with Docker Compose:

```bash
docker-compose up --build
```

3. Access services:
   - Frontend (UI): `http://localhost:5000`
   - Swagger API docs: `http://localhost:8080/swagger-ui/index.html`

## ⚠️ Tech Notes

- For simplicity, validated photos are returned as **names only** (stored locally in PostgreSQL).

- In a production-ready system, photo files would typically be uploaded to a **cloud storage provider** (e.g. AWS S3, Google Cloud Storage), and a URL or secure token would be returned instead.

## Feel free to explore

You can test the system by uploading sample photos or using the built-in webcam support.  
Single-face photos will pass validation; multiple faces will trigger a rejection.

## 🤝 Thanks for your interest!

- I'm always open to feedback, collaboration, or professional connections.
- Feel free to reach out!
