import { useRef, useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AppContext } from '../context/AppContext.jsx';
import axios from 'axios';
import { toast } from 'react-toastify';

const EmailVerify = () => {
  const inputRef = useRef([]);
  const [loading, setLoading] = useState(false);
  const { getUserdata, backendURL } = useContext(AppContext);
  const navigate = useNavigate();

  const handleChange = (e, index) => {
    const value = e.target.value.replace(/\D/, ''); // allow only digits
    if (value) {
      inputRef.current[index].value = value;
      if (index < 3) inputRef.current[index + 1].focus();
    }
  };

  const handleKeyDown = (e, index) => {
    if (e.key === 'Backspace' && !inputRef.current[index].value && index > 0) {
      inputRef.current[index - 1].focus();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('Text').replace(/\D/g, '').slice(0, 4);
    pasted.split('').forEach((char, i) => {
      if (inputRef.current[i]) inputRef.current[i].value = char;
    });
    if (pasted.length > 0) {
      const lastIndex = Math.min(pasted.length, 4) - 1;
      inputRef.current[lastIndex].focus();
    }
  };

  const verifyEmail = async () => {
    const otp = inputRef.current.map((input) => input.value).join('');

    if (otp.length !== 4) {
      toast.error('Please enter a valid 4-digit OTP');
      return;
    }

    try {
      setLoading(true);

      const response = await axios.post(`${backendURL}/verify-otp`, { otp });

      if (response.status === 200) {
        toast.success('Email verified successfully!');
        getUserdata(); // Update user context
        navigate("/"); // or wherever needed
      } else {
        toast.error('Invalid OTP');
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Verification failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="email-verify-container d-flex align-items-center justify-content-center vh-100 pos-relative"
      style={{
        backgroundImage: 'linear-gradient(90deg, #859ba5ff, #7a878bff, #2c5364)',
        border: 'none',
      }}
    >
      <Link className="position-absolute text-decoration-none top-0 start-0 p-4 text-light d-flex align-items-center">
        <img src="./security logo.png" alt="AuthLogo" width={32} height={32} />
        <span className="fs-4 fw-semibold-text-light">Authenticator</span>
      </Link>

      <div
        className="p-5 rounded-4 shadow bg-white"
        style={{ width: '400px', border: 'none' }}
      >
        <h4 className="text-center fw-bold mb-2">Email Verification OTP</h4>
        <p className="text-center text-black-50 mb-4">
          Enter the OTP sent to your email
        </p>

        <div className="d-flex justify-content-between gap-2 mb-4 text-center">
          {[...Array(4)].map((_, index) => (
            <input
              key={index}
              type="text"
              maxLength="1"
              ref={(el) => (inputRef.current[index] = el)}
              onChange={(e) => handleChange(e, index)}
              onKeyDown={(e) => handleKeyDown(e, index)}
              onPaste={handlePaste}
              className="form-control text-center fs-4 fw-bold"
              style={{ width: '50px', height: '50px' }}
            />
          ))}
        </div>

        <button
          className="btn btn-primary w-100 fw-semibold"
          onClick={verifyEmail}
          disabled={loading}
        >
          {loading ? 'Verifying...' : 'Verify Email'}
        </button>
      </div>
    </div>
  );
};

export default EmailVerify;
