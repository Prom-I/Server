# ToMeetToMe: 팀 일정 조율 및 최적 시간 추천 서비스

ToMeetToMe는 팀원 간의 미팅 일정을 효과적으로 조율하고, 모든 참석자의 일정을 고려하여 최적의 약속 시간을 추천해주는 백엔드 서비스입니다. 외부 캘린더 연동을 통해 사용자의 기존 일정을 자동으로 반영하여 편리성을 높였습니다.

## ✨ 주요 기능

*   **팀 생성 및 관리:** 사용자는 팀을 생성하고 팀원을 초대/관리할 수 있습니다.
*   **약속 생성 및 조건 설정:** 미팅의 목적, 예상 소요 시간, 원하는 기간/시간대, 선호 요일 등 상세 조건을 설정하여 약속 생성을 요청할 수 있습니다.
*   **최적 시간 추천:** 설정된 조건과 모든 팀원의 외부 캘린더 일정을 종합적으로 분석하여, 참석률이 가장 높은 최적의 미팅 후보 시간을 탐색하고 추천합니다. (자체 개발한 휴리스틱 기반 평가 로직 적용)
*   **외부 캘린더 연동:** CalDAV 프로토콜과 ical4j 라이브러리를 활용하여 Google Calendar, iCloud Calendar 등 주요 외부 캘린더 서비스와 실시간 양방향 연동을 지원합니다.
*   **참석 여부 투표 (Optional):** 추천된 후보 시간에 대한 팀원들의 참석 가능 여부를 투표하는 기능을 제공할 수 있습니다. (구현 여부에 따라 추가/삭제)
*   **알림 기능 (Optional):** 약속 확정, 변경 등에 대한 알림 기능을 제공할 수 있습니다. (구현 여부에 따라 추가/삭제)

## 🛠️ 기술 스택

*   **Language:** Java 17
*   **Framework:** Spring Boot (버전 명시)
*   **Data:** Spring Data JPA, MySQL
*   **Calendar Integration:** ical4j
*   **Build Tool:** Gradle

## 🚀 설치 및 실행 방법

1.  **저장소 복제:**
    ```bash
    git clone https://github.com/your-username/ToMeetToMe.git
    cd ToMeetToMe
    ```

2.  **환경 설정:**
    *   `src/main/resources/application.properties` (또는 `application.yml`) 파일에서 데이터베이스 연결 정보 (URL, username, password) 등 필요한 설정을 수정합니다.
    *   (외부 캘린더 연동을 위한 추가 설정이 필요하다면 명시)

3.  **빌드:**
    *   **Maven:**
        ```bash
        ./mvnw clean package
        ```
    *   **Gradle:**
        ```bash
        ./gradlew clean build
        ```

4.  **실행:**
    ```bash
    java -jar target/tomeettome-0.0.1-SNAPSHOT.jar 
    # 또는 빌드 도구 플러그인 사용
    # ./mvnw spring-boot:run
    # ./gradlew bootRun 
    ```
