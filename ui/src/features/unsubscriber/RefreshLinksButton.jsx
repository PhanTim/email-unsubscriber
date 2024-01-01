import  React, {setState, useEffect, useState }  from 'react';
import {Box, Button} from '@mui/material';
import axios from 'axios';
import { clientUrl } from '../../util';
import { cookies } from '../../AuthContext';
import LoadingScreen from '../../LoadingScreen';

const token = cookies.get("token");

const RefreshLinksButton = ({children}) => {
  const [loading, setLoading] = useState(false);
  let [emails, setEmails] = useState(null);

  //TODO: Look into React Query 

  const getEmails = async () => {
  try{
    setLoading(true);
    const result = await axios.post(`${clientUrl}/api/gmail/refresh`, {
      token: token
    });
    if(result.data.authToken){
      cookies.set("token", result.data.authToken);
    }
    setLoading(false);
    setEmails(result);
    window.location.reload();
  }
  catch(error){
    setLoading(false);
    console.error(error);
  }
}
  
  return ((loading) ? <LoadingScreen/> : <Box sx ={{p: 2}} display='inline-flex'><Button variant='contained' onClick={() => getEmails()} >Refresh Links</Button></Box>);
}

export default RefreshLinksButton;