<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>유저 클릭 이벤트 테스트</title>
    <style>
        body { font-family: 'Noto Sans KR', sans-serif; margin: 30px; }
        table { border-collapse: collapse; width: 70%; margin-top: 20px; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: center; }
        th { background: #f5f5f5; }
        .msg { color: green; margin-top: 20px; }
    </style>
    <script>
        // 추천 정보 불러오기
        function loadRecommendations(diaryId) {
            fetch('/api/diary/recommendations?diaryId=' + diaryId)
                .then(res => res.json())
                .then(data => {
                    if (data.data && data.data.length > 0) {
                        renderTable(data.data);
                    } else {
                        document.getElementById('recommendTable').innerHTML = '<tr><td colspan="5">추천 정보가 없습니다.</td></tr>';
                    }
                })
                .catch(err => {
                    document.getElementById('recommendTable').innerHTML = '<tr><td colspan="5">오류 발생: ' + err + '</td></tr>';
                });
        }

        // 테이블 렌더링
        function renderTable(recommendations) {
            var html = '';
            for (var i = 0; i < recommendations.length; i++) {
                var rec = recommendations[i];
                var recId = rec.recommendationId;
                var recTitle = rec.title;
                var recGenre = rec.genre;
                html += '<tr>'
                    + '<td>' + recId + '</td>'
                    + '<td>' + rec.type + '</td>'
                    + '<td>' + recGenre + '</td>'
                    + '<td>' + recTitle + '</td>'
                    + '<td><a href="' + rec.link + '" target="_blank" class="recommend-link"'
                    + ' data-recid="' + recId + '"'
                    + ' data-type="' + rec.type + '"'
                    + ' data-genre="' + recGenre + '"'
                    + ' data-title="' + recTitle + '">' + rec.link + '</a></td>'
                    + '<td><button type="button" class="recommend-btn"'
                    + ' data-recid="' + recId + '"'
                    + ' data-type="' + rec.type + '"'
                    + ' data-genre="' + recGenre + '"'
                    + ' data-title="' + recTitle + '">클릭 이벤트 전송</button></td>'
                    + '</tr>';
            }
            document.getElementById('recommendTable').innerHTML = html;
            addEventListeners();
        }

        function addEventListeners() {
            // 링크 클릭
            document.querySelectorAll('.recommend-link').forEach(function(link) {
                link.addEventListener('click', function(e) {
                    e.preventDefault();
                    sendClickEvent(
                        this.dataset.recid,
                        this.dataset.type,
                        this.dataset.genre,
                        this.dataset.title
                    );
                    window.open(this.href, '_blank');
                });
            });
            // 버튼 클릭
            document.querySelectorAll('.recommend-btn').forEach(function(btn) {
                btn.addEventListener('click', function() {
                    sendClickEvent(
                        this.dataset.recid,
                        this.dataset.type,
                        this.dataset.genre,
                        this.dataset.title
                    );
                });
            });
        }

        // 클릭 이벤트 전송 함수는 기존과 동일
        function sendClickEvent(recommendationId, type, genre, title) {
            fetch('/api/user-click-event/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    userId: 1,
                    recommendationId: recommendationId,
                    type: type,
                    genre: genre,
                    title: title
                })
            })
            .then(res => res.json())
            .then(data => {
                document.getElementById('msg').innerText = data.message || '클릭 이벤트 전송 완료!';
            })
            .catch(err => {
                document.getElementById('msg').innerText = '오류 발생: ' + err;
            });
        }

        // 페이지 로드 시 추천 정보 불러오기 (테스트용 diaryId=1)
        window.onload = function() {
            loadRecommendations(1); // diaryId를 원하는 값으로 바꿔서 테스트
        }
    </script>
</head>
<body>
<h2>유저 클릭 이벤트 테스트</h2>
<table>
    <thead>
    <tr>
        <th>추천ID</th>
        <th>타입</th>
        <th>장르</th>
        <th>제목</th>
        <th>링크</th>
        <th>클릭</th>
    </tr>
    </thead>
    <tbody id="recommendTable">
    <!-- JS로 동적 생성 -->
    </tbody>
</table>
<div id="msg" class="msg"></div>
</body>
</html> 