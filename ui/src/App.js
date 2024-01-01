import logo from './logo.svg';
import './App.css';
import UnsubscriberPage from './features/unsubscriber/UnsubscriberPage';
import { Routes, Route } from 'react-router';
import LoginPage from './features/login/LoginPage.jsx';
import axios from 'axios';
import { isAuthenticated } from './AuthContext';
import { Navigate } from 'react-router-dom';

function App() {

  return (
    <Routes>
      <Route path="/" element={<UnsubscriberPage/>}/>
      <Route path="/login" element={isAuthenticated() ? <Navigate to={"/"}/> : <LoginPage/>}/>
      <Route path="*" element={<Navigate to={"/"}/>}/>
    </Routes>
  );
}

export default App;
