
import './App.css'
import { Routes, Route } from 'react-router-dom'
import { ToastContainer } from 'react-toastify'
import Home from './pages/Home'
import Login from './pages/Login'
import ResetPassword from './pages/Resetpassword'
import EmailVerify from './pages/EmailVerify'

const App = () => {
    return (
        <div>
            <ToastContainer/>
           <Routes>
  <Route path="/" element={<Home />} />
  <Route path="/login" element={<Login />} />
  <Route path="/reset-password" element={<ResetPassword />} />
  <Route path="/email-verify" element={<EmailVerify />} />
</Routes>

        </div>
    )
}

export default App
