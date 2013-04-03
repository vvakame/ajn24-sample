package net.vvakame.ajn24sample.service;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.slim3.tester.AppEngineTestCase;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class MemcacheSampleTest extends AppEngineTestCase {

	@Test
	public void cacheEntity() throws EntityNotFoundException {
		final DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		final MemcacheService memcache = MemcacheServiceFactory
				.getMemcacheService();

		Key key;
		{ // 保存時にMemcacheに突っ込んでおこう
			Entity entity = new Entity("sample");
			entity.setProperty("str", "Hello memcache!");
			datastore.put(entity);
			key = entity.getKey();

			memcache.put(key, entity);
		}
		{ // 読出時にMemcacheをまずチェック！
			// あるかなー…？
			Entity entity = (Entity) memcache.get(key);
			if (entity == null) {
				// なかったわー
				entity = datastore.get(key);
			}
		}
	}

	@Test
	public void cacheQuery() throws EntityNotFoundException {
		final DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		{
			Entity entity = new Entity("sample");
			entity.setProperty("str", "Hi!");
			datastore.put(entity);
		}
		{
			Entity entity = new Entity("sample");
			entity.setProperty("str", "Hello!");
			datastore.put(entity);
		}

		final MemcacheService memcache = MemcacheServiceFactory
				.getMemcacheService();
		{ // 初回はキャッシュされていない
			Query query = new Query("sample");
			@SuppressWarnings("unchecked")
			List<Entity> list = (List<Entity>) memcache.get(query);
			if (list == null) {
				list = datastore.prepare(query).asList(
						FetchOptions.Builder.withDefaults());
				memcache.put(query, list);
			}
			assertThat("2件検索される", list.size(), is(2));
		}
		{ // 2回目はMemcacheから読み出せる
			Query query = new Query("sample");
			@SuppressWarnings("unchecked")
			List<Entity> list = (List<Entity>) memcache.get(query);
			if (list == null) {
				list = datastore.prepare(query).asList(
						FetchOptions.Builder.withDefaults());
				memcache.put(query, list);
			}
			assertThat("2件検索される", list.size(), is(2));
		}
	}

	@Test
	public void cacheQueryWithCleanup() throws EntityNotFoundException {
		final DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();

		{
			Entity entity = new Entity("sample");
			entity.setProperty("str", "Hi!");
			datastore.put(entity);
		}
		{
			Entity entity = new Entity("sample");
			entity.setProperty("str", "Hello!");
			datastore.put(entity);
		}

		final MemcacheService memcache = MemcacheServiceFactory
				.getMemcacheService();
		{ // 初回はキャッシュされていない
			Query query = new Query("sample");
			@SuppressWarnings("unchecked")
			List<Entity> list = (List<Entity>) memcache.get(query);
			if (list == null) {
				list = datastore.prepare(query).asList(
						FetchOptions.Builder.withDefaults());
				memcache.put(query, list);
			}
			assertThat("2件検索される", list.size(), is(2));
		}

		{
			Entity entity = new Entity("sample");
			entity.setProperty("str", "Good night!");
			datastore.put(entity);

			// sample kind に Put があったらキャッシュ消す
			Query query = new Query("sample");
			memcache.delete(query);
			@SuppressWarnings("unchecked")
			List<Entity> list = (List<Entity>) memcache.get(query);
			assertThat("キャッシュ消去済", list, nullValue());
		}
	}
}
