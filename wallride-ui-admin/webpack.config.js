const webpack = require('webpack');
const path = require('path');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
	context: path.resolve(__dirname, "src"),
	entry: "./app.js",
	output: {
		path: path.resolve(__dirname, "dist"),
		filename: 'resources/admin/bundle.js'
	},
	module: {
		rules: [
			{
				test: /\.css$/,
				use: [ 'style-loader', 'css-loader' ]
			},
			{
				test: /\.(jpg|png|gif)$/,
				use: 'url-loader?limit=10000&name=./resources/admin/img/[hash].[ext]',
			},
			{
				test: /\.woff2?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
				use: 'url-loader?limit=10000&name=./resources/admin/font/[hash].[ext]',
			},
			{
				test: /\.(ttf|eot|svg)(\?[\s\S]+)?$/,
				use: 'file-loader?name=./resources/admin/font/[hash].[ext]',
			}
		]
	},
	plugins: [
		new webpack.ProvidePlugin({
			$: "jquery",
			jQuery: "jquery",
			"window.jQuery": "jquery"
		}),
		new CopyWebpackPlugin([
			{ from: 'font/**/*', to: 'resources/admin', context: 'resources' },
			{ from: 'img/**/*', to: 'resources/admin', context: 'resources' },
			{ from: '**/*', to: 'templates/admin', context: 'templates' }
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