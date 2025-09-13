# 🔗 API Evidence Integration Documentation

## Overview

The Sprinto Evidence Bot now includes comprehensive API evidence retrieval capabilities that integrate with external change management and ticketing providers. This implementation fulfills the mandatory requirement for pulling specific records, logs, and configurations based on query parameters and formatting results into human-readable summaries and downloadable files.

## 🎯 Implementation Features

### ✅ Mandatory Requirements Fulfilled

1. **API Integration with Change Management Providers**
   - ✅ GitHub integration (commits, pull requests, issues)
   - 🔄 Jira integration (planned for future releases)

2. **Query-Based Record Retrieval**
   - ✅ Pull specific commits based on query parameters
   - ✅ Filter by author, date range, and search terms
   - ✅ Retrieve pull requests with state filtering
   - ✅ Access issues with label and status filtering

3. **Human-Readable Summaries**
   - ✅ AI-generated analysis of retrieved evidence
   - ✅ Compliance scoring and recommendations
   - ✅ Formatted summaries with key metrics

4. **Downloadable Files**
   - ✅ CSV export functionality
   - ✅ Unified reporting across multiple providers
   - ✅ Batch evidence retrieval

## 🏗️ Architecture

### Core Components

```
┌─────────────────────┐    ┌──────────────────────┐    ┌─────────────────────┐
│   Frontend UI       │◄──►│  APIEvidenceService  │◄──►│  External APIs      │
│   (React/HTML)      │    │  (Unified Interface) │    │  (GitHub, Jira)     │
└─────────────────────┘    └──────────────────────┘    └─────────────────────┘
                                      │
                                      ▼
                           ┌──────────────────────┐
                           │ GitHubEvidenceService│
                           │ (Provider-Specific)  │
                           └──────────────────────┘
```

### Service Layer

1. **APIEvidenceService** - Unified interface for all providers
2. **GitHubEvidenceService** - GitHub-specific implementation
3. **APIEvidenceController** - REST API endpoints
4. **GitHubEvidenceController** - GitHub-specific endpoints

## 📡 API Endpoints

### Unified Evidence Retrieval

```http
POST /api/evidence/retrieve
Content-Type: application/json

{
  "provider": "github",
  "params": {
    "owner": "microsoft",
    "repo": "vscode",
    "type": "commits",
    "query": "security",
    "author": "username",
    "since": "2024-01-01"
  }
}
```

### GitHub-Specific Endpoints

```http
# Get evidence with advanced parameters
GET /api/github/evidence/{owner}/{repo}?type=commits&query=security&since=2024-01-01&author=username

# Download CSV report
GET /api/github/download/{owner}/{repo}?type=commits

# Get repositories for an owner
GET /api/github/repositories/{owner}
```

### Batch Operations

```http
POST /api/evidence/batch
Content-Type: application/json

{
  "requests": [
    {
      "provider": "github",
      "params": {
        "owner": "microsoft",
        "repo": "vscode",
        "type": "commits"
      }
    },
    {
      "provider": "github",
      "params": {
        "owner": "facebook",
        "repo": "react",
        "type": "prs"
      }
    }
  ]
}
```

### Compliance Reporting

```http
GET /api/evidence/compliance/{provider}/{project}?timeframe=30d
```

## 🔧 Configuration

### GitHub API Setup

1. **Generate Personal Access Token**:
   - Go to GitHub Settings → Developer settings → Personal access tokens
   - Generate new token with `repo` and `read:org` scopes

2. **Configure Application**:
   ```properties
   # application.properties
   github.api.token=your_github_token_here
   github.api.base-url=https://api.github.com
   ```

### Supported Query Parameters

#### GitHub Provider

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `owner` | String | Repository owner | `microsoft` |
| `repo` | String | Repository name | `vscode` |
| `type` | String | Evidence type | `commits`, `prs`, `issues` |
| `query` | String | Search term | `security` |
| `author` | String | GitHub username | `john.doe` |
| `since` | Date | Start date (ISO) | `2024-01-01T00:00:00Z` |
| `until` | Date | End date (ISO) | `2024-12-31T23:59:59Z` |

## 📊 Response Format

### Standard Evidence Response

