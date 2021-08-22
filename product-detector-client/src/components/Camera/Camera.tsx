import React, {forwardRef, useImperativeHandle} from "react";
import Webcam from "react-webcam";
import { Button } from "@material-ui/core";

import './Camera.css';

class WebcamVideoConstraints {
  width?: number = 720;
  height?: number = 640;
  facingMode?: string = "environment";
}

class WebcamCaptureComponentProps {
  reference?: React.Ref<any>;
  //onDetectButtonClicked?: (imageSrc: any) => void;
  videoConstraints?: WebcamVideoConstraints = new WebcamVideoConstraints();
  width?: number = 720;
  height?: number = 640;
  audio?: boolean = false;
}
  
function WebcamCaptureComponent(props: WebcamCaptureComponentProps, ref) {
  const defaultProps = new WebcamCaptureComponentProps();
  const { /* onDetectButtonClicked,*/ videoConstraints, ...other } = { ...defaultProps, ...props };

  const webcamRef = React.useRef(null);

  const [imgSrc, setImgSrc] = React.useState(null);
  
  const handleOnDetectButtonClicked = React.useCallback(
    () => {
      const imageSrc = webcamRef?.current?.getScreenshot();
      setImgSrc(imageSrc);

      //onDetectButtonClicked(imageSrc);
    },
    [webcamRef, setImgSrc]
  );

  if (other.reference) {
      useImperativeHandle(other.reference, () => ({

          getScreenshot() {
              return webcamRef?.current?.getScreenshot();
          }

      }));
  }

  return (
      <>
       <div className="cameraCaptureComponent">
          <Webcam
              style={{ marginLeft: 'auto', marginRight: 'auto', display: 'block', height: '100%', width: '100%', maxWidth: '400px'}}
          ref={webcamRef}
          audio={other.audio}
          height={other.height}
          width={other.width}
          screenshotFormat="image/jpeg"
          videoConstraints={videoConstraints}
          />
          <br/>
           {/*
          <Button variant="contained" color="primary" onClick={handleOnDetectButtonClicked}>Detect</Button>
           */}
      </div>
      </>
    );
  };

export const WebcamCapture = forwardRef(WebcamCaptureComponent);