const path = require("path");
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyPlugin = require("copy-webpack-plugin");

module.exports = {
  mode: "none",
  entry: "./src/index.js",
  output: {
    path: __dirname + "/dist",
    filename: "bundle.js",
    publicPath: "/",
  },
  devtool: "cheap-module-eval-source-map",
  devServer: {
    contentBase: path.join(__dirname, "dist"),
    port: 80,
    host: '0.0.0.0', // server to be accessible externally
    public: "localhost", // force to open localhost instead of 0.0.0.0
    open: true, // open the default browser
    historyApiFallback: true, // serve index.html instead of routes leading to no specific ressource
    proxy: {
      "/api": {
        target: "http://localhost:8081",
        pathRewrite: {'^/api' : ''}
      },
    },
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ["style-loader", "css-loader"],
      },
      {
        test: /\.(png|svg|jpg|gif)$/,
        use: ["file-loader"],
      },
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options: {
            presets: [
              [
                "@babel/preset-env",
                {
                  targets: {
                    esmodules: true,
                  },
                },
              ],
            ],
          },
        },
      },
    ],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: "./src/index.html"
    }),
    new CopyPlugin({
      patterns: [
        {
          from: path.join(__dirname, "./src/assets"),
          to: path.join(__dirname, "./dist/assets"),
        },
      ],
    })
  ]
};
