import React from "react";
import { Product } from "../../services/ApplicationService";

import './ProductPeview.css';


class ProductPreviewProps {
  ref?: React.Ref<any>;
  product: Product;
  onPreviewButtonClicked?: (product: any) => void;
  width?: number = 720;
  height?: number = 640;
}
  
export const ProductPreview = (props: ProductPreviewProps, ref) => {
  const defaultProps = new ProductPreviewProps();
  const { product, onPreviewButtonClicked, ...other } = { ...defaultProps, ...props };

  const handleOnDetectButtonClicked = React.useCallback(
    () => {
        onPreviewButtonClicked(product);
    }, []
  );

  return (
      <>
       <div className="productPreview" onClick={handleOnDetectButtonClicked}>
           <div style={{width: '100px', marginLeft: 'auto', marginRight: 'auto' }}>
           {product.mainImage && (
               <img src={product.mainImage} style={{width: '100px', height: '100px'}}/>
           )}
           </div>
           <p style={{fontSize: '12px', textAlign: 'center', marginTop: '10px', fontWeight: 'bold' }}>{product.name}</p>
      </div>
      </>
    );
  };