<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>감정 통계 테스트</title>
    <style>
        body { font-family: 'Noto Sans KR', sans-serif; margin: 30px; }
        table { border-collapse: collapse; width: 60%; margin-top: 20px; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: center; }
        th { background: #f5f5f5; }
        .form-box { margin-bottom: 20px; }
    </style>
</head>
<body>
<h2>감정 통계 테스트 (월별/주별)</h2>
<div class="form-box">
    <form method="get" action="/api/emotion/statistic">
        <label>유저 ID: <input type="number" name="userId" value="1" required></label>
        <label>시작일: <input type="date" name="startDate" required></label>
        <label>종료일: <input type="date" name="endDate" required></label>
        <label>통계 타입:
            <select name="periodType">
                <option value="MONTH">월별</option>
                <option value="WEEK">주별</option>
            </select>
        </label>
        <button type="submit">통계 조회</button>
    </form>
</div>

<c:if test="${not empty successResponse}">
    <h3>통계 결과</h3>
    <p><b>기간 타입:</b> ${successResponse.data.periodType}</p>
    <p><b>기간 라벨:</b> ${successResponse.data.periodLabel}</p>
    <p><b>최다 감정:</b> ${successResponse.data.topEmotion}</p>
    <table>
        <thead>
        <tr>
            <th>감정명</th>
            <th>개수</th>
            <th>비율</th>
            <th>평균 강도</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="stat" items="${successResponse.data.stats}">
            <tr>
                <td>${stat.emotionLabel}</td>
                <td>${stat.count}</td>
                <td><fmt:formatNumber value="${stat.ratio * 100}" pattern="#.##"/>%</td>
                <td><fmt:formatNumber value="${stat.avgLevel}" pattern="#.##"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:if>

</body>
</html> 