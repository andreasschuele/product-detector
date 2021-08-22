import * as React from 'react';
import {
    AppBar, Button, Tab, TextField, Typography
} from '@material-ui/core';

import $ from "jquery";

import { Login } from '../Login/Login';
import { ProductPreview } from "../ProductPreview/ProductPreview";
import { ProductNewDialog } from "../ProductNewDialog/ProductNewDialog";
import { ProductRecordDialog } from "../ProductRecordDialog/ProductRecordDialog";
import { ProductImage } from "../ProductImage/ProductImage";
import { NotificationBar } from "../NotificatonBar/NotificationBar";
import { WebcamCapture } from '../Camera/Camera'

import { ApplicationService, User, Product } from '../../services/ApplicationService';

import './Application.css';

class ApplicationState {
    tabIndexValue?: number = 2;
    tabDisplayState?: string = 'LOGIN';
    product?: Product;
    productImage?: string;
    similarProducts?: Product[];
    productsAll?: Product[];
    maintainSelectedProduct?: Product;
    maintainImage?: string;
    user?: User;
}

function nullToEmptyString(val) {
    return val === null ? '' : val;
}

export default class Application extends React.Component<any, ApplicationState> {

    private applicationService: ApplicationService = new ApplicationService();

    private webcamDetectRef: React.Ref<any> = React.createRef();

    private productNewDialogRef: React.Ref<any> = React.createRef();

    private productRecordDialogRef: React.Ref<any> = React.createRef();

    private textFieldMaintainNameRef: React.Ref<any> = React.createRef();

    private notificationBarRef: React.Ref<any> = React.createRef();

    private textFieldMaintainNotesRef: React.Ref<any> = React.createRef();

    constructor(props) {
        super(props);

        this.state = {
            tabIndexValue: 2,
            tabDisplayState: 'LOGIN',
            product: null,
            productImage: null,
            similarProducts: [],
            productsAll: [],
            maintainSelectedProduct: null,
            maintainImage:null,
            user: null
        }

        console.log(this.state)

        // this.onLoginButtonClicked('admin', 'secret');
    }

    componentDidMount() {
        (window as any).notify = (this.notificationBarRef as any)?.current?.notify;
    }

    onLoginButtonClicked(username, password) {
        this.applicationService.signin(username, password).then((u) => {
            this.setState({... this.state,
                user: u,
                tabDisplayState: 'DETECT'
            });
        });
    }

    onDetectButtonClicked(imageSrc) {
        this.setState({
            ... this.state, 
            product: null, 
            productImage: null, 
            similarProducts: []
        });

        const result = this.applicationService.productDetect(imageSrc);
        
        result.then((value: Product[]) => {
            console.log('value received: ' + value[0].name);
            
            this.setState({... this.state, product: value[0], productImage: value[0].mainImage, similarProducts: value});

            $('.presented-item-panel')[0].scrollIntoView();
        });
    }

    handleOnDetectButtonClicked(webcamDetectRef: any) {
        this.onDetectButtonClicked(webcamDetectRef?.current?.getScreenshot());
    }

    handleButtonDemoDataSetup() {
        let result = this.applicationService.productDemoDataSetup();

        result.then((v) => {
            (window as any).notify("success", "Demo products created.", 3000);
        });
    }

    handleOnPreviewButtonClicked(product: Product) {
        this.setState({... this.state,
            product: product,
            productImage: product.mainImage
        });
    }

    productNewDialogHandleCreate(productName: string): Promise<boolean> {
        return new Promise((resolve => {
            console.log("Try to create a new product: " + productName);

            let result = this.state.productsAll.filter(product => product.name === productName)

            if (result.length > 0) {
                (window as any).notify("error", "A product with the same name already exists.", 3000);

                resolve(false);

                return;
            }

            this.applicationService.productCreate(productName)
               .catch(() => {
                   (window as any).notify("error", "Can't create new product.", 3000);

                   resolve(false);
               })
               .then((product) => {
                   (window as any).notify("success", "New product '" + productName + "' created.", 3000);

                   this.fetchAllProducts();

                   this.handleOnChangeItemsList((product as any).id);

                   resolve(true);
               });
        }));
    }

    handleOnChangeItemsList(productId: number) {
        this.applicationService.productGet(productId).then(product => {
            this.setState({... this.state,
                maintainSelectedProduct: product,
                maintainImage: product.mainImage
            });

            (this.textFieldMaintainNameRef as any).current.value = product.name;
            (this.textFieldMaintainNotesRef as any).current.value = product.notes;
        });
    }


