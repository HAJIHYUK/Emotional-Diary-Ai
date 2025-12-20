import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Card, Button, Spinner, Alert, Row, Col, ListGroup, Badge, Modal } from 'react-bootstrap';
import { getDiaryDetail, getRecommendations, deleteDiary, analyzeDiaryEmotion, saveUserClickEvent } from '../api/diaryApi';
import { FaRegCalendarAlt, FaCloudSun, FaHeart, FaMusic, FaFilm, FaBook, FaTrash, FaArrowLeft, FaMagic, FaBrain, FaPencilAlt, FaLightbulb, FaYoutube, FaInstagram, FaMapMarkedAlt, FaBloggerB, FaRegNewspaper, FaLink, FaInfoCircle } from 'react-icons/fa';

// --- Helper & Presentational Components ---

const weatherMap = {
  '맑음': '맑음 ☀️',
  '흐림': '흐림 ☁️',
  '비': '비 🌧️',
  '눈': '눈 ❄️',
  '바람': '바람 💨',
  '안개': '안개 🌫️',
  '천둥/번개': '천둥/번개 ⚡',
};

const EmotionDisplay = ({ emotions }) => {
  const emotionStyle = {
    '기쁨': { bg: '#FFFDE7', text: '#FBC02D', border: '#FBC02D' },
    '슬픔': { bg: '#E0F7FA', text: '#0097A7', border: '#0097A7' },
    '분노': { bg: '#FFEBEE', text: '#D32F2F', border: '#D32F2F' },
    '불안': { bg: '#F3E5F5', text: '#7B1FA2', border: '#7B1FA2' },
    '사랑': { bg: '#FCE4EC', text: '#D81B60', border: '#D81B60' },
    '평온': { bg: '#E8F5E9', text: '#388E3C', border: '#388E3C' },
    '중립': { bg: '#ECEFF1', text: '#546E7A', border: '#546E7A' },
    '기본': { bg: '#ECEFF1', text: '#546E7A', border: '#546E7A' },
  };
  const emojiMap = { '기쁨': '😊', '슬픔': '😢', '분노': '😡', '불안': '😟', '사랑': '🥰', '평온': '😌', '중립': '😐', '기본': '🤔' };

  return (
    <Row className="mt-4 g-3">
      {(emotions || []).map((e, index) => {
        const style = emotionStyle[e.label] || emotionStyle['기본'];
        return (
          <Col key={index} md={6}>
            <div 
              className="emotion-report-item p-2" 
              style={{ 
                backgroundColor: style.bg, 
                borderLeft: `4px solid ${style.border}`, 
                borderRadius: '8px' 
              }}
            >
              <div className="d-flex justify-content-between align-items-center">
                <span className="fw-bold" style={{ color: style.text }}>
                  {emojiMap[e.label] || emojiMap['기본']} {e.label}
                </span>
                <span className="fw-bold fs-5" style={{ color: style.text }}>
                  {(e.ratio * 100).toFixed(0)}%
                </span>
              </div>
              <p className="text-muted mt-1 mb-0 small">
                <FaInfoCircle className="me-1" />
                {e.description || '감정 설명이 없습니다.'}
              </p>
            </div>
          </Col>
        );
      })}
    </Row>
  );
};

const RecommendationIcon = ({ linkType }) => {
  const iconMap = {
    YOUTUBE: <FaYoutube style={{ color: '#FF0000' }} />,
    INSTAGRAM: <FaInstagram style={{ color: '#E4405F' }} />,
    NAVER_PLACE: <FaMapMarkedAlt style={{ color: '#03C75A' }} />,
    NAVER_BLOG: <FaBloggerB style={{ color: '#03C75A' }} />,
    ARTICLE: <FaRegNewspaper style={{ color: '#6c757d' }} />,
    GENERIC: <FaLink style={{ color: '#6c757d' }} />,
  };
  return iconMap[linkType] || <FaLink style={{ color: '#6c757d' }} />;
};

