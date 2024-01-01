import { cookies } from "../../AuthContext";
import axios from 'axios';
import { Button } from "@mui/material";

const LogoutButton = () => {
    const googleLogout = () => {
        try{
            const tokens = axios.post('http://localhost:8080/revoke', {
                token: cookies.get("token")
            });
            cookies.remove("token");
            }
            catch (error) {
                console.error(error.response.data);
            }
            console.log("Logged out.");
            window.location.reload();
    }

    return(
        <Button onClick={() => googleLogout()} color ="inherit">Sign Out</Button>
    )
}

export default LogoutButton;