    handleOnMouseDownTrainDialog(capturedImage, selectedProduct: Product): Promise<boolean> {
        return new Promise<boolean>(resolve => {
            this.applicationService.productTrain(this.state.maintainSelectedProduct.id, capturedImage)
                .catch(() => {
                    resolve(false);
                })
                .then(() => {
                    console.log("Recorded");
                    resolve(true);
                });
        });
    }

    handleOnNewProductButtonClicked(selectedProduct: Product) {
        (this.productNewDialogRef as any)?.current?.showDialog();
    }

    handleOnSaveProductButtonClicked(selectedProduct: Product) {
        const productNotes = (this.textFieldMaintainNotesRef as any).current.value;
        const productMainImage = (document.getElementById('maintainImage') as any)?.src;

        this.applicationService.productUpdate(selectedProduct.id, {
            notes: productNotes,
            mainImage: productMainImage
        }).then(() => {
            (window as any).notify("success", "Product saved.", 1000);

            this.fetchAllProducts();
        });
    }

    handleOnDeleteProductButtonClicked(selectedProduct: Product) {
        this.applicationService.productDelete(selectedProduct.id).then(() => {
            this.setState({... this.state,
                maintainSelectedProduct: null,
                maintainImage: null
            });
            this.fetchAllProducts();
            (this.textFieldMaintainNameRef as any).current.value = "";
            (this.textFieldMaintainNotesRef as any).current.value = "";

            (window as any).notify("success", "Product '" + selectedProduct.name + "' deleted!", 3000);
        });
    }

    handleOnTrainProductButtonClicked(selectedProduct: Product) {
        (this.productRecordDialogRef as any)?.current?.showDialog();
    }

    handleOnTrainAllProductButtonClicked() {
        this.applicationService.productTrainAll();
    }

    fetchAllProducts() {
        this.applicationService.productGetAll().then(products => {
            if (products.length !== 0) {
                this.setState({... this.state, productsAll: products});
            }
        });
    }

    renderLoginPanel() {
        return (
            <div className="start-panel">
                { !this.state.user &&
                <div className="panel login-panel">
                    <Login onLoginButtonClicked={(username, password) => this.onLoginButtonClicked(username, password)}></Login>
                </div>
                }
            </div>
        );
    }

    renderDetectPanel() {
        return (
            <div className="detect-panel">
                <div className="panel camera-panel">
                    <p className="panel-title">CAMERA</p>
                    <div style={{ marginLeft: 'auto', marginRight: 'auto', display: 'block' }}>
                        <WebcamCapture
                            reference={this.webcamDetectRef}
                            width={720} height={640}/>
                    </div>
                    <Button style={{width:'100%'}} variant="contained" color="primary" onClick={() => this.handleOnDetectButtonClicked(this.webcamDetectRef)}>DETECT</Button>
                </div>

                { this.state.similarProducts?.length !== 0 && (

                    <>

                <div className="hbreak"></div>

                <div className="panel presented-item-panel">
                    <p className="panel-title">DETECTED / SELECTED</p>

                    <ProductImage
                        image={this.state.productImage}
                    />

                    <div style={{width: "100%", height: "5px"}}/>

                    <TextField
                        disabled
                        id="product-name"
                        label="Name"
                        fullWidth={true}
                        value={nullToEmptyString(this.state.product?.name)}
                        variant="filled"
                    />

                    <div style={{width: "100%", height: "5px"}}/>

                    <TextField
                        disabled
                        id="product-notes"
                        label="Notes"
                        multiline
                        rows={4} fullWidth={true}
                        value={nullToEmptyString(this.state.product?.notes)}
                        variant="filled"
                    />
                </div>

                <div className="hbreak"></div>

                <div className="panel similar-items-panel">
                    <p className="panel-title">SIMILAR PRODUCTS</p>

                    <div className="similar-items-container">
                        {this.state.similarProducts.length !== 0 &&
                        this.state.similarProducts.map((product) => {
                            return (
                                <div key={product.id} className="simliar-item-container">
                                    <ProductPreview product={product} onPreviewButtonClicked={(product) => this.handleOnPreviewButtonClicked(product)}/>
                                </div>
                            )
                        })
                        }
                    </div>
                </div>

                    </>
                )}
            </div>
        );
    }

