import React, {forwardRef} from "react";
import $ from "jquery";

import './ProductImage.css';


class ProductImageProps {
  reference?: React.Ref<any>;
  image?: string = null;
  canUpload?: boolean = false;
  onUploaded?: (image: string) => void = null;
}
  
function ProductImageComponent(props: ProductImageProps, ref) {
  const defaultProps = new ProductImageProps();
  const { image, canUpload, onUploaded, ...other } = { ...defaultProps, ...props };

  const onImageLoaded = React.useCallback(
    (image) => {
        if (onUploaded) {
            onUploaded(image);
        }
    },[]
  );

  return (
      <>
          <div className="presented-item-image-container"
               style={{
                   margin: 'auto',
                   display: 'flex',
                   height: '100%',
                   width: '100%',
                   minHeight: '400px',
                   position: 'relative'
                   }}>
              {image !== null && (                  
                <img id="maintainImage" src={image} style={{
                    margin: 'auto',
                    display: 'block',
                    height: '100%',
                    width: '100%'}}/>
              )}

              {canUpload && (
                  <>
                      <label htmlFor="upload" 
                        style={{
                            right: '0px',
                            bottom: '0px',
                            width: '24px',
                            height: '24px',
                            position: 'absolute',
                            backgroundColor: 'blueviolet',
                            fontSize: '18px',
                            color: 'white',
                            textAlign: 'center'
                        }}
                        onClick={() => {
                          console.log('clicked');
                          $('#imageUpload').click();
                      }}>
                          <span className="bi bi-upload" aria-hidden="true"></span>
                          <input id="imageUpload"
                                 accept=".jpg, .jpeg"
                                 type="file"
                                 style={{display:'none'}}
                                 onChange={(e) => {
                                     const file = e.target.files[0];

                                     const reader = new FileReader();
                                     reader.onloadend = () => {
                                         onImageLoaded(reader.result);
                                     };
                                     reader.readAsDataURL(file);
                                 }} />
                      </label>
                  </>
              )}
          </div>
      </>
  );
};

export const ProductImage = forwardRef(ProductImageComponent);