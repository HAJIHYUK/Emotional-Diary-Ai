import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Spinner } from 'react-bootstrap';
import { loginWithKakao } from '../../api/diaryApi';

/**
 * 카카오 로그인 후 리디렉션되는 페이지.
 * 백엔드와 통신하여 최종 로그인을 처리합니다.
 */
function KakaoRedirectHandler() {
    const location = useLocation();
    const navigate = useNavigate();

    // 카카오 로그인 처리를 위한 핵심 로직 함수
    const handleLogin = async (code) => {
        try {
            console.log("백엔드에 인증 코드 전송 시도:", code);
            const response = await loginWithKakao(code);
            
            const { token, isNewUser } = response.data;
            console.log(`JWT 수신 성공, isNewUser: ${isNewUser}`);

            localStorage.setItem('jwt', token);
            
            // [수정] isNewUser 값에 따라 다른 페이지로 이동시킵니다.
            if (isNewUser) {
                // 신규 사용자는 온보딩 플로우 페이지로 이동
                window.location.href = '/onboarding-flow';
            } else {
                // 기존 사용자는 메인 페이지로 이동
                window.location.href = '/';
            }

        } catch (error) {
            console.error("카카오 로그인 처리 실패:", error);
            navigate('/login', { state: { error: "로그인에 실패했습니다." }, replace: true });
        }
    };

    useEffect(() => {
        const code = new URLSearchParams(location.search).get('code');

        if (code) {
            handleLogin(code);
        } else {
            console.error("인가 코드를 찾을 수 없습니다.");
            navigate('/login', { replace: true });
        }
    }, []);

    return (
        <div className="d-flex justify-content-center align-items-center vh-100">
            <Spinner animation="border" />
            <p className="ms-3">로그인 처리 중입니다...</p>
        </div>
    );
}

export default KakaoRedirectHandler;