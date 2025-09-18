import { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation, Link } from 'react-router-dom';
import { Form, Button, Card, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { createDiary, updateDiary, getDiaryDetail } from '../api/diaryApi';
import { FaPaperPlane, FaTimes } from 'react-icons/fa';

function DiaryWrite() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const isEditMode = !!id;

  const [content, setContent] = useState('');
  const [entryDate, setEntryDate] = useState(new Date().toISOString().split('T')[0]);
  const [weather, setWeather] = useState('맑음 ☀️');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const weatherOptions = ['맑음 ☀️', '흐림 ☁️', '비 🌧️', '눈 ❄️', '바람 💨', '안개 🌫️', '천둥/번개 ⚡'];

  useEffect(() => {
    if (isEditMode) {
      const initialData = location.state?.diary;
      if (initialData) {
        // 상세 페이지에서 전달받은 데이터로 폼 채우기
        setContent(initialData.content || '');
        setWeather(initialData.weather ? `${initialData.weather} ${weatherOptions.find(w => w.startsWith(initialData.weather))?.split(' ')[1] || ''}`.trim() : '맑음 ☀️');
        setEntryDate(initialData.entryDate ? new Date(initialData.entryDate).toISOString().split('T')[0] : new Date().toISOString().split('T')[0]);
      } else {
        // 데이터가 없을 경우 (e.g., 페이지 새로고침) API로 다시 조회
        setLoading(true);
        getDiaryDetail(id)
          .then(res => {
            const diary = res.data.data;
            setContent(diary.content || '');
            setWeather(diary.weather ? `${diary.weather} ${weatherOptions.find(w => w.startsWith(diary.weather))?.split(' ')[1] || ''}`.trim() : '맑음 ☀️');
            setEntryDate(diary.entryDate ? new Date(diary.entryDate).toISOString().split('T')[0] : new Date().toISOString().split('T')[0]);
          })
          .catch(err => setError('일기 정보를 불러오는 데 실패했습니다.'))
          .finally(() => setLoading(false));
      }
    }
  }, [id, isEditMode, location.state]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) {
      setError('일기 내용을 입력해주세요.');
      return;
    }
    setLoading(true);
    
    const weatherText = weather.split(' ')[0];
    const diaryData = { content, entryDate, weather: weatherText };

    try {
      if (isEditMode) {
        await updateDiary(id, diaryData);
        navigate(`/diary/${id}`); // 수정 후 상세 페이지로 이동
      } else {
        const response = await createDiary(diaryData);
        const newDiaryId = response.data.data.recordId;
        navigate(`/diary/${newDiaryId}`); // 생성 후 상세 페이지로 이동
      }
    } catch (err) {
      console.error(err);
      setError(isEditMode ? '일기 수정에 실패했습니다.' : '일기 작성에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <h1 className="fw-bold mb-4">{isEditMode ? '일기 수정하기' : '새로운 마음 기록'}</h1>
      <Card>
        <Card.Body className="p-4 p-md-5">
          {error && <Alert variant="danger">{error}</Alert>}
          <Form onSubmit={handleSubmit}>
            <Row className="mb-4">
              <Col md={6}>
                <Form.Group controlId="formEntryDate">
                  <Form.Label className="fw-bold">날짜</Form.Label>
                  <Form.Control
                    type="date"
                    value={entryDate}
                    onChange={(e) => setEntryDate(e.target.value)}
                    disabled={loading}
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group controlId="formWeather">
                  <Form.Label className="fw-bold">날씨</Form.Label>
                  <Form.Select
                    value={weather}
                    onChange={(e) => setWeather(e.target.value)}
                    disabled={loading}
                  >
                    {weatherOptions.map(opt => <option key={opt} value={opt}>{opt}</option>)}
                  </Form.Select>
                </Form.Group>
              </Col>
            </Row>

            <Form.Group controlId="formDiaryContent">
              <Form.Label className="fw-bold fs-5 mb-3">오늘의 이야기</Form.Label>
              <Form.Control
                as="textarea"
                rows={14}
                placeholder="오늘 하루는 어떠셨나요? 당신의 감정을 솔직하게 들려주세요. AI가 당신의 마음에 귀 기울여 드릴게요."
                value={content}
                onChange={(e) => setContent(e.target.value)}
                disabled={loading}
                style={{ backgroundColor: '#fafafa', border: 'none' }}
              />
            </Form.Group>
            <div className="d-flex justify-content-end gap-2 mt-4">
              <Button as={Link} to={isEditMode ? `/diary/${id}` : '/'} variant="light" disabled={loading}><FaTimes className="me-2"/>취소</Button>
              <Button variant="primary" type="submit" disabled={loading}>
                {loading ? <Spinner as="span" animation="border" size="sm" /> : <><FaPaperPlane className="me-2"/>저장하기</>}</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </>
  );
}

export default DiaryWrite;
