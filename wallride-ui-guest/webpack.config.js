const webpack = require('webpack');
const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
	entry: "./src/app.js",
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
				test: /\.(jpg|png|gif)$/,
				use: 'url-loader',
			},
			{
				test: /\.(ttf|otf|eot|svg|woff2?)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
				loader: 'file-loader',
				options: {
					name: '[path][name].[ext]',
					emitFile: false
				}
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
			{ from: 'node_modules/bootstrap/dist/fonts/*', to: 'resources/guest' },
			{ context: 'src/resources', from: 'img/**/*', to: 'resources/guest' },
			{ context: 'src/templates', from: '**/*', to: 'templates/guest' }
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