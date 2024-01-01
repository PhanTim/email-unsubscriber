import React from 'react';
import { AppBar, Toolbar, Button, Typography, Box} from '@mui/material';
import logo from './favicon.ico';
import LogoutButton from './features/login/LogoutButton';
import { isAuthenticated } from './AuthContext';

const Layout = ({children}) => {
    return (
        
        <>
        { isAuthenticated() ? 
        <>
            <Box sx={{ flexGrow: 1}}>
                <AppBar position='static'>
                    <Toolbar sx={ {bgcolor:"#757575", justifyContent: 'space-between'}}>
                        <Button color ="inherit" edge="start" ><img src={logo}/></Button>
                        <LogoutButton/>
                    </Toolbar>
                </AppBar>
            </Box>
        </>
        : null }

        <main>{children}</main>
        </>
        
    )
}

export default Layout; 