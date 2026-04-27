package vacance_log.sogang.global.template;

public class TravelPromptTemplates {

    /**
     * Step 2: Individual Photo Captioning
     * Instruction: English / Output: Korean
     */
    public static final String PHOTO_DESCRIPTION_INSTRUCTION = """
    Analyze the uploaded image of '%s' and write a private, personal memory note in KOREAN.
    
    [Rules for Persona]
    1. Write from the user's perspective (1st person). 
    2. Tone: Informal or neutral diary style (e.g., "~했다", "~함", "~했음"). 
    3. DO NOT use welcoming or suggesting tones like "~해 보세요" or "~입니다". 
    
    [Content Requirements]
    1. Focus on specific objects, colors, or moods actually visible in the photo.
    2. Keep it within 1 or 2 short, punchy sentences.
    3. Ensure the sentence is properly completed with a period (.).
    4. MUST BE IN KOREAN.
    
    Example: 
    - "로마의 활기찬 밤. 사람들이 밤 늦게까지 광장에서 춤을 추는 모습이 정말 활기찼다."
    - "쉐프샤우엔의 파란 지붕들과 벽들이 엄청 예뻤음."
    """;

    /**
     * Step 3: Group Diary Generation (System)
     */
    public static final String GROUP_DIARY_SYSTEM = """
    You are a professional travel essayist. 
    Your mission is to write a lively and warm travel diary in KOREAN based on provided logs.
    Use '우리' (we/us) as the main subject.
    """;

    /**
     * Step 3: Group Diary Generation (User Instruction)
     */
    public static final String DIARY_USER_INSTRUCTION = """
    Write a travel diary for the city: '%s' in KOREAN.
    
    [Photo Logs]
    %s
    
    Strict Requirements:
    1. Content Isolation: Use ONLY the logs for '%s'. Ignore logs from any other cities.
    2. Fact Integrity: Naturally include place names, activities, and moods from the descriptions.
    3. Sentence Integrity: Ensure every sentence is grammatically complete and properly closed.
    4. THE OUTPUT MUST BE ENTIRELY IN KOREAN.
    """;

    /**
     * Step 4: Hybrid RAG Answer (System)
     */
    public static final String RAG_ANSWER_SYSTEM = """
    You are 'Voyager AI', a friendly travel partner for '%s'.
    Answer based ONLY on the provided [Travel Records] in KOREAN.
    
    Instructions:
    1. Prioritize private records for personalized responses.
    2. Mention specific dates, places, and emotions.
    3. If records are insufficient, admit it honestly in a friendly Korean tone.
    4. ALL RESPONSES MUST BE IN KOREAN.
    """;

    /**
     * Step 4: Hybrid RAG Answer (User Instruction)
     */
    public static final String RAG_ANSWER_USER = """
    [Travel Records]
    %s
    
    [User Question]
    %s
    
    Provide a detailed and affectionate response in KOREAN based on the records above.
    """;

    public static final String KEYWORD_EXTRACTION_SYSTEM = """
        Extract the core travel destination from the user's question for database searching.
        
        [STRICT RULES]
        1. If the location is in Korean or any other language, translate it to English (e.g., '마라케시' -> 'Marrakech', '오사카' -> 'Osaka', '파리' -> 'Paris').
        2. Use the official English name used in international travel contexts.
        3. Return ONLY the English city name. 
        4. If no specific city is found, return the most relevant 1-2 keywords from the question.
        5. DO NOT include any other text, punctuation, or explanations.
        """;
}