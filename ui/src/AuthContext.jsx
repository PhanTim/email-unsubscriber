import Cookies from 'universal-cookie';
import { useState, useEffect, createContext } from 'react';
import LoginPage from './features/login/LoginPage';
import { Navigate } from 'react-router-dom';

const AuthContext = createContext();

export const isAuthenticated = () => {
    const token = cookies.get('token');
    if(token === null){
        return {}
    }
    return token;
}

export const cookies = new Cookies(null, {path: '/'});

export const AuthProvider = ({children}) => {
    const currentUser = isAuthenticated();
    return (
        <AuthContext.Provider value={[currentUser]}>
            { currentUser ? children : <><Navigate replace to="/login"/>{children}</>}
        </AuthContext.Provider>
    )
}