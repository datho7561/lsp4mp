/*******************************************************************************
* Copyright (c) 2025 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.commons.runtime;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing type signatures into reflective {@link Type}
 * instances.
 *
 * <p>
 * This parser supports simple and generic type declarations such as:
 * </p>
 *
 * <pre>
 * java.lang.String
 * java.util.List&lt;java.lang.String&gt;
 * java.util.Map&lt;java.lang.String, java.lang.Integer&gt;
 * </pre>
 *
 * <p>
 * The parser is designed to be lightweight and tolerant — it resolves class
 * names using the provided {@link ClassLoader} and constructs
 * {@link ParameterizedType} instances for generic types.
 * </p>
 *
 * <h2>Examples</h2>
 * 
 * <pre>
 * Type type1 = TypeSignatureParser.parse("java.lang.String");
 * Type type2 = TypeSignatureParser.parse("java.util.List&lt;java.lang.String&gt;");
 * Type type3 = TypeSignatureParser.parse("java.util.Map&lt;java.lang.String, java.lang.Integer&gt;");
 * </pre>
 */
public class TypeSignatureParser {

	/**
	 * Parses the given type signature using the current thread context class
	 * loader.
	 *
	 * @param signature the type signature to parse (e.g.
	 *                  {@code "java.util.List<java.lang.String>"})
	 * @return the corresponding {@link Type} representation
	 * @throws IllegalArgumentException if the type signature is invalid or a class
	 *                                  cannot be found
	 */
	public static Type parse(String signature) {
		return parse(signature, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Parses the given type signature using the specified class loader.
	 *
	 * @param signature   the type signature to parse
	 * @param classLoader the {@link ClassLoader} to use for resolving class names
	 * @return the corresponding {@link Type} representation
	 * @throws IllegalArgumentException if the type signature is invalid or a class
	 *                                  cannot be found
	 */
	public static Type parse(String signature, ClassLoader classLoader) {
		return new Parser(signature, classLoader).parseType();
	}

	/**
	 * Internal recursive parser for interpreting type signatures.
	 */
	private static class Parser {
		private final String s;
		private int pos = 0;
		private final ClassLoader classLoader;

		Parser(String s, ClassLoader classLoader) {
			this.s = s;
			this.classLoader = classLoader;
		}

		/**
		 * Parses a single type, possibly parameterized.
		 *
		 * @return a {@link Type} representing the parsed signature
		 */
		Type parseType() {
			String raw = readIdentifier();
			skipSpaces();

			if (peek() != '<') {
				return loadClass(raw);
			}

			next(); // '<'
			List<Type> args = new ArrayList<>();

			while (true) {
				skipSpaces();
				args.add(parseType());
				skipSpaces();

				if (peek() == ',') {
					next();
					continue;
				}
				break;
			}

			expect('>');
			return parameterized(raw, args.toArray(Type[]::new));
		}

		/**
		 * Reads a fully qualified class name or identifier until a boundary
		 * (whitespace, comma, or angle bracket).
		 *
		 * @return the identifier string
		 */
		String readIdentifier() {
			int start = pos;
			while (pos < s.length() && !isBoundary(s.charAt(pos)))
				pos++;
			return s.substring(start, pos).trim();
		}

		/**
		 * Determines whether a character marks the end of an identifier.
		 *
		 * @param c the character to test
		 * @return {@code true} if it is a boundary character
		 */
		boolean isBoundary(char c) {
			return c == '<' || c == '>' || c == ',' || Character.isWhitespace(c);
		}

		/**
		 * Returns the next character without advancing the cursor.
		 *
		 * @return the next character or {@code '\0'} if the end is reached
		 */
		char peek() {
			return pos < s.length() ? s.charAt(pos) : '\0';
		}

		/**
		 * Advances and returns the next character.
		 *
		 * @return the next character, or -1 if the end is reached
		 */
		int next() {
			return pos < s.length() ? s.charAt(pos++) : -1;
		}

		/**
		 * Skips all whitespace characters.
		 */
		void skipSpaces() {
			while (pos < s.length() && Character.isWhitespace(s.charAt(pos)))
				pos++;
		}

		/**
		 * Expects a specific character at the current position.
		 *
		 * @param c the expected character
		 * @throws IllegalArgumentException if a different character is found
		 */
		void expect(char c) {
			if (peek() != c) {
				throw new IllegalArgumentException("Expected '" + c + "' at position " + pos + " in: " + s);
			}
			pos++;
		}

		/**
		 * Loads a class by its fully qualified name.
		 *
		 * @param name the class name
		 * @return the corresponding {@link Class}
		 * @throws IllegalArgumentException if the class cannot be found
		 */
		Type loadClass(String name) {
			try {
				return Class.forName(name, false, classLoader);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Unknown type: " + name);
			}
		}

		/**
		 * Creates a {@link ParameterizedType} instance for the given raw type and type
		 * arguments.
		 *
		 * @param raw  the raw type name
		 * @param args the type arguments
		 * @return a synthetic {@link ParameterizedType} implementation
		 */
		ParameterizedType parameterized(String raw, Type... args) {
			return new ParameterizedType() {
				@Override
				public Type[] getActualTypeArguments() {
					return args;
				}

				@Override
				public Type getRawType() {
					return loadClass(raw);
				}

				@Override
				public Type getOwnerType() {
					return null;
				}

				@Override
				public String toString() {
					String[] parts = new String[args.length];
					for (int i = 0; i < args.length; i++)
						parts[i] = args[i].toString();
					return raw + "<" + String.join(", ", parts) + ">";
				}
			};
		}
	}
}
