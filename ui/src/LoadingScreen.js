import CircularProgress from '@mui/material/CircularProgress';
import Box from '@mui/material/Box';

const LoadingScreen = () => {
    return(
        <>
        <Box sx ={{ p: 2, display: 'flex', justifyContent: 'center', alignItems:'center'}}>
            <CircularProgress/>
        </Box>
        <Box sx ={{ display: 'flex', justifyContent: 'center', alignItems:'center'}}>
        </Box>
        </>
        
    );

};

export default LoadingScreen; 