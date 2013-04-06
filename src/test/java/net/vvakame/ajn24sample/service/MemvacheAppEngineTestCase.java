package net.vvakame.ajn24sample.service;

import net.vvakame.memvache.MemvacheDelegate;

import org.junit.After;
import org.junit.Before;
import org.slim3.tester.AppEngineTestCase;

public class MemvacheAppEngineTestCase extends AppEngineTestCase {
	MemvacheDelegate delegate;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		delegate = MemvacheDelegate.install();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		delegate.uninstall();

		super.tearDown();
	}
}