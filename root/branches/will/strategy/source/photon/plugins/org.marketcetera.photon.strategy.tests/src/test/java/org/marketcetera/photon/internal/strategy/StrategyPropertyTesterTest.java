package org.marketcetera.photon.internal.strategy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.marketcetera.photon.internal.strategy.Strategy.State;

/* $License$ */

/**
 * Test {@link StrategyPropertyTester}.
 *
 * @author <a href="mailto:will@marketcetera.com">Will Horn</a>
 * @version $Id$
 * @since $Release$
 */
public class StrategyPropertyTesterTest {

	private StrategyPropertyTester mFixture;

	@Before
	public void setUp() {
		mFixture = new StrategyPropertyTester();
	}
	
	@Test
	public void illegalProperty() {
		try {
			mFixture.test(StrategyTest.createTestStrategy(), "abc", null, "STOPPED");
		} catch (Exception e) {
			return;
		}
		fail("Exception expected");
	}
	
	@Test
	public void testProperty() {
		Strategy strategy = StrategyTest.createTestStrategy();
		strategy.setState(State.STOPPED);
		assertTrue(mFixture.test(strategy, "state", null, "STOPPED"));
		assertFalse(mFixture.test(strategy, "state", null, "RUNNING"));
		assertFalse(mFixture.test(strategy, "state", null, "ABC"));
		strategy.setState(State.RUNNING);
		assertTrue(mFixture.test(strategy, "state", null, "RUNNING"));
		assertFalse(mFixture.test(strategy, "state", null, "STOPPED"));
		assertFalse(mFixture.test(strategy, "state", null, "ABC"));
	}

}
