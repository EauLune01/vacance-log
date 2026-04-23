# vacance-log

AI 융합 캡스톤 디자인: 개인 맞춤형 여행 다이어리 & 가이드 서비스
사용자의 개인 여행 기록(Diary)과 도시별 전문 지식(Travel Knowledge)을 결합하여, 단순한 답변을 넘어 초개인화된 여행 경험을 제공하는 Hybrid RAG 기반 백엔드 서비스입니다.

🛠 Tech Stack (Backend)
1. Spring Boot Core
Java 21 / Spring Boot 3.5.x: 최신 자바 가상 머신의 성능 이점과 스프링 부트의 최신 생태계 활용.

Spring Data JPA: 객체 지향적 데이터 관리 및 도메인 모델 중심의 비즈니스 로직 개발.

QueryDSL (5.0): 컴파일 타임 타입 체크를 통한 동적 쿼리 작성 및 Fetch Join 최적화로 데이터 조회 시 N+1 문제 원천 해결.

Spring Data Redis:

Geospatial Indexing: GEOADD, GEORADIUS 명령어를 활용하여 실시간 위치 기반 Geofencing(반경 200m 진입 감지) 기술 구현.

Distributed Caching: 분산 환경에서의 상태 관리 및 데이터 조회 성능 최적화.

Spring AMQP (RabbitMQ): Vision 분석, 대규모 데이터 처리, 알림 전송 로직의 비동기화를 통해 시스템 응답성 및 결합도 개선.

Spring Validation: 계층 간 데이터 전송(DTO) 시 데이터 정합성 사전 검증.

SpringDoc OpenAPI (Swagger): API 문서 자동화 및 프론트엔드 협업을 위한 테스트 환경 구축.

2. Spring AI & LLM Ecosystem
Spring AI OpenAI Starter: GPT-4o(Vision 포함) 및 Embedding 모델과의 인터페이스 표준화 및 LLM 오케스트레이션.

Vector Store (PgVector): PostgreSQL 기반 벡터 DB를 활용하여 개인 다이어리 및 여행 지식 데이터의 시맨틱 검색(Semantic Search) 구현.

Spring AI RAG & Document Readers (PDF, Tika, Jsoup): 다양한 비정형 소스(공공 데이터, 웹 페이지 등)로부터 여행 지식 데이터를 파싱하고 ETL(Extract, Transform, Load) 파이프라인 구축.

Advisors: Vector Store와 연계하여 대화 맥락(Context)을 유지하고 지능적으로 최적의 정보를 주입.

3. Infrastructure & Others
PostgreSQL (pgvector): 단일 데이터베이스 내에서 관계형 데이터와 벡터 데이터를 통합 관리하여 아키텍처 단순화.

AWS S3 (Spring Cloud AWS): 사용자 사진의 안정적인 저장, 관리 및 효율적인 이미지 URL 서빙.

Lombok: 반복적인 코드(Boilerplate) 제거를 통한 도메인 및 DTO 객체의 가독성 향상.

🤖 LLM (GPT-4o) 호출 아키텍처
본 프로젝트에서는 서비스 흐름에 따라 총 세 가지 시점에서 LLM을 전략적으로 호출합니다.

1. 비동기 사진 분석 및 메모 생성 (Vision Analysis)
Trigger: 유저가 여행 사진을 업로드할 때 RabbitMQ를 통해 비동기 이벤트 발생.

Logic: OpenAiService가 이미지 S3 URL과 해당 장소의 Travel Knowledge를 결합하여 캡션 생성.

Goal: 사진 속 시각적 정보와 전문 지식을 버무려 '단 20바이트' 내외의 감성적인 시적 메모(Short Memo) 자동 생성.

2. 위치 기반 지능형 추천 알림 (Proactive Recommendation)
Trigger: 그룹의 리더(Leader)가 특정 장소(POI) 반경 200m 이내로 진입했을 때 발생.

Logic:

리더의 현재 좌표를 기반으로 인근 placeCode를 식별.

해당 장소에 대한 **전문 지식(Travel Knowledge)**과 현재 여행 그룹의 페르소나(Persona), 그리고 이전 여행 컨텍스트를 결합.

실시간 상황에 최적화된 추천 메시지를 생성하여 푸시 알림 전송.

Goal: 사용자가 장소를 찾아보기 전에 AI가 먼저 "이 근처에 당신이 좋아할 만한 ~가 있고, 지금 가면 ~하기 좋습니다"라는 초개인화된 가이드를 능동적으로 제공.

3. 지능형 굿즈 검색 및 챗봇 (Hybrid RAG Search)
Trigger: 사용자가 자신의 여행 기록에 대해 질문하거나 검색할 때.

Logic:

extractSearchKeywords를 통해 유저 질문에서 핵심 키워드 추출.

벡터 스토어에서 개인 다이어리와 공식 여행 지식을 이중 검색(Double Search).

추출된 두 종류의 컨텍스트를 하이브리드 형태로 재구성하여 답변 생성.

Goal: 사용자가 기억하지 못하는 사소한 일기 내용과 도시의 유용한 정보를 결합한 풍부한 답변 제공.
