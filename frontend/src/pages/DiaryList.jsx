import { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { Row, Col, Card, Button, Spinner, Alert, Placeholder, ButtonGroup, Form } from 'react-bootstrap';
import { getDiaryList } from '../api/diaryApi';
import { FaFeatherAlt, FaSync } from 'react-icons/fa';

const weatherMap = {
  '맑음': '맑음 ☀️',
  '흐림': '흐림 ☁️',
  '비': '비 🌧️',
  '눈': '눈 ❄️',
  '바람': '바람 💨',
  '안개': '안개 🌫️',
  '천둥/번개': '천둥/번개 ⚡',
};

const months = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];

// 날짜 포맷팅 헬퍼 함수
const formatDateTime = (dateString) => {
  const date = new Date(dateString);
  return `${date.getFullYear()}. ${date.getMonth() + 1}. ${date.getDate()}. ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

function DiaryList() {
  const [diaries, setDiaries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sortType, setSortType] = useState('createdAt');
  
  const [selectedYear, setSelectedYear] = useState('');
  const [selectedMonth, setSelectedMonth] = useState('');

  useEffect(() => {
    const fetchDiaries = async () => {
      try {
        const response = await getDiaryList();
        setDiaries(response.data?.data || []);
      } catch (err) {
        setError('일기 목록을 불러오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    fetchDiaries();
  }, []);

  const availableYears = useMemo(() => {
    if (diaries.length === 0) return [];
    const years = new Set();
    diaries.forEach(diary => {
      if (diary.createdAt) years.add(new Date(diary.createdAt).getFullYear());
      if (diary.entryDate) years.add(new Date(diary.entryDate).getFullYear());
    });
    return Array.from(years).sort((a, b) => b - a);
  }, [diaries]);

  const filteredAndSortedDiaries = useMemo(() => {
    const filteredDiaries = diaries.filter(diary => {
      if (!selectedYear && !selectedMonth) return true;
      const dateToFilter = new Date(sortType === 'entryDate' ? diary.entryDate : diary.createdAt);
      const yearMatch = !selectedYear || dateToFilter.getFullYear() === parseInt(selectedYear, 10);
      const monthMatch = !selectedMonth || (dateToFilter.getMonth() + 1) === parseInt(selectedMonth, 10);
      return yearMatch && monthMatch;
    });

    return filteredDiaries.sort((a, b) => {
      if (sortType === 'entryDate') {
        const dateA = new Date(a.entryDate || a.createdAt);
        const dateB = new Date(b.entryDate || b.createdAt);
        if (dateB.toDateString() !== dateA.toDateString()) {
          return dateB - dateA;
        }
      }
      return new Date(b.createdAt) - new Date(a.createdAt);
    });
  }, [diaries, sortType, selectedYear, selectedMonth]);

  const handleResetFilter = () => {
    setSelectedYear('');
    setSelectedMonth('');
  };

  const getDominantEmotionEmoji = (emotions) => {
    const emojiMap = { '기쁨': '😊', '슬픔': '😢', '분노': '😡', '불안': '😟', '사랑': '🥰', '평온': '😌' };
    if (!emotions || emotions.length === 0) return null;
    const dominantEmotion = emotions.reduce((max, current) => (current.ratio > max.ratio ? current : max), emotions[0]);
    return emojiMap[dominantEmotion.label] || null;
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
      <div className="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-3">
        <h1 className="fw-bold mb-0">내 일기장</h1>
        
        <div className="d-flex align-items-center gap-2">
          <Form.Select size="sm" value={selectedYear} onChange={e => setSelectedYear(e.target.value)} style={{width: '100px'}}>
            <option value="">년도</option>
            {availableYears.map(year => <option key={year} value={year}>{year}년</option>)}
          </Form.Select>

          <Form.Select size="sm" value={selectedMonth} onChange={e => setSelectedMonth(e.target.value)} style={{width: '90px'}}>
            <option value="">월</option>
            {months.map(month => <option key={month} value={month}>{month}월</option>)}
          </Form.Select>

          <Button variant="outline-secondary" size="sm" onClick={handleResetFilter} title="필터 초기화">
            <FaSync />
          </Button>

          <ButtonGroup>
            <Button variant={sortType === 'createdAt' ? 'primary' : 'outline-secondary'} onClick={() => setSortType('createdAt')}>만든 날짜순</Button>
            <Button variant={sortType === 'entryDate' ? 'primary' : 'outline-secondary'} onClick={() => setSortType('entryDate')}>일기 날짜순</Button>
          </ButtonGroup>
        </div>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      <Row>
        {loading ? renderSkeletons() : filteredAndSortedDiaries.length > 0 ? (
          filteredAndSortedDiaries.map((diary) => (
            <Col lg={4} md={6} className="mb-4" key={diary.diaryRecordId}>
              <Card as={Link} to={`/diary/${diary.diaryRecordId}`} className="h-100 text-decoration-none">
                <Card.Body>
                  <div className="d-flex justify-content-between align-items-start">
                    <div>
                      <Card.Title className="mb-1 fw-bold" style={{color: 'var(--text-color)'}}>{new Date(diary.entryDate || diary.createdAt).toLocaleDateString('ko-KR')}</Card.Title>
                      {sortType === 'createdAt' && (
                        <small className="text-muted">만든 시간: {formatDateTime(diary.createdAt)}</small>
                      )}
                    </div>
                    <span style={{ fontSize: '1.8rem' }}>
                      {getDominantEmotionEmoji(diary.emotions)}
                    </span>
                  </div>
                  <Card.Subtitle className="mt-2 text-muted fw-light">{weatherMap[diary.weather] || diary.weather || '날씨 기록 없음'}</Card.Subtitle>
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
                    <p className="text-muted mb-4">{selectedYear || selectedMonth ? '해당 기간에 작성된 일기가 없어요.' : '오늘의 감정을 기록하고, AI의 특별한 추천을 받아보세요.'}</p>
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