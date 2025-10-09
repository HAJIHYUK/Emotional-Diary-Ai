import { useState, useEffect, useCallback } from 'react';
import { Button, Spinner, Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import PreferenceForm from '../components/PreferenceForm';
import { getUserPreferences, updateUserPreferences, deleteUserPreferences } from '../api/diaryApi';
import { FaSave, FaTimes } from 'react-icons/fa';

// PreferenceForm에서 사용하는 데이터와 동일한 구조
const preferenceData = {
    MUSIC: ["발라드", "댄스", "팝", "록", "재즈", "클래식"],
    MOVIE: ["액션", "코미디", "로맨스", "스릴러", "SF", "애니메이션"],
    BOOK: ["소설", "자기계발", "인문", "과학", "만화"],
    CAFE: ["분위기 좋은", "디저트가 맛있는", "공부하기 좋은", "테라스가 있는"],
    FOOD: ["한식", "중식", "일식", "양식", "분식"],
    ACTIVITY: ["전시회", "콘서트", "스포츠 관람", "산책", "등산", "게임"],
};

function UserPreferences() {
  const navigate = useNavigate();
  
  // API로부터 받은 원본 데이터 저장 (id, category, genre 포함)
  const [originalPreferences, setOriginalPreferences] = useState([]);
  // 화면 표시 및 선택 관리를 위한 Set (genre 문자열만 저장)
  const [selectedGenres, setSelectedGenres] = useState(new Set());

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  // 페이지 로드 시 기존 취향 불러오기
  useEffect(() => {
    const fetchPreferences = async () => {
      try {
        const res = await getUserPreferences();
        const data = res.data.data || [];
        setOriginalPreferences(data);
        setSelectedGenres(new Set(data.map(p => p.genre)));
      } catch (err) {
        setError('취향 정보를 불러오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    fetchPreferences();
  }, []);

  // 소분류(genre) 선택/해제를 처리하는 함수
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

  // 저장 버튼 클릭 시 실행
  const handleSave = async () => {
    setSaving(true);
    setError(null);

    const originalGenreSet = new Set(originalPreferences.map(p => p.genre));
    const currentGenreSet = selectedGenres;

    // 1. 새로 추가된 장르 목록 찾기
    const genresToAdd = [...currentGenreSet].filter(g => !originalGenreSet.has(g));
    
    // 2. 삭제된 취향의 ID 목록 찾기
    const idsToDelete = originalPreferences
      .filter(p => !currentGenreSet.has(p.genre))
      .map(p => p.userPreferenceId);

    try {
      // 추가 API 호출
      if (genresToAdd.length > 0) {
        const payload = [];
        const groupedByCat = {};

        genresToAdd.forEach(genre => {
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
      }

      // 삭제 API 호출
      if (idsToDelete.length > 0) {
        await deleteUserPreferences(idsToDelete);
      }

      navigate('/settings');
    } catch (err) {
      console.error(err);
      setError('취향 저장에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="text-center mt-5"><Spinner animation="border" /></div>;
  }

  return (
    <>
      <h1 className="fw-bold mb-4">나의 취향 설정</h1>
      {error && <Alert variant="danger">{error}</Alert>}
      
      <PreferenceForm 
        selectedGenres={selectedGenres}
        onToggleGenre={handleToggleGenre}
      />

      <div className="d-flex justify-content-end gap-2 mt-4">
        <Button variant="light" onClick={() => navigate('/settings')} disabled={saving}>
          <FaTimes className="me-2"/>취소
        </Button>
        <Button variant="primary" onClick={handleSave} disabled={saving}>
          {saving ? <Spinner as="span" animation="border" size="sm" /> : <><FaSave className="me-2"/>저장하기</>}
        </Button>
      </div>
    </>
  );
}

export default UserPreferences;