    renderMaintainPanel() {
        const selectedProduct = this.state.maintainSelectedProduct;

         return (
            <div className="maintain-panel">
                <div className="panel maintain-items-list-panel">
                    <p className="panel-title">PRODUCTS</p>
                    <ul className="product-list">
                        {this.state.productsAll.length !== 0 &&
                        this.state.productsAll.map((product) => {
                            return (
                                <li key={product.id} className={this.state.maintainSelectedProduct?.id === product.id ? 'product-list-item-selected' : ''}
                                    onClick={() => this.handleOnChangeItemsList(product.id)}>{product.name}</li>
                            );
                        })
                        }
                    </ul>

                </div>

                <div className="panel maintain-item-panel">
                    <p className="panel-title">SELECTED PRODUCT</p>

                    <div style={{width: "100%", height: "5px"}}/>

                    <TextField
                        disabled
                        inputRef={this.textFieldMaintainNameRef}
                        id="maintain-product-name"
                        label="Name"
                        fullWidth={true}
                        defaultValue=" "
                        variant="filled"
                    />

                    <div style={{width: "100%", height: "5px"}}/>

                    <ProductImage
                        image={this.state.maintainImage}
                        canUpload={true}
                        onUploaded={(image) => {
                            this.setState({... this.state,
                                maintainImage: image
                            });
                        }}
                    />

                    <div style={{width: "100%", height: "5px"}}/>

                    <TextField
                        inputRef={this.textFieldMaintainNotesRef}
                        id="maintain-product-notes"
                        label="Notes"
                        multiline
                        rows={4}
                        fullWidth={true}
                        defaultValue={null}
                        variant="filled"
                    />

                    <div style={{width: "100%", height: "5px"}}/>

                    <Button style={{width:'100%', marginTop: '5px'}} variant="contained" color="primary" onClick={() => this.handleOnNewProductButtonClicked(selectedProduct)}>NEW PRODUCT</Button>
                    <Button disabled={selectedProduct===null} style={{width:'100%', marginTop: '5px'}} variant="contained" color="primary"  onClick={() => this.handleOnSaveProductButtonClicked(selectedProduct)}>SAVE PRODUCT</Button>
                    <Button disabled={selectedProduct===null} style={{width:'100%', marginTop: '5px'}} variant="contained" color="primary" onClick={() => this.handleOnTrainProductButtonClicked(selectedProduct)}>RECORD PRODUCT</Button>
                    <Button disabled={selectedProduct===null} style={{width:'100%', marginTop: '5px'}} variant="contained" color="secondary" onClick={() => this.handleOnDeleteProductButtonClicked(selectedProduct)}>DELETE PRODUCT</Button>

                    <div className="hbreak"></div>

                    <Button style={{width:'100%', marginTop: '5px'}} variant="contained" color="primary" onClick={() => this.handleOnTrainAllProductButtonClicked()}>TRAIN ALL PRODUCTS</Button>

                    <div className="hbreak"></div>

                    <Button style={{width:'100%', marginTop: '5px'}} variant="contained" color="secondary" onClick={() => this.handleButtonDemoDataSetup()}>ReCreate DEMO PRODUCT DATA</Button>
                </div>


                <ProductNewDialog
                    reference={this.productNewDialogRef}
                    onCreateNewProduct={(productName) => this.productNewDialogHandleCreate(productName)}
                />

                <ProductRecordDialog
                    reference={this.productRecordDialogRef}
                    onNewImage={(capturedImage) => this.handleOnMouseDownTrainDialog(capturedImage, this.state.maintainSelectedProduct) }
                />

            </div>
        );
    }

    render() {
        return (
            <div className="App">
                <AppBar position="static">
                    <Typography className="applicationTitle" variant="h4">PRODUCT DETECTOR</Typography>

                    <div style={{ marginLeft: 'auto', marginRight: 'auto', display: 'block', height: '100%'}}>
                        {this.state.user && (
                            <Tab label="Detect" onClick={() => {
                                this.setState({ ...this.state,
                                    tabDisplayState: 'DETECT'
                                })
                            }}/>
                        ) }

                        {this.state.user && this.state.user?.role.indexOf('ROLE_ADMIN') !== -1 && (
                            <Tab label="Maintain" onClick={() => {
                                this.fetchAllProducts();

                                this.setState({ ...this.state,
                                    tabDisplayState: 'MAINTAIN'
                                })
                            }}/>
                        ) }

                        {this.state.user && (
                            <Tab label="Logout" onClick={() => {
                                this.setState({ ...this.state,
                                    tabDisplayState: 'LOGIN',
                                    user: null
                                })
                            }}/>
                        ) }
                    </div>

                </AppBar>

                <NotificationBar reference={this.notificationBarRef}/>

                <div className="app-inner-container">
                    {this.state.tabDisplayState === 'DETECT' && this.renderDetectPanel() }

                    {this.state.tabDisplayState === 'MAINTAIN' && this.renderMaintainPanel() }

                    {this.state.tabDisplayState === 'LOGIN' && this.renderLoginPanel() }
                </div>
            </div>
        );
    }

};