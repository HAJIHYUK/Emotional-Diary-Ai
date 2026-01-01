import axios from 'axios';

const api = axios.create({
    baseURL: '', //  'http://localhost:8080' - > ' '  배포를 위해 상대 경로로 바꿈 
    withCredentials: true,
});

// --- 요청 인터셉터 ---
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('jwt');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// --- 응답 인터셉터 ---
api.interceptors.response.use(
    // 1. 성공적인 응답은 그대로 통과시킵니다.
    (response) => {
        return response;
    },
    // 2. 에러가 발생한 응답을 처리합니다.
    (error) => {
        // --- 디버깅 로그 ---
        console.log("Axios 응답 인터셉터 에러 발생!");
        console.log("전체 에러 객체:", error);
        console.log("에러 config:", error.config);
        console.log("에러 code:", error.code);
        console.log("에러 request:", error.request);
        if (error.response) {
            console.log("에러 response 데이터:", error.response.data);
            console.log("에러 response 상태:", error.response.status);
            console.log("에러 response 헤더:", error.response.headers);
        } else {
            console.log("에러에 response 객체가 없습니다.");
        }
        // --- 디버깅 로그 끝 ---

        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            console.log(`${error.response.status} 에러 감지. 로그아웃 처리 시작.`);
            localStorage.removeItem('jwt');
            window.location.href = '/login';
        }
        
        return Promise.reject(error);
    }
);

export default api;