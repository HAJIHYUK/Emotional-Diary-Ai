import { Link } from 'react-router-dom';
import { ListGroup, Card } from 'react-bootstrap';
import { FaPalette } from 'react-icons/fa';

function Settings() {
  return (
    <>
      <h1 className="fw-bold mb-4">설정</h1>
      <Card>
        <ListGroup variant="flush">
          <ListGroup.Item as={Link} to="/settings/preferences" action className="p-3">
            <FaPalette className="me-3" />
            <span className="fw-bold">나의 취향 설정</span>
          </ListGroup.Item>
          {/* 다른 설정 항목들이 추가될 수 있는 공간 */}
        </ListGroup>
      </Card>
    </>
  );
}

export default Settings;