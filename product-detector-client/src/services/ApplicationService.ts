import { AxiosPromise, AxiosResponse } from 'axios';
import { ProductRestControllerApiFactory, UserRestControllerApiFactory } from '../api/product-detector';
import {
    JwtAuthenticationResponseDto,
    ProductDetectResponseDto,
    SignoutRequestDto,
    ProductCreateRequestDto,
    ProductCreateResponseDto,
    ImageDto,
    ProductTrainAllRequestDto, ProductAddExampleDataRequestDto
} from '../api/product-detector/models';

export interface User {
    username: string;
    accessToken: string;
    role?: string;
}

export interface Product {
    id?: number;
    name?: string;
    notes?: string;
    mainImage?: string;
    probability?: any;
    active?: boolean;
}

export class ApplicationService {

    private readonly basePath: string = window.location.origin;

    private readonly baseOptions: any = {
        headers: {
        }
    };

    private user: User = null;

    constructor() {
        console.log('basePath: ' + this.basePath)
    }

    private userControllerApi = UserRestControllerApiFactory({
        basePath: this.basePath,
        baseOptions: this.baseOptions
    }, this.basePath);

    private productRestControllerApiApi = ProductRestControllerApiFactory({
        basePath: this.basePath,
        baseOptions: this.baseOptions
    }, this.basePath);


    async signin(username: string, password: string): Promise<User>  {
        return new Promise((resolve, reject) => {
            this.signout().finally(() => {
                const request = this.userControllerApi.signin({
                    usernameOrEmail: username,
                    password: password
                });

                request.catch(() => {
                    reject();
                });

                request.then((value) => {
                    this.user = {
                        username: username,
                        accessToken: value.data.accessToken,
                        role: value.data.role
                    };

                    this.baseOptions.headers['Authorization'] = 'Bearer ' + value.data.accessToken;

                    resolve(this.user)
                });
            });
        });
    }

    async signout(): Promise<void> {
        return new Promise((resolve) => {
            if (this.getSignedInUser() === null) {
                resolve();
                return;
            }

            const request = this.userControllerApi.signout({});

            request.finally(() => {
                this.user = null;
                delete this.baseOptions.headers['Authorization'];

                resolve();
            });
        });
    }

    getSignedInUser(): User {
        return this.user;
    }

    async productCreate(name: string = null): Promise<Product> {
        return new Promise((resolve, reject) => {

            const request = this.productRestControllerApiApi.productCreate({
                name: name
            });

            request.catch(() => {
                reject();
            })

            request.then((value) => {
                resolve({
                    id: value.data.id,
                    name: value.data.name,
                    notes: value.data.notes
                });
            });
        });
    }

    async productGet(id: number): Promise<Product> {
        return new Promise((resolve, reject) => {

            const request = this.productRestControllerApiApi.productGet(id);

            request.catch(() => {
                reject();
            })

            request.then((value) => {
                resolve({
                    id: value.data.id,
                    name: value.data.name,
                    notes: value.data.notes,
                    mainImage: value.data.mainImage
                });
            });
        });
    }

    async productGetAll(): Promise<Product[]> {
        return new Promise((resolve, reject) => {

            const request = this.productRestControllerApiApi.productGetAll();

            request.catch(() => {
                reject();
            })

            request.then((value) => {
                resolve(value.data.products.map(item => {
                    return {
                        id: item.id,
                        name: item.name,
                        notes: item.notes,
                        tags: item.tags
                    }
                }));
            });
        });
    }

    async productUpdate(id: number, product: Product): Promise<Product> {
        return new Promise((resolve, reject) => {

            const request = this.productRestControllerApiApi.productUpdate({
                name: product.name,
                notes: product.notes,
                active: product.active,
                mainImage: product.mainImage
            }, id);

            request.catch(() => {
                reject();
            })

            request.then((value) => {
                resolve({
                    id: value.data.id,
                    name: value.data.name,
                    notes: value.data.notes,
                    mainImage: value.data.mainImage
                });
            });
        });
    }

    async productDelete(id: number): Promise<void> {
        return new Promise((resolve, reject) => {

            const request = this.productRestControllerApiApi.productDelete(id);

            request.catch(() => {
                reject();
            })


            request.then((value) => {
                resolve();
            });
        });
    }

    async productDetect(imageAsBase64: string): Promise<Product[]> {
        return new Promise((resolve) => {
            console.log("image: " + imageAsBase64);
            let requestPromise: AxiosPromise<ProductDetectResponseDto> = this.productRestControllerApiApi.detect({
                image: {
                    data: imageAsBase64,
                    dataEncoding: 'base64',
                    format: 'jpg'
                } as ImageDto
            });

            requestPromise.then((value: AxiosResponse<ProductDetectResponseDto>) => {
                resolve(value.data.products);
            } );
        });
    }

    async productTrain(productId: number, productImage: string): Promise<void> {
        return new Promise((resolve, reject) => {
            let requestPromise: AxiosPromise = this.productRestControllerApiApi.productAddProductExampleData({
                productImage: productImage
            } as ProductAddExampleDataRequestDto, productId);

            requestPromise.then(() => {
                resolve();
            } );
        });
    }

    async productTrainAll(): Promise<void> {
        return new Promise((resolve, reject) => {
            let requestPromise: AxiosPromise = this.productRestControllerApiApi.trainAll({
            } as ProductTrainAllRequestDto);

            requestPromise.then(() => {
                resolve();
            } );
        });
    }

    async productDemoDataSetup(): Promise<void> {
        return new Promise((resolve) => {
            let requestPromise: AxiosPromise = this.productRestControllerApiApi.demoDataSetup();

            requestPromise.then((value: AxiosResponse) => {
                resolve();
            } );
        });
    }
}
