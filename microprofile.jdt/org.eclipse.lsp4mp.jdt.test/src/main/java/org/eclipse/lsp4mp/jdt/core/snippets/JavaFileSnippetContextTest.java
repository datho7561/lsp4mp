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
package org.eclipse.lsp4mp.jdt.core.snippets;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4mp.commons.JavaCursorContextKind;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCompletionParams;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.PropertiesManagerForJava;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for the implementation of <code>microprofile/java/snippetCursorContext</code>.
 */
public class JavaFileSnippetContextTest extends BasePropertiesManagerTest {

	private static final IProgressMonitor MONITOR = new NullProgressMonitor();

	@After
	public void cleanUp() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/Empty.java"));
		javaFile.refreshLocal(IResource.DEPTH_ZERO, null);
		javaFile.setContents(new ByteArrayInputStream("".getBytes()), 0, MONITOR);
	}

	@Test
	public void testEmptyFileContext() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/Empty.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		MicroProfileJavaCompletionParams params = new MicroProfileJavaCompletionParams(javaFileUri, new Position(0, 0));
		assertEquals(JavaCursorContextKind.IN_EMPTY_FILE, PropertiesManagerForJava.getInstance().javaCursorContext(params, JDT_UTILS, MONITOR).getKind());
	}

	@Test
	public void testJustSnippetFileContext() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/Empty.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();
		javaFile.refreshLocal(IResource.DEPTH_ZERO, null);
		javaFile.setContents(new ByteArrayInputStream("rest_class".getBytes()), 0, MONITOR);

		MicroProfileJavaCompletionParams params = new MicroProfileJavaCompletionParams(javaFileUri, new Position(0, 0));
		assertEquals(JavaCursorContextKind.IN_EMPTY_FILE, PropertiesManagerForJava.getInstance().javaCursorContext(params, JDT_UTILS, MONITOR).getKind());
	}

	@Test
	public void testBeforeFieldContext() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		MicroProfileJavaCompletionParams params = new MicroProfileJavaCompletionParams(javaFileUri, new Position(15, 4));
		assertEquals(JavaCursorContextKind.IN_FIELD_ANNOTATIONS, PropertiesManagerForJava.getInstance().javaCursorContext(params, JDT_UTILS, MONITOR).getKind());

		params = new MicroProfileJavaCompletionParams(javaFileUri, new Position(14, 4));
		assertEquals(JavaCursorContextKind.BEFORE_FIELD, PropertiesManagerForJava.getInstance().javaCursorContext(params, JDT_UTILS, MONITOR).getKind());
	}

	@Test
	public void testBeforeMethodContext() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		MicroProfileJavaCompletionParams params = new MicroProfileJavaCompletionParams(javaFileUri, new Position(34, 4));
		assertEquals(JavaCursorContextKind.IN_METHOD_ANNOTATIONS, PropertiesManagerForJava.getInstance().javaCursorContext(params, JDT_UTILS, MONITOR).getKind());

		params = new MicroProfileJavaCompletionParams(javaFileUri, new Position(32, 4));
		assertEquals(JavaCursorContextKind.BEFORE_METHOD, PropertiesManagerForJava.getInstance().javaCursorContext(params, JDT_UTILS, MONITOR).getKind());
	}

	@Test
	public void testInMethodContext() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		MicroProfileJavaCompletionParams params = new MicroProfileJavaCompletionParams(javaFileUri, new Position(35, 0));
		assertEquals(JavaCursorContextKind.NONE, PropertiesManagerForJava.getInstance().javaCursorContext(params, JDT_UTILS, MONITOR).getKind());

		params = new MicroProfileJavaCompletionParams(javaFileUri, new Position(34, 5));
		assertEquals(JavaCursorContextKind.NONE, PropertiesManagerForJava.getInstance().javaCursorContext(params, JDT_UTILS, MONITOR).getKind());
	}

	@Test
	public void testInClassContext() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		MicroProfileJavaCompletionParams params = new MicroProfileJavaCompletionParams(javaFileUri, new Position(37, 0));
		assertEquals(JavaCursorContextKind.IN_CLASS, PropertiesManagerForJava.getInstance().javaCursorContext(params, JDT_UTILS, MONITOR).getKind());
	}

	@Test
	public void testAfterClassContext() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		MicroProfileJavaCompletionParams params = new MicroProfileJavaCompletionParams(javaFileUri, new Position(38, 0));
		assertEquals(JavaCursorContextKind.NONE, PropertiesManagerForJava.getInstance().javaCursorContext(params, JDT_UTILS, MONITOR).getKind());
	}

}
