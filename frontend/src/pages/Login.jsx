import React from 'react';
import { Card, Button } from 'react-bootstrap';
import { RiKakaoTalkFill } from 'react-icons/ri';
import styles from './Login.module.css';

function Login() {
    // [배포 수정] 환경변수 대신 하드코딩 (빌드 문제 해결용)
    const KAKAO_JAVASCRIPT_KEY = '4b4f2c67b097ab92e7c26510b13baa70'; // JavaScript 키
    const KAKAO_REDIRECT_URI = 'http://3.39.187.53:8080/auth/kakao/callback'; // 서버 IP 주소

    const kakaoLoginUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_JAVASCRIPT_KEY}&redirect_uri=${KAKAO_REDIRECT_URI}&response_type=code`;

    return (
        <div className={styles.fullScreenContainer}>
            <div className={styles.contentWrapper}>
                <h1 className={styles.mainTitle}>감정 일기</h1>
                <p className={styles.subtitle}>
                    당신의 하루를 기록하고, AI와 함께 마음을 돌보는 특별한 공간입니다.
                </p>

                <Card className={styles.loginCard}>
                    <Card.Body>
                        <Card.Title className={styles.loginTitle}>
                            카카오로 1초만에 시작하기
                        </Card.Title>
                        <Button 
                            href={kakaoLoginUrl}
                            variant="warning" 
                            size="lg" 
                            className={`w-100 ${styles.kakaoLoginBtn}`}
                        >
                            <RiKakaoTalkFill className="me-2" />
                            카카오 로그인
                        </Button>
                    </Card.Body>
                </Card>
            </div>
        </div>
    );
}

export default Login;