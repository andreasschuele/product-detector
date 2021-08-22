import React, {forwardRef, useImperativeHandle} from "react";
import {Snackbar} from "@material-ui/core";
import MuiAlert, { AlertProps, Color } from '@material-ui/lab/Alert';

import './NotificationBar.css';


class NotificationBarProps {
  reference?: React.Ref<any>;
}

function Alert(props: AlertProps) {
    return <MuiAlert elevation={6} variant="filled" {...props} />;
}
  
function NotificationBarComponent(props: NotificationBarProps, ref) {
  const defaultProps = new NotificationBarProps();
  const { ...other } = { ...defaultProps, ...props };

  const [open, setOpen] = React.useState(false);
  const [message, setMessage] = React.useState("");
  const [severity, setSeverity] = React.useState("success" as Color);
  const [autoHide, setAutoHide] = React.useState(6000);

  const handleClose = (event?: React.SyntheticEvent, reason?: string) => {
      if (reason === 'clickaway') {
          return;
      }

      setOpen(false);
  };

    if (other.reference) {
        useImperativeHandle(other.reference, () => ({
            notify(severity, message, autoHide = 3000) {
                setOpen(true);
                setSeverity(severity);
                setMessage(message);

                if (autoHide) {
                    setAutoHide(autoHide);
                }
            }
        }));
    }

  return (
      <>
          <div style={{
              'width': '100%',
              'marginTop': '2'
          }}>
              <Snackbar open={open} autoHideDuration={autoHide} onClose={handleClose}>
                  <Alert onClose={handleClose} severity={severity}>
                      {message}
                  </Alert>
              </Snackbar>
          </div>
      </>
  );
};

export const NotificationBar = forwardRef(NotificationBarComponent);