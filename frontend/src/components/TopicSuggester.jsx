import { useState } from 'react';
import { Button, Card, Spinner, Badge, Stack } from 'react-bootstrap';
import { getDiaryTopics } from '../api/diaryApi';

// 부모 컴포넌트로부터 받을 props 타입을 정의함 (TypeScript의 경우)
// 여기서는 onTopicSelect 함수를 props로 받음
function TopicSuggester({ onTopicSelect }) {
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedEmotion, setSelectedEmotion] = useState(null);

  // 4가지 핵심 감정으로 수정하고, category 값을 DB와 일치시킴
  const emotions = [
    { category: 'JOY', label: '기쁨', emoji: '😊' },
    { category: 'SADNESS', label: '슬픔', emoji: '😢' },
    { category: 'ANGER', label: '분노', emoji: '😠' },
    { category: 'ANXIETY', label: '불안', emoji: '😨' },
  ];

  // 감정 버튼 클릭 핸들러
  const handleEmotionClick = async (category) => {
    setLoading(true);
    setError(null);
    setSelectedEmotion(category);
    try {
      // API 호출 시 감정 카테고리를 type 파라미터로 전달
      const response = await getDiaryTopics(category);
      setSuggestions(response.data.data || []);
    } catch (err) {
      setError('주제를 불러오는 데 실패했습니다.');
      setSuggestions([]);
    } finally {
      setLoading(false);
    }
  };

  // 랜덤 추천 버튼 클릭 핸들러
  const handleRandomClick = () => {
    // API 호출 시 "random" 문자열을 type 파라미터로 전달
    handleEmotionClick("random");
  };

  return (
    <Card className="mb-4 bg-light border-0">
      <Card.Body>
        <p className="fw-bold mb-2">✍️ 무슨 이야기를 쓸지 막막하신가요?</p>
        <p className="text-muted mb-3" style={{ fontSize: '0.9rem' }}>
          오늘의 감정을 선택하거나 랜덤 추천을 받아보세요.
        </p>
        
        <Stack direction="horizontal" gap={2} className="mb-3 flex-wrap">
          {emotions.map(({ category, label, emoji }) => (
            <Button 
              key={category} 
              variant={selectedEmotion === category ? 'primary' : 'outline-secondary'}
              size="sm"
              onClick={() => handleEmotionClick(category)}
            >
              {emoji} {label}
            </Button>
          ))}
          <Button variant="outline-success" size="sm" onClick={handleRandomClick}>🎲 랜덤 추천</Button>
        </Stack>

        {loading && (
          <div className="text-center">
            <Spinner animation="border" size="sm" />
            <span className="ms-2">주제를 찾고 있어요...</span>
          </div>
        )}

        {error && <p className="text-danger mt-2">{error}</p>}

        {suggestions.length > 0 && (
          <Stack gap={2} className="mt-3">
            {suggestions.map((topic) => (
              <Badge 
                key={topic.id}
                pill 
                bg="light"
                text="dark"
                onClick={() => onTopicSelect(topic.topicText)} 
                className="p-2 px-3 fw-normal user-select-none" 
                style={{ cursor: 'pointer' }}
              >
                {topic.topicText}
              </Badge>
            ))}
          </Stack>
        )}
      </Card.Body>
    </Card>
  );
}

export default TopicSuggester;
