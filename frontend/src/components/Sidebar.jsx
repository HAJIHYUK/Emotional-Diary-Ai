import { NavLink } from 'react-router-dom';
// react-icons 라이브러리가 설치되어 있어야 아이콘이 보입니다.
// (터미널에서 npm install react-icons 실행)
import { FaBook, FaPlus, FaChartBar, FaLightbulb, FaCog } from 'react-icons/fa';

function Sidebar() {
  return (
    <div className="sidebar">
      <div className="sidebar-header">
        하루의 조각
      </div>
      <nav className="sidebar-nav">
        <NavLink to="/" end>
          <FaBook className="nav-icon" />
          내 일기장
        </NavLink>
        <NavLink to="/write">
          <FaPlus className="nav-icon" />
          새 글 쓰기
        </NavLink>
        <NavLink to="/stats">
          <FaChartBar className="nav-icon" />
          감정 통계
        </NavLink>
        <NavLink to="/recommendations">
          <FaLightbulb className="nav-icon" />
          추천
        </NavLink>
        <NavLink to="/settings">
          <FaCog className="nav-icon" />
          설정
        </NavLink>
      </nav>
    </div>
  );
}

export default Sidebar;