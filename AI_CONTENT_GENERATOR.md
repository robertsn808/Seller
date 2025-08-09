# AI Content Generator with Memory

This document describes the AI-powered content generator implementation for the Real Estate Seller Funnel application.

## Overview

The AI Content Generator is designed to create relevant, non-redundant marketing content using artificial intelligence. It includes memory capabilities to track previously generated content and ensure variety in output.

## Features

### 1. AI-Powered Content Generation

- Integrates with OpenAI GPT-3.5-turbo for content generation
- Supports multiple content types: Facebook posts, Google ads, emails, Craigslist ads
- Targets different audiences: sellers, buyers, or both
- Category-specific content generation (distressed properties, inherited properties, investors, etc.)

### 2. Memory System

- **Content Tracking**: Stores all generated content with metadata
- **Similarity Detection**: Uses Jaro-Winkler similarity algorithm to detect redundant content
- **Usage Analytics**: Tracks how often content is used and when it was last accessed
- **Keyword Extraction**: Automatically extracts and stores relevant keywords

### 3. Smart Suggestions

- Provides content suggestions based on existing patterns
- Offers best practices for different content types
- Suggests keywords and phrases based on successful content

### 4. Content Variations

- Generates multiple variations of the same content
- Ensures each variation is unique and non-redundant
- Allows users to choose from different options

## Architecture

### Models

#### AIGeneratedContent

- Stores AI-generated content with metadata
- Tracks similarity scores, usage patterns, and keywords
- Links content to specific categories and target audiences

### Services

#### AIContentGenerationService

- Handles communication with OpenAI API
- Manages content generation with memory consideration
- Provides fallback templates when AI is unavailable
- Generates content variations

#### ContentMemoryService

- Manages content similarity detection
- Provides content suggestions and statistics
- Tracks content usage patterns
- Extracts keywords from content

### Controllers

#### MarketingController

- REST endpoints for AI content generation
- Handles content similarity checks
- Provides content suggestions
- Manages content usage tracking

## Setup Instructions

### 1. Dependencies

The following dependencies have been added to `pom.xml`:

```xml
<!-- OpenAI Client -->
<dependency>
    <groupId>com.theokanning.openai-gpt3-java</groupId>
    <artifactId>service</artifactId>
    <version>0.18.2</version>
</dependency>

<!-- Text Processing -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-text</artifactId>
    <version>1.11.0</version>
</dependency>
```

### 2. Configuration

Add OpenAI API configuration to `application.properties`:

```properties
# OpenAI Configuration
openai.api.key=${OPENAI_API_KEY:}
openai.model=gpt-3.5-turbo
```

### 3. Environment Variables

Set the OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY=your_openai_api_key_here
```

## Usage

### 1. Access the AI Generator

1. Navigate to the Marketing Dashboard
2. Click on "Content Generator"
3. Click the "AI Content Generator" button

### 2. Generate Content

1. Fill in the content description
2. Select content type and target audience
3. Choose category (optional)
4. Add additional context (optional)
5. Select number of variations
6. Click "Generate Content"

### 3. Get Suggestions

1. Select content type and target audience
2. Click "Get Suggestions"
3. Review AI-generated suggestions for better content

### 4. Preview and Copy

- Preview generated content in different formats
- Copy content to clipboard
- Track usage automatically

## API Endpoints

### Generate Content

```html-request
POST /admin/marketing/content-generator/generate
```

Parameters:

- `prompt`: Content description
- `contentType`: Type of content (FACEBOOK_POST, GOOGLE_AD, etc.)
- `targetAudience`: Target audience (SELLERS, BUYERS, BOTH)
- `category`: Optional category
- `context`: Optional additional context

### Generate Variations

```html-request
POST /admin/marketing/content-generator/generate-variations
```

Additional parameter:

- `count`: Number of variations to generate

### Check Similarity

```html-request
GET /admin/marketing/content-generator/similarity-check
```

Parameters:

- `content`: Content to check
- `contentType`: Content type
- `targetAudience`: Target audience

### Get Suggestions

```html-request
GET /admin/marketing/content-generator/suggestions
```

Parameters:

- `contentType`: Content type
- `targetAudience`: Target audience

### Update Usage

```html-request
POST /admin/marketing/content-generator/use-content
```

Parameters:

- `contentId`: ID of the content being used

## Memory Features

### Similarity Detection

- Uses Jaro-Winkler similarity algorithm
- Configurable similarity threshold (default: 0.7)
- Prevents generation of highly similar content

### Content Tracking

- Stores generation count for each content piece
- Tracks last usage time
- Maintains similarity scores

### Keyword Extraction

- Automatically extracts keywords from generated content
- Filters out common stop words
- Stores keywords for future reference

## Fallback System

When OpenAI API is unavailable:

1. Uses predefined templates based on content type and audience
2. Maintains functionality without AI
3. Provides category-specific fallback content

## Best Practices

### 1. Content Generation

- Provide detailed prompts for better results
- Use specific categories for targeted content
- Include relevant context for more accurate generation

### 2. Memory Management

- Regularly review generated content
- Use similarity checks before publishing
- Monitor usage patterns to identify successful content

### 3. API Usage

- Set appropriate rate limits
- Monitor API usage costs
- Implement error handling for API failures

## Security Considerations

1. **API Key Protection**: Store OpenAI API key securely
2. **Content Validation**: Validate generated content before use
3. **Rate Limiting**: Implement rate limiting for API calls
4. **Error Handling**: Handle API failures gracefully

## Future Enhancements

1. **Advanced NLP**: Implement more sophisticated text analysis
2. **Content Optimization**: Add A/B testing capabilities
3. **Multi-language Support**: Support for multiple languages
4. **Integration**: Connect with other AI providers
5. **Analytics Dashboard**: Enhanced content performance tracking

## Troubleshooting

### Common Issues

1. **API Key Not Set**
   - Ensure OPENAI_API_KEY environment variable is set
   - Check application.properties configuration

2. **Content Too Similar**
   - Adjust similarity threshold in ContentMemoryService
   - Provide more specific prompts

3. **API Rate Limits**
   - Implement exponential backoff
   - Monitor API usage

4. **Memory Issues**
   - Clean up old content periodically
   - Optimize database queries

## Support

For issues or questions about the AI Content Generator:

1. Check the application logs
2. Verify API configuration
3. Test with fallback templates
4. Review memory settings
