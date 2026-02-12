import { assets } from './../assets/assets';
import { Link, useNavigate } from 'react-router-dom';
import { useContext, useState } from 'react';
import axios from 'axios';
import { AppContext } from '../context/AppContext.jsx';
import { toast } from 'react-toastify';

const Login = () => {
  const [isCreateAccount, setIsCreateAccount] = useState(true);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { backendURL, getUserdata, setUserRole } = useContext(AppContext);
  const navigate = useNavigate();

  const onSubmitHandler = async (e) => {
    e.preventDefault();
    setLoading(true);

    // Client-side validation
    if (isCreateAccount && !name.trim()) {
      toast.error('Name is required');
      setLoading(false);
      return;
    }
    if (!email.trim() || !password.trim()) {
      toast.error('Email and password are required');
      setLoading(false);
      return;
    }

    try {
      axios.defaults.withCredentials = true;
      if (isCreateAccount) {
        const response = await axios.post(`${backendURL}/profile/register`, {
          name,
          email,
          password,
        });
        if (response.status === 200 || response.status === 201) {
          toast.success('Registration successful! Please log in.');
          setName('');
          setEmail('');
          setPassword('');
          setIsCreateAccount(false); // Switch to login form after successful signup
        }
        else {
          toast.error('Email already exists..');
        }
      } else {
        const response = await axios.post(`${backendURL}/login`, {
          email,
          password,
        });
        if (response.status === 200) {
          if (response.data.role) {
            setUserRole(response.data.role);
          }
          toast.success('Login successful');
          getUserdata();
          navigate('/'); // Redirect to main page
        }
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'An error occurred. Please try again.';
      // Check if the error indicates the email already exists
      if (isCreateAccount && errorMessage.toLowerCase().includes('email already exists')) {
        toast.error('Email already exists');
      } else {
        toast.error(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="position-relative min-vh-100 d-flex flex-column"
      style={{
        background: 'linear-gradient(90deg, #9ec9d4ff 0%, #6a776cff 100%)',
        border: 'none',
      }}
    >
      {/* Logo at top-left */}
      <div
        style={{
          position: 'absolute',
          top: '20px',
          left: '20px',
          display: 'flex',
          alignItems: 'center',
        }}
      >
        <Link
          to="/"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            padding: '10px 20px',
            borderRadius: '10px',
            border: 'none',
            fontSize: '1.2rem',
            color: 'white',
            background: 'transparent',
            textDecoration: 'none',
          }}
        >
          <img src={assets.AuthLogo} alt="AuthLogo" width={32} height={32} />
          <span className="fw-bold fs-4 text-light">Authenticator</span>
        </Link>
      </div>

      {/* Centered Login Form */}
      <div className="d-flex justify-content-center align-items-center flex-grow-1">
        <div className="card p-4 shadow" style={{ maxWidth: '400px', width: '100%' }}>
          <h2 className="text-center mb-4">{isCreateAccount ? 'Create Account' : 'Login'}</h2>
          <form onSubmit={onSubmitHandler}>
            {isCreateAccount && (
              <div className="mb-3">
                <label htmlFor="name" className="form-label">
                  Name
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="name"
                  placeholder="Enter your name"
                  required
                  onChange={(e) => setName(e.target.value)}
                  value={name}
                />
              </div>
            )}
            <div className="mb-3">
              <label htmlFor="email" className="form-label">
                Email
              </label>
              <input
                type="email"
                className="form-control"
                id="email"
                placeholder="Enter your email"
                required
                onChange={(e) => setEmail(e.target.value)}
                value={email}
              />
            </div>
            <div className="mb-1">
              <label htmlFor="password" className="form-label">
                Password
              </label>
              <input
                type="password"
                className="form-control"
                id="password"
                placeholder="Enter your password"
                required
                onChange={(e) => setPassword(e.target.value)}
                value={password}
              />
            </div>

            {/* Forgot Password Link */}
            <div className="mb-3 text-end">
              {!isCreateAccount && (
                <Link to="/reset-password" className="text-decoration-none">
                  Forgot Password?
                </Link>
              )}
            </div>

            <button type="submit" className="btn btn-primary w-100" disabled={loading}>
              {loading ? 'Processing...' : isCreateAccount ? 'Register Account' : 'Login'}
            </button>
          </form>
          <div>
            <p className="text-center mt-3">
              {isCreateAccount ? 'Already have an account? ' : "Don't have an account? "}
              <span
                onClick={() => setIsCreateAccount(!isCreateAccount)}
                style={{ cursor: 'pointer', color: '#0d6efd', textDecoration: 'underline' }}
              >
                {isCreateAccount ? 'Login' : 'Register'}
              </span>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;