import { useNavigate } from 'react-router-dom';
import { assets } from '../assets/assets';

const MenuBar = () => {
  const navigate = useNavigate();

  return (
    <nav className="navbar bg-white px-5 py-4 justify-content-between align-items-center">
      <div className="d-flex align-items-center gap-2">
        <img src={assets.homeLogo} alt="HomeIcon" width={32} height={32} />
        <span className="fw-bold fs-4 text-dark">Authenticator</span>
      </div>

      <div
        className="btn btn-outline-dark rounded-pill px-3"
        onClick={() => navigate('/login')}
      >
        Login <i className="bi bi-arrow-right ms-2"></i>
      </div>
    </nav>
  );
};

export default MenuBar;
