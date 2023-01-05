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
 * Represents the context where the cursor is in a Java file.
 */
public enum JavaCursorContextKind {

	/**
	 * The cursor is in a file that does not have a root type declaration.
	 */
	IN_EMPTY_FILE(1),

	/**
	 * The cursor is before a type declaration body, either at the root of a file or
	 * within another class. The cursor is before any annotations on the type.
	 */
	BEFORE_CLASS(2),

	/**
	 * The cursor is in a type declaration body, and the next declaration in the
	 * body is a method declaration. The cursor is before any annotations on the
	 * method.
	 */
	BEFORE_METHOD(3),

	/**
	 * The cursor is in a type declaration body, and the next declaration in the
	 * body is a field declaration. The cursor is before any annotations on the
	 * field.
	 */
	BEFORE_FIELD(4),

	/**
	 * The cursor is before a type declaration body, either at the root of a file or
	 * within another class. The cursor is somewhere within the annotation
	 * declarations on the class.
	 */
	IN_CLASS_ANNOTATIONS(5),

	/**
	 * The cursor is in a type declaration body, and the next declaration in the
	 * body is a method declaration. The cursor is somewhere within the annotation
	 * declarations on the method.
	 */
	IN_METHOD_ANNOTATIONS(6),

	/**
	 * The cursor is in a type declaration body, and the next declaration in the
	 * body is a field declaration. The cursor is somewhere within the annotation
	 * declarations on the field.
	 */
	IN_FIELD_ANNOTATIONS(7),

	/**
	 * The cursor is in a type declaration body, after all the declarations for the
	 * type.
	 */
	IN_CLASS(8),

	/**
	 * None of the above context apply.
	 */
	NONE(2000);

	private final int value;

	private JavaCursorContextKind(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static JavaCursorContextKind forValue(int value) {
		JavaCursorContextKind[] allValues = JavaCursorContextKind.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

}
