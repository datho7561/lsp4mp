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
package org.eclipse.lsp4mp.commons.runtime.converter;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Utility class for resolving and creating {@link ConverterValidator} instances
 * for different Java types.
 * 
 * <p>
 * This class handles:
 * <ul>
 * <li>Raw types, parameterized types (List, Set, Map, Optional, Supplier)</li>
 * <li>Array types</li>
 * <li>Delegation to {@link ConverterValidatorImpl} for non-generic types</li>
 * <li>Collections via {@link CollectionConverter}</li>
 * </ul>
 * </p>
 */
public class Converters {

	/**
	 * Resolves a converter for the specified type using the provided Config
	 * instance.
	 * 
	 * <p>
	 * Handles collections, maps, optionals, suppliers, and arrays by returning
	 * appropriate {@link ConverterValidator} instances.
	 * </p>
	 * 
	 * @param type   the Java type to resolve a converter for
	 * @param config the MicroProfile Config instance
	 * @return a {@link ConverterValidator} capable of validating values of the
	 *         given type
	 */
	static ConverterValidator resolveConverter(Type type, Object config) {
		Class rawType = rawTypeOf(type);
		if (type instanceof ParameterizedType) {
			Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
			if (rawType == List.class) {
				return newCollectionConverter(resolveConverter(typeArgs[0], config));
			}

			if (rawType == Set.class) {
				return newCollectionConverter(resolveConverter(typeArgs[0], config));
			}

			if (rawType == Map.class) {
				return newMapConverter(resolveConverter(typeArgs[0], config), resolveConverter(typeArgs[1], config));
			}

			if (rawType == Optional.class) {
				return newOptionalConverter(resolveConverter(typeArgs[0], config));
			}

			if (rawType == Supplier.class || "jakarta.inject.Provider".equals(rawType.getName())) {
				return resolveConverter(typeArgs[0], config);
			}
		} else if (rawType != null && rawType.isArray()) {
			return newCollectionConverter(resolveConverter(rawType.getComponentType(), config));
		}

		return new ConverterValidatorImpl(config, type);
	}

	private static ConverterValidator newOptionalConverter(ConverterValidator converter) {
		return converter;
	}

	private static ConverterValidator newMapConverter(ConverterValidator converter, ConverterValidator converter2) {
		return converter;
	}

	private static ConverterValidator newCollectionConverter(ConverterValidator converter) {
		return new CollectionConverter(converter);
	}

	/**
	 * Returns the raw {@link Class} corresponding to a given {@link Type}.
	 * 
	 * @param <T>  the type of the raw class
	 * @param type the Java type
	 * @return the raw {@link Class} of the type, or {@code null} if it cannot be
	 *         determined
	 */
	static <T> Class<T> rawTypeOf(Type type) {
		if (type instanceof Class) {
			return (Class) type;
		} else if (type instanceof ParameterizedType) {
			return rawTypeOf(((ParameterizedType) type).getRawType());
		} else if (type instanceof GenericArrayType) {
			return (Class<T>) Array.newInstance(rawTypeOf(((GenericArrayType) type).getGenericComponentType()), 0)
					.getClass();
		} else {
			return null;
		}
	}

	/**
	 * Converter implementation for collections (List, Set, array) that delegates
	 * validation to an underlying element converter.
	 */
	static class CollectionConverter implements ConverterValidator {

		private ConverterValidator delegate;

		/**
		 * Creates a new collection converter delegating to the given element converter.
		 * 
		 * @param delegate the element converter
		 */
		CollectionConverter(ConverterValidator delegate) {
			this.delegate = delegate;
		}

		/**
		 * Returns {@code true} if the underlying element converter can validate values.
		 */
		@Override
		public boolean canValidate() {
			return delegate.canValidate();
		}

		/**
		 * Validates a comma-separated list of values by delegating each element to the
		 * underlying converter.
		 * 
		 * @param value     the string containing one or more comma-separated elements
		 * @param start     the start offset for diagnostics
		 * @param collector the collector to report validation errors
		 */
		@Override
		public void validate(String value, int start, DiagnosticsCollector collector) {
			int end = 0;

			StringBuilder currentValue = new StringBuilder();
			for (int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				if (c == ',') {
					delegate.validate(currentValue.toString(), start, collector);
					currentValue.setLength(0);
					start = i + 1;
				} else {
					currentValue.append(c);
				}
			}
			if (!currentValue.isEmpty()) {
				delegate.validate(currentValue.toString(), start, collector);
			}
		}
	}

}
