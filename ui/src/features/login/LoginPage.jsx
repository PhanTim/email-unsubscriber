import React from 'react';
import {Button} from '@mui/material';
import axios from 'axios';
import { useGoogleLogin } from '@react-oauth/google';
import { cookies } from '../../AuthContext.jsx';

const LoginPage = ({}) =>
{
    const googleLogin = useGoogleLogin({
        scope: 'https://mail.google.com/ https://www.googleapis.com/auth/userinfo.email',
        flow: 'auth-code', 
        redirect_uri: "http://localhost:3000/",
        onSuccess: async codeResponse => {
            try{
            const tokens = await axios.post('http://localhost:8080/auth', {
                code: codeResponse.code
            });
            cookies.set(tokens.data.name, tokens.data.value);
            }
            catch (error) {
                console.error(error.response.data);
            }
            window.location.reload();
        },
        onError: errorResponse => console.log(errorResponse),
    });
    
    return(
        <Button onClick={() => googleLogin()}>Sign in with Google</Button>
    )
}

export default LoginPage;