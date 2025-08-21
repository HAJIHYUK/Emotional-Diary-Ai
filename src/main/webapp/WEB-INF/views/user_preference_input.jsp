<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>유저 선호도 입력</title>
    <link rel="stylesheet" href="/common.css">
</head>
<body>
<div class="container">
    <h2>유저 선호도 입력 (간단버전)</h2>
    <form id="preferenceForm">
        <label for="userId">유저 ID</label>
        <input type="text" id="userId" name="userId" required>
        <button type="button" id="loadPreferenceBtn">선호도 조회</button>
        <button type="button" id="autoPreferenceBtn">유저 클릭이벤트 기반 선호도 자동저장 테스트</button>
        <label for="category">카테고리</label>
        <input type="text" id="category" name="category" required>
        <label for="genres">장르/소분류(쉼표로 구분)</label>
        <input type="text" id="genres" name="genres" required>
        <button type="submit">저장</button>
    </form>
    <div id="preferenceTableWrap" style="margin-top:30px; display:none;">
        <h3>유저 선호도 목록</h3>
        <table id="preferenceTable" border="1" style="width:100%; border-collapse:collapse;">
            <thead>
                <tr>
                    <th>카테고리</th>
                    <th>아이템명</th>
                    <th>타입</th>
                    <th>사용횟수</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>
</div>
<script>
document.getElementById('preferenceForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const userId = document.getElementById('userId').value;
    const category = document.getElementById('category').value;
    const genres = document.getElementById('genres').value.split(',').map(s => s.trim());
    // List<UserPreferenceInitialRequestDto> 구조에 맞게 배열로 보냄
    const preferences = [{ category, genres }];
    fetch('/api/user-preference/save?userId=' + encodeURIComponent(userId), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(preferences)
    })
    .then(res => res.json())
    .then(data => {
        alert(data.status === 0 ? '저장 성공!' : '저장 실패: ' + data.message);
        if(data.status === 0) location.reload();
    })
    .catch(err => alert('에러 발생: ' + err));
});

document.getElementById('loadPreferenceBtn').addEventListener('click', function() {
    const userId = document.getElementById('userId').value;
    if(!userId) { alert('유저 ID를 입력하세요.'); return; }
    fetch('/api/user-preference/list?userId=' + encodeURIComponent(userId))
        .then(res => res.json())
        .then(data => {
            if(data.status !== 0) {
                alert('조회 실패: ' + data.message);
                return;
            }
            const list = data.data;
            const tbody = document.querySelector('#preferenceTable tbody');
            tbody.innerHTML = '';
            if(list.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4">데이터 없음</td></tr>';
            } else {
                list.forEach(pref => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `<td>\${pref.category}</td><td>\${pref.genre}</td><td>\${pref.type}</td><td>\${pref.useCount}</td>`;
                    tbody.appendChild(tr);
                });
            }
            document.getElementById('preferenceTableWrap').style.display = 'block';
        })
        .catch(err => alert('에러 발생: ' + err));
});

document.getElementById('autoPreferenceBtn').addEventListener('click', function() {
    fetch('/api/user-preference/preferenceTest', {
        method: 'POST'
    })
    .then(res => res.json())
    .then(data => {
        alert(data.status === 0 ? '자동저장 성공!' : '자동저장 실패: ' + data.message);
        if(data.status === 0) location.reload();
        })
        .catch(err => alert('에러 발생: ' + err));
});
</script>
</body>
</html> 