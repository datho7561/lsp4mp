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
package org.eclipse.lsp4mp.commons;

/**
 * Represents context related to the cursor location in the given document.
 */
public class JavaCursorContextResult {

	private JavaCursorContextKind kind;

	public JavaCursorContextResult(JavaCursorContextKind kind) {
		this.kind = kind;
	}

	public JavaCursorContextResult() {
		this(null);
	}

	/**
	 * Returns the context of the cursor in the Java file or <code>NONE</code> if
	 * none of the contexts apply.
	 *
	 * For instance, it returns <code>IN_METHOD_ANNOTATIONS</code> if the cursor is
	 * in the list of annotations before a method declaration.
	 *
	 * @return the context of the cursor in the Java file
	 */
	public JavaCursorContextKind getKind() {
		return kind;
	}

}
