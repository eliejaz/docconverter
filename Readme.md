# Document Converter

Document Converter is a Spring Boot application that provides REST APIs to convert documents between different formats. The application supports converting PDF to DOCX, PDF to text-only DOCX, and DOCX to PDF. The conversion processes are handled asynchronously to allow for non-blocking API responses.

## Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Setup](#setup)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Features

- Convert PDF to DOCX
- Convert PDF to text-only DOCX
- Convert DOCX to PDF
- Asynchronous processing of conversion tasks
- Swagger UI for API documentation

## Technologies

- Java 21
- Spring Boot 3.x
- Spring Data JPA
- H2 Database
- Apache PDFBox
- Apache POI
- pdf2docx Python library
- Lombok
- Mockito
- JUnit 5

## Setup

### Prerequisites

- Java 21
- Maven
- Python 3.x
- pdf2docx Python library

### Installation

1. Clone the repository:

```bash
git clone https://github.com/eliejaz/docconverter.git
cd docconverter
```

2. Build the project:

```bash
mvn clean install
```

3. Install the pdf2docx Python library:

```bash
pip install pdf2docx
```

4. Run the application:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## Usage

### Swagger UI

Access the Swagger UI for API documentation at `http://localhost:8080/swagger-ui/index.html#/`.

### Upload a Document

You need to upload a document before requesting a conversion.

### Convert a Document

After uploading, you can request a conversion using the provided API endpoints.

### Check Conversion Status

Check the status of your conversion request using the conversion ID.

### Download Converted Document

Once the conversion is completed, you can download the converted document using the conversion ID.

## Testing

To run the tests:

```bash
mvn test
```

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=eliejaz_docconverter&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=eliejaz_docconverter)