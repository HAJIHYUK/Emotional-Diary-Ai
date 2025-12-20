import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ListGroup, Card, Button, Modal } from 'react-bootstrap';
import { FaPalette, FaMapMarkerAlt, FaSignOutAlt, FaUserSlash } from 'react-icons/fa';
import LocationSetup from '../components/LocationSetup';
import { deactivateUser } from '../api/diaryApi';

function Settings() {
  const navigate = useNavigate();
  const [showModal, setShowModal] = useState(false);

  const handleLocationSave = () => {
    alert('위치 정보가 업데이트되었습니다.');
  };

  const handleLogout = () => {
    localStorage.removeItem('jwt');
    navigate('/login');
    window.location.reload();
  };

  const handleShowModal = () => setShowModal(true);
  const handleCloseModal = () => setShowModal(false);

  const handleDeactivateUser = async () => {
    handleCloseModal();
    try {
      // userId를 보내지 않음
      await deactivateUser(); 
      alert('회원 탈퇴가 성공적으로 처리되었습니다. 이용해주셔서 감사합니다.');
      handleLogout();
    } catch (error) {
      console.error('회원 탈퇴 실패:', error);
      alert('회원 탈퇴 중 오류가 발생했습니다. 다시 시도해주세요.');
    }
  };

  return (
    <>
      <h1 className="fw-bold mb-4">설정</h1>
      
      <Card className="mb-4">
        <Card.Header className="fw-bold">위치 설정</Card.Header>
        <Card.Body>
          <LocationSetup 
            onSave={handleLocationSave}
            onSkip={() => {}}
            showSkipButton={false}
          />
        </Card.Body>
      </Card>

      <Card className="mb-4">
        <Card.Header className="fw-bold">개인화 설정</Card.Header>
        <ListGroup variant="flush">
          <ListGroup.Item as={Link} to="/settings/preferences" action className="p-3">
            <FaPalette className="me-3" />
            <span className="fw-bold">나의 취향 설정</span>
          </ListGroup.Item>
        </ListGroup>
      </Card>

      <Card>
        <Card.Header className="fw-bold">계정 관리</Card.Header>
        <ListGroup variant="flush">
          <ListGroup.Item action className="p-3 text-danger" onClick={handleLogout}>
            <FaSignOutAlt className="me-3" />
            <span className="fw-bold">로그아웃</span>
          </ListGroup.Item>
          <ListGroup.Item action className="p-3 text-danger" onClick={handleShowModal}>
            <FaUserSlash className="me-3" />
            <span className="fw-bold">회원 탈퇴</span>
          </ListGroup.Item>
        </ListGroup>
      </Card>

      <Modal show={showModal} onHide={handleCloseModal} centered>
        <Modal.Header closeButton>
          <Modal.Title>회원 탈퇴</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          정말로 회원 탈퇴를 하시겠습니까? <br/>
          탈퇴 시 모든 데이터는 복구할 수 없으며, 30일간 재가입이 불가능합니다.
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleCloseModal}>
            취소
          </Button>
          <Button variant="danger" onClick={handleDeactivateUser}>
            탈퇴
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
}

export default Settings;