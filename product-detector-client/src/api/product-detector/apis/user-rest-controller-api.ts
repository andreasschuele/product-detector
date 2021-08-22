/* tslint:disable */
/* eslint-disable */
/**
 * OpenAPI definition
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: v0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
import globalAxios, { AxiosPromise, AxiosInstance } from 'axios';
import { Configuration } from '../configuration';
// Some imports not used depending on template conditions
// @ts-ignore
import { BASE_PATH, COLLECTION_FORMATS, RequestArgs, BaseAPI, RequiredError } from '../base';
import { ApiResponseDto } from '../models';
import { JwtAuthenticationResponseDto } from '../models';
import { SigninRequestDto } from '../models';
import { SignoutRequestDto } from '../models';
/**
 * UserRestControllerApi - axios parameter creator
 * @export
 */
export const UserRestControllerApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * 
         * @param {SigninRequestDto} body 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        signin: async (body: SigninRequestDto, options: any = {}): Promise<RequestArgs> => {
            // verify required parameter 'body' is not null or undefined
            if (body === null || body === undefined) {
                throw new RequiredError('body','Required parameter body was null or undefined when calling signin.');
            }
            const localVarPath = `/api/v1/user/signin`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, 'https://example.com');
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }
            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            localVarHeaderParameter['Content-Type'] = 'application/json';

            const query = new URLSearchParams(localVarUrlObj.search);
            for (const key in localVarQueryParameter) {
                query.set(key, localVarQueryParameter[key]);
            }
            for (const key in options.query) {
                query.set(key, options.query[key]);
            }
            localVarUrlObj.search = (new URLSearchParams(query)).toString();
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            const needsSerialization = (typeof body !== "string") || localVarRequestOptions.headers['Content-Type'] === 'application/json';
            localVarRequestOptions.data =  needsSerialization ? JSON.stringify(body !== undefined ? body : {}) : (body || "");

            return {
                url: localVarUrlObj.pathname + localVarUrlObj.search + localVarUrlObj.hash,
                options: localVarRequestOptions,
            };
        },
        /**
         * 
         * @param {SignoutRequestDto} body 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        signout: async (body: SignoutRequestDto, options: any = {}): Promise<RequestArgs> => {
            // verify required parameter 'body' is not null or undefined
            if (body === null || body === undefined) {
                throw new RequiredError('body','Required parameter body was null or undefined when calling signout.');
            }
            const localVarPath = `/api/v1/user/signout`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, 'https://example.com');
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }
            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            localVarHeaderParameter['Content-Type'] = 'application/json';

            const query = new URLSearchParams(localVarUrlObj.search);
            for (const key in localVarQueryParameter) {
                query.set(key, localVarQueryParameter[key]);
            }
            for (const key in options.query) {
                query.set(key, options.query[key]);
            }
            localVarUrlObj.search = (new URLSearchParams(query)).toString();
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            const needsSerialization = (typeof body !== "string") || localVarRequestOptions.headers['Content-Type'] === 'application/json';
            localVarRequestOptions.data =  needsSerialization ? JSON.stringify(body !== undefined ? body : {}) : (body || "");

            return {
                url: localVarUrlObj.pathname + localVarUrlObj.search + localVarUrlObj.hash,
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * UserRestControllerApi - functional programming interface
 * @export
 */
export const UserRestControllerApiFp = function(configuration?: Configuration) {
    return {
        /**
         * 
         * @param {SigninRequestDto} body 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async signin(body: SigninRequestDto, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<JwtAuthenticationResponseDto>> {
            const localVarAxiosArgs = await UserRestControllerApiAxiosParamCreator(configuration).signin(body, options);
            return (axios: AxiosInstance = globalAxios, basePath: string = BASE_PATH) => {
                const axiosRequestArgs = {...localVarAxiosArgs.options, url: basePath + localVarAxiosArgs.url};
                return axios.request(axiosRequestArgs);
            };
        },
        /**
         * 
         * @param {SignoutRequestDto} body 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async signout(body: SignoutRequestDto, options?: any): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiResponseDto>> {
            const localVarAxiosArgs = await UserRestControllerApiAxiosParamCreator(configuration).signout(body, options);
            return (axios: AxiosInstance = globalAxios, basePath: string = BASE_PATH) => {
                const axiosRequestArgs = {...localVarAxiosArgs.options, url: basePath + localVarAxiosArgs.url};
                return axios.request(axiosRequestArgs);
            };
        },
    }
};

/**
 * UserRestControllerApi - factory interface
 * @export
 */
export const UserRestControllerApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    return {
        /**
         * 
         * @param {SigninRequestDto} body 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        signin(body: SigninRequestDto, options?: any): AxiosPromise<JwtAuthenticationResponseDto> {
            return UserRestControllerApiFp(configuration).signin(body, options).then((request) => request(axios, basePath));
        },
        /**
         * 
         * @param {SignoutRequestDto} body 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        signout(body: SignoutRequestDto, options?: any): AxiosPromise<ApiResponseDto> {
            return UserRestControllerApiFp(configuration).signout(body, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * UserRestControllerApi - object-oriented interface
 * @export
 * @class UserRestControllerApi
 * @extends {BaseAPI}
 */
export class UserRestControllerApi extends BaseAPI {
    /**
     * 
     * @param {SigninRequestDto} body 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UserRestControllerApi
     */
    public signin(body: SigninRequestDto, options?: any) {
        return UserRestControllerApiFp(this.configuration).signin(body, options).then((request) => request(this.axios, this.basePath));
    }
    /**
     * 
     * @param {SignoutRequestDto} body 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UserRestControllerApi
     */
    public signout(body: SignoutRequestDto, options?: any) {
        return UserRestControllerApiFp(this.configuration).signout(body, options).then((request) => request(this.axios, this.basePath));
    }
}