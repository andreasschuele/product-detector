import React, {forwardRef, useImperativeHandle} from "react";
import {
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    TextField
} from '@material-ui/core';

import './ProductNewDialog.css';


class ProductNewDialogProps {
  reference?: React.Ref<any>;
  onCreateNewProduct?: (productName: string) => Promise<boolean>;
}
  
function ProductNewDialogComponent(props: ProductNewDialogProps, ref) {
  const defaultProps = new ProductNewDialogProps();
  const { onCreateNewProduct, ...other } = { ...defaultProps, ...props };

  const textFieldMaintainNameRef = React.useRef(null);

  const [dialogVisible, setDialogVisible] = React.useState(false);
  
  const onCreateButtonClicked = React.useCallback(
    () => {
      const productName = (textFieldMaintainNameRef as any).current.value;

      if (onCreateNewProduct) {
          onCreateNewProduct(productName).then(result => {
             setDialogVisible(!result);
          });
      }
    },[]
  );


  const onCancelButtonClicked = React.useCallback(
      () => {
          setDialogVisible(false);
      },[]
  );

  if (other.reference) {
      useImperativeHandle(other.reference, () => ({
          showDialog() {
              if ((textFieldMaintainNameRef as any)?.current) {
                  (textFieldMaintainNameRef as any).current.value = '';
              }

              setDialogVisible(true);
          }
      }));
  }

  return (
      <>
      <Dialog open={dialogVisible}>
          <DialogTitle>NEW PRODUCT</DialogTitle>
          <DialogContent>
              <div style={{width:'400px', 'textAlign': 'justify'}}>
                  Please enter a unique product name.
              </div>
              <br/>
              <TextField
                  inputRef={textFieldMaintainNameRef}
                  label="Name"
                  fullWidth={true}
                  defaultValue={null}
                  variant="filled"
              />
              <br/>
              <br/>
              <Button style={{width:'100%', margin: '5px'}}
                      variant="contained" color="primary"
                      onClick={() => onCreateButtonClicked()}>CREATE</Button>
              <Button style={{width:'100%', margin: '5px'}}
                      variant="contained"
                      color="primary"
                      onClick={() => onCancelButtonClicked()}>CANCEL</Button>
          </DialogContent>
      </Dialog>
      </>
  );
};

export const ProductNewDialog = forwardRef(ProductNewDialogComponent);