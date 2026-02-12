import { useNavigate } from 'react-router-dom';
import { useContext, useEffect, useRef, useState } from 'react';
import { assets } from '../assets/assets';
import { AppContext } from '../context/AppContext.jsx';
import React from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';

const MenuBar = () => {
  const navigate = useNavigate();
  const { userData, setUserData, setIsLoggedIn, backendURL, userRole, setUserRole } = useContext(AppContext);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);
  const [isVerified, setIsVerified] = useState(userData?.isEmailVerified);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Fetch latest user data on mount or when dropdown opens
  useEffect(() => {
    if (dropdownOpen) {
      axios
        .get(`${backendURL}/get-user`, { withCredentials: true })
        .then((res) => {
          if (res.data?.user) {
            setUserData(res.data.user);
            setIsVerified(res.data.user.isEmailVerified);
          }
        })
        .catch((err) => {
          console.log(err);
        });
    }
  }, [dropdownOpen]);

  const handleLogout = () => {
    try {
      axios.defaults.withCredentials = true;
      axios.post(`${backendURL}/logout`).then((response) => {
        if (response.status === 200) {
          setUserData(false);
          setIsLoggedIn(false);
          setUserRole(null);
          navigate('/');
        }
      });
    } catch (error) {
      console.error(error);
      toast.error(error.message);
    }
  };

  const handleVerifyClick = async () => {
    try {
      const res = await axios.post(`${backendURL}/send-Otp`, {}, { withCredentials: true });
      if (res.status === 200) {
        toast.success('OTP sent to your email');
        navigate('/email-verify');
      }
    } catch (error) {
      toast.error('Failed to send OTP');
    }
  };

  return (
    <nav className="navbar bg-white px-5 py-4 justify-content-between align-items-center">
      <div className="d-flex align-items-center gap-2">
        <img src={assets.homeLogo} alt="HomeIcon" width={32} height={32} />
        <span className="fw-bold fs-4 text-dark">Authenticator</span>
      </div>

      {userData ? (
        <div className="position-relative" ref={dropdownRef}>
          <div
            className="bg-dark text-white rounded-circle d-flex justify-content-center align-items-center"
            style={{
              width: '32px',
              height: '32px',
              cursor: 'pointer',
              userSelect: 'none'
            }}
            onClick={() => setDropdownOpen(!dropdownOpen)}
          >
            <span className="fw-bold fs-5">{userData?.name.charAt(0).toUpperCase()}</span>
          </div>

          {/* Dropdown */}
          <div
            className={`dropdown-menu ${dropdownOpen ? 'show' : ''}`}
            style={{
              position: 'absolute',
              top: '100%',
              right: '0',
              minWidth: '140px',
              marginTop: '0.5rem',
              borderRadius: '0.25rem',
              boxShadow: '0 2px 5px rgba(0, 0, 0, 0.1)',
              zIndex: 1000,
              display: dropdownOpen ? 'block' : 'none',
              padding: '0.5rem'
            }}
          >
            {userRole && (
              <div className="dropdown-item text-muted d-flex align-items-center" style={{ fontSize: '0.8rem', pointerEvents: 'none' }}>
                <i className="bi bi-shield-lock me-2"></i>
                {userRole.replace('ROLE_', '')}
              </div>
            )}
            <hr className="dropdown-divider my-1" />
            {!isVerified ? (
              <div className="dropdown-item" style={{ cursor: 'pointer' }} onClick={handleVerifyClick}>
                Verify Email
              </div>
            ) : (
              <div className="dropdown-item text-success d-flex justify-content-between align-items-center">
                Verified <i className="bi bi-patch-check-fill ms-2"></i>
              </div>
            )}
            <div
              className="dropdown-item text-danger"
              onClick={handleLogout}
              style={{ cursor: 'pointer' }}
            >
              Logout
            </div>
          </div>
        </div>
      ) : (
        <div
          className="btn btn-outline-dark rounded-pill px-3"
          onClick={() => navigate('/login')}
        >
          Login <i className="bi bi-arrow-right ms-2"></i>
        </div>
      )}
    </nav>
  );
};

export default MenuBar;
