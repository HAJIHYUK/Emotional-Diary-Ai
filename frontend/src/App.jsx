import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import DiaryList from './pages/DiaryList';
import DiaryWrite from './pages/DiaryWrite';
import DiaryDetail from './pages/DiaryDetail';
import EmotionStats from './pages/EmotionStats';
import Recommendations from './pages/Recommendations';
import Settings from './pages/Settings';
import './index.css'; // 완전히 새로워진 파스텔톤 CSS 임포트

function App() {
  return (
    <Router>
      <div className="app-container">
        <Sidebar />
        <main className="content-area">
          <Routes>
            <Route path="/" element={<DiaryList />} />
            <Route path="/write" element={<DiaryWrite />} />
            <Route path="/edit/:id" element={<DiaryWrite />} /> {/* 수정 경로 추가 */}
            <Route path="/diary/:id" element={<DiaryDetail />} />
            <Route path="/stats" element={<EmotionStats />} />
            <Route path="/recommendations" element={<Recommendations />} />
            <Route path="/settings" element={<Settings />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;