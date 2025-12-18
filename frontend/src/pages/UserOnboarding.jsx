import React from 'react';
import { useNavigate } from 'react-router-dom';
import PreferenceSetup from '../components/PreferenceSetup';

function UserOnboarding() {
  const navigate = useNavigate();

  return (
    <PreferenceSetup 
      isEditMode={false} // 최초 설정이므로 false
      onSave={() => navigate('/')} // 저장 후 메인 페이지로 이동
    />
  );
}

export default UserOnboarding;