const PageHeader = ({ onAnalyze, isAnalyzing, emotionAnalysisCount, onDeleteClick, onEditClick }) => (
  <Row className="align-items-center mb-4">
    <Col><h1 className="fw-bold">상세 보기</h1></Col>
    <Col xs="auto" className="d-flex gap-2">
      {emotionAnalysisCount === 0 && (
        <Button variant="primary" onClick={onAnalyze} disabled={isAnalyzing}>
          {isAnalyzing ? <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true"/> : <FaMagic className="me-2"/>}
          {isAnalyzing ? ' 분석 중...' : ' AI 분석하기'}
        </Button>
      )}
      <Button variant="secondary" onClick={onEditClick}><FaPencilAlt className="me-2"/>수정</Button>
      <Button as={Link} to="/" variant="light"><FaArrowLeft className="me-2"/>목록으로</Button>
      <Button variant="danger" size="sm" onClick={onDeleteClick}><FaTrash/></Button>
    </Col>
  </Row>
);

const DiaryContentCard = ({ diary }) => (
  <Card className="mb-4">
    <Card.Body className="p-4">
      <div className="d-flex justify-content-start gap-4 text-muted mb-4 pb-3 border-bottom">
        <span><FaRegCalendarAlt className="me-2"/>{new Date(diary.entryDate || diary.createdAt).toLocaleDateString('ko-KR')}</span>
        <span><FaCloudSun className="me-2"/>{weatherMap[diary.weather] || diary.weather || '날씨 기록 없음'}</span>
      </div>
      <Card.Text style={{ whiteSpace: 'pre-wrap', fontSize: '1.1rem', lineHeight: '1.9' }}>
        {diary.content}
      </Card.Text>
    </Card.Body>
  </Card>
);

const AnalysisCard = ({ isAnalyzing, diary }) => (
  <Card className="analysis-card">
    <Card.Body>
      <Card.Title as="h5" className="fw-bold mb-4 d-flex align-items-center">
        <FaBrain className="me-2" style={{ color: 'var(--primary-color)' }}/> AI의 마음 분석 리포트
      </Card.Title>
      {isAnalyzing ? (
        <div className="text-center p-5">
          <Spinner animation="border" style={{color: 'var(--primary-color)'}}/>
          <p className="mt-3 text-muted">AI가 일기를 읽고 감정을 분석하고 있어요...</p>
        </div>
      ) : diary.emotionAnalysisCount > 0 ? (
        <div>
          <blockquote className="ai-comment-quote">
            <p className="mb-0">{diary.aiComment}</p>
          </blockquote>
          <EmotionDisplay emotions={diary.emotions} />
        </div>
      ) : (
        <div className="text-center p-5 text-muted">
          <FaHeart size={40} className="mb-3" style={{color: 'var(--border-color)'}}/>
          <p>아직 감정 분석이 완료되지 않았어요.<br/>상단의 'AI 분석하기' 버튼을 눌러 감정을 확인해보세요!</p>
        </div>
      )}
    </Card.Body>
  </Card>
);

const RecommendationsCard = ({ recommendations, onRecommendationClick }) => {
      const getYouTubeVideoId = (url) => {
        if (!url) return null;
        try {
          const urlObj = new URL(url);
          if (urlObj.hostname === 'youtu.be') {
            return urlObj.pathname.slice(1);
          }
          if (urlObj.hostname.includes('youtube.com')) {
            return urlObj.searchParams.get('v');
          }
          return null;
        } catch (error) {
          console.error("Invalid URL for YouTube parsing:", error);
          return null;
        }
      };

      // 유튜브 오버레이 클릭 핸들러
      const handleOverlayClick = (e, rec, videoId) => {
        // 1. 서버로 클릭 이벤트 전송
        onRecommendationClick(rec);
        
        // 2. 오버레이 숨김
        e.currentTarget.style.display = 'none';
        
        // 3. iframe 자동 재생 트리거 (src에 autoplay=1 추가)
        const iframe = e.currentTarget.nextElementSibling;
        if (iframe) {
          iframe.src = `https://www.youtube.com/embed/${videoId}?autoplay=1`;
        }
      };

      return (
        <Card style={{ minHeight: '200px' }}>
          <Card.Header className="fw-bold">AI 추천 콘텐츠</Card.Header>
          <ListGroup variant="flush">
            {recommendations.length > 0 ? recommendations.map(rec => {
              const videoId = getYouTubeVideoId(rec.link);
              return (
                <ListGroup.Item key={rec.recommendationId} className="recommendation-item">
                  <a 
                    href={rec.link} 
                    target="_blank" 
                    rel="noopener noreferrer" 
                    className="recommendation-link-area"
                    onClick={() => onRecommendationClick(rec)}
                  >
                    <span style={{ fontSize: '1.5rem' }}><RecommendationIcon linkType={rec.linkType} /></span>
                    <div className="recommendation-text">
                      <strong className="d-block">{rec.title}</strong>
                      <small className="text-muted">{rec.reason}</small>
                    </div>
                  </a>
                  {rec.linkType === 'YOUTUBE' && videoId && (
                    <div className="video-preview-container mt-3" style={{ position: 'relative', width: '100%', maxWidth: '480px', aspectRatio: '16/9' }}>
                       {/* 유튜브 재생 클릭 감지를 위한 투명 오버레이 */}
                       <div 
                          className="youtube-click-overlay"
                          style={{
                            position: 'absolute',
                            top: 0,
                            left: 0,
                            width: '100%',
                            height: '100%',
                            zIndex: 10,
                            cursor: 'pointer',
                          }}
                          onClick={(e) => handleOverlayClick(e, rec, videoId)}
                        />
                      <iframe
                        src={`https://www.youtube.com/embed/${videoId}`}
                        frameBorder="0"
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                        allowFullScreen
                        title={rec.title}
                        style={{ 
                          position: 'absolute', 
                          top: 0, 
                          left: 0, 
                          width: '100%', 
                          height: '100%', 
                          zIndex: 1 
                        }}
                      ></iframe>
                    </div>
                  )}
                </ListGroup.Item>
              );
            }) : (
              <ListGroup.Item className="text-muted p-4 text-center d-flex flex-column justify-content-center align-items-center" style={{ minHeight: '100px' }}>
                <FaLightbulb size={25} className="mb-2" style={{ color: 'var(--border-color)' }} />
                <span>감정 분석을 완료하면 맞춤 콘텐츠를 추천해드려요.</span>
              </ListGroup.Item>
            )}
          </ListGroup>
        </Card>
      );
    };

const DeleteConfirmationModal = ({ show, onHide, onConfirm }) => (
  <Modal show={show} onHide={onHide} centered>
    <Modal.Header closeButton><Modal.Title>삭제 확인</Modal.Title></Modal.Header>
    <Modal.Body>이 일기를 정말 삭제하시겠습니까?</Modal.Body>
    <Modal.Footer>
      <Button variant="light" onClick={onHide}>취소</Button>
      <Button variant="danger" onClick={onConfirm}>삭제</Button>
    </Modal.Footer>
  </Modal>
);


// --- Main Component ---

function DiaryDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [diary, setDiary] = useState(null);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [error, setError] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  
  // 클릭 중복 방지를 위한 로컬 상태 (잠깐 동안 클릭 막기)
  const [clickedRecommendations, setClickedRecommendations] = useState(new Set());

  const pollingRef = useRef(null);
  const timeoutRef = useRef(null);

  const stopPolling = useCallback(() => {
    if (pollingRef.current) {
      clearInterval(pollingRef.current);
      pollingRef.current = null;
    }
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }
  }, []);

  const handleRecommendationClick = async (rec) => {
    // 1. 이미 클릭 목록에 있으면 무시 (간단한 디바운스)
    if (clickedRecommendations.has(rec.recommendationId)) {
      return;
    }

    // 2. 클릭 목록에 추가
    setClickedRecommendations(prev => new Set(prev).add(rec.recommendationId));

    try {
      // 3. 서버로 이벤트 전송
      await saveUserClickEvent({
        recommendationId: rec.recommendationId,
        type: rec.type, // 대분류 (MOVIE, MUSIC 등)
        title: rec.title,
        genre: rec.genre // 소분류 (ACTION, BALLAD 등)
      });
      console.log('클릭 이벤트 전송 성공:', rec.title);
    } catch (err) {
      console.error('클릭 이벤트 전송 실패:', err);
      // 실패 시 다시 클릭할 수 있게 제거할 수도 있지만, 
      // 사용자 경험상 그냥 두는 게 나을 수 있음 (이미 이동했거나 재생 중이니까)
    }
  };

  const fetchRecommendations = useCallback(async () => {
    try {
      const recommendRes = await getRecommendations(id);
      setRecommendations(recommendRes.data.data || []);
    } catch (err) {
      console.error('추천 정보를 불러오는 데 실패했습니다.', err);
    }
  }, [id]);

  const fetchDiary = useCallback(async () => {
    try {
      const diaryRes = await getDiaryDetail(id);
      const updatedDiary = diaryRes.data.data;
      setDiary(updatedDiary);
      
      if (updatedDiary.emotionAnalysisCount > 0) {
        stopPolling();
        setIsAnalyzing(false);
        fetchRecommendations();
      }
      return updatedDiary;
    } catch (err) {
      setError('일기 정보를 불러오는 데 실패했습니다.');
      return null;
    }
  }, [id, stopPolling, fetchRecommendations]);

  useEffect(() => {
    const fetchDetails = async () => {
      setLoading(true);
      const fetchedDiary = await fetchDiary();
      if (fetchedDiary && fetchedDiary.emotionAnalysisCount > 0) {
        await fetchRecommendations();
      }
      setLoading(false);
    };
    fetchDetails();
    
    return () => stopPolling();
  }, [id, fetchDiary, fetchRecommendations, stopPolling]);

  const handleAnalyze = async () => {
    setIsAnalyzing(true);
    setError(null);

    try {
      await analyzeDiaryEmotion(id);
    } catch (err) {
      console.error("초기 분석 요청은 실패할 수 있습니다 (정상 동작):", err);
    }

    stopPolling();
    pollingRef.current = setInterval(fetchDiary, 2000);

    timeoutRef.current = setTimeout(() => {
      if (pollingRef.current) {
        stopPolling();
        if (diary && diary.emotionAnalysisCount === 0) {
          setError('분석 시간이 너무 오래 걸립니다. 잠시 후 다시 시도해주세요.');
          setIsAnalyzing(false);
        }
      }
    }, 120000); // 120초 (2분) 타임아웃으로 변경
  };

  const handleDelete = async () => {
    try {
      await deleteDiary(id);
      navigate('/');
    } catch (err) {
      setError('일기 삭제에 실패했습니다.');
      setShowDeleteModal(false);
    }
  };

  if (loading) {
    return <div className="text-center mt-5"><Spinner animation="border" style={{ width: '3rem', height: '3rem', color: 'var(--primary-color)' }} /></div>;
  }

  if (error && !diary) {
    return <Alert variant="danger">{error}</Alert>;
  }

  return (
    <>
      {error && <Alert variant="danger" onClose={() => setError(null)} dismissible>{error}</Alert>}
      
      {diary && (
        <>
          <PageHeader 
            onAnalyze={handleAnalyze} 
            isAnalyzing={isAnalyzing} 
            emotionAnalysisCount={diary.emotionAnalysisCount} 
            onDeleteClick={() => setShowDeleteModal(true)}
            onEditClick={() => navigate(`/edit/${id}`, { state: { diary } })}
          />
          <DiaryContentCard diary={diary} />
          <Row>
            <Col md={12} className="mb-4">
              <AnalysisCard isAnalyzing={isAnalyzing} diary={diary} />
            </Col>
            <Col md={12} className="mb-4">
              <RecommendationsCard 
                recommendations={recommendations} 
                onRecommendationClick={handleRecommendationClick} 
              />
            </Col>
          </Row>
        </>
      )}

      <DeleteConfirmationModal 
        show={showDeleteModal} 
        onHide={() => setShowDeleteModal(false)} 
        onConfirm={handleDelete}
      />
    </>
  );
}

export default DiaryDetail;
