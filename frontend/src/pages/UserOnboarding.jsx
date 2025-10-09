import { useState, useCallback } from 'react';
import { Button, Spinner, Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import PreferenceForm from '../components/PreferenceForm';
import { updateUserPreferences } from '../api/diaryApi';
import { FaPaperPlane } from 'react-icons/fa';

// PreferenceForm에서 사용하는 데이터와 동일한 구조
const preferenceData = {
    MUSIC: ["발라드", "댄스", "팝", "록", "재즈", "클래식"],
    MOVIE: ["액션", "코미디", "로맨스", "스릴러", "SF", "애니메이션"],
    BOOK: ["소설", "자기계발", "인문", "과학", "만화"],
    CAFE: ["분위기 좋은", "디저트가 맛있는", "공부하기 좋은", "테라스가 있는"],
    FOOD: ["한식", "중식", "일식", "양식", "분식"],
    ACTIVITY: ["전시회", "콘서트", "스포츠 관람", "산책", "등산", "게임"],
};

function UserOnboarding() {
  const navigate = useNavigate();
  const [selectedGenres, setSelectedGenres] = useState(new Set());
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const handleToggleGenre = useCallback((genre) => {
    setSelectedGenres(prev => {
      const newSet = new Set(prev);
      if (newSet.has(genre)) {
        newSet.delete(genre);
      } else {
        newSet.add(genre);
      }
      return newSet;
    });
  }, []);

  const handleSave = async () => {
    if (selectedGenres.size === 0) {
      setError('최소 1개 이상의 취향을 선택해주세요.');
      return;
    }
    setSaving(true);
    setError(null);

    try {
      const payload = [];
      const groupedByCat = {};

      selectedGenres.forEach(genre => {
        for (const cat in preferenceData) {
          if (preferenceData[cat].includes(genre)) {
            if (!groupedByCat[cat]) groupedByCat[cat] = [];
            groupedByCat[cat].push(genre);
            break;
          }
        }
      });

      for (const cat in groupedByCat) {
        payload.push({ category: cat, genres: groupedByCat[cat] });
      }
      
      await updateUserPreferences(payload);
      navigate('/'); // 저장 후 메인 페이지로 이동
    } catch (err) {
      console.error(err);
      setError('취향 저장에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="d-flex flex-column justify-content-center align-items-center vh-100 bg-light">
      <div style={{maxWidth: '800px', width: '100%', padding: '20px'}}>
        <h1 className="fw-bold mb-2 text-center">환영합니다!</h1>
        <p className="text-muted mb-4 text-center">더 정확한 추천을 위해, 당신의 취향을 알려주세요.</p>
        
        {error && <Alert variant="danger" onClose={() => setError(null)} dismissible>{error}</Alert>}
        
        <PreferenceForm 
          selectedGenres={selectedGenres}
          onToggleGenre={handleToggleGenre}
        />

        <div className="d-grid mt-4">
          <Button variant="primary" size="lg" onClick={handleSave} disabled={saving}>
            {saving ? <Spinner as="span" animation="border" size="sm" /> : <><FaPaperPlane className="me-2"/>저장하고 시작하기</>}
          </Button>
        </div>
      </div>
    </div>
  );
}

export default UserOnboarding;
