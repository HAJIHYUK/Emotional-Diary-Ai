import React from 'react';
import { useNavigate } from 'react-router-dom';
import PreferenceSetup from '../components/PreferenceSetup';

function UserPreferences() {
  const navigate = useNavigate();

  return (
    <PreferenceSetup 
      isEditMode={true} // 기존 설정을 수정하는 것이므로 true
      onSave={() => navigate('/settings')} // 저장 후 설정 페이지로 이동
      showCancel={true} // 취소 버튼 표시
      onCancel={() => navigate('/settings')} // 취소 시 설정 페이지로 이동
    />
  );
}

export default UserPreferences;