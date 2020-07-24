const path = require('path');

module.exports = {
  entry: path.resolve(__dirname, "js/index.js"),
  output: {
    path: path.resolve(__dirname, "dist"),
    publicPath: '/assets/',
    filename: 'bundle.js'
  },
    module: {
  rules: [
    {
      test: /\.css$/,
      use: [
        'style-loader',
        'css-loader'
      ],
      exclude: /\.module\.css$/
    },
    {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        loader: "babel-loader",
        query: {
          presets: ["@babel/preset-react"].map(require.resolve),
          plugins: ["@babel/plugin-proposal-class-properties"].map(require.resolve)
        },

      }]
},
  watchOptions: {
    aggregateTimeout: 300,
    poll: 1000
  },
  resolve: {
    modules: [path.resolve(__dirname, 'node_modules'), 'node_modules'],
    extensions: [".js", ".json", ".jsx", ".css"]
  }
};