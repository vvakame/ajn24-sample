package net.vvakame.ajn24sample.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import net.vvakame.ajn24sample.helper.RpcCounterDelegate;
import net.vvakame.memvache.MemvacheDelegate;
import net.vvakame.memvache.Pair;
import net.vvakame.memvache.RpcVisitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slim3.tester.AppEngineTestCase;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.apphosting.api.DatastorePb.NextRequest;

public class AggressiveQueryCacheStrategySampleTest extends AppEngineTestCase {

	@Test
	public void test() {
		final DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		for (int i = 0; i < 111; i++) {
			datastore.put(new Entity("sample"));
		}

		Query query = new Query("sample");
		PreparedQuery prepare = datastore.prepare(query);

		QueryResultList<Entity> list;
		Cursor cursor = null;
		int totalLength = 0;
		do {
			FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
			fetchOptions.prefetchSize(1);
			if (cursor != null) {
				fetchOptions.startCursor(cursor);
			}
			list = prepare.asQueryResultList(fetchOptions);
			totalLength += list.size();
			cursor = list.getCursor();
		} while (list.size() != 0);

		assertThat(totalLength, is(111));
		for (String key : counter.countMap.keySet()) {
			System.out.println("key=" + key + ", count="
					+ counter.countMap.get(key));
		}
	}

	RpcCounterDelegate counter;
	MemvacheDelegate memvache;

	@Before
	@Override
	public void setUp() throws Exception {
		MemvacheDelegate.addStrategy(SniffStrategy.class);
		super.setUp();

		counter = RpcCounterDelegate.install();
		memvache = MemvacheDelegate.install();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		memvache.uninstall();
		counter.uninstall();

		super.tearDown();
	}

	public static class SniffStrategy extends RpcVisitor {

		@Override
		public Pair<byte[], byte[]> pre_datastore_v3_RunQuery(
				com.google.apphosting.api.DatastorePb.Query requestPb) {
			return null;
		}

		@Override
		public Pair<byte[], byte[]> pre_datastore_v3_Next(NextRequest requestPb) {
			System.out.println("==========Next request==========");
			System.out.println(requestPb.toString());
			return null;
		}
	}
}
