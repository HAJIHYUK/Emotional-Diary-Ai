import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import DiaryList from './pages/DiaryList';
import DiaryWrite from './pages/DiaryWrite';
import DiaryDetail from './pages/DiaryDetail';
import EmotionStats from './pages/EmotionStats';
import Recommendations from './pages/Recommendations';
import Settings from './pages/Settings';
import UserPreferences from './pages/UserPreferences';
import UserOnboarding from './pages/UserOnboarding';
import OnboardingFlow from './pages/OnboardingFlow';
import Login from './pages/Login';
import KakaoRedirectHandler from './pages/auth/KakaoRedirectHandler';
import './index.css';

// 라우팅 및 레이아웃 로직을 담당할 내부 컴포넌트
function AppLayout() {
  const isUserLoggedIn = !!localStorage.getItem('jwt');
  const location = useLocation(); // 이제 Router 내부에 있으므로 정상 작동

  // 현재 경로가 온보딩 플로우인지 확인
  const isOnboardingFlow = location.pathname.startsWith('/onboarding-flow');

  return (
    <div className="app-container">
      {/* 로그인이 되어 있고 온보딩 플로우가 아닐 때만 사이드바를 렌더링 */}
      {isUserLoggedIn && !isOnboardingFlow && <Sidebar />}
      
      {/* 로그인 상태 및 온보딩 플로우 여부에 따라 content-area의 클래스를 동적으로 변경 */}
      <main className={isUserLoggedIn && !isOnboardingFlow ? "content-area" : "content-area-full"}>
        <Routes>
          {isUserLoggedIn ? (
            <>
              {/* === 로그인 후 접근 가능한 경로 === */}
              <Route path="/" element={<DiaryList />} />
              <Route path="/write" element={<DiaryWrite />} />
              <Route path="/edit/:id" element={<DiaryWrite />} />
              <Route path="/diary/:id" element={<DiaryDetail />} />
              <Route path="/stats" element={<EmotionStats />} />
              <Route path="/recommendations" element={<Recommendations />} />
              <Route path="/settings" element={<Settings />} />
              <Route path="/settings/preferences" element={<UserPreferences />} />
              <Route path="/onboarding" element={<UserOnboarding />} />
              <Route path="/onboarding-flow" element={<OnboardingFlow />} />
              {/* 로그인 후 /login 경로 접근 시 메인으로 리디렉션 */}
              <Route path="/login" element={<Navigate to="/" />} />
               {/* 로그인 후 다른 모든 경로는 메인으로 */}
              <Route path="*" element={<Navigate to="/" />} />
            </>
          ) : (
            <>
              {/* === 로그인 전 접근 가능한 경로 === */}
              <Route path="/login" element={<Login />} />
              <Route path="/auth/kakao/callback" element={<KakaoRedirectHandler />} />
              {/* 그 외 모든 경로는 로그인 페이지로 리디렉션 */}
              <Route path="*" element={<Navigate to="/login" />} />
            </>
          )}
        </Routes>
      </main>
    </div>
  );
}

// 최상위 App 컴포넌트는 Router만 제공
function App() {
  return (
    <Router>
      <AppLayout />
    </Router>
  );
}

export default App;
