import axios from 'axios';

// 백엔드 API 서버의 기본 주소를 설정합니다.
const instance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000, // 요청 타임아웃 5초
});

export default instance;
