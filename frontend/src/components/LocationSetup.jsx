import React, { useState } from 'react';
import { Container, Form, Button, Alert, Spinner, InputGroup } from 'react-bootstrap';
import { FaMapMarkerAlt, FaSave, FaSearch } from 'react-icons/fa';
import { saveUserLocation } from '../api/diaryApi';

function LocationSetup({ onSave, onSkip, showSkipButton = true }) {
  const [displayAddress, setDisplayAddress] = useState(''); // 화면 표시용 주소
  const [areaAddress, setAreaAddress] = useState(''); // [수정] 백엔드 전송용 '지역' 주소
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSearchAddress = () => {
    new window.daum.Postcode({
      oncomplete: function(data) {
        // [수정] 화면 표시용 주소와 백엔드 전송용 '동' 이름을 분리하여 저장
        const selectedAddress = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
        setDisplayAddress(selectedAddress); // 화면에는 전체 주소 표시

        // [수정] '시/도' + '시/군/구' + '법정동' 조합으로 주소 생성
        let addressForBackend = data.sido;
        if (data.sigungu) {
          addressForBackend += ' ' + data.sigungu;
        }
        if (data.bname) {
          addressForBackend += ' ' + data.bname;
        }
        setAreaAddress(addressForBackend); // 최종 조합된 주소를 상태에 저장

        setError(null);
      },
    }).open();
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setError(null);

    if (!areaAddress.trim()) { // [수정] '지역' 주소가 있는지 확인
      setError('주소 검색을 통해 위치를 설정해주세요.');
      return;
    }

    setLoading(true);
    try {
      await saveUserLocation(areaAddress.trim()); // [수정] 최종 조합된 주소를 백엔드로 전송
      alert('위치 정보가 성공적으로 저장되었습니다!');
      if (onSave) onSave();
    } catch (err) {
      console.error('위치 저장 실패:', err);
      setError('위치 정보를 저장하는 데 실패했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container className="my-5" style={{ maxWidth: '600px' }}>
      <div className="text-center mb-4">
        <FaMapMarkerAlt size={50} className="text-primary mb-3" />
        <h1 className="fw-bold mb-2">어디에 계신가요?</h1>
        <p className="text-muted mb-3">
          <strong className="text-primary">더욱 정확한 추천</strong>을 위해 현재 계신 위치를 알려주세요.
        </p>
        <p className="text-muted mb-4">
          <span className="fw-bold">동네 이름</span>(예: '역삼동', '서현동')만으로도 충분해요! <span className="text-success">상세 주소는 필요하지 않으니 안심하세요.</span>
        </p>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      <Form onSubmit={handleSave} className="p-4 border rounded-3 bg-white shadow-sm">
        <Form.Group className="mb-4">
          <Form.Label className="fw-bold">현재 위치</Form.Label>
          <InputGroup>
            <Form.Control
              type="text"
              placeholder="오른쪽 '주소 검색' 버튼을 눌러주세요."
              value={displayAddress} // [수정] 화면에는 전체 주소 표시
              readOnly
              disabled={loading}
            />
            <Button 
              variant="secondary" 
              onClick={handleSearchAddress} 
              disabled={loading}
            >
              <FaSearch className="me-2"/> 주소 검색
            </Button>
          </InputGroup>
        </Form.Group>

        <div className="d-grid gap-2">
          <Button variant="primary" type="submit" disabled={loading} size="lg">
            {loading ? <Spinner as="span" animation="border" size="sm" /> : <><FaSave className="me-2"/> 위치 저장</>}
          </Button>
        </div>
      </Form>
    </Container>
  );
}

export default LocationSetup;