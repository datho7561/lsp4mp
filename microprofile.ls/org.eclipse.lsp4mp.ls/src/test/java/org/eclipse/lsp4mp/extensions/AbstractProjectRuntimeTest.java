/*******************************************************************************
* Copyright (c) 2025 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.extensions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4mp.commons.runtime.MicroProfileProjectRuntime;
import org.eclipse.lsp4mp.commons.runtime.converter.ConverterRuntimeSupport;
import org.junit.Assert;

/**
 * Abstract class to test project runtime.
 */
public abstract class AbstractProjectRuntimeTest {

	private final MicroProfileProjectRuntime projectRuntime;

	public AbstractProjectRuntimeTest(MicroProfileProjectRuntime projectRuntime) {
		this.projectRuntime = projectRuntime;
	}

	public void assertValiateWithConverter(String value, String type, String... expectedMessages) {
		ConverterRuntimeSupport converterRuntimeSupport = projectRuntime
				.getRuntimeSupport(ConverterRuntimeSupport.class);
		final List<String> actualMessages = new ArrayList<>();
		converterRuntimeSupport.validate(value, type, (errorMessage, source, errorCode, start, end) -> {
			actualMessages.add(errorMessage);
		});
		Assert.assertArrayEquals("", expectedMessages, actualMessages.toArray());
	}
}