```json
{
  "source": "GitHub Commits",
  "repository": "microsoft/vscode",
  "query_params": "query: security, author: john.doe",
  "evidence": [
    {
      "sha": "abc12345",
      "message": "Fix security vulnerability in authentication",
      "author": "john.doe",
      "date": "2024-01-15",
      "url": "https://github.com/microsoft/vscode/commit/abc12345",
      "files_changed": "3"
    }
  ],
  "summary": "Found 15 commits matching 'security'",
  "human_readable": "Repository microsoft/vscode shows 15 commits. Most active contributor: john.doe (8 commits). Latest activity on 2024-01-15."
}
```

### CSV Export Format

#### Commits CSV
```csv
SHA,Message,Author,Date,Files Changed,URL
abc12345,"Fix security vulnerability",john.doe,2024-01-15,3,https://github.com/...
```

#### Pull Requests CSV
```csv
Number,Title,State,Author,Created,Merged,URL
123,"Add security feature",merged,jane.doe,2024-01-10,2024-01-12,https://github.com/...
```

#### Issues CSV
```csv
Number,Title,State,Author,Created,Closed,Labels,URL
456,"Security bug report",closed,user123,2024-01-05,2024-01-08,"bug,security",https://github.com/...
```

## 🎨 Frontend Integration

### API Evidence Tab

The frontend includes a dedicated "API Evidence" tab that provides:

1. **Provider Selection**: Choose between GitHub and Jira (when available)
2. **Dynamic Parameter Forms**: Context-aware input fields based on selected provider
3. **Real-time Results**: Live display of retrieved evidence
4. **AI Analysis**: Human-readable summaries and insights
5. **Export Options**: Download evidence as CSV files

### Usage Examples

```javascript
// Retrieve GitHub commits
const evidence = await fetch('/api/evidence/retrieve', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    provider: 'github',
    params: {
      owner: 'microsoft',
      repo: 'vscode',
      type: 'commits',
      query: 'security',
      since: '2024-01-01'
    }
  })
});
```

## 🔒 Security & Best Practices

### Authentication
- GitHub Personal Access Tokens stored securely
- Rate limiting implemented to prevent API abuse
- Input validation on all parameters

### Error Handling
- Comprehensive error messages for debugging
- Graceful fallbacks for API failures
- User-friendly error displays in UI

### Performance
- Pagination support for large result sets
- Caching of frequently requested data
- Batch operations for multiple requests

## 📈 Compliance Features

### Change Management Evidence
- **Commit History**: Track all code changes with author attribution
- **Pull Request Workflow**: Evidence of code review processes
- **Issue Tracking**: Documentation of bug reports and feature requests

### Audit Trail
- **Downloadable Reports**: CSV exports for audit documentation
- **Human-Readable Summaries**: AI-generated compliance insights
- **Batch Reporting**: Multiple repositories in single report

### Compliance Scoring
- **Automated Assessment**: Calculate compliance scores based on activity
- **Recommendations**: AI-generated improvement suggestions
- **Trend Analysis**: Track compliance metrics over time

## 🚀 Future Enhancements

### Planned Integrations
1. **Jira Integration**: Issue tracking and project management
2. **Azure DevOps**: Microsoft ecosystem integration
3. **GitLab**: Alternative Git provider support
4. **ServiceNow**: ITSM integration

### Advanced Features
1. **Real-time Webhooks**: Live evidence updates
2. **Custom Dashboards**: Personalized compliance views
3. **Automated Alerts**: Compliance threshold notifications
4. **Advanced Analytics**: Machine learning insights

## 🧪 Testing

### Manual Testing
1. Start the Spring Boot application
2. Navigate to the "API Evidence" tab
3. Enter GitHub repository details (e.g., microsoft/vscode)
4. Select evidence type (commits, PRs, issues)
5. Add optional filters (query, author, date range)
6. Click "Retrieve Evidence"
7. Review results and download CSV

### Example Test Cases
- **Basic Retrieval**: Get recent commits from public repository
- **Filtered Search**: Search for security-related commits
- **Author Filter**: Get commits by specific developer
- **Date Range**: Retrieve evidence from specific time period
- **CSV Export**: Download and verify CSV format
- **Error Handling**: Test with invalid repository names

## 📞 Support

For issues or questions regarding API evidence integration:
- Check GitHub token permissions and validity
- Verify repository access (public vs private)
- Review API rate limits (5000 requests/hour for authenticated users)
- Ensure proper network connectivity to GitHub API

## 📄 License

This implementation is part of the Sprinto Evidence Bot project and follows the same MIT License terms.