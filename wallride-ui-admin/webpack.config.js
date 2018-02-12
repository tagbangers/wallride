const webpack = require('webpack');
const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
	entry: "./src/app.js",
	output: {
		path: path.resolve(__dirname, "dist"),
		filename: 'resources/admin/bundle.js'
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
				test: /src\/.+\.(ttf|otf|eot|svg|woff2?)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
				loader: 'file-loader',
				options: {
					name: '[name].[ext]',
					publicPath: './font/',
					emitFile: false
				}
			},
			{
				test: /node_modules\/.+\.(ttf|otf|eot|svg|woff2?)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
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
			jQuery: "jquery",
			"window.jQuery": "jquery"
		}),
		new ExtractTextPlugin("resources/admin/bundle.css"),
		new CopyWebpackPlugin([
			{ from: 'node_modules/bootstrap/dist/fonts/*', to: 'resources/admin' },
			{ context: 'src/resources', from: 'css/wallride.custom.css', to: 'resources/admin/css' },
			{ context: 'src/resources', from: 'font/**/*', to: 'resources/admin' },
			{ context: 'src/resources', from: 'img/**/*', to: 'resources/admin' },
			{ context: 'src/templates', from: '**/*', to: 'templates/admin' }
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