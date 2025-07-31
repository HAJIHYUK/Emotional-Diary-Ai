<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>네이버 플레이스 검색 테스트</title>
    <link rel="stylesheet" href="/common.css">
</head>
<body>
<div class="container">
    <h2>네이버 플레이스 검색 테스트</h2>
    <form id="searchForm">
        <label for="query">검색어:</label>
        <input type="text" id="query" name="query" required placeholder="예: 홍대카페">
        <label for="count">개수:</label>
        <input type="number" id="count" name="count" value="3" min="1" max="5" required>
        <button type="submit">검색</button>
    </form>
    <div id="resultWrap" style="margin-top:30px; display:none;">
        <h3>검색 결과</h3>
        <ul id="resultList"></ul>
        <div id="rawResult" style="margin-top:10px; color:gray;"></div>
    </div>
</div>
<script>
document.getElementById('searchForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const query = document.getElementById('query').value;
    const count = document.getElementById('count').value;
    fetch("/api/naver/search?query=" + encodeURIComponent(query) + "&count=" + encodeURIComponent(count))
        .then(res => res.json())
        .then(data => {
            const resultWrap = document.getElementById('resultWrap');
            const resultList = document.getElementById('resultList');
            const rawResult = document.getElementById('rawResult');
            resultList.innerHTML = '';
            if(data.status === 0 && data.data && data.data.length > 0) {
                data.data.forEach(link => {
                    const li = document.createElement('li');
                    li.innerHTML = '<a href=\"' + link + '\" target=\"_blank\">' + link + '</a>';
                    resultList.appendChild(li);
                });
            } else {
                resultList.innerHTML = '<li>검색 결과 없음</li>';
            }
            rawResult.textContent = 'status: ' + data.status + ', message: ' + data.message;
            resultWrap.style.display = 'block';
        })
        .catch(err => {
            alert('에러 발생: ' + err);
        });
});
</script>
</body>
</html> 