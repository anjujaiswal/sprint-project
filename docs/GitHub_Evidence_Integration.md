# 🔗 GitHub Evidence Integration

## Overview

The Sprinto Evidence Bot includes GitHub API integration for retrieving compliance evidence from repositories. This implementation fulfills the mandatory requirement for API evidence retrieval from change management providers.

## ✅ Features

### GitHub Integration
- **Commits**: Retrieve commit history with filtering
- **Pull Requests**: Access PR data with state filtering  
- **Issues**: Get issue tracking information
- **Advanced Filtering**: Query by author, date range, search terms
- **CSV Export**: Download evidence for audit documentation
- **Human-Readable Summaries**: AI-generated compliance insights

## 🔧 Configuration

### GitHub API Setup
1. Generate Personal Access Token at GitHub Settings → Developer settings → Personal access tokens
2. Select scopes: `repo` (for private repos) or `public_repo` (for public repos)
3. Update `application.properties`:
```properties
github.api.token=your_github_token_here
github.api.base-url=https://api.github.com
```

## 📡 API Endpoints

### Evidence Retrieval
```http
POST /api/github/evidence
{
  "owner": "microsoft",
  "repo": "vscode", 
  "type": "commits",
  "query": "security",
  "author": "username",
  "since": "2024-01-01"
}
```

### CSV Download
```http
GET /api/github/download/{owner}/{repo}?type=commits
```

### Repository Listing
```http
GET /api/github/repositories/{owner}
```

## 📊 Response Format

```json
{
  "source": "GitHub Commits",
  "repository": "microsoft/vscode",
  "query_params": "query: security, author: john.doe",
  "evidence": [
    {
      "sha": "abc12345",
      "message": "Fix security vulnerability",
      "author": "john.doe", 
      "date": "2024-01-15",
      "url": "https://github.com/microsoft/vscode/commit/abc12345"
    }
  ],
  "summary": "Found 15 commits matching 'security'",
  "human_readable": "Repository shows active development with proper change management practices."
}
```

## 🎨 Frontend Usage

The GitHub Evidence tab provides:
- Repository owner/name input
- Evidence type selection (commits/PRs/issues)
- Optional query filtering
- Real-time results display
- CSV download functionality

## 🔒 Security

- Personal Access Tokens stored securely
- Rate limiting (5000 requests/hour for authenticated users)
- Input validation on all parameters
- Error handling for API failures

## 📈 Compliance Benefits

- **Change Management**: Track all code changes with author attribution
- **Code Review Process**: Evidence of pull request workflows
- **Issue Tracking**: Documentation of bug reports and feature requests
- **Audit Trail**: Downloadable CSV reports for compliance documentation