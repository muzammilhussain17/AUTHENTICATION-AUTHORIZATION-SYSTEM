import React, { useState } from 'react';
import { Card, Button, Form } from 'react-bootstrap';

const ResetPassword = () => {
  const [step, setStep] = useState(1); // Tracks current step
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const handleEmailSubmit = () => {
    // TODO: Add API call to send OTP
    console.log('Sending OTP to:', email);
    setStep(2);
  };

  const handleOtpSubmit = () => {
    // TODO: Add API call to verify OTP
    console.log('Verifying OTP:', otp);
    setStep(3);
  };

  const handlePasswordReset = () => {
    // TODO: Add API call to reset password
    console.log('Resetting password:', newPassword);
    alert('Password reset successful!');
  };

  return (
    <div className="d-flex justify-content-center align-items-center vh-100 bg-light">
      {step === 1 && (
        <Card className="p-4" style={{ width: '22rem' }}>
          <Card.Title className="text-center mb-3">Enter Your Email</Card.Title>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label>Email Address</Form.Label>
              <Form.Control
                type="email"
                placeholder="Enter your registered email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </Form.Group>
            <Button variant="primary" className="w-100" onClick={handleEmailSubmit}>
              Send OTP
            </Button>
          </Form>
        </Card>
      )}

      {step === 2 && (
        <Card className="p-4" style={{ width: '22rem' }}>
          <Card.Title className="text-center mb-3">Verify OTP</Card.Title>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label>Enter 4-digit OTP</Form.Label>
              <Form.Control
                type="text"
                maxLength="4"
                value={otp}
                onChange={(e) => {
                  const val = e.target.value;
                  if (/^\d*$/.test(val)) setOtp(val); // only digits
                }}
                placeholder="e.g. 1234"
              />
            </Form.Group>
            <Button variant="primary" className="w-100" onClick={handleOtpSubmit}>
              Verify OTP
            </Button>
          </Form>
        </Card>
      )}

      {step === 3 && (
        <Card className="p-4" style={{ width: '22rem' }}>
          <Card.Title className="text-center mb-3">Reset Password</Card.Title>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label>New Password</Form.Label>
              <Form.Control
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="Enter new password"
              />
            </Form.Group>
            <Button variant="success" className="w-100" onClick={handlePasswordReset}>
              Reset Password
            </Button>
          </Form>
        </Card>
      )}
    </div>
  );
};

export default ResetPassword;
