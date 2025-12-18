import api from './axiosConfig';

/**
 * 일기 목록을 조회합니다.
 */
export const getDiaryList = () => {
  return api.get(`/api/diary/list`);
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
  return api.post(`/api/emotion/analyze`, { diaryRecordId: diaryId });
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
      startDate,
      endDate,
      periodType,
    },
  });
};

/**
 * 카카오 인증 코드를 백엔드로 보내 로그인을 처리하고 JWT를 받아옵니다.
 * @param {string} code - 카카오로부터 받은 인증 코드
 */
export const loginWithKakao = (code) => {
  return api.post('/api/auth/kakao', { code });
};

/**
 * 현재 사용자의 취향 목록을 조회합니다.
 */
export const getUserPreferences = () => {
  return api.get(`/api/user-preference/list`);
};

/**
 * 사용자의 취향 목록을 업데이트(전체 교체)합니다.
 * @param {Array<{category: string, genres: string[]}>} preferences - 선택된 취향 객체의 배열
 */
export const updateUserPreferences = (preferences) => {
  return api.post(`/api/user-preference/save`, preferences);
};

/**
 * 사용자의 취향 목록을 ID를 기준으로 삭제합니다.
 * @param {number[]} preferenceIds - 삭제할 취향 ID의 배열
 */
export const deleteUserPreferences = (preferenceIds) => {
  return api.post(`/api/user-preference/delete`, preferenceIds);
};

/**
 * 사용자의 위치 정보를 저장합니다.
 * @param {string} location - 저장할 위치 문자열
 */
export const saveUserLocation = (location) => {
  return api.post(`/api/user-data/savelocation`, null, { params: { location } });
};

/**
 * 사용자 계정을 비활성화(탈퇴)합니다.
 */
export const deactivateUser = () => {
  return api.post(`/api/user-data/deactivate`);
};

/**
 * 일기 주제를 추천받습니다.
 * @param {string} type - 추천 타입 ('random' 또는 감정 카테고리 e.g., 'JOY').
 */
export const getDiaryTopics = (type) => {
  return api.get('/api/diary-topics', { params: { type } });
};