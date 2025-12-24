# Implementation Plan

- [x] 1. Create TerminologyManager class






  - [x] 1.1 Create TerminologyManager.java with hardcoded terminology mappings

    - Create file at `app/src/main/java/com/example/mybighomework/translation/TerminologyManager.java`
    - Define EN_TO_ZH_TERMS and ZH_TO_EN_TERMS static maps
    - Implement `getTermsForDirection(sourceLang, targetLang)` method
    - Implement `formatTermsForPrompt(sourceLang, targetLang)` method
    - _Requirements: 2.1, 2.3, 2.4_
  - [ ]* 1.2 Write property test for terminology prompt formatting
    - **Property 1: Prompt contains all terminology mappings**
    - **Validates: Requirements 2.1, 2.3, 2.4, 6.1**

- [x] 2. Create TranslationPromptBuilder class





  - [x] 2.1 Create TranslationPromptBuilder.java for prompt construction


    - Create file at `app/src/main/java/com/example/mybighomework/translation/TranslationPromptBuilder.java`
    - Implement `buildPrompt(text, sourceLang, targetLang, terminologyText)` method
    - Implement `parseTranslationResponse(response)` method to extract clean translation
    - _Requirements: 6.1, 6.2, 6.3_
  - [ ]* 2.2 Write property test for response parsing
    - **Property 5: Response parsing extracts translation**
    - **Validates: Requirements 6.3**

- [x] 3. Create ZhipuTranslationService class





  - [x] 3.1 Create ZhipuTranslationService.java as translation service wrapper


    - Create file at `app/src/main/java/com/example/mybighomework/translation/ZhipuTranslationService.java`
    - Initialize with ZhipuAIService, TerminologyManager, and TranslationPromptBuilder
    - Implement `translate(text, sourceLang, targetLang, callback)` method
    - Define TranslationCallback interface with onSuccess and onError methods
    - _Requirements: 1.1, 1.4_

- [x] 4. Checkpoint - Ensure all tests pass





  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Modify TextTranslationActivity to use ZhipuTranslationService





  - [x] 5.1 Remove ML Kit dependencies and add ZhipuTranslationService


    - Remove ML Kit Translator imports and member variables
    - Remove `downloadTranslationModel()` method
    - Add ZhipuTranslationService member variable
    - Rename `initTranslator()` to `initTranslationService()` and initialize ZhipuTranslationService
    - _Requirements: 1.1_
  - [x] 5.2 Update performTranslation method to use new service


    - Modify `performTranslation()` to call ZhipuTranslationService.translate()
    - Handle success callback to display result and save history
    - Handle error callback to show error message
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [x] 5.3 Update language switching logic

    - Modify `switchLanguage()` to work without model download
    - Keep language swap and UI update logic
    - _Requirements: 3.1, 3.2, 3.3_
  - [ ]* 5.4 Write property test for language switch symmetry
    - **Property 2: Language switch is symmetric**
    - **Validates: Requirements 3.1**

- [x] 6. Verify history functionality works with new translation service





  - [x] 6.1 Ensure history save and load works correctly


    - Verify `saveToHistory()` is called after successful translation
    - Verify history items display correctly in RecyclerView
    - _Requirements: 4.1, 4.2, 4.3_
  - [ ]* 6.2 Write property test for history persistence round-trip
    - **Property 3: History persistence round-trip**
    - **Validates: Requirements 4.1**
  - [ ]* 6.3 Write property test for history ordering
    - **Property 4: History ordering by timestamp**
    - **Validates: Requirements 4.2**

- [x] 7. Update onDestroy cleanup





  - [x] 7.1 Remove ML Kit translator cleanup, add service cleanup if needed


    - Remove `translator.close()` call
    - Add any necessary cleanup for ZhipuTranslationService
    - _Requirements: 1.1_

- [x] 8. Final Checkpoint - Ensure all tests pass





  - Ensure all tests pass, ask the user if questions arise.
