import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Alert, Spinner } from 'react-bootstrap';
import { getUserPreferences, updateUserPreferences } from '../api/diaryApi';
import styles from '../pages/UserOnboarding.module.css'; // 스타일 재사용

// React-icons 임포트
import {
  FaYoutube, FaSmile, FaCoffee, FaUtensils, FaFilm, FaBook, FaMusic,
  FaMapMarkedAlt, FaWalking, FaBicycle, FaHamburger, FaSave, FaTimes
} from 'react-icons/fa';

// 취향 옵션
const PREFERENCE_OPTIONS = {
  YOUTUBE: { icon: <FaYoutube />, genres: ['VLOG', 'ASMR', '게임', '음악', '교육', '먹방', '뉴스/시사', '뷰티/패션', 'IT/기술', '여행', '동물', '스포츠', '요리'] },
  ENTERTAINMENT: { icon: <FaSmile />, genres: ['코미디', '드라마', '다큐멘터리', '예능', '애니메이션', '공연', '스포츠', '웹툰', '게임방송', '토크쇼'] },
  CAFE: { icon: <FaCoffee />, genres: ['모던', '빈티지', '테라스', '조용한', '디저트', '북카페', '루프탑', '스터디', '애견동반', '뷰맛집'] },
  RESTAURANT: { icon: <FaUtensils />, genres: ['한식', '양식', '중식', '일식', '퓨전', '패밀리', '파인 다이닝', '캐주얼', '비건', '가성비', '분위기 좋은'] },
  MOVIE: { icon: <FaFilm />, genres: ['액션', '코미디', '드라마', 'SF', '스릴러', '로맨스', '애니메이션', '판타지', '공포', '독립영화', '다큐멘터리'] },
  BOOK: { icon: <FaBook />, genres: ['소설', '에세이', '자기계발', '인문학', '과학', '판타지', '역사', '시', '경제/경영', '만화', '추리'] },
  MUSIC: { icon: <FaMusic />, genres: ['K-POP', 'POP', '발라드', '힙합', 'R&B', '클래식', '재즈', '록', '인디', 'EDM', 'OST', '뉴에이지'] },
  PLACE: { icon: <FaMapMarkedAlt />, genres: ['공원', '미술관', '박물관', '전시회', '쇼핑몰', '서점', '테마파크', '유적지', '전통시장', '바다', '산'] },
  WALKING_TRAIL: { icon: <FaWalking />, genres: ['숲길', '강변', '해변', '도심', '둘레길', '산책로', '계곡', '공원', '야경'] },
  ACTIVITY: { icon: <FaBicycle />, genres: ['요가', '필라테스', '헬스', '등산', '클라이밍', '수영', '자전거', '볼링', '스키/보드', '서핑', '전시관람', '방탈출'] },
  FOOD: { icon: <FaHamburger />, genres: ['한식', '양식', '중식', '일식', '아시안', '햄버거', '피자', '치킨', '분식', '디저트', '샐러드', '샌드위치', '스테이크', '파스타', '초밥', '족발/보쌈', '마라탕', '베이커리'] },
};

const CATEGORY_KEYS = Object.keys(PREFERENCE_OPTIONS);

// isEditMode: 기존 취향을 불러올지 여부 (true/false)
// onSave: 저장이 완료된 후 실행할 함수
// showCancel: 취소 버튼 표시 여부
// onCancel: 취소 버튼 클릭 시 실행할 함수
function PreferenceSetup({ isEditMode = false, onSave, showCancel = false, onCancel }) {
  const [activeCategory, setActiveCategory] = useState(CATEGORY_KEYS[0]);
  const [selectedPreferences, setSelectedPreferences] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(isEditMode); // 수정 모드일 때만 초기 로딩
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (isEditMode) {
      const fetchPreferences = async () => {
        try {
          const response = await getUserPreferences();
          const savedPreferences = response.data.data || [];
          
          const preferencesMap = {};
          savedPreferences.forEach(pref => {
            if (!preferencesMap[pref.category]) {
              preferencesMap[pref.category] = [];
            }
            preferencesMap[pref.category].push(pref.genre);
          });
          setSelectedPreferences(preferencesMap);

        } catch (err) {
          console.error("취향 정보 로딩 실패:", err);
          setError("취향 정보를 불러오는 데 실패했습니다.");
        } finally {
          setLoading(false);
        }
      };
      fetchPreferences();
    }
  }, [isEditMode]);

  const togglePreference = (category, genre) => {
    setSelectedPreferences(prev => {
      const currentGenres = prev[category] || [];
      const newGenres = currentGenres.includes(genre)
        ? currentGenres.filter(item => item !== genre)
        : [...currentGenres, genre];
      return { ...prev, [category]: newGenres };
    });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setError('');

    const preferencesToSave = Object.entries(selectedPreferences)
      .filter(([, genres]) => genres.length > 0)
      .map(([category, genres]) => ({ category, genres }));

    if (preferencesToSave.length === 0) {
      setError('최소 하나 이상의 취향을 선택해주세요.');
      return;
    }
    
    setSaving(true);
    try {
      await updateUserPreferences(preferencesToSave);
      alert('취향 정보가 성공적으로 저장되었습니다!');
      if (onSave) onSave(); // 부모가 넘겨준 onSave 함수 실행
    } catch (err) {
      console.error('취향 저장 실패:', err);
      setError('취향 정보를 저장하는 데 실패했습니다. 다시 시도해주세요.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ height: '80vh' }}>
        <Spinner animation="border" />
        <p className="ms-3">취향 정보를 불러오는 중입니다...</p>
      </div>
    );
  }

  return (
    <div className={styles.pageContainer}>
      <div className={styles.header}>
        <h1>{isEditMode ? '나의 취향 설정' : '당신의 취향을 알려주세요!'}</h1>
        <p>{isEditMode ? '언제든지 취향을 다시 설정하고 관리할 수 있습니다.' : '더 정확한 추천을 위해 좋아하는 것들을 선택해주세요.'}</p>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      <form onSubmit={handleSave}>
        <div className={styles.mainContent}>
          <nav className={styles.categoryNav}>
            <ul className={styles.categoryList}>
              {CATEGORY_KEYS.map(category => (
                <li
                  key={category}
                  className={`${styles.categoryItem} ${activeCategory === category ? styles.active : ''}`}
                  onClick={() => setActiveCategory(category)}
                >
                  {PREFERENCE_OPTIONS[category].icon}
                  {category}
                </li>
              ))}
            </ul>
          </nav>

          <main className={styles.genreSelection}>
            <div className={styles.genreHeader}>
              <h3>
                {PREFERENCE_OPTIONS[activeCategory].icon}
                {activeCategory}
              </h3>
            </div>
            <div className={styles.genreGrid}>
              {PREFERENCE_OPTIONS[activeCategory].genres.map(genre => (
                <button
                  type="button"
                  key={genre}
                  className={`${styles.genreButton} ${selectedPreferences[activeCategory]?.includes(genre) ? styles.active : ''}`}
                  onClick={() => togglePreference(activeCategory, genre)}
                  disabled={saving}
                >
                  {genre}
                </button>
              ))}
            </div>

            {Object.values(selectedPreferences).some(genres => genres.length > 0) && (
              <div className={styles.summaryContainer}>
                <h4 className={styles.summaryTitle}>내가 선택한 취향</h4>
                {Object.entries(selectedPreferences).map(([category, genres]) =>
                  genres.length > 0 && (
                    <div key={category} className={styles.summaryCategoryGroup}>
                      <h5 className={styles.summaryCategoryTitle}>{category}</h5>
                      <div className={styles.summaryTagList}>
                        {genres.map(genre => (
                          <span key={genre} className={styles.summaryTag}>
                            {genre}
                            <button
                              type="button"
                              className={styles.removeTagBtn}
                              onClick={() => togglePreference(category, genre)}
                              aria-label={`Remove ${genre}`}
                            >
                              &times;
                            </button>
                          </span>
                        ))}
                      </div>
                    </div>
                  )
                )}
              </div>
            )}
          </main>
        </div>

        <div className={styles.submitContainer}>
           {showCancel && (
             <Button variant="light" onClick={onCancel} disabled={saving} className="me-2">
                <FaTimes className="me-1"/> 취소
              </Button>
           )}
          <Button type="submit" disabled={saving || loading} className={styles.submitButton}>
            {saving ? '저장 중...' : <><FaSave className="me-2"/> {isEditMode ? '변경사항 저장' : '취향 저장하고 시작하기'}</>}
          </Button>
        </div>
      </form>
    </div>
  );
}

export default PreferenceSetup;
