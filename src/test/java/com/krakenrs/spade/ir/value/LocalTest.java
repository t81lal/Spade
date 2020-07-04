package com.krakenrs.spade.ir.value;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class LocalTest {

	@Test
	public void testUnversionedLocal() {
		Local uLocal = new Local(0, true);
		assertFalse(uLocal.isVersioned());
		assertThrows(UnsupportedOperationException.class, () -> uLocal.version());
		assertEquals("svar0", uLocal.toString());
	}
	
	@Test
	public void testVersionedLocal() {
		Local vLocal = new Local(0, true, 0);
		assertTrue(vLocal.isVersioned());
		assertEquals(0, vLocal.version());
		assertEquals("svar0_0", vLocal.toString());
	}
	
	@Test
	public void testVersioningEquals() {
		Local uLocal = new Local(0, true);
		Local vLocal = new Local(0, true, 0);
		
		assertFalse(uLocal.equals(vLocal));
		assertFalse(vLocal.equals(uLocal));
	}
}
