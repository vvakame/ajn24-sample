package net.vvakame.ajn24sample.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import net.vvakame.ajn24sample.helper.RpcCounterDelegate;
import net.vvakame.memvache.MemvacheDelegate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slim3.tester.AppEngineTestCase;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class MemvacheSampleTest extends AppEngineTestCase {

	@Test
	public void test() throws EntityNotFoundException {
		final DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		final MemcacheService memcache = MemcacheServiceFactory
				.getMemcacheService();

		Key key;
		{ // 明示的にMemcacheは使っていないですよ。
			Entity entity = new Entity("sample");
			entity.setProperty("str", "Hello memcache!");

			assertThat("作成前", memcache.getStatistics().getItemCount(), is(0L));
			datastore.put(entity);
			assertThat("作成後", memcache.getStatistics().getItemCount(), not(0L));

			key = entity.getKey();
		}
		{ // Memvacheさんありがとう
			Entity entity = datastore.get(key);
			assertThat(entity, notNullValue());
		}
		assertThat("見かけ上のRPC", counter2.countMap.get("datastore_v3@Get"), is(1));
		assertThat("Memvache適用後", counter1.countMap.get("datastore_v3@Get"),
				is(0));
	}

	MemvacheDelegate memvache;
	RpcCounterDelegate counter1;
	RpcCounterDelegate counter2;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		// 下から上の順番で適用される
		counter1 = RpcCounterDelegate.install();
		memvache = MemvacheDelegate.install();
		counter2 = RpcCounterDelegate.install();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		counter2.uninstall();
		memvache.uninstall();
		counter1.uninstall();

		super.tearDown();
	}
}
