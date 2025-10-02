import api from './axiosConfig';

// 참고: userId는 '1'로 고정합니다.
const MOCK_USER_ID = 1;

/**
 * 일기 목록을 조회합니다.
 */
export const getDiaryList = () => {
  return api.get(`/api/diary/list?userId=${MOCK_USER_ID}`);
};

/**
 * 특정 일기의 상세 내용을 조회합니다.
 * @param {number} diaryId 조회할 일기 ID
 */
export const getDiaryDetail = (diaryId) => {
  return api.get(`/api/diary/detail?diaryId=${diaryId}`);
};

/**
 * 특정 일기의 추천 정보를 조회합니다.
 * @param {number} diaryId 추천을 조회할 일기 ID
 */
export const getRecommendations = (diaryId) => {
  return api.get(`/api/diary/recommendations?diaryId=${diaryId}`);
};

/**
 * 새 일기를 작성합니다.
 * @param {{content: string, weather?: string, entryDate?: string}} diaryData - 일기 데이터
 */
export const createDiary = (diaryData) => {
  const payload = {
    // TODO: 추후 로그인 기능 구현 시 실제 유저 ID로 변경해야 함
    userId: MOCK_USER_ID,
    content: diaryData.content,
    weather: diaryData.weather || null,
    entryDate: diaryData.entryDate || null,
  };
  
  return api.post('/api/diary/write', payload);
};

/**
 * 특정 일기를 수정합니다.
 * @param {number} diaryId 수정할 일기 ID
 * @param {{content: string}} diaryData - {내용}
 */
export const updateDiary = (diaryId, diaryData) => {
  const payload = {
    userId: MOCK_USER_ID, // 누락되었던 유저 ID 추가
    content: diaryData.content,
    weather: diaryData.weather || null,
    entryDate: diaryData.entryDate || null,
  };
  return api.post(`/api/diary/update?diaryId=${diaryId}`, payload);
};

/**
 * 특정 일기를 삭제합니다.
 * @param {number} diaryId 삭제할 일기 ID
 */
export const deleteDiary = (diaryId) => {
    return api.post(`/api/diary/delete?diaryId=${diaryId}`);
};

/**
 * 특정 일기의 감정 분석을 요청합니다.
 * @param {number} diaryId 분석할 일기 ID
 */
export const analyzeDiaryEmotion = (diaryId) => {
  return api.post(`/api/emotion/analyze?userId=${MOCK_USER_ID}`, { diaryRecordId: diaryId });
};

/**
 * 지정된 기간의 감정 통계를 조회합니다.
 * @param {string} startDate 시작일 (YYYY-MM-DD)
 * @param {string} endDate 종료일 (YYYY-MM-DD)
 * @param {string} periodType 기간 타입 ('WEEK' 또는 'MONTH')
 */
export const getEmotionStats = (startDate, endDate, periodType) => {
  return api.get(`/api/emotion/statistic`, {
    params: {
      userId: MOCK_USER_ID,
      startDate,
      endDate,
      periodType,
    },
  });
};
