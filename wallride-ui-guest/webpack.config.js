const webpack = require('webpack');
const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
	context: path.resolve(__dirname, "src"),
	entry: "./app.js",
	output: {
		path: path.resolve(__dirname, "dist"),
		filename: 'resources/guest/bundle.js'
	},
	module: {
		rules: [
			{
				test: /\.css$/,
				use: ExtractTextPlugin.extract({
					fallback: "style-loader",
					use: "css-loader"
				})
			},
			{
				test: /\.woff2?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
				use: 'url-loader?limit=10000&name=./resources/guest/font/[hash].[ext]',
			},
			{
				test: /\.(ttf|eot|svg)(\?[\s\S]+)?$/,
				use: 'file-loader?name=./resources/guest/font/[hash].[ext]',
			}
		]
	},
	plugins: [
		new webpack.ProvidePlugin({
			$: "jquery",
			jQuery: "jquery"
		}),
		new ExtractTextPlugin("resources/guest/bundle.css"),
		new CopyWebpackPlugin([
			{ from: 'img/**/*', to: 'resources/guest', context: 'resources' },
			{ from: '**/*', to: 'templates/guest', context: 'templates' }
		])
	],
	devServer: {
		contentBase: [
			path.resolve(__dirname, "src/templates"),
			path.resolve(__dirname, "src/resources")
		],
		port: 8000
	}
};