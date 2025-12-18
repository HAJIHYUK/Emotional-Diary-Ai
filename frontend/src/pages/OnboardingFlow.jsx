import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import LocationSetup from '../components/LocationSetup';
import PreferenceSetup from '../components/PreferenceSetup';

function OnboardingFlow() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1); // 1: 위치 설정, 2: 취향 설정

  // 위치 설정 완료 시 호출될 함수
  const handleLocationComplete = () => {
    setStep(2); // 다음 단계(취향 설정)로 이동
  };

  // 취향 설정 완료 시 호출될 함수
  const handlePreferencesComplete = () => {
    navigate('/'); // 모든 설정이 끝났으므로 메인 페이지로 이동
    window.location.reload(); // 페이지를 새로고침하여 로그인 상태를 완전히 적용
  };

  return (
    <div className="onboarding-container"> {/* [수정] className 추가 */}
      {step === 1 && (
        <LocationSetup 
          onSave={handleLocationComplete}
          onSkip={handleLocationComplete} // onSkip은 제거되었지만 호환성을 위해 남겨둠
          showSkipButton={false} // 명시적으로 false 전달
        />
      )}
      {step === 2 && (
        <PreferenceSetup 
          isEditMode={false}
          onSave={handlePreferencesComplete}
        />
      )}
    </div>
  );
}

export default OnboardingFlow;
