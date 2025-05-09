/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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
package org.eclipse.lsp4mp.jdt.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;

/**
 * Abstract class for properties provider based on type declaration (class,
 * interface, annotation type, etc) search.
 *
 * @author Angelo ZERR
 *
 */
public abstract class AbstractTypeDeclarationPropertiesProvider extends AbstractPropertiesProvider {

	private static final Logger LOGGER = Logger.getLogger(AbstractTypeDeclarationPropertiesProvider.class.getName());

	@Override
	protected String[] getPatterns() {
		return getTypeNames();
	}

	/**
	 * Returns the type names to search.
	 *
	 * @return the type names to search.
	 */
	protected abstract String[] getTypeNames();

	@Override
	public void collectProperties(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		Object element = match.getElement();
		if (element instanceof IType) {
			IType type = (IType) element;
			String className = type.getFullyQualifiedName();
			String[] names = getTypeNames();
			for (String name : names) {
				if (name.equals(className)) {
					try {
						// The provider matches the annotation based
						if (isAlreadyProcessed(type, context)) {
							// The processAnnotation has already been done for the Java type
							return;
						}
						// Collect properties from the class name and stop the loop.
						processClass(type, className, context, monitor);
						break;
					} catch (Exception e) {
						if (LOGGER.isLoggable(Level.SEVERE)) {
							LOGGER.log(Level.SEVERE,
									"Cannot compute MicroProfile properties for the Java class '" + className + "'.",
									e);
						}
					}
				}
			}
		}
	}

	protected abstract void processClass(IType type, String className, SearchContext context, IProgressMonitor monitor)
			throws JavaModelException;
}
