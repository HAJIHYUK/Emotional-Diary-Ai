<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>감정 분석 테스트</title>
<style>
    body {
        font-family: 'Noto Sans KR', sans-serif;
        margin: 0;
        padding: 20px;
        background-color: #f5f5f5;
    }
    .container {
        max-width: 800px;
        margin: 0 auto;
        background-color: #fff;
        padding: 20px;
        border-radius: 10px;
        box-shadow: 0 0 10px rgba(0,0,0,0.1);
    }
    h1 {
        color: #333;
        text-align: center;
        margin-bottom: 30px;
    }
    textarea {
        width: 100%;
        height: 150px;
        padding: 12px;
        border: 1px solid #ddd;
        border-radius: 5px;
        font-size: 16px;
        margin-bottom: 20px;
        resize: vertical;
    }
    button {
        background-color: #4CAF50;
        color: white;
        padding: 12px 20px;
        border: none;
        border-radius: 5px;
        cursor: pointer;
        font-size: 16px;
        display: block;
        margin: 0 auto;
    }
    button:hover {
        background-color: #45a049;
    }
    #result {
        margin-top: 20px;
        padding: 15px;
        border: 1px solid #ddd;
        border-radius: 5px;
        background-color: #f9f9f9;
        min-height: 100px;
    }
    .loading {
        text-align: center;
        display: none;
        margin: 20px 0;
    }
    .loading-spinner {
        display: inline-block;
        width: 40px;
        height: 40px;
        border: 4px solid rgba(0,0,0,0.1);
        border-radius: 50%;
        border-top-color: #4CAF50;
        animation: spin 1s ease-in-out infinite;
    }
    @keyframes spin {
        to { transform: rotate(360deg); }
    }
</style>
</head>
<body>
<div class="container">
    <h1>AI 감정 분석 테스트</h1>
    <label for="diary-id-input">분석할 diaryRecordId 입력:</label>
    <input type="number" id="diary-id-input" placeholder="예: 1" required />
    <button onclick="analyzeEmotion()">감정 분석하기</button>
    
    <div class="loading" id="loading">
        <div class="loading-spinner"></div>
        <p>분석 중입니다...</p>
    </div>
    
    <div id="result" style="display: none;">
        <h3>분석 결과:</h3>
        <pre id="result-content"></pre>
    </div>

    <hr>
    <!-- 감정분석 결과 조회 UI 추가 -->
    <h2>저장된 감정분석 결과 조회</h2>
    <label for="result-diary-id-input">조회할 diaryRecordId 입력:</label>
    <input type="number" id="result-diary-id-input" placeholder="예: 1" required />
    <button onclick="getEmotionResult()">감정분석 결과 조회</button>
    <div id="get-result" style="display: none;">
        <h3>저장된 분석 결과:</h3>
        <pre id="get-result-content"></pre>
    </div>
</div>

<script>
function analyzeEmotion() {
    const diaryId = document.getElementById('diary-id-input').value.trim();
    if (!diaryId) {
        alert('분석할 diaryRecordId를 입력해주세요.');
        return;
    }
    document.getElementById('loading').style.display = 'block';
    document.getElementById('result').style.display = 'none';
    fetch('/api/emotion/analyze?userId=1', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ diaryRecordId: Number(diaryId) })
    })
    .then(response => {
        if (!response.ok) throw new Error('API 호출 중 오류가 발생했습니다.');
        return response.json();
    })
    .then(data => {
        document.getElementById('loading').style.display = 'none';
        document.getElementById('result').style.display = 'block';
        document.getElementById('result-content').textContent = 
            JSON.stringify(data.data, null, 2);
    })
    .catch(error => {
        document.getElementById('loading').style.display = 'none';
        document.getElementById('result').style.display = 'block';
        document.getElementById('result-content').textContent = 
            '오류 발생: ' + error.message;
    });
}

// 저장된 감정분석 결과 조회 함수
function getEmotionResult() {
    const diaryId = document.getElementById('result-diary-id-input').value.trim();
    if (!diaryId) {
        alert('조회할 diaryRecordId를 입력해주세요.');
        return;
    }
    fetch('/api/emotion/result?diaryId=' + encodeURIComponent(diaryId))
    .then(response => {
        if (!response.ok) throw new Error('API 호출 중 오류가 발생했습니다.');
        return response.json();
    })
    .then(data => {
        document.getElementById('get-result').style.display = 'block';
        document.getElementById('get-result-content').textContent = 
            JSON.stringify(data.data, null, 2);
    })
    .catch(error => {
        document.getElementById('get-result').style.display = 'block';
        document.getElementById('get-result-content').textContent = 
            '오류 발생: ' + error.message;
    });
}
</script>
</body>
</html>