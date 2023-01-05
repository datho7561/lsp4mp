/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.snippets;

/**
 * Represents the type of the content of a snippet.
 */
public enum SnippetContentType {
	CLASS(1), METHOD(2), FIELD(3), METHOD_ANNOTATION(4);

	private final int value;

	private SnippetContentType(int value) {
		this.value = value;
	}

	/**
	 * Returns the integer representation of this SnippetContentType.
	 *
	 * @return the integer representation of this SnippetContentType
	 */
	public int getValue() {
		return value;
	}

	public static SnippetContentType forValue(int value) {
		SnippetContentType[] allValues = SnippetContentType.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}
}
