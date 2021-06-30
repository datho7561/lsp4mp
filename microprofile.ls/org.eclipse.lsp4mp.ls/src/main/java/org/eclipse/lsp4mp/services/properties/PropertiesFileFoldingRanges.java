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

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.ls.commons.TextDocument;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;

/**
 * Calculates folding ranges for microprofile-config.properties files
 *
 * Handles:
 *  * region/endregion comments
 *  * block comments
 *  * properties on consecutive lines that share the same prefix
 *    (i.e. the substring up until the first period)
 *
 * @author datho7561
 */
public class PropertiesFileFoldingRanges {

	private static final Logger LOGGER = Logger.getLogger(PropertiesFileFoldingRanges.class.getName());

	private static final Pattern BEGIN_REGION_PTN = Pattern.compile("# ?region(?: .*)?$");
	private static final Pattern END_REGION_PTN = Pattern.compile("# ?endregion(?: .*)?$");

	public List<FoldingRange> findFoldingRanges(PropertiesModel document) {

		Deque<State> stateStack = new LinkedList<>();
		List<FoldingRange> ranges = new ArrayList<>();
		Node node;
		TextDocument textDocument = document.getDocument();
		int lineNumber = 0;

		try {
			for (int i = 0; i < document.getChildren().size(); i++) {

				node = document.getChildren().get(i);
				int newLineNumber = textDocument.positionAt(node.getStart()).getLine();
				// Model doesn't capture newlines
				if (newLineNumber - lineNumber > 1) {
					while (stateStack.peek() != null && stateStack.peek().getKind() != Kind.Region) {
						ranges.add(new FoldingRange(stateStack.peek().getLineNumber(), lineNumber));
						stateStack.pop();
					}
				}
				lineNumber = newLineNumber;

				switch (node.getNodeType()) {
					case COMMENTS: {
						String text = node.getText();
						Matcher m = BEGIN_REGION_PTN.matcher(text);
						boolean isBeginRegion = m.find();
						m = END_REGION_PTN.matcher(text);
						boolean isEndRegion = m.matches();
						if (isEndRegion) {
							while (stateStack.peek() != null && stateStack.peek().getKind() != Kind.Region) {
								ranges.add(new FoldingRange(stateStack.peek().getLineNumber(), lineNumber));
								stateStack.pop();
							}
							if (stateStack.peek() != null) {
								ranges.add(new FoldingRange(stateStack.peek().getLineNumber(), lineNumber));
								stateStack.pop();
							}
						} else if (isBeginRegion) {
							while (stateStack.peek() != null && stateStack.peek().getKind() != Kind.Region) {
								ranges.add(new FoldingRange(stateStack.peek().getLineNumber(), lineNumber));
								stateStack.pop();
							}
							stateStack.push(new State(lineNumber, Kind.Region, null));
						} else {
							if (stateStack.peek() != null && stateStack.peek().getKind() != Kind.Region) {
								if (stateStack.peek().getKind() != Kind.Comment) {
									while (stateStack.peek() != null && stateStack.peek().getKind() == Kind.Prefix) {
										ranges.add(new FoldingRange(stateStack.peek().getLineNumber(), lineNumber));
										stateStack.pop();
									}
									stateStack.push(new State(lineNumber, Kind.Comment, null));
								}
							} else {
								stateStack.push(new State(lineNumber, Kind.Comment, null));
							}
						}
						break;
					}
					case PROPERTY: {
						Property property = (Property) node;
						String prefix = getPrefixFromProperty(property);
						if (stateStack.peek() != null) {
							if (stateStack.peek().getKind() == Kind.Comment) {
								ranges.add(new FoldingRange(stateStack.peek().getLineNumber(), lineNumber - 1));
								stateStack.pop();
								stateStack.push(new State(lineNumber, Kind.Prefix, prefix));
							} else if (stateStack.peek().getKind() == Kind.Region) {
								stateStack.push(new State(lineNumber, Kind.Prefix, prefix));
							} else {
								// if the prefix is the same, don't end it, otherwise end it
								if (!stateStack.peek().getPrefix().equals(prefix)) {
									ranges.add(new FoldingRange(stateStack.peek().getLineNumber(), lineNumber - 1));
									stateStack.pop();
									stateStack.push(new State(lineNumber, Kind.Prefix, prefix));
								}
							}
						} else {
							stateStack.push(new State(lineNumber, Kind.Prefix, prefix));
						}
						break;
					}
					default:
						break;
				}

			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Passed bad properties model", e);
		}

		ranges.removeIf(range -> range.getStartLine() == range.getEndLine());

		return ranges;
	}

	private class State {

		private final int lineNumber;
		private final Kind kind;
		private final String prefix;

		State(int lineNumber, Kind kind, String prefix) {
			if (lineNumber < 0) {
				throw new IllegalArgumentException("Line number must be positive");
			}
			if (kind == Kind.Prefix && prefix == null) {
				throw new IllegalArgumentException("Must specify the prefix");
			} else if (kind != Kind.Prefix && prefix != null) {
				throw new IllegalArgumentException("Can only specify prefix for prefix kind");
			}
			this.lineNumber = lineNumber;
			this.kind = kind;
			this.prefix = prefix;
		}

		public Kind getKind() {
			return kind;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public String getPrefix() {
			return prefix;
		}

	}

	private static enum Kind {
		Comment, Prefix, Region
	}

	private static final String getPrefixFromProperty(Property property) {
		String key = property.getPropertyKey();
		int end = key.indexOf(".");
		end = end < 0 ? key.length() : end;
		return key.substring(0, end);
	}

}
