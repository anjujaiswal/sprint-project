# 🔍 Sprinto Evidence Bot

## AI-Powered Evidence-on-Demand for Compliance & Audit

The Sprinto Evidence Bot is an innovative application that leverages artificial intelligence to streamline compliance documentation, evidence collection, and audit preparation. Built with Spring Boot and integrated with OpenAI GPT-3.5, it transforms how organizations manage their compliance requirements.

---

## 🎯 What Problem Does It Solve?

### Traditional Compliance Challenges:
- **Manual Evidence Collection**: Hours spent searching through documents for compliance evidence
- **Audit Preparation**: Difficulty in organizing and presenting relevant documentation
- **Knowledge Gaps**: Compliance requirements scattered across multiple documents
- **Time-Intensive Reviews**: Manual analysis of policies and procedures
- **Human Error**: Risk of missing critical compliance information

### Our Solution:
- **Instant Evidence Retrieval**: AI-powered search finds relevant compliance evidence in seconds
- **Intelligent Document Analysis**: Automated analysis of policies, procedures, and compliance documents
- **Interactive AI Chat**: Natural language queries to understand compliance status
- **Gap Analysis**: Identifies missing compliance requirements and recommendations
- **Audit-Ready Reports**: Generates comprehensive compliance reports instantly

---

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend UI   │◄──►│   Spring Boot    │◄──►│   OpenAI API    │
│   (HTML/JS)     │    │     Backend      │    │   (GPT-3.5)     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │  H2 Database    │
                       │ (In-Memory)     │
                       └─────────────────┘
```

### Technology Stack:
- **Backend**: Spring Boot 3.2.0 with Java 17
- **Frontend**: HTML5 with vanilla JavaScript and React CDN
- **Database**: H2 In-Memory Database
- **AI Integration**: OpenAI GPT-3.5 Turbo
- **Build Tool**: Maven
- **File Processing**: Apache PDFBox, Apache POI for document parsing

---

## 🚀 How to Start the Application

### Prerequisites:
- Java 17 or higher
- Maven 3.6+
- OpenAI API Key
- Modern web browser

### Step 1: Clone and Navigate
```bash
git clone https://github.com/anjujaiswal/sprinto-project
cd sprinto
```

### Step 2: Configure OpenAI API Key
Edit `backend/src/main/resources/application.properties`:
```properties
openai.api.key=your_openai_api_key_here
```

### Step 3: Start the Backend
```bash
cd backend
mvn spring-boot:run
```
✅ Backend will start on http://localhost:8080

### Step 4: Access the Frontend
Open your web browser and navigate to:
```
http://localhost:3000/sprinto-app.html
```

Or directly open the file:
```bash
open ui/sprinto-app.html
```

### Step 5: Start Using the Application
1. **Upload Documents**: Use the "Upload Documents" tab to add your compliance documents
2. **Search Evidence**: Use the "Search Evidence" tab to find specific compliance information
3. **AI Chat**: Interact with the AI assistant for detailed analysis
4. **View Reports**: Check the "Compliance Report" tab for overview metrics

---

## 🎮 How It Works

### 1. Document Upload & Processing
- **Supported Formats**: PDF, Word documents (.doc, .docx), Text files (.txt)
- **Text Extraction**: Automatically extracts and processes document content
- **Storage**: Securely stores documents in H2 database
- **Indexing**: Creates searchable content for fast retrieval

### 2. AI-Powered Search
- **Natural Language Queries**: Ask questions in plain English
- **Contextual Understanding**: AI understands compliance terminology
- **Relevant Results**: Returns focused, actionable evidence
- **Source Citation**: References specific documents for transparency

### 3. Intelligent Chat Interface
- **Conversational AI**: Natural dialogue about compliance topics
- **Document Analysis**: Deep analysis of uploaded compliance documents
- **Gap Identification**: Highlights missing compliance requirements
- **Recommendations**: Provides actionable improvement suggestions

### 4. Compliance Reporting
- **Real-time Metrics**: Live compliance score and document counts
- **Policy Analysis**: Automatic categorization of policy vs procedure documents
- **Audit Readiness**: Generates comprehensive compliance reports

---

## 📁 Project Structure

```
sprinto/
├── backend/                    # Spring Boot Application
│   ├── src/main/java/
│   │   └── com/sprinto/evidencebot/
│   │       ├── controller/     # REST API Controllers
│   │       ├── model/         # Data Models
│   │       ├── repository/    # Data Access Layer
│   │       └── service/       # Business Logic
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml               # Maven Dependencies
├── ui/
│   └── sprinto-app.html      # Frontend Application
├── docs/                     # Documentation and Sample Files
├── README.md                 # This file
└── presentation.pptx         # Project Presentation
```

---

## 🔧 API Endpoints

### Document Management
- `GET /api/documents` - List all documents
- `POST /api/documents/upload` - Upload new document
- `GET /api/documents/{id}` - Get specific document

### Evidence Generation
- `POST /api/evidence/generate` - Generate evidence for query

### AI Chat
- `POST /api/ai/chat` - Chat with AI assistant
- `POST /api/ai/analyze-document` - Analyze specific document
- `GET /api/ai/suggestions` - Get query suggestions

### Compliance Reports
- `GET /api/compliance/report/{domain}` - Generate compliance report

---

## 🔒 Security Features

- **Data Encryption**: All sensitive data encrypted in transit
- **Input Validation**: Comprehensive input sanitization
- **File Type Restrictions**: Only approved document formats accepted
- **API Rate Limiting**: Prevents abuse of AI services
- **CORS Protection**: Secure cross-origin resource sharing

---

## 🎯 Use Cases

### For Compliance Teams:
- **Audit Preparation**: Quickly gather evidence for compliance audits
- **Policy Reviews**: Analyze existing policies for gaps and improvements
- **Documentation Gaps**: Identify missing compliance documentation
- **Training Material**: Generate compliance training content

### For IT Teams:
- **Security Assessments**: Analyze security policies and procedures
- **Risk Management**: Identify compliance risks and mitigation strategies
- **Vendor Assessments**: Evaluate third-party compliance documentation

### For Management:
- **Compliance Dashboards**: Real-time compliance status overview
- **Executive Reports**: High-level compliance metrics and trends
- **Decision Support**: Data-driven compliance investment decisions

---

## 🔮 Future Enhancements

- **Multi-tenant Support**: Support for multiple organizations
- **Advanced Analytics**: Detailed compliance trend analysis
- **Integration APIs**: Connect with popular compliance platforms
- **Mobile Application**: Native mobile app for on-the-go access
- **Real-time Notifications**: Alerts for compliance deadlines
- **Automated Workflows**: Streamlined compliance processes

---

## 🤝 Contributing

We welcome contributions! Please see our contributing guidelines for details.

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 📞 Support

For support and questions:
- Email: support@sprinto.com
- Documentation: Check the `/docs` folder
- Issues: Submit GitHub issues for bug reports

---

**Built with ❤️ for the compliance community**