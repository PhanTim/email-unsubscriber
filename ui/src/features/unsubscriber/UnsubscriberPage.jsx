import  React, {setState, useEffect, useState }  from 'react';
import {Box, Button, Link, Typography} from '@mui/material';
import axios from 'axios';
import { clientUrl } from '../../util';
import { cookies } from '../../AuthContext';
import LoadingScreen from '../../LoadingScreen';
import RefreshLinksButton from './RefreshLinksButton';
import UnsubscribeButton from './UnsubscribeButton';
import { DataGrid, useGridApiEventHandler, useGridApiContext } from '@mui/x-data-grid';
import DoneOutlineTwoToneIcon from '@mui/icons-material/DoneOutlineTwoTone';
import DeleteUnsubscribeLinksButton from './DeleteUnsubscribeLinksButton';
import MarkUnsubscribedButton from './MarkUnsubscribedButton';

const token = cookies.get("token");

const UnsubscriberPage = ({children}) => {
  const [loading, setLoading] = useState(true);
  let [emails, setEmails] = useState(null);
  let [rows, setRows]= useState(null);
  let [selectedRows, setSelectedRows] = useState(null);
  let [doesSelectedRowsIncludeEmptyLinks, setDoesSelectedRowsIncludeEmptyLinks] = useState(false);

  //TODO: Look into React Query 
  useEffect(() => {
    const getEmails = async () => {
    try{
      setLoading(true);
      const result = await axios.post(`${clientUrl}/api/gmail/`, {
        token: token
      });
      if(result.data.authToken){
        cookies.set("token", result.data.authToken);
      }
      setLoading(false);
      if(result != null){
        setEmails(result);
        let dummyRows = [];
        result.data.unsubscribeLinkSenders.forEach(
          function (item, index, arr) 
            { 
              dummyRows.push({id: index, sender: arr[index], link: result.data.unsubscribeLinkURLs[index], isunsubscribed: result.data.isUnsubscribedValues[index]}) 
            }
        );
        setRows(dummyRows);
      }
    }
    catch(error){
      setLoading(false);
      console.error(error);
    }
  }
  getEmails();
  }, [])


  const columns = [
    { field: 'sender', headerName: 'Sender', width: 255 },
    { field: 'link', headerName: 'Unsubscribe Link', flex: 1, renderCell: (params) => {
      return <div style={{overflow: "hidden", textOverflow: "ellipsis", flex: 1}}><Link href={params.value}>{params.value}</Link></div>
    } },
    { field: 'isunsubscribed', headerName: "Unsubscribed", flex: 1, renderCell: (params) => {
      return (params.value ? <DoneOutlineTwoToneIcon/> : null )
    }}
  ];

  if(emails != null)
  
  return (loading) ? (<><LoadingScreen/></>) 
   : (
   <>
   <p><strong>Disclaimer: This program currently has no way of knowing if you have unsubscribed after clicking the link, thus there is an option to manually mark links as unsubscribed.</strong></p>
   <Box>
    <DataGrid 
    rows={rows} 
    columns={columns} 
    autoHeight 
    checkboxSelection 
    disableRowSelectionOnClick
    onRowSelectionModelChange={(rowSelectionModel) => {
      
      const selectedRowData = rows.filter(row => rowSelectionModel.includes(row.id));
      const selectedRowDataIncludesEmptyLinks = selectedRowData.filter(({ link }) => link === "");
      setSelectedRows(selectedRowData);
      setDoesSelectedRowsIncludeEmptyLinks(selectedRowDataIncludesEmptyLinks.length > 0);
    }}
    />
   </Box>
   <Box sx={{flexGrow: 1}}>
    <UnsubscribeButton selectedRows={selectedRows} 
    disabled ={doesSelectedRowsIncludeEmptyLinks} />
    <MarkUnsubscribedButton selectedRows={selectedRows} />
    <RefreshLinksButton/> 
    <DeleteUnsubscribeLinksButton selectedRows={selectedRows}/>
   </Box>
  </>); 
}

export default UnsubscriberPage;