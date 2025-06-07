<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>일기 작성</title>
    <link rel="stylesheet" type="text/css" href="/css/common.css">
</head>
<body>
    <h2>일기 작성</h2>
    <form action="/api/diary/write" method="post">
        <!-- 유저 아이디는 테스트용으로 1로 고정 -->
        <input type="hidden" name="userId" value="1" />

        <label for="content">일기 내용 (최대 1000자):</label><br>
        <textarea id="content" name="content" rows="8" cols="50" maxlength="1000" required></textarea><br><br>

        <label for="weather">날씨:</label>
        <input type="text" id="weather" name="weather" maxlength="20" /><br><br>

        <label for="entryDate">작성 날짜:</label>
        <input type="date" id="entryDate" name="entryDate" required /><br><br>

        <button type="submit">저장</button>
    </form>

    <hr>
    <h3>일기 목록 조회 (userId 입력)</h3>
    <form action="/api/diary/list" method="get">
        <label for="listUserId">userId:</label>
        <input type="number" id="listUserId" name="userId" value="1" required />
        <button type="submit">목록 조회</button>
    </form>

    <h3>일기 상세 조회 (diaryId 입력)</h3>
    <form action="/api/diary/detail" method="get">
        <label for="detailDiaryId">diaryId:</label>
        <input type="number" id="detailDiaryId" name="diaryId" required />
        <button type="submit">상세 조회</button>
    </form>

    <h3>일기 수정 (diaryId와 내용 입력)</h3>
    <form action="/api/diary/update" method="post" onsubmit="this._method.value='put';">
        <input type="hidden" name="_method" value="put" />
        <label for="updateDiaryId">diaryId:</label>
        <input type="number" id="updateDiaryId" name="diaryId" required /><br>
        <label for="updateContent">내용:</label><br>
        <textarea id="updateContent" name="content" rows="4" cols="50" maxlength="1000" required></textarea><br>
        <label for="updateWeather">날씨:</label>
        <input type="text" id="updateWeather" name="weather" maxlength="20" /><br>
        <label for="updateEntryDate">작성 날짜:</label>
        <input type="date" id="updateEntryDate" name="entryDate" required /><br>
        <button type="submit">수정</button>
    </form>

    <h3>일기 삭제 (diaryId 입력)</h3>
    <form action="/api/diary/delete" method="post" onsubmit="this._method.value='delete';">
        <input type="hidden" name="_method" value="delete" />
        <label for="deleteDiaryId">diaryId:</label>
        <input type="number" id="deleteDiaryId" name="diaryId" required />
        <button type="submit">삭제</button>
    </form>
</body>
</html> 