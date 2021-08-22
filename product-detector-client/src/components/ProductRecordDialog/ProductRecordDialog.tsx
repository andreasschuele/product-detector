import React, {forwardRef, useImperativeHandle} from "react";
import {
  Button,
  Dialog,
  DialogTitle,
  DialogContent
} from '@material-ui/core';
import {WebcamCapture} from "../Camera/Camera";

import './ProductRecordDialog.css';


class ProductRecordDialogProps {
  reference?: React.Ref<any>;
  onNewImage?: (image: string) => Promise<boolean>;
  capturingInterval?: number = 100;
}
  
function ProductRecordDialogComponent(props: ProductRecordDialogProps, ref) {
  const defaultProps = new ProductRecordDialogProps();
  const { onNewImage, capturingInterval, ...other } = { ...defaultProps, ...props };

  const webcamTrainRef = React.useRef(null);

  const [dialogVisible, setDialogVisible] = React.useState(false);
  const [holdToRecordPressed, setHoldToRecordPressed] = React.useState(false);
  const [recordedImagesCounter, setRecordedImagesCounter] = React.useState(0);

  const getState = (f) => {
      let stateValue = null;
      f(state => stateValue = state);
      return stateValue;
  }

  const onStartCapturing = React.useCallback(() => {
      if (holdToRecordPressed === true) {
          return;
      }

      setHoldToRecordPressed(true);

      const recordFunction = () => {
          const webcamCapture = (webcamTrainRef as any)?.current?.getScreenshot();

          if (onNewImage) {
              onNewImage(webcamCapture)
                  .then((recordingSuccessful: boolean) => {
                    if (recordingSuccessful) {
                      setRecordedImagesCounter(getState(setRecordedImagesCounter) + 1);
                    }
                  })
                  .finally(() => {
                    if (getState(setHoldToRecordPressed) === true) {
                      setTimeout(recordFunction, capturingInterval);
                    }
                 });
          }
      }

      recordFunction();
    },[webcamTrainRef]
  );

  const onStopCapturing = React.useCallback(() => {
      setHoldToRecordPressed(false);
    },[webcamTrainRef]
  );

  const onCancelButtonClicked = React.useCallback(
      () => {
          setDialogVisible(false);
      },[]
  );

  if (other.reference) {
      useImperativeHandle(other.reference, () => ({
          showDialog() {
              setRecordedImagesCounter(0);
              setDialogVisible(true);
          }
      }));
  }

  return (
      <>
      <Dialog open={dialogVisible}>
          <DialogTitle>RECORD PRODUCT</DialogTitle>
          <DialogContent>
              <div style={{maxWidth:'400px', 'textAlign': 'justify'}}>
                  Place the product you want to record in front of the camera and press and hold the 'Record' button.
              </div>
              <WebcamCapture
                  reference={webcamTrainRef}
                  width={360} height={320}/>
              <div style={{'textAlign': 'center', 'fontWeight': 'bold'}}>{recordedImagesCounter} recorded images</div>
              <br/>
              <Button style={{width:'100%', margin: '5px'}}
                      variant="contained"
                      color={holdToRecordPressed ? 'secondary' : 'primary'}
                      onTouchStart={() => onStartCapturing()}
                      onTouchEnd={() => onStopCapturing()}
                      onMouseDown={() => onStartCapturing()}
                      onMouseUp={() => onStopCapturing()}
                      onMouseOutCapture={() => onStopCapturing()}>{holdToRecordPressed ? 'Recording ...' : 'Record'}</Button>
              <Button style={{width:'100%', margin: '5px'}}
                      variant="contained"
                      color="primary"
                      onClick={() => onCancelButtonClicked()}>Finish</Button>
          </DialogContent>
      </Dialog>
      </>
  );
};

export const ProductRecordDialog = forwardRef(ProductRecordDialogComponent);