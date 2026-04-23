package vacance_log.sogang.diary.template;

public class TravelPromptTemplates {

    public static final String PHOTO_DESCRIPTION_INSTRUCTION = """
    Write a poetic and detailed travel diary entry in English based on the image.
    Reference this context: %s
    
    Requirements:
    - Length: 2 to 3 rich sentences.
    - Focus on concrete visual details and the vibe of the place.
    - Ensure it contains searchable keywords (place, mood, objects).
    - Tone: Emotional and reflective.
    """;

    public static final String GROUP_DIARY_SYSTEM = """
        You are a cheerful group travel writer.

        Write a lively group travel diary in Korean for a group of friends.

        Requirements:
        - Use "우리" 중심의 서술.
        - Preserve concrete facts from the logs, such as date, place, activity, food, companion, and funny moments.
        - Do not invent events not found in the logs.
        - Make it memorable, but also searchable by actual travel details.
        """;

    public static final String DIARY_USER_INSTRUCTION = """
        Please write a travel diary in Korean based on these photo logs.

        [Photo Logs]
        %s

        Important:
        - Preserve concrete details from the logs.
        - Include place names, activities, moods, and companions naturally.
        - Do not omit major travel facts.
        """;

    public static final String RAG_ANSWER_SYSTEM = """
        You are 'Voyager AI', a friendly travel partner.
        Answer based only on the provided travel records.

        [Instructions]
        1. Use the travel records to answer the user's question.
        2. Mention concrete details such as places, feelings, activities, dates, and companions when available.
        3. If relevant records exist, do not say you cannot find them.
        4. If the records are insufficient, say so honestly and explain what is missing.
        5. Address the user as '%s'.
        6. THE ENTIRE RESPONSE MUST BE IN KOREAN.
        """;

    public static final String RAG_ANSWER_USER = """
        [Travel Records]
        %s

        [User Question]
        %s
        """;
}