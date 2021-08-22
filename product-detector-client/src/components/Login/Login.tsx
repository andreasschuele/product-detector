import * as React from "react";
import {
  Button,
  Grid,
  TextField
} from '@material-ui/core';

import './Login.css';


export function Login(props) {
  const [state, setState] = React.useState({
    username: '',
    password: ''
  });

  const onLoginButtonClicked = () => {
    props.onLoginButtonClicked(state.username, state.password);
  };

  return (
    <div className="loginForm">
        <Grid container 
            spacing={3}
            direction="column"
            justifyContent="center"
            alignItems="center">
            <Grid item xs>
                <TextField
                    helperText="Enter your Username"
                    label="Username"
                    onChange={(event) => setState({ ...state, username: event.target.value})}
            />
            </Grid>
            <Grid item xs>
                <TextField
                    type="password"
                    helperText="Enter your Password"
                    label="Password"
                    onChange={(event) => setState({ ...state, password: event.target.value})}
                />
            </Grid>    
            <Grid item xs>
                <Button variant="contained" color="primary" onClick={() => onLoginButtonClicked()}>Login</Button>
            </Grid>      
      </Grid>
    </div>
  );
}