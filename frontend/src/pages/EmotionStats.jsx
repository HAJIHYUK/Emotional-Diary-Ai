import { useState, useEffect, useMemo } from 'react';
import { Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Button, ButtonGroup, Card, Row, Col, Spinner, Alert, Form, Stack } from 'react-bootstrap';
import { getEmotionStats, getDiaryList } from '../api/diaryApi';
import { FaSearch, FaRegGrinStars, FaCommentDots } from 'react-icons/fa';

// Chart.js 필수 요소 등록
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

// --- 헬퍼 함수 ---

// 특정 연/월의 주차 정보를 계산하는 함수
const getWeeksInMonth = (year, month) => {
  const weeks = [];
  const firstDay = new Date(year, month - 1, 1);
  const lastDay = new Date(year, month, 0);
  let weekStart = new Date(firstDay);
  weekStart.setDate(weekStart.getDate() - weekStart.getDay());

  while (weekStart <= lastDay) {
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekEnd.getDate() + 6);
    weeks.push({
      week: weeks.length + 1,
      start: weekStart.toISOString().split('T')[0],
      end: weekEnd.toISOString().split('T')[0],
    });
    weekStart.setDate(weekStart.getDate() + 7);
  }
  return weeks;
};

// --- 메인 컴포넌트 ---

// 감정별 색상 고정 맵
const EMOTION_COLORS = {
  '기쁨': 'rgba(255, 206, 86, 0.7)',   // 노란색
  '슬픔': 'rgba(54, 162, 235, 0.7)',   // 파란색
  '분노': 'rgba(255, 99, 132, 0.7)',   // 빨간색
  '불안': 'rgba(153, 102, 255, 0.7)',  // 보라색
  '놀람': 'rgba(75, 192, 192, 0.7)',   // 청록색
  '사랑': 'rgba(255, 159, 64, 0.7)',   // 주황색
  '평온': 'rgba(40, 167, 69, 0.7)',    // 녹색
  '중립': 'rgba(108, 117, 125, 0.7)',  // 회색
};
const DEFAULT_COLOR = 'rgba(201, 203, 207, 0.7)'; // 기본 회색

function EmotionStats() {
  // --- 상태 관리 ---
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [periodType, setPeriodType] = useState('MONTH');
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedWeek, setSelectedWeek] = useState(1);

  const [availableDates, setAvailableDates] = useState({ years: [], months: {}, weeks: {} });

  // --- 데이터 조회 로직 ---

  useEffect(() => {
    const extractAvailableDates = async () => {
      try {
        const response = await getDiaryList();
        const diaries = response.data.data || [];
        if (diaries.length === 0) {
          setLoading(false);
          return;
        }

        const dates = { years: new Set(), months: {}, weeks: {} };
        diaries.forEach(diary => {
          const date = new Date(diary.entryDate || diary.createdAt);
          const year = date.getFullYear();
          const month = date.getMonth() + 1;

          dates.years.add(year);
          if (!dates.months[year]) dates.months[year] = new Set();
          dates.months[year].add(month);
        });

        Object.keys(dates.months).forEach(year => {
            dates.weeks[year] = {};
            dates.months[year].forEach(month => {
                dates.weeks[year][month] = getWeeksInMonth(year, month);
            });
        });

        setAvailableDates({
          years: Array.from(dates.years).sort((a, b) => b - a),
          months: Object.keys(dates.months).reduce((acc, year) => {
            acc[year] = Array.from(dates.months[year]).sort((a, b) => a - b);
            return acc;
          }, {}),
          weeks: dates.weeks,
        });

      } catch (err) {
        setError('일기 목록을 불러오는 데 실패하여 날짜 필터를 생성할 수 없습니다.');
      }
    };

    extractAvailableDates();
  }, []);

  const fetchStats = async () => {
    setLoading(true);
    setError(null);
    setStats(null);

    try {
      let startDate, endDate;
      if (periodType === 'MONTH') {
        startDate = `${selectedYear}-${String(selectedMonth).padStart(2, '0')}-01`;
        const lastDay = new Date(selectedYear, selectedMonth, 0).getDate();
        endDate = `${selectedYear}-${String(selectedMonth).padStart(2, '0')}-${lastDay}`;
      } else {
        const weekInfo = availableDates.weeks[selectedYear]?.[selectedMonth]?.find(w => w.week === selectedWeek);
        if (!weekInfo) throw new Error("선택된 주차 정보가 올바르지 않습니다.");
        startDate = weekInfo.start;
        endDate = weekInfo.end;
      }
      
      const response = await getEmotionStats(startDate, endDate, periodType);
      setStats(response.data.data);

    } catch (err) {
      setError('통계 데이터를 불러오는 데 실패했습니다. 해당 기간에 분석된 일기가 없을 수 있습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    if (availableDates.years.length > 0) {
      fetchStats();
    } else {
      setLoading(false);
    }
  }, [availableDates]);

  // --- UI에 표시될 텍스트 생성 ---
  const displayComment = useMemo(() => {
    if (!stats?.topEmotion) return "감정 분석 데이터가 부족하여 코멘트를 생성할 수 없어요.";
    
    if (periodType === 'MONTH') {
        return `${selectedYear}년 ${selectedMonth}월 동안 가장 자주 느낀 감정은 '${stats.topEmotion}'이네요!`;
    } else {
        return `${selectedYear}년 ${selectedMonth}월 ${selectedWeek}주차 동안 가장 자주 느낀 감정은 '${stats.topEmotion}'이네요!`;
    }
  }, [stats, periodType, selectedYear, selectedMonth, selectedWeek]);

  const displayPeriodLabel = useMemo(() => {
    if (periodType === 'MONTH') {
        return `${selectedYear}년 ${selectedMonth}월`;
    } else {
        return `${selectedYear}년 ${selectedMonth}월 ${selectedWeek}주차`;
    }
  }, [periodType, selectedYear, selectedMonth, selectedWeek]);


  // --- 차트 데이터 및 옵션 ---
  const chartData = useMemo(() => {
    if (!stats?.stats?.length) return { labels: [], datasets: [] };

    const labels = stats.stats.map(s => s.emotionLabel);
    const data = stats.stats.map(s => (s.totalRatio * 100).toFixed(1));
    
    // 각 감정 레이블에 맞는 색상을 매핑합니다.
    const backgroundColors = labels.map(label => EMOTION_COLORS[label] || DEFAULT_COLOR);

    return {
      labels,
      datasets: [{
        label: '감정 비율 (%)',
        data,
        backgroundColor: backgroundColors,
        borderColor: backgroundColors.map(c => c.replace('0.7', '1')),
        borderWidth: 1,
        barPercentage: 0.6,
        categoryPercentage: 0.7,
      }],
    };
  }, [stats]);

  const chartOptions = useMemo(() => ({
    responsive: true,
    maintainAspectRatio: false,
    layout: {
      // 제목과 데이터 레이블을 위한 상단 여백
      padding: {
        top: 80 
      }
    },
    scales: {
      x: {
        ticks: {
          font: {
            size: 14,
            weight: 'bold',
          },
        },
      },
      y: {
        beginAtZero: true,
        ticks: { callback: (value) => value + '%' },
      },
    },
    plugins: {
      // 기본 제목 플러그인은 비활성화하고 커스텀 플러그인으로 직접 그립니다.
      title: {
        display: false
      },
      legend: { display: false },
      tooltip: { enabled: false },
    },
  }), []); // 이제 displayPeriodLabel 의존성 필요 없음

  // --- 렌더링 ---

  // RGBA 색상을 어둡게 만들어 짙은 파스텔톤 효과를 내는 헬퍼 함수
  const darkenColor = (rgbaColor) => {
    try {
      const parts = rgbaColor.match(/rgba\((\d+), (\d+), (\d+), (\d*\.?\d+)\)/);
      if (!parts) return '#495057';
      const r = Math.max(0, parseInt(parts[1]) - 60);
      const g = Math.max(0, parseInt(parts[2]) - 60);
      const b = Math.max(0, parseInt(parts[3]) - 60);
      return `rgb(${r}, ${g}, ${b})`;
    } catch (e) {
      return '#495057';
    }
  };

  // 제목과 데이터 레이블을 모두 그리는 커스텀 플러그인
  const customDrawingPlugin = {
    id: 'customDrawingPlugin',
    // 차트가 그려지기 전에 제목을 먼저 그립니다.
    beforeDraw: (chart) => {
      const { ctx } = chart;
      const titleText = `\"${displayPeriodLabel}\" 감정 분포`;
      
      ctx.save();
      ctx.font = 'bold 16px "Noto Sans KR", sans-serif';
      ctx.fillStyle = '#495057';
      ctx.textAlign = 'center';
      // 제목을 캔버스 상단에 직접 그립니다 (Y 좌표: 20)
      ctx.fillText(titleText, chart.width / 2, 20);
      ctx.restore();
    },
    // 차트가 그려진 후에 데이터 레이블을 그립니다.
    afterDatasetsDraw: (chart) => {
      const { ctx, data } = chart;
      const allStats = stats?.stats || [];

      chart.getDatasetMeta(0).data.forEach((bar, index) => {
        const { x, y: barY } = bar;
        const stat = allStats[index];
        if (!stat) return;

        const percentage = `${parseFloat(data.datasets[0].data[index]).toFixed(1)}%`;
        const avgLevel = `(평균 감정 강도: ${stat.avgLevel.toFixed(1)})`;
        
        const textY = barY - 25;
        const barTopY = chart.scales.y.getPixelForValue(data.datasets[0].data[index]);
        const textInsideY = barTopY + 30;

        const isInside = barTopY < 80; // 제목과의 충돌을 피하기 위해 임계값 증가
        const finalY = isInside ? textInsideY : textY;
        
        const barColor = bar.options.backgroundColor;
        const textColor = darkenColor(barColor);
        const finalColor = isInside ? 'white' : textColor;

        ctx.save();
        
        ctx.font = 'bold 15px "Noto Sans KR", sans-serif';
        ctx.fillStyle = finalColor;
        ctx.textAlign = 'center';
        ctx.fillText(percentage, x, finalY);

        ctx.font = '12px "Noto Sans KR", sans-serif';
        ctx.fillStyle = finalColor;
        ctx.fillText(avgLevel, x, finalY + 18);
        
        ctx.restore();
      });
    },
  };

  return (
    <>
      <h1 className="fw-bold mb-4">나의 감정 리포트</h1>
      
      <Card className="mb-4">
        <Card.Body>
          <Stack direction="horizontal" gap={3} className="flex-wrap">
            <ButtonGroup>
              <Button variant={periodType === 'MONTH' ? 'primary' : 'outline-primary'} onClick={() => setPeriodType('MONTH')}>월별</Button>
              <Button variant={periodType === 'WEEK' ? 'primary' : 'outline-primary'} onClick={() => setPeriodType('WEEK')}>주별</Button>
            </ButtonGroup>
            
            <Form.Select style={{width: '120px'}} value={selectedYear} onChange={e => setSelectedYear(Number(e.target.value))} disabled={availableDates.years.length === 0}>
              {availableDates.years.map(y => <option key={y} value={y}>{y}년</option>)}
            </Form.Select>

            <Form.Select style={{width: '100px'}} value={selectedMonth} onChange={e => setSelectedMonth(Number(e.target.value))} disabled={!availableDates.months[selectedYear]}>
              {(availableDates.months[selectedYear] || []).map(m => <option key={m} value={m}>{m}월</option>)}
            </Form.Select>

            {periodType === 'WEEK' && (
              <Form.Select style={{width: '220px'}} value={selectedWeek} onChange={e => setSelectedWeek(Number(e.target.value))} disabled={!availableDates.weeks[selectedYear]?.[selectedMonth]}>
                {(availableDates.weeks[selectedYear]?.[selectedMonth] || []).map(w => <option key={w.week} value={w.week}>{w.week}주차 ({w.start} ~ {w.end})</option>)}
              </Form.Select>
            )}

            <Button onClick={fetchStats} disabled={loading || availableDates.years.length === 0} className="ms-auto">
              <FaSearch /> {loading ? '조회 중...' : '조회하기'}
            </Button>
          </Stack>
        </Card.Body>
      </Card>

      {loading && <div className="text-center mt-5"><Spinner animation="border" style={{ width: '3rem', height: '3rem' }} /></div>}
      
      {error && !loading && <Alert variant="warning">{error}</Alert>}

      {!loading && !error && (
        availableDates.years.length === 0 ? (
            <Alert variant="info">
                <Alert.Heading>작성된 일기 없음</Alert.Heading>
                <p>감정 통계를 확인하려면 먼저 일기를 작성해주세요.</p>
            </Alert>
        ) : stats?.stats?.length > 0 ? (
          <Row>
            <Col lg={4} className="mb-4">
              <Card className="h-100 text-center shadow-sm">
                <Card.Body>
                  <FaRegGrinStars size={40} className="mb-3" style={{color: 'var(--primary-color)'}}/>
                  <h5 className="fw-bold">가장 많이 느낀 감정</h5>
                  <p className="display-6 fw-bold" style={{color: 'var(--primary-color)'}}>{stats.topEmotion}</p>
                </Card.Body>
              </Card>
            </Col>
            <Col lg={8} className="mb-4">
              <Card className="h-100 shadow-sm">
                <Card.Body>
                  <FaCommentDots size={30} className="mb-3" style={{color: 'var(--primary-color)'}}/>
                  <blockquote className="blockquote mb-0">
                    <p className="fs-5">"{displayComment}"</p>
                    <footer className="blockquote-footer mt-2">AI의 따뜻한 한마디</footer>
                  </blockquote>
                </Card.Body>
              </Card>
            </Col>
            <Col xs={12}>
              <Card className="shadow-sm">
                <Card.Body>
                  <div style={{ height: '400px' }}>
                    <Bar options={chartOptions} data={chartData} plugins={[customDrawingPlugin]} />
                  </div>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        ) : (
          <Alert variant="info">
            <Alert.Heading>데이터 없음</Alert.Heading>
            <p>선택하신 기간에 분석된 감정 데이터가 없습니다. 다른 기간을 선택하시거나, 일기를 작성하고 감정 분석을 먼저 진행해주세요.</p>
          </Alert>
        )
      )}
    </>
  );
}

export default EmotionStats;