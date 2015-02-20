package org.wallride.core.support;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.jgroups.Address;
import org.jgroups.annotations.Property;
import org.jgroups.protocols.FILE_PING;
import org.jgroups.protocols.PingData;
import org.jgroups.util.Responses;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class S3_CLIENT_PING extends FILE_PING {

	@Property(description = "Skip the code which checks if a bucket exists in initialization")
	protected boolean skip_bucket_existence_check = false;

	protected AmazonS3 amazonS3;

	public void init() throws Exception {
		super.init();
		amazonS3 = new AmazonS3Client();
		if (!skip_bucket_existence_check && !amazonS3.doesBucketExist(location)) {
			amazonS3.createBucket(location);
		}
	}

	@Override
	protected void createRootDir() {
		// do *not* create root file system (don't remove !)
	}

	@Override
	protected void readAll(List<Address> members, String clustername, Responses responses) {
		if (clustername == null) {
			return;
		}

		try {
			clustername = sanitize(clustername);
			ObjectListing objectListing = amazonS3.listObjects(location, clustername);
			for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
				try {
					readResponse(summary, members, responses);
				} catch (Throwable t) {
					log.error("failed reading key %s: %s", summary.getKey(), t);
				}

			}
		} catch (AmazonServiceException ex) {
			log.error("failed reading addresses", ex);
		}
	}

	protected void readResponse(S3ObjectSummary summary, List<Address> members, Responses responses) {
		S3Object object = amazonS3.getObject(summary.getBucketName(), summary.getKey());
		List<PingData> list;
		try {
			list = read(object.getObjectContent());
			if (list != null) {
				for (PingData data : list) {
					if (members == null || members.contains(data.getAddress())) {
						responses.addResponse(data, data.isCoord());
					}
					if (local_addr != null && !local_addr.equals(data.getAddress())) {
						addDiscoveryResponseToCaches(data.getAddress(), data.getLogicalName(), data.getPhysicalAddr());
					}
				}
			}
		} catch (Throwable e) {
			log.error("failed unmarshalling response", e);
		}
	}

	@Override
	protected void write(List<PingData> list, String clustername) {
		String filename = addressToFilename(local_addr);
		String key = sanitize(clustername) + "/" + sanitize(filename);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
			write(list, out);
			byte[] data = out.toByteArray();

			ObjectMetadata meta = new ObjectMetadata();
			meta.setContentType("text/plain");
			meta.setContentLength(data.length);
			amazonS3.putObject(location, key, new ByteArrayInputStream(data), meta);
		} catch (Exception e) {
			log.error("Error marshalling object", e);
		}
	}

	@Override
	protected void remove(String clustername, Address addr) {
		if (clustername == null || addr == null) {
			return;
		}
		String filename = addressToFilename(addr);//  addr instanceof org.jgroups.util.UUID? ((org.jgroups.util.UUID)addr).toStringLong() : addr.toString();
		String key = sanitize(clustername) + "/" + sanitize(filename);
		try {
			amazonS3.deleteObject(location, key);
			if (log.isTraceEnabled()) {
				log.trace("removing " + location + "/" + key);
			}
		} catch (Exception e) {
			log.error("failure removing data", e);
		}
	}

	protected static String sanitize(final String name) {
		String retval = name;
		retval = retval.replace('/', '-');
		retval = retval.replace('\\', '-');
		return retval;
	}
}





