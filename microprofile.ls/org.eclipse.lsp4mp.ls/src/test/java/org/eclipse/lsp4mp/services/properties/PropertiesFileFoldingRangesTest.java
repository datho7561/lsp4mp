/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.lsp4mp.services.properties;

import org.junit.Test;

/**
 * Tests for folding ranges in properties files
 *
 * @author datho7561
 */
public class PropertiesFileFoldingRangesTest {

	// TODO: actually write assertions

	@Test
	public void blockComment() {
		String file = "# hello\n" + //
				"# this is a block comment\n" + //
				"# it is three lines long\n";
	}

	@Test
	public void blockOfProperties() {
		String file = "microprofile.name = Micro Profile\n" + //
				"microprofile.port = 8080\n" + //
				"microprofile.authentication = none\n";
	}

	@Test
	public void regions() {
		String file = "#region\n" + //
				"# region\n" + //
				"\n" + //
				"# endregion\n" + //
				"\n" + //
				"# region named\n" + //
				"\n" + //
				"# endregion named\n" + //
				"\n" + //
				"# regional manager\n" + //
				"\n" + //
				"# endregional manager\n" + //
				"\n" + //
				"#endregion";
	}

	@Test
	public void commentBeforeProperties() {
		String file = "# this is a comment\n" + //
				"microprofile.name = Micro Profile\n" + //
				"microprofile.port = 8080\n" + //
				"microprofile.authentication = none\n";
	}

	@Test
	public void twoBlocksOfProperties() {
		String file = "microprofile.name = Micro Profile\n" + //
				"microprofile.port = 8080\n" + //
				"database.name = PostgreSQL\n" + //
				"database.port = 3000\n";
	}

}
