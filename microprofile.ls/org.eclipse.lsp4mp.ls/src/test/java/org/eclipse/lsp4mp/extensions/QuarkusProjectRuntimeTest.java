package org.eclipse.lsp4mp.extensions;

import static org.eclipse.lsp4mp.services.properties.PropertiesFileAssert.QUARKUS_PROJECT_RUNTIME;

import org.junit.Test;

/**
 * Test MicroProfile converter validation with Quarkus project runtime
 * (Converters are registered with SmallRye).
 */
public class QuarkusProjectRuntimeTest extends AbstractProjectRuntimeTest {

	public QuarkusProjectRuntimeTest() {
		super(QUARKUS_PROJECT_RUNTIME);
	}

	@Test
	public void testInt() {
		assertValiateWithConverter("1", "int");
		assertValiateWithConverter("1X", "int", "SRCFG00029: Expected an integer value, got \"1X\"");
	}

	@Test
	public void testInteger() {
		assertValiateWithConverter("1", "java.lang.Integer");
		assertValiateWithConverter("1X", "java.lang.Integer", "SRCFG00029: Expected an integer value, got \"1X\"");
	}

	@Test
	public void testIntArray() {
		assertValiateWithConverter("1,2,3,4", "int[]");
		assertValiateWithConverter("1,2X,3x,4", "int[]", //
				"SRCFG00029: Expected an integer value, got \"2X\"", //
				"SRCFG00029: Expected an integer value, got \"3x\"");
	}

	@Test
	public void testIntegerArray() {
		assertValiateWithConverter("1,2,3,4", "java.lang.Integer[]");
		assertValiateWithConverter("1,2X,3x,4", "java.lang.Integer[]", //
				"SRCFG00029: Expected an integer value, got \"2X\"", //
				"SRCFG00029: Expected an integer value, got \"3x\"");
	}

	@Test
	public void testIntegerList() {
		assertValiateWithConverter("1,2,3,4", "java.util.List<java.lang.Integer>");
		assertValiateWithConverter("1,2X,3x,4", "java.lang.Integer[]", //
				"SRCFG00029: Expected an integer value, got \"2X\"", //
				"SRCFG00029: Expected an integer value, got \"3x\"");
	}
}
