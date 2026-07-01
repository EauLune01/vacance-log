# ✈️ vacance-log
> **AI 융합 캡스톤 디자인 (개인 맞춤형 여행 다이어리 & 하이브리드 가이드 서비스)**
> 
> 사용자의 개인 여행 기록(**Diary**)과 도시별 전문 지식(**Travel Knowledge**)을 결합하여, 단순한 질의응답을 넘어 초개인화된 여행 가이드 경험을 제공하는 **Hybrid RAG 기반 백엔드 시스템**입니다.

---

## 🏛️ System Architecture Overview
* **단일 DB 아키텍처**: PostgreSQL(with `pgvector`)을 채택하여 관계형 데이터(RDB)와 고차원 벡터 데이터(Vector Store)를 단일 엔진에서 통합 관리, 인프라 복잡성을 획기적으로 줄였습니다.
* **비동기 이벤트 기반 구조**: RabbitMQ를 도입하여 Vision 분석 및 대규모 데이터 처리 등 무거운 작업을 비동기화함으로써 메인 API 스레드의 블로킹을 원천 차단하고 시스템 결합도를 낮췄습니다.

---

## 🛠️ Tech Stack & Architecture Justification

### Framework & Core
* **Java 21 / Spring Boot 3.5.x**: 가상 스레드(Virtual Threads) 도입을 고려한 최신 런타임 환경 구축 및 가상 머신의 성능 최적화 혜택 극대화.
* **Spring Data JPA & QueryDSL 5.0**: 컴파일 타임 타입 체크를 통한 동적 쿼리 구현 및 `Fetch Join` 최적화를 통해 객체 지향적 데이터 관리 및 조회 시 **N+1 문제 원천 해결**.
* **Spring Validation**: Layer 간 데이터 전송(DTO) 시 유효성 검증을 자동화하여 시스템 정합성 사전 확보.
* **SpringDoc OpenAPI**: RestController 기반 API 문서 자동화 및 프론트엔드와 안정적인 협업 계약(Contract) 구축.

### AI & LLM Ecosystem
* **Spring AI (OpenAI Starter)**: GPT-4o(Vision 포함) 및 Embedding 모델과의 인터페이스를 표준화하고, LLM 오케스트레이션 효율화.
* **Vector Store (PgVector)**: 사용자의 정성적인 일기 내용과 정형화된 도시 지식을 벡터화하여 다차원 시맨틱 검색(Semantic Search) 환경 구현.
* **Spring AI RAG & Document Readers**: `PDF`, `Tika`, `Jsoup` 라이브러리를 활용해 공공 데이터 및 웹 페이지 등의 비정형 소스를 파싱하는 **자체 AI ETL(Extract, Transform, Load) 파이프라인** 구축.
* **Spring AI Advisors**: Vector Store 검색 결과와 대화 맥락(Context)을 결합하여 가용한 토큰 범위 내 최적의 프롬프트를 동적으로 주입.

### Storage & Event Messaging
* **Spring Data Redis**:
    * *Geospatial Indexing*: `GEOADD`, `GEORADIUS` 명령어를 활용해 대규모 유저 좌표 데이터에 대한 **실시간 위치 기반 Geofencing(반경 200m 진입 감지)** 초고속 구현.
    * *Distributed Caching*: 반복적인 API 통신 비용 및 여행 지식 데이터의 중복 조회 성능 최적화.
* **Spring AMQP (RabbitMQ)**: Vision API 호출, 대규모 데이터 처리, 푸시 알림 전송 로직을 메시지 큐 기반으로 비동기 처리하여 메인 서버의 부하 분산.
* **AWS S3 (Spring Cloud AWS)**: 유저 업로드 사진의 안정적인 영속화 및 효율적인 미디어 URL 서빙 파이프라인 구축.

---

## 🤖 LLM (GPT-4o) 전략적 호출 아키텍처

### 1. 비동기 사진 분석 및 감성 메모 생성 (Vision Analysis)
* **Trigger**: 유저가 여행 사진을 업로드할 때 RabbitMQ를 통해 비동기 이벤트 발행.
* **Logic**: `OpenAiService`가 S3 이미지 URL과 해당 장소의 `Travel Knowledge` 소스를 Multi-modal 프롬프트로 결합 후 전송.
* **Goal**: 사진 속 시각적 단서와 도시 전문 지식을 융합하여, **'단 20바이트 내외'의 초압축 감성 시적 메모(Short Memo)**를 자동 생성 및 다이어리에 바인딩.

### 2. 위치 기반 능동형 지능 가이드 (Proactive Recommendation)
* **Trigger**: 여행 그룹의 리더(Leader)가 특정 주요 장소(POI) 반경 200m 이내로 진입 (`GEORADIUS` 감지).
* **Logic**: 
    1. 리더의 실시간 GPS 좌표 기반 인근 `placeCode` 식별.
    2. 해당 장소의 `Travel Knowledge` + 현재 그룹 페르소나(Persona) + 직전 대화 컨텍스트 결합.
    3. 실시간 상황(시간, 장소의 유래 등)에 최적화된 추천 스크립트 작성 후 FCM 푸시 알림 전송.
* **Goal**: 유저가 검색하기 전에 AI가 먼저 가이드를 건네는 **"초개인화 푸시 가이딩"** 서비스.

### 3. 지능형 기록 검색 및 하이브리드 챗봇 (Hybrid RAG Search)
* **Trigger**: 사용자가 챗봇에게 자신의 과거 여행 기록이나 도시에 대해 질문할 때.
* **Logic**:
    1. 유저의 자연어 질의에서 핵심 검색 키워드 추출 (`extractSearchKeywords`).
    2. Vector Store 내 **개인 다이어리(정성 데이터)**와 **공식 여행 지식(정량 데이터)** 영역을 이중 검색(**Double Search**).
    3. 추출된 다차원 컨텍스트를 하이브리드 형태로 재구성하여 LLM 프롬프트 주입 후 답변 생성.
* **Goal**: "내가 작년에 파리에서 먹었던 크루아상 맛집이랑 그 성당 얽힌 전설이 뭐였지?" 같은 복합 질문에 완벽히 대응.

---

## 🚀 Key Optimization Points (성능 및 동시성 최적화)
* **하이브리드 RAG 데이터 분리 및 격리**: 유저의 민감한 다이어리 정보와 공공 여행 지식 데이터를 벡터 스토어 내 메타데이터 및 네임스페이스 기반으로 안전하게 격리 및 필터링하여 테넌트(Tenant) 간 보안성 확보.
* **비정형 데이터 정제 및 ETL 최적화**: 웹 스크래핑(`Jsoup`) 및 가이드 문서(`PDF`, `Tika`) 로드 시 발생하는 텍스트 노이즈를 청크 쪼개기(Chunking) 단계에서 정규식 기반으로 사전 정제하여 임베딩 벡터의 품질 및 시맨틱 검색 정확도 향상.
