import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Form, Button, Card, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { createDiary } from '../api/diaryApi';
import { FaPaperPlane, FaTimes } from 'react-icons/fa';

function DiaryWrite() {
  const [content, setContent] = useState('');
  const [entryDate, setEntryDate] = useState(new Date().toISOString().split('T')[0]);
  const [weather, setWeather] = useState('맑음 ☀️');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const weatherOptions = ['맑음 ☀️', '흐림 ☁️', '비 🌧️', '눈 ❄️', '바람 💨', '안개 🌫️', '천둥/번개 ⚡'];

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) {
      setError('일기 내용을 입력해주세요.');
      return;
    }
    setLoading(true);
    try {
      // 백엔드로 전송하기 전에 이모지를 분리하고 텍스트만 추출합니다.
      const weatherText = weather.split(' ')[0];
      await createDiary({ content, entryDate, weather: weatherText });
      navigate('/');
    } catch (err) {
      console.error(err); // 디버깅을 위해 에러를 콘솔에 출력
      setError('일기 작성에 실패했습니다. 서버 상태를 확인해주세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <h1 className="fw-bold mb-4">새로운 마음 기록</h1>
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
              <Button as={Link} to="/" variant="light" disabled={loading}><FaTimes className="me-2"/>취소</Button>
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