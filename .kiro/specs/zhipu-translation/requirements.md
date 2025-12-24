# Requirements Document

## Introduction

本功能将输入翻译模块从 Google ML Kit 离线翻译改造为调用智谱AI在线翻译。新方案支持专业术语约束翻译，即普通语句正常翻译，而预定义的专业术语按照指定的翻译方式进行翻译。翻译结果等待完整响应后一次性显示。

## Glossary

- **TextTranslationActivity**: 文本输入翻译界面，用户在此输入文本并获取翻译结果
- **ZhipuAIService**: 智谱AI API服务类，负责与智谱AI大模型进行通信
- **约束词/术语表**: 预定义的专业术语及其对应翻译的映射表
- **glm-4-flash**: 智谱AI提供的免费大语言模型
- **源语言**: 用户输入文本的语言（中文或英文）
- **目标语言**: 翻译输出的语言（英文或中文）

## Requirements

### Requirement 1

**User Story:** As a user, I want to input text and get AI-powered translation, so that I can get higher quality translations than offline models.

#### Acceptance Criteria

1. WHEN a user enters text and clicks the translate button THEN the System SHALL send the text to ZhipuAI API and display the complete translation result
2. WHEN the translation request is in progress THEN the System SHALL display a loading indicator to inform the user
3. WHEN the translation completes successfully THEN the System SHALL hide the loading indicator and display the translated text in the result area
4. IF the translation request fails THEN the System SHALL display an appropriate error message to the user

### Requirement 2

**User Story:** As a user, I want professional terms to be translated according to predefined rules, so that domain-specific vocabulary is translated accurately.

#### Acceptance Criteria

1. WHEN the input text contains predefined professional terms THEN the System SHALL translate those terms according to the terminology mapping
2. WHEN the input text contains terms not in the terminology list THEN the System SHALL translate those terms using standard AI translation
3. WHEN translating from English to Chinese THEN the System SHALL apply English-to-Chinese terminology mappings
4. WHEN translating from Chinese to English THEN the System SHALL apply Chinese-to-English terminology mappings

### Requirement 3

**User Story:** As a user, I want to switch between Chinese-English and English-Chinese translation, so that I can translate in both directions.

#### Acceptance Criteria

1. WHEN a user clicks the language switch button THEN the System SHALL swap the source and target languages
2. WHEN the language direction changes THEN the System SHALL clear the input field and translation result
3. WHEN the language direction changes THEN the System SHALL update the language labels to reflect the new direction

### Requirement 4

**User Story:** As a user, I want my translation history to be saved, so that I can review previous translations.

#### Acceptance Criteria

1. WHEN a translation completes successfully THEN the System SHALL save the source text, translated text, and language direction to the history database
2. WHEN the user views the translation history THEN the System SHALL display recent translations in chronological order
3. WHEN a user clicks on a history item THEN the System SHALL populate the input field and result area with that translation

### Requirement 5

**User Story:** As a user, I want to use voice input for translation, so that I can translate spoken content conveniently.

#### Acceptance Criteria

1. WHEN a user clicks the voice input button THEN the System SHALL start speech recognition in the current source language
2. WHEN speech recognition completes THEN the System SHALL populate the input field with the recognized text
3. WHEN speech recognition completes with valid text THEN the System SHALL automatically trigger translation

### Requirement 6

**User Story:** As a developer, I want the translation prompt to include terminology constraints, so that the AI follows the predefined term mappings.

#### Acceptance Criteria

1. WHEN constructing the translation prompt THEN the System SHALL include the terminology mapping as part of the system instructions
2. WHEN the terminology list is provided THEN the System SHALL instruct the AI to prioritize these mappings over general translation
3. WHEN the AI returns a response THEN the System SHALL extract only the translated text without additional explanations
