package sun.net.www.protocol.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {

	protected URLConnection openConnection(URL url) throws IOException {

		return new URLConnection(url) {

			@Override
			public InputStream getInputStream() throws IOException {
				String accessKey = null;
				String secretKey = null;

				if (url.getUserInfo() != null) {
					String[] credentials = url.getUserInfo().split("[:]");
					accessKey = credentials[0];
					secretKey = credentials[1];
				}

				String bucket = url.getHost().substring(0, url.getHost().indexOf("."));
				String key = url.getPath().substring(1);

				try {
					AmazonS3 amazonS3;
					if (accessKey != null && secretKey != null) {
						amazonS3 = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
					} else {
						amazonS3 = new AmazonS3Client();
					}
					S3Object object = amazonS3.getObject(bucket, key);
					return object.getObjectContent();
				} catch (AmazonClientException e) {
					throw new IOException(e);
				}
			}

			@Override
			public void connect() throws IOException {
			}
		};
	}
}