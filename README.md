# 레이야 돈 모으자

용돈 기입장 안드로이드 앱. 인터넷·로그인 없이 폰 안에만 저장합니다.

## 열기

Android Studio → **Open** → 이 폴더 선택 → Gradle 동기화가 끝나면 ▶ Run.

- Kotlin · Jetpack Compose (Material 3)
- Room (SQLite) — 내역, 카테고리
- DataStore — 홈 기간, 예산, 요약 접힘 상태
- 첨부 파일은 앱 내부 저장소 `files/attachments/`에 복사해서 보관
- minSdk 26 (Android 8.0), targetSdk 35

## 화면

| 화면 | 내용 |
|---|---|
| 홈 | [추가하기], 기간(시작일~종료일, ◀ ▶로 이전/다음 기간), 예산, 총지출·추가수입(자동 계산), 남은 예산. 기간 아래는 접기 가능(상태 기억). 목록은 기간 안 내역만, 생성일 최신순, 2줄 행 + 삭제(확인 팝업) |
| 추가하기 / 수정하기 | 날짜(연 시각 자동, 변경 가능), 지출/수입, 결제수단(지출일 때만 현금/카드), 금액(숫자만, 쉼표 자동), 카테고리, 요약 15자, 상세 내역 200자, 사진/파일 1개. 필수: 구분·금액·카테고리 |
| 건별 상세 | 입력 내용 전체 표시, 사진 크게 보기 / 파일 열기, 수정·삭제 |
| 기록 | 기간과 상관없이 전체 내역을 월별로 묶어 날짜 최신순. 월마다 지출/수입 합계, 전체·지출·수입 필터, 요약·상세 내역 검색. 누르면 건별 상세 |
| 통계 | 기간(기본값 = 홈 기간, 바꿔도 홈은 그대로), 총지출·총수입, 결제수단별·카테고리별 지출 합계(0원 숨김, 큰 금액 순) |
| 설정 | 카테고리 추가·이름 변경·삭제(중복 이름 불가, 최대 10자). 삭제하면 기존 내역은 ‘미분류’로 남음. 처음 설치 시 식비·간식·교통·문구·용돈·저축 |

## 코드 구조

```
app/src/main/java/com/raye/donmoeuja/
├─ DonApp.kt, MainActivity.kt
├─ data/        Entities, Daos, AppDatabase, MoneyRepository, SettingsRepository, AttachmentStore
└─ ui/          AppNav(하단 탭·화면 이동), Components, Format, theme/
   ├─ home/     HomeScreen, HomeViewModel
   ├─ history/  HistoryScreen, HistoryViewModel
   ├─ edit/     EditScreen, EditViewModel  (추가·수정 공용)
   ├─ detail/   DetailScreen, DetailViewModel
   ├─ stats/    StatsScreen, StatsViewModel
   └─ settings/ SettingsScreen, SettingsViewModel
```
