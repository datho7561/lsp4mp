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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4mp.commons.runtime.MicroProfileProjectRuntime;
import org.eclipse.lsp4mp.commons.runtime.MicroProfileRuntimeSupport;

/**
 * ConverterRuntimeSupport allows dynamic validation of string values against
 * Java types using MicroProfile Config converters from the project classpath.
 *
 * <p>
 * This class uses reflection to avoid classloader issues and caches converters
 * per type for performance. Only converters discovered via
 * Config.getConverter(Class) are used.
 * </p>
 * 
 * @author Angelo ZERR
 */
public class ConverterRuntimeSupport extends MicroProfileRuntimeSupport {

	private static final Logger LOGGER = Logger.getLogger(ConverterRuntimeSupport.class.getName());

	private static final String[] DEFAULT_RESOLVERS = { "io.smallrye.config.SmallRyeConfigProviderResolver" };

	private Object config;
	private boolean initialized;

	/** Cache of ConverterInvoker per type */
	private final Map<String, ConverterValidator> converterCache = new ConcurrentHashMap<>();

	public ConverterRuntimeSupport(MicroProfileProjectRuntime project) {
		super(project);
	}

	public void validate(String value, String type, DiagnosticsCollector collector) {
		try {
			Object cfg = getConfigReflect();
			if (cfg == null) {
				return;
			}
			// Get or prepare the converter invoker
			ConverterValidator validator = converterCache.computeIfAbsent(type,
					t -> Converters.resolveConverter(getProject().getClass(t), cfg));
			if (validator.canValidate()) {
				validator.validate(value, collector);
			}
		} catch (Throwable e) {
			LOGGER.log(Level.WARNING, "Error while validating '" + value + "' value with type '" + type + "", e);
		}
	}

	/**
	 * Lazily obtains a Config instance via reflection.
	 */
	private synchronized Object getConfigReflect() {
		if (config != null || initialized) {
			return config;
		}

		Object cfg = null;
		Object resolver = loadConfigProviderResolverReflect();

		if (resolver != null) {
			try {
				Method getBuilder = resolver.getClass().getMethod("getBuilder");
				Object builder = getBuilder.invoke(resolver);

				builder.getClass().getMethod("forClassLoader", ClassLoader.class).invoke(builder,
						getProject().getRuntimeClassLoader());

				builder.getClass().getMethod("addDiscoveredConverters").invoke(builder);

				cfg = builder.getClass().getMethod("build").invoke(builder);

			} catch (Throwable e) {
				LOGGER.log(Level.INFO,
						"Error creating MicroProfile Config via reflection from " + resolver.getClass().getName(), e);
			}
		}

		config = cfg;
		initialized = true;
		return config;
	}

	/**
	 * Loads the first available ConfigProviderResolver.
	 */
	private Object loadConfigProviderResolverReflect() {
		ClassLoader runtimeCL = getProject().getRuntimeClassLoader();
		if (runtimeCL == null)
			return null;

		List<Object> resolvers = new ArrayList<>();
		for (String className : DEFAULT_RESOLVERS) {
			try {
				Class<?> cls = Class.forName(className, false, runtimeCL);
				resolvers.add(cls.getConstructor().newInstance());
				LOGGER.info("Loaded ConfigProviderResolver: " + className);
			} catch (ClassNotFoundException e) {
				// ignore
			} catch (Throwable t) {
				LOGGER.log(Level.INFO, "Cannot instantiate ConfigProviderResolver: " + className, t);
			}
		}

		if (resolvers.isEmpty()) {
			try {
				Class<?> resolverClass = Class.forName("org.eclipse.microprofile.config.spi.ConfigProviderResolver",
						true, runtimeCL);
				ServiceLoader<?> loader = ServiceLoader.load(resolverClass, runtimeCL);
				for (Object provider : loader) {
					resolvers.add(provider);
					LOGGER.info("Loaded ConfigProviderResolver via ServiceLoader: " + provider.getClass().getName());
				}
			} catch (Throwable t) {
				LOGGER.log(Level.INFO, "ServiceLoader scan failed for ConfigProviderResolver", t);
			}
		}

		if (resolvers.isEmpty()) {
			LOGGER.warning("No ConfigProviderResolver found in project classpath");
			return null;
		}

		return resolvers.get(0);
	}

	/**
	 * Returns true if classpath hosts an implementation of MicroProfile
	 * ConfigProviderResolver and false otherwise.
	 * 
	 * @return true if classpath hosts an implementation of MicroProfile
	 *         ConfigProviderResolver and false otherwise.
	 */
	public boolean hasConfigProviderResolver() {
		getConfigReflect();
		return initialized && config != null;
	}

	/**
	 * Reset cached config and converters.
	 */
	@Override
	protected void reset() {
		config = null;
		initialized = false;
		converterCache.clear();
	}

}
