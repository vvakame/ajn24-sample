package net.vvakame.ajn24sample.service;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class MemvacheInstruction1Test extends MemvacheAppEngineTestCase {

	public void test() {
		Key key = Datastore.createKey("sample", "fumble");
		Entity entity = Datastore.get(key);
		entity.toString();
	}
}
