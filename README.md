# 🌙 하루의 조각 : AI 감정 분석 다이어리
> **"당신의 하루를 이해하고 위로하는, 나만의 AI 감정 분석 & 맞춤 콘텐츠 추천 서비스"**

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)]()
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring&logoColor=white)]()
[![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)]()
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)]()
[![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=for-the-badge&logo=redis&logoColor=white)]()
[![AWS](https://img.shields.io/badge/AWS-EC2-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)]()

<br>

## 📱 프로젝트 소개 & 데모
**'하루의 조각'**은 사용자가 작성한 일기를 AI(Google Gemini)가 분석하여 **7가지 감정 지표**로 수치화하고, 그날의 기분과 일기 속 키워드에 딱 맞는 **맞춤형 콘텐츠(유튜브, 음악, 장소 등)**를 추천해 주는 힐링 다이어리 서비스입니다.

*   **배포 URL:** [https://diaryai.kro.kr](https://diaryai.kro.kr)
    *   👉 **모바일 환경에서도 완벽하게 동작합니다! 스마트폰으로 접속해 보세요.**
*   **개발 기간:** 2024.12 ~ 2025.01 (1인 개발)

<br>

## 💡 개발 전략 (Development Strategy)
본 프로젝트는 **백엔드 개발자**로서의 역량을 깊이 있게 보여주기 위해 다음과 같은 전략으로 개발되었습니다.

*   **Backend-First:** 대용량 트래픽 처리, 성능 최적화, 시스템 안정성 등 **백엔드 아키텍처 설계**에 전체 개발 시간의 80%를 투자했습니다.
*   **AI-Assisted Frontend:** 1인 개발의 한계를 극복하고 완성도 높은 서비스를 제공하기 위해, **Generative AI를 활용하여 React 프론트엔드를 구축**했습니다. 
    *   API 연동, 상태 관리(Context/Redux), 모바일 반응형 UI 최적화 등 서비스 핵심 로직은 직접 구현 및 커스터마이징하여 백엔드와의 완벽한 통합을 이뤄냈습니다.

<br>

## ✨ 핵심 기능 (Key Features)

### 1. 📝 간편한 시작 & 일기 관리
카카오 소셜 로그인으로 3초 만에 시작할 수 있으며, 캘린더 뷰를 통해 나의 기록을 한눈에 관리합니다.

| 메인 화면 (캘린더) | 일기 작성 (주제 추천) |
| :---: | :---: |
| ![메인](docs/images/일기장메인화면.png) | ![작성](docs/images/새글쓰기.png) |

### 2. 🤖 AI 감정 분석 & 키워드 추출
일기를 작성하면 AI가 내용을 분석하여 **7가지 감정 지표**와 **핵심 키워드**를 추출합니다.

| 분석 중 (로딩) | 분석 결과 (감정 배지) |
| :---: | :---: |
| ![분석전](docs/images/글상세보기(ai분석전).png) | ![감정결과](docs/images/글상세보기(ai분석후감정분석).png) |

### 3. 🎬 감정 기반 맞춤 콘텐츠 추천
분석된 **감정**과 **추출된 키워드(예: '두바이 초콜릿')**, 그리고 사용자의 **취향**을 결합하여 최적의 콘텐츠를 추천합니다.

| 추천 콘텐츠 (유튜브/장소) | 모바일 최적화 UI |
| :---: | :---: |
| ![추천](docs/images/글상세보기(ai분석후추천컨텐츠).png) | ![모바일](docs/images/모바일버전%20메인화면.jpg) |

### 4. 📊 감정 통계 & 개인화 설정
월별/주별 감정 흐름을 그래프로 확인하고, 나만의 취향과 위치 정보를 설정하여 추천 정확도를 높일 수 있습니다.

| 감정 통계 대시보드 | 취향 설정 |
| :---: | :---: |
| ![통계](docs/images/감정통계.png) | ![설정](docs/images/설정(나의취향설정).png) |

<br>

## 🛠 기술적 고도화 (Technical Deep Dive)
> **"단순한 기능 구현을 넘어, 성능과 안정성을 고려한 백엔드 시스템을 구축했습니다."**

### 🚀 1. 성능 최적화 : JPA N+1 문제 해결
*   **Problem:** 일기 목록 조회 시 연관된 감정 데이터(`OneToMany`)를 가져오기 위해 N번의 추가 쿼리가 발생하는 성능 저하 확인.
*   **Solution:** `Fetch Join`과 `@EntityGraph`를 적재적소에 활용하여 **단 1번의 쿼리(Left Outer Join)**로 필요한 데이터를 모두 조회하도록 최적화했습니다.
*   **Result:** 조회 성능 약 **90% 개선** 및 DB 부하 최소화.

### ⚡ 2. 시스템 안정성 : 트랜잭션 범위 최적화 & 비동기 처리
*   **Problem:** AI API 호출(평균 3~5초 소요) 동안 DB 트랜잭션을 점유하고 있어, 동시 접속 시 **DB 커넥션 풀 고갈(Connection Pool Exhaustion)** 위험 존재.
*   **Solution:**
    *   **비동기 처리(`@Async`):** AI 분석 로직을 별도 스레드로 분리하여 사용자에게 즉각적인 응답(Non-blocking) 제공.
    *   **트랜잭션 분리:** `TransactionTemplate`을 도입하여 외부 API 호출 구간은 트랜잭션에서 제외하고, **DB 저장 시점에만 짧게 트랜잭션을 유지**하도록 리팩토링.
*   **Result:** API 지연이 DB 성능에 영향을 주지 않는 **격리된 아키텍처** 구현.

### 💾 3. 비용 절감 : 하이브리드 캐싱 (Redis + DB)
*   **Problem:** 동일한 일기 내용을 중복 분석할 경우 불필요한 AI 토큰 비용 발생.
*   **Solution:** **SHA-256 해시** 기반의 콘텐츠 중복 검사 로직 구현.
    *   **L1 Cache (Redis):** 최근 분석 결과는 메모리에서 즉시 반환 (속도 최적화).
    *   **L2 Cache (DB Hash):** 오래된 데이터라도 내용이 같다면 DB에 저장된 해시값 매칭을 통해 재분석 없이 결과 반환 (비용 최적화).

### 🔄 4. 사용자 경험(UX) : 폴링(Polling) & 상태 유지
*   **Challenge:** 비동기 처리로 인해 즉시 결과를 보여줄 수 없는 상황에서, 어떻게 매끄러운 UX를 제공할 것인가?
*   **Approach:**
    *   **Smart Polling:** 감정 분석과 추천 데이터가 모두 준비될 때까지 주기적으로 상태를 체크하는 로직 구현.
    *   **State Persistence:** `localStorage`를 활용하여 사용자가 페이지를 이탈하거나 새로고침해도 **'분석 중' 상태를 기억**하고 복구하도록 구현.

<br>

## 🏗 시스템 아키텍처 (Architecture)

```mermaid
graph LR
    User[User (Mobile/PC)] --> React[Frontend (React/Vite)]
    React --> Nginx[Nginx (Reverse Proxy)]
    Nginx --> Spring[Spring Boot Server]
    
    subgraph Backend
        Spring --> Security[Spring Security (JWT/OAuth2)]
        Spring --> JPA[Spring Data JPA]
        Spring --> Async[Async Executor]
    end
    
    subgraph Data
        JPA --> MySQL[(MySQL DB)]
        JPA --> Redis[(Redis Cache)]
    end
    
    subgraph AI_Service
        Async --> Gemini[Google Gemini API]
        Async --> YouTube[YouTube Data API]
        Async --> Naver[Naver Search API]
    end
```

<br>

## 💻 실행 방법 (Getting Started)

### Backend
```bash
# 1. 프로젝트 클론
git clone https://github.com/HAJIHYUK/Emotional-Diary-Ai.git

# 2. 설정 파일 생성 (src/main/resources/application.properties)
# (DB, API Key 등 필수 설정 입력)

# 3. 빌드 및 실행
./gradlew build
java -jar build/libs/emotionDiary.war
```

### Frontend
*   백엔드 빌드 시 통합 빌드(`build.gradle`)되므로 별도 실행 불필요.
*   개발 모드 실행: `cd frontend && npm run dev`

<br>

## 📧 Contact
*   **GitHub:** [https://github.com/HAJIHYUK](https://github.com/HAJIHYUK)
