import { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { Row, Col, Card, Button, Spinner, Alert, Placeholder, ButtonGroup } from 'react-bootstrap';
import { getDiaryList } from '../api/diaryApi';
import { FaFeatherAlt } from 'react-icons/fa';

// 상세 페이지와 UI 일관성을 위한 날씨 맵
const weatherMap = {
  '맑음': '맑음 ☀️',
  '흐림': '흐림 ☁️',
  '비': '비 🌧️',
  '눈': '눈 ❄️',
  '바람': '바람 💨',
  '안개': '안개 🌫️',
  '천둥/번개': '천둥/번개 ⚡',
};

function DiaryList() {
  const [diaries, setDiaries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  // 기본 정렬을 '만든 날짜순' (createdAt)으로 변경
  const [sortType, setSortType] = useState('createdAt'); 

  useEffect(() => {
    const fetchDiaries = async () => {
      try {
        const response = await getDiaryList();
        const diariesData = response.data && Array.isArray(response.data.data) ? response.data.data : [];
        console.log('API에서 받은 일기 데이터:', diariesData);
        setDiaries(diariesData);
      } catch (err) {
        setError('일기 목록을 불러오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    fetchDiaries();
  }, []);

  // 정렬 로직: 내림차순 (최신순)으로 설정
  const sortedDiaries = useMemo(() => {
    const newDiaries = [...diaries];
    newDiaries.sort((a, b) => {
      if (sortType === 'entryDate') {
        const dateA = new Date(a.entryDate || a.createdAt).setHours(0, 0, 0, 0);
        const dateB = new Date(b.entryDate || b.createdAt).setHours(0, 0, 0, 0);

        if (dateB !== dateA) {
          return new Date(b.entryDate || b.createdAt) - new Date(a.entryDate || a.createdAt);
        }
        return new Date(b.createdAt) - new Date(a.createdAt);
      } else {
        return new Date(b.createdAt) - new Date(a.createdAt);
      }
    });
    return newDiaries;
  }, [diaries, sortType]);


  // 가장 비중이 높은 대표 감정의 이모지를 반환하는 함수
  const getDominantEmotionEmoji = (emotions) => {
    const emojiMap = { '기쁨': '😊', '슬픔': '😢', '분노': '😡', '불안': '😟', '사랑': '🥰', '평온': '😌' };
    
    if (!emotions || emotions.length === 0) {
      return '🤔'; // 감정 정보가 없으면 기본 이모지
    }

    // 가장 percentage가 높은 감정을 찾음
    const dominantEmotion = emotions.reduce((max, current) => (current.percentage > max.percentage ? current : max), emotions[0]);
    return emojiMap[dominantEmotion.label] || '🤔';
  };

  const renderSkeletons = () => (
    Array.from({ length: 6 }).map((_, index) => (
      <Col lg={4} md={6} className="mb-4" key={index}>
        <Card className="h-100"><Card.Body><Placeholder as={Card.Title} animation="glow"><Placeholder xs={7} /></Placeholder><Placeholder as={Card.Text} animation="glow"><Placeholder xs={10} /><Placeholder xs={8} /></Placeholder></Card.Body></Card>
      </Col>
    ))
  );

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-5">
        <h1 className="fw-bold" style={{ color: 'var(--text-color)' }}>내 일기장</h1>
        {/* 버튼 텍스트를 명확하게 수정하고, 기본값인 '만든 날짜순'을 먼저 표시 */}
        <ButtonGroup>
          <Button 
            variant={sortType === 'createdAt' ? 'primary' : 'outline-secondary'} 
            onClick={() => setSortType('createdAt')}
          >
            만든 날짜순
          </Button>
          <Button 
            variant={sortType === 'entryDate' ? 'primary' : 'outline-secondary'} 
            onClick={() => setSortType('entryDate')}
          >
            일기 날짜순
          </Button>
        </ButtonGroup>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      <Row>
        {loading ? renderSkeletons() : sortedDiaries.length > 0 ? (
          sortedDiaries.map((diary) => (
            <Col lg={4} md={6} className="mb-4" key={diary.diaryRecordId}>
              <Card as={Link} to={`/diary/${diary.diaryRecordId}`} className="h-100 text-decoration-none">
                <Card.Body>
                  <div className="d-flex justify-content-between align-items-start">
                    <Card.Title className="mb-1 fw-bold" style={{color: 'var(--text-color)'}}>{new Date(diary.entryDate || diary.createdAt).toLocaleDateString('ko-KR')}</Card.Title>
                    <span style={{ fontSize: '1.8rem' }}>
                      {getDominantEmotionEmoji(diary.emotions)}
                    </span>
                  </div>
                  {/* 날씨 표시에 weatherMap 사용 */}
                  <Card.Subtitle className="text-muted fw-light">{weatherMap[diary.weather] || diary.weather || '날씨 기록 없음'}</Card.Subtitle>
                  <hr style={{borderColor: 'var(--border-color)'}}/>
                  <Card.Text className="mt-3" style={{color: 'var(--text-light-color)'}}>
                    {diary.content.substring(0, 90)}...
                  </Card.Text>
                </Card.Body>
              </Card>
            </Col>
          ))
        ) : (
          <Col className="text-center py-5">
            <Card className="p-5">
                <Card.Body>
                    <h2 className="fw-light mb-3">아직 비어있네요!</h2>
                    <p className="text-muted mb-4">오늘의 감정을 기록하고, AI의 특별한 추천을 받아보세요.</p>
                    <Button as={Link} to="/write" size="lg"><FaFeatherAlt className="me-2"/>첫 일기 쓰러가기</Button>
                </Card.Body>
            </Card>
          </Col>
        )}
      </Row>
    </>
  );
}

export default DiaryList;
