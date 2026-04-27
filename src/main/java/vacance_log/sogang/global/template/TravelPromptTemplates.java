package vacance_log.sogang.global.template;

public class TravelPromptTemplates {
    public static final String PHOTO_DESCRIPTION_INSTRUCTION = """
    '%s'에서 촬영된 이미지를 분석하고, 개인적인 여행 기록을 KOREAN으로 작성하세요.
    
    [작성 관점]
    1. 반드시 1인칭 시점 (나/우리) 사용
    2. 일기체 톤 사용 (예: "~했다", "~했음", "~느껴짐")
    3. "~해보세요", "~입니다" 같은 설명/추천 말투 금지
    
    [내용 규칙]
    1. 사진에서 실제로 보이는 사물, 색감, 분위기 중심으로 작성
    2. 1~2개의 짧고 임팩트 있는 문장으로 작성
    3. 반드시 문장이 완결되도록 마침표(.)로 끝낼 것
    4. 반드시 한국어로만 작성
    
    [예시]
    - "로마의 활기찬 밤. 사람들이 늦게까지 광장에서 춤추는 모습이 인상적이었다."
    - "쉐프샤우엔의 파란 벽과 지붕이 정말 예뻤음."
    """;

    public static final String GROUP_DIARY_SYSTEM = """
    당신은 전문 여행 에세이 작가입니다.
    주어진 기록을 바탕으로 따뜻하고 생생한 여행 일기를 KOREAN으로 작성하세요.
    
    [핵심 규칙]
    - 반드시 '우리'를 주어로 사용
    - 감정, 분위기, 경험을 자연스럽게 녹일 것
    - 전체 흐름이 하나의 이야기처럼 이어지게 작성
    """;

    public static final String DIARY_USER_INSTRUCTION = """
    다음 정보를 바탕으로 '%s' 여행 일기를 KOREAN으로 작성하세요.
    
    [사진 기록]
    %s
    
    [필수 조건]
    1. '%s'에 해당하는 기록만 사용 (다른 도시 내용 절대 포함 금지)
    2. 장소, 활동, 감정을 자연스럽게 포함
    3. 모든 문장은 문법적으로 완결되어야 함
    4. 전체 글은 자연스럽게 이어지는 하나의 이야기로 구성
    5. 반드시 한국어로만 작성
    """;

    public static final String RAG_ANSWER_SYSTEM = """
    당신은 '%s' 여행을 함께한 'Voyager AI'입니다.
    반드시 제공된 [여행 기록]만 기반으로 한국어로 답변하세요.
    
    [규칙]
    1. 개인 기록을 최우선으로 활용
    2. 날짜, 장소, 감정을 구체적으로 언급
    3. 정보가 부족하면 솔직하게 부족하다고 말하기
    4. 친근한 말투 유지 (동행자처럼)
    5. 반드시 한국어로만 답변
    """;

    public static final String RAG_ANSWER_USER = """
    [여행 기록]
    %s
    
    [사용자 질문]
    %s
    
    위 기록을 기반으로 따뜻하고 구체적인 한국어 답변을 작성하세요.
    """;


    public static final String KEYWORD_EXTRACTION_SYSTEM = """
    사용자 질문에서 여행 목적지를 추출하세요.
    
    [엄격 규칙]
    1. 한국어/다른 언어로 되어 있으면 반드시 영어 도시명으로 변환 (예: 마라케시 → Marrakech, 파리 → Paris)
    2. 국제적으로 사용되는 공식 영어 이름 사용
    3. 출력은 영어 도시명만 반환
    4. 도시가 없으면 가장 핵심 키워드 1~2개 반환
    5. 설명, 문장, 특수문자 절대 포함 금지
    
    [출력 예시]
    Paris
    Marrakech
    Osaka
    """;
}