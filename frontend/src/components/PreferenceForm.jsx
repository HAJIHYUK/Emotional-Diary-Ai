import { useMemo } from 'react';
import { Card, Badge } from 'react-bootstrap';
import { FaPlusCircle, FaMinusCircle } from 'react-icons/fa';

// 백엔드 PreferenceCategory Enum과 맞춘 대분류 목록
const categories = ["MUSIC", "MOVIE", "BOOK", "CAFE", "FOOD", "ACTIVITY"];

// 프론트엔드에서 사용할 대분류-소분류 전체 데이터 (제가 임의로 정의)
const preferenceData = {
  MUSIC: ["발라드", "댄스", "팝", "록", "재즈", "클래식"],
  MOVIE: ["액션", "코미디", "로맨스", "스릴러", "SF", "애니메이션"],
  BOOK: ["소설", "자기계발", "인문", "과학", "만화"],
  CAFE: ["분위기 좋은", "디저트가 맛있는", "공부하기 좋은", "테라스가 있는"],
  FOOD: ["한식", "중식", "일식", "양식", "분식"],
  ACTIVITY: ["전시회", "콘서트", "스포츠 관람", "산책", "등산", "게임"],
};

// 화면에 표시될 대분류 한글 이름
const categoryKorean = {
    MUSIC: "음악",
    MOVIE: "영화",
    BOOK: "책",
    CAFE: "카페",
    FOOD: "음식",
    ACTIVITY: "활동",
}

function PreferenceForm({ selectedGenres, onToggleGenre }) {

  // 선택 가능한 소분류 목록을 동적으로 계산
  const availableGenres = useMemo(() => {
    const available = {};
    categories.forEach(cat => {
      available[cat] = preferenceData[cat].filter(
        genre => !selectedGenres.has(genre)
      );
    });
    return available;
  }, [selectedGenres]);

  return (
    <>
      {/* 1. 선택 가능한 취향 목록 (상단) */}
      <Card className="mb-4">
        <Card.Header as="h5" className="fw-bold">취향 선택</Card.Header>
        <Card.Body>
          {categories.map(category => (
            <div key={category} className="mb-4">
              <h6 className="fw-bold text-muted border-bottom pb-2 mb-3">{categoryKorean[category]}</h6>
              {availableGenres[category].length > 0 ? (
                <div className="d-flex flex-wrap gap-2">
                  {availableGenres[category].map(genre => (
                    <Badge 
                      key={genre} 
                      pill 
                      bg="light" 
                      text="dark" 
                      className="preference-badge available"
                      onClick={() => onToggleGenre(genre, category)}
                    >
                      <FaPlusCircle className="me-1" /> {genre}
                    </Badge>
                  ))}
                </div>
              ) : (
                <p className="text-muted small fst-italic">이 카테고리의 모든 취향을 선택하셨습니다.</p>
              )}
            </div>
          ))}
        </Card.Body>
      </Card>

      {/* 2. 현재 선택된 취향 목록 (하단) */}
      <Card>
        <Card.Header as="h5" className="fw-bold">나의 취향</Card.Header>
        <Card.Body style={{ minHeight: '100px' }}>
          {selectedGenres.size > 0 ? (
            <div className="d-flex flex-wrap gap-2">
              {Array.from(selectedGenres).map(genre => (
                <Badge 
                  key={genre} 
                  pill 
                  bg="primary" 
                  className="preference-badge selected"
                  onClick={() => onToggleGenre(genre, null)} // 카테고리 정보 없이 토글
                >
                  <FaMinusCircle className="me-1" /> {genre}
                </Badge>
              ))}
            </div>
          ) : (
            <p className="text-muted">아직 선택한 취향이 없습니다. 위에서 원하는 취향을 클릭하여 추가해보세요.</p>
          )}
        </Card.Body>
      </Card>
    </>
  );
}

export default PreferenceForm;
