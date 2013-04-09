package net.vvakame.ajn24sample;

import net.vvakame.memvache.MemvacheDelegate;
import net.vvakame.memvache.Pair;
import net.vvakame.memvache.RpcVisitor;
import net.vvakame.memvache.Strategy;

import org.junit.Before;
import org.junit.Test;
import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Entity;
import com.google.apphosting.api.DatastorePb.PutRequest;
import com.google.apphosting.api.DatastorePb.PutResponse;

public class OreOreStrategyTest extends MemvacheAppEngineTestCase {

	@Test
	public void test() {
		Datastore.put(new Entity("sample"));
	}

	@Before
	@Override
	public void setUp() throws Exception {
		MemvacheDelegate.addStrategy(OreOre1Strategy.class);
		MemvacheDelegate.addStrategy(OreOre2Strategy.class);

		super.setUp();
	}

	public static class OreOre1Strategy implements Strategy {

		@Override
		public Pair<byte[], byte[]> preProcess(String packageName,
				String method, byte[] request) {
			// Pair.request でリクエストを書き換える
			// Pair.response でレスポンスを生成して返す
			// null を返して何もしない
			return null;
		}

		@Override
		public byte[] postProcess(String packageName, String method,
				byte[] request, byte[] response) {
			// レスポンスを書き換えて返す
			// null を返して何もしない
			return null;
		}
	}

	public static class OreOre2Strategy extends RpcVisitor {

		@Override
		public Pair<byte[], byte[]> pre_datastore_v3_Put(PutRequest requestPb) {
			// packageName, method ごとに変換済のオブジェクトが渡される。後は Strategy と変わらない。
			return null;
		}

		@Override
		public byte[] post_datastore_v3_Put(PutRequest requestPb,
				PutResponse responsePb) {
			// 同上 足りないものは pull request 待ってます✩
			return null;
		}
	}
}
