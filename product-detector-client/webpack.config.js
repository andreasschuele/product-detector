"use strict";

let path = require('path');

console.log(path.resolve(__dirname, "dist"));

module.exports = {
    mode: "development", 

    entry: "./src/index",

    output: {
        filename: "bundle.js",

        path: path.resolve(__dirname, "dist"),

        publicPath: "/",
    },

    devtool: "eval-cheap-source-map",

    resolve: {
        extensions: [".ts", ".tsx", ".js", ".json", ".css"]
    },

    module: {
        rules: [
            { test: /\.tsx?$/, loader: "ts-loader" },
            { test: /\.js$/, loader: "source-map-loader", enforce: "pre" },
            { test: /\.svg$/, loader: 'svg-loader' },
            { test: /\.css$/, use: ['style-loader', 'css-loader'] }
        ]
    },

    target: "web",

    watch: true,

    devServer: {
        host: "0.0.0.0",
        disableHostCheck: true,
        port: 8081,
        contentBase: path.join(__dirname, 'public'),
        hot: false,
        open: false,
        watchContentBase: true,

    }
};