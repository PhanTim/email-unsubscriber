import {Box, Button} from '@mui/material';

const UnsubscribeButton = ({selectedRows, disabled, children}) => {
    return (
        <>
            <Box sx={{p:2, justifyContent:'space-between'}} display='inline-flex'>
                <Button variant='contained' disabled={disabled} sx={{}} 
                onClick=
                {() =>
                    {
                        console.log(selectedRows);
                        selectedRows.forEach((row, index) => setTimeout( function() { window.open(row.link, '_blank') }, index*1000));
                    }
                }
                >
                    Unsubscribe
                </Button>
            </Box>
        </>
    )
};

export default UnsubscribeButton;