package net.vvakame.ajn24sample;

import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.junit.Test;
import org.slim3.tester.AppEngineTestCase;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.ApiConfig;
import com.google.apphosting.api.ApiProxy.ApiProxyException;
import com.google.apphosting.api.ApiProxy.Delegate;
import com.google.apphosting.api.ApiProxy.Environment;
import com.google.apphosting.api.ApiProxy.LogRecord;
import com.google.apphosting.api.DatastorePb.PutRequest;

public class ApiProxyDemoTest extends AppEngineTestCase {

	static final Logger logger = Logger.getLogger(ApiProxyDemoTest.class
			.getName());

	@Test
	@SuppressWarnings("unchecked")
	public void delegate() {
		final Delegate<Environment> original = ApiProxy.getDelegate();
		ApiProxy.setDelegate(new DelegateStub(original) {

			@Override
			public byte[] makeSyncCall(Environment environment,
					String packageName, String methodName, byte[] request)
					throws ApiProxyException {
				logger.info("sync packageName=" + packageName + ", methodName="
						+ methodName);
				return super.makeSyncCall(environment, packageName, methodName,
						request);
			}

			@Override
			public Future<byte[]> makeAsyncCall(Environment environment,
					String packageName, String methodName, byte[] request,
					ApiConfig apiConfig) {
				logger.info("async packageName=" + packageName
						+ ", methodName=" + methodName);
				return super.makeAsyncCall(environment, packageName,
						methodName, request, apiConfig);
			}
		});

		final DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Entity entity = new Entity("sample");
		// INFO: async packageName=datastore_v3, methodName=Put
		datastore.put(entity);

		ApiProxy.setDelegate(original);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deserialize() {
		final Delegate<Environment> original = ApiProxy.getDelegate();
		ApiProxy.setDelegate(new DelegateStub(original) {

			@Override
			public Future<byte[]> makeAsyncCall(Environment environment,
					String packageName, String methodName, byte[] request,
					ApiConfig apiConfig) {
				if ("datastore_v3".equals(packageName)
						&& "Put".equals(methodName)) {
					PutRequest requestPb = new PutRequest();
					requestPb.mergeFrom(request);
					logger.info(requestPb.toString());
				}
				logger.info("async packageName=" + packageName
						+ ", methodName=" + methodName);
				return super.makeAsyncCall(environment, packageName,
						methodName, request, apiConfig);
			}
		});

		final DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Entity entity = new Entity("sample");
		// INFO: async packageName=datastore_v3, methodName=Put
		datastore.put(entity);

		ApiProxy.setDelegate(original);
	}

	static class DelegateStub implements Delegate<Environment> {
		final Delegate<Environment> original;

		public DelegateStub(Delegate<Environment> original) {
			this.original = original;
		}

		@Override
		public byte[] makeSyncCall(Environment environment, String packageName,
				String methodName, byte[] request) throws ApiProxyException {
			return original.makeSyncCall(environment, packageName, methodName,
					request);
		}

		@Override
		public Future<byte[]> makeAsyncCall(Environment environment,
				String packageName, String methodName, byte[] request,
				ApiConfig apiConfig) {
			return original.makeAsyncCall(environment, packageName, methodName,
					request, apiConfig);
		}

		@Override
		public void flushLogs(Environment environment) {
			original.flushLogs(environment);
		}

		@Override
		public List<Thread> getRequestThreads(Environment environment) {
			return original.getRequestThreads(environment);
		}

		@Override
		public void log(Environment environment, LogRecord record) {
			original.log(environment, record);
		}
	}
}
