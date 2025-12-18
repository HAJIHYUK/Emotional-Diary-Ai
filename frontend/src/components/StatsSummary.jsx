import { Card } from 'react-bootstrap';
import { FaLightbulb } from 'react-icons/fa';

// 감정 강도를 5개 구간으로 세분화하여 텍스트로 변환하는 함수
const getLevelDescription = (level) => {
  if (level > 8) return { text: '매우 강렬하게', detail: '일상에 큰 영향을 줄 만큼 선명하고 압도적인 감정이었어요.' };
  if (level > 6) return { text: '강하게', detail: '분명하게 인지할 수 있으며, 행동이나 생각에 영향을 미치는 감정을 느끼셨네요.' };
  if (level > 4) return { text: '뚜렷하게', detail: '일상 속에서 알아차릴 수 있을 만큼 의미 있는 감정을 경험하셨어요.' };
  if (level > 2) return { text: '잔잔하게', detail: '마음속에 부드럽게 스쳐 지나가는 감정이었을 거예요.' };
  return { text: '희미하게', detail: '감정이 거의 느껴지지 않는 평온한 상태에 가까웠네요.' };
};

// 주된 감정에 따라 추가 코멘트를 제공하는 함수 (7가지 감정 모두 반영)
const getEmotionSpecificComment = (emotion) => {
  switch (emotion) {
    case 'JOY':
    case '기쁨':
      return '이 즐거운 순간들을 마음에 담아두면, 힘든 날에 큰 힘이 될 거예요.';
    case 'SADNESS':
    case '슬픔':
      return '슬픔을 느끼는 것은 자연스러운 일이에요. 충분히 느끼고 흘려보내도 괜찮아요.';
    case 'ANGER':
    case '분노':
      return '무엇이 마음을 불편하게 했는지 돌아보는 것은 감정을 해소하는 첫걸음이 될 수 있어요.';
    case 'ANXIETY':
    case '불안':
      return '불안감이 느껴질 땐, 잠시 하던 일을 멈추고 편안한 호흡에 집중해보는 건 어떨까요?';
    case 'SURPRISE':
    case '놀람':
      return '예상치 못한 순간이 당신의 일상에 새로운 활력을 불어넣어 주었네요!';
    case 'DISGUST':
    case '역겨움':
      return '불쾌한 감정은 우리가 무엇을 원하지 않는지 알려주는 중요한 신호일 수 있어요.';
    case 'NEUTRAL':
    case '중립':
      return '때로는 아무 일 없는 평온한 하루가 가장 큰 선물일 수 있어요.';
    default:
      return '다양한 감정들이 모여 당신의 하루를 만들고 있네요.';
  }
};

function StatsSummary({ stats }) {
  if (!stats || !stats.stats || stats.stats.length === 0) {
    return null; // 데이터가 없으면 아무것도 렌더링하지 않음
  }

  // 1. 주요 데이터 추출
  const mainEmotion = stats.stats[0]; // 가장 비율이 높은 감정
  const secondEmotion = stats.stats.length > 1 ? stats.stats[1] : null;

  // 2. 감정 강도에 대한 설명 생성
  const levelInfo = getLevelDescription(mainEmotion.avgLevel);

  // 3. 최종 해설 문장 조합
  const summary = {
    intro: `이번 기간 동안 당신의 마음을 가장 많이 채운 감정은 '${mainEmotion.emotionLabel}'이었어요.`,
    level: `특히 이 감정을 평균 ${mainEmotion.avgLevel.toFixed(1)}의 강도로, ${levelInfo.text} 느끼셨네요.`,
    levelDetail: levelInfo.detail,
    specificComment: getEmotionSpecificComment(mainEmotion.emotionLabel),
    secondary: secondEmotion ? `그 뒤를 이어 '${secondEmotion.emotionLabel}' 감정도 함께 나타났어요. 당신의 다채로운 마음을 보여주는 신호일 수 있어요.` : '하나의 감정이 당신의 마음을 가득 채운 시기였네요.',
  };

  return (
    <Card className="h-100 shadow-sm">
      <Card.Body>
        <div className="d-flex align-items-center mb-3">
          <FaLightbulb size={24} className="me-3" style={{ color: 'var(--primary-color)' }} />
          <h5 className="fw-bold mb-0">감정 리포트 해석</h5>
        </div>
        <div className="ps-4 border-start border-3" style={{ borderColor: 'var(--primary-color) !important' }}>
          <p className="mb-2">{summary.intro}</p>
          <p className="mb-2">{summary.level} <span className="text-muted" style={{fontSize: '0.9rem'}}>{summary.levelDetail}</span></p>
          <p className="mb-2 fst-italic">{summary.specificComment}</p>
          <p className="mb-0 text-muted" style={{fontSize: '0.9rem'}}>{summary.secondary}</p>
        </div>
      </Card.Body>
    </Card>
  );
}

export default StatsSummary;