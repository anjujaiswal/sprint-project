package com.sprinto.evidencebot.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAIService {

    private final OpenAiService openAiService;

    public OpenAIService(@Value("${openai.api.key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
    }

    public String generateEvidenceSummary(String query, List<String> documentContents) {
        String systemPrompt = "You are an expert compliance and evidence analyst. " +
                "Your role is to analyze documents and provide clear, actionable evidence summaries for compliance purposes. " +
                "Focus on identifying relevant compliance evidence, risks, and recommendations.";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Query: ").append(query).append("\n\n");
        userPrompt.append("Please analyze the following documents and provide:\n");
        userPrompt.append("1. A comprehensive summary of evidence related to the query\n");
        userPrompt.append("2. Compliance status assessment\n");
        userPrompt.append("3. Identified gaps or risks\n");
        userPrompt.append("4. Specific recommendations for improvement\n\n");
        userPrompt.append("Documents:\n");

        for (int i = 0; i < documentContents.size(); i++) {
            userPrompt.append("Document ").append(i + 1).append(":\n");
            String content = documentContents.get(i);
            // Limit content to avoid token limits
            if (content.length() > 2000) {
                content = content.substring(0, 2000) + "...";
            }
            userPrompt.append(content).append("\n\n");
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), userPrompt.toString()));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .maxTokens(1000)
                .temperature(0.3)
                .build();

        try {
            return openAiService.createChatCompletion(request)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
        } catch (Exception e) {
            System.err.println("OpenAI API Error in generateEvidenceSummary: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return "Error generating AI summary: " + e.getMessage();
        }
    }

    public String analyzeComplianceGaps(String domain, List<String> documentContents) {
        String systemPrompt = "You are a compliance expert specializing in " + domain + " compliance. " +
                "Analyze the provided documents to identify compliance gaps, risks, and provide specific recommendations.";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Please analyze these documents for ").append(domain).append(" compliance and provide:\n");
        userPrompt.append("1. Current compliance status\n");
        userPrompt.append("2. Identified gaps and missing requirements\n");
        userPrompt.append("3. Risk assessment\n");
        userPrompt.append("4. Prioritized action items\n");
        userPrompt.append("5. Recommended evidence to collect\n\n");

        for (int i = 0; i < documentContents.size(); i++) {
            userPrompt.append("Document ").append(i + 1).append(":\n");
            String content = documentContents.get(i);
            if (content.length() > 1500) {
                content = content.substring(0, 1500) + "...";
            }
            userPrompt.append(content).append("\n\n");
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), userPrompt.toString()));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .maxTokens(1200)
                .temperature(0.2)
                .build();

        try {
            return openAiService.createChatCompletion(request)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
        } catch (Exception e) {
            System.err.println("OpenAI API Error in analyzeComplianceGaps: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return "Error analyzing compliance gaps: " + e.getMessage();
        }
    }

    public String chatWithDocuments(String userQuery, List<String> documentContents, String conversationHistory) {
        String systemPrompt = "You are an intelligent assistant that helps users understand and analyze their compliance documents. " +
                "Answer questions based on the provided documents and conversation history. " +
                "Be helpful, accurate, and cite specific information from the documents when possible.";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("User Question: ").append(userQuery).append("\n\n");
        
        if (conversationHistory != null && !conversationHistory.trim().isEmpty()) {
            userPrompt.append("Previous Conversation:\n").append(conversationHistory).append("\n\n");
        }

        userPrompt.append("Available Documents:\n");
        for (int i = 0; i < documentContents.size(); i++) {
            userPrompt.append("Document ").append(i + 1).append(":\n");
            String content = documentContents.get(i);
            if (content.length() > 1000) {
                content = content.substring(0, 1000) + "...";
            }
            userPrompt.append(content).append("\n\n");
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), userPrompt.toString()));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .maxTokens(800)
                .temperature(0.4)
                .build();

        try {
            return openAiService.createChatCompletion(request)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
        } catch (Exception e) {
            System.err.println("OpenAI API Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return "Error processing your question: " + e.getMessage();
        }
    }
}