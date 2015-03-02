/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.config.conventions.checkstyle;

import java.util.LinkedList;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class SuppressGeneratedHolder extends Check {

  /**
   * Records a suppression region of a file.
   */
  private static class Region {

    private int firstLine;

    private int firstColumn;

    private int lastLine;

    private int lastColumn;

    /**
     * Constructs a new suppression region entry.
     *
     * @param firstLine
     *          the first line of the suppression region
     * @param firstColumn
     *          the first column of the suppression region
     * @param lastLine
     *          the last line of the suppression region
     * @param lastColumn
     *          the last column of the suppression region
     */
    public Region(final int firstLine, final int firstColumn,
        final int lastLine, final int lastColumn) {
      this.firstLine = firstLine;
      this.firstColumn = firstColumn;
      this.lastLine = lastLine;
      this.lastColumn = lastColumn;
    }

  }

  /**
   * Returns the Java identifier represented by an AST.
   *
   * @param ast
   *          an AST node for an IDENT or DOT
   * @return the Java identifier represented by the given AST subtree
   * @throws IllegalArgumentException
   *           if the AST is invalid
   */
  private static String getIdentifier(final DetailAST ast) {

    if (ast != null) {

      if (ast.getType() == TokenTypes.IDENT) {

        return ast.getText();

      } else if (ast.getType() == TokenTypes.DOT) {

        return SuppressGeneratedHolder.getIdentifier(ast.getFirstChild()) + "."
            + SuppressGeneratedHolder.getIdentifier(ast.getLastChild());

      }
    }

    throw new IllegalArgumentException("Identifier AST expected: " + ast);
  }

  /**
   * Returns the n'th child of an AST node.
   *
   * @param ast
   *          the AST node to get the child of
   * @param index
   *          the index of the child to get
   * @return the n'th child of the given AST node, or {@code null} if none
   */
  private static DetailAST getNthChild(final DetailAST ast, final int index) {

    DetailAST child = ast.getFirstChild();

    if (child != null) {
      for (int i = 0; (i < index) && (child != null); ++i) {
        child = child.getNextSibling();
      }
    }

    return child;
  }

  /**
   * Checks for a suppression of a check with the given source name and location in the last file
   * processed.
   *
   * @param line
   *          the line number of the check
   * @param column
   *          the column number of the check
   * @return whether the check with the given name is suppressed at the given source location
   */
  public static boolean isSuppressed(final int line, final int column) {

    List<Region> regions = GENERATED_REGIONS.get();

    if (regions != null) {

      for (Region region : regions) {

        boolean afterStart = (region.firstLine < line)
            || ((region.firstLine == line) && (region.firstColumn <= column));

        boolean beforeEnd = (region.lastLine > line)
            || ((region.lastLine == line) && (region.lastColumn >= column));

        if (afterStart && beforeEnd) {
          return true;
        }

      }
    }

    return false;
  }

  private static final String JAVAX_ANNOTATION_PREFIX = "javax.annotation.";

  private static final String GENERATED = "Generated";

  /**
   * a thread-local holder for the list of suppression entries for the last file parsed
   */
  private static final ThreadLocal<List<Region>> GENERATED_REGIONS =
      new ThreadLocal<List<Region>>();

  @Override
  public void beginTree(final DetailAST rootAST) {
    GENERATED_REGIONS.set(new LinkedList<Region>());
  }

  @Override
  public int[] getDefaultTokens() {
    return new int[] { TokenTypes.ANNOTATION };
  }

  @Override
  public void visitToken(final DetailAST ast) {

    String identifier = SuppressGeneratedHolder.getIdentifier(
        SuppressGeneratedHolder.getNthChild(ast, 1));

    if (identifier.startsWith(JAVAX_ANNOTATION_PREFIX)) {
      identifier = identifier.substring(JAVAX_ANNOTATION_PREFIX.length());
    }
    if (GENERATED.equals(identifier)) {

      // get target of annotation
      DetailAST targetAST = null;
      DetailAST parentAST = ast.getParent();
      if (parentAST != null) {
        switch (parentAST.getType()) {
        case TokenTypes.MODIFIERS:
        case TokenTypes.ANNOTATIONS:
          parentAST = parentAST.getParent();
          if (parentAST != null) {
            switch (parentAST.getType()) {
            case TokenTypes.ANNOTATION_DEF:
            case TokenTypes.PACKAGE_DEF:
            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
            case TokenTypes.ENUM_DEF:
            case TokenTypes.ENUM_CONSTANT_DEF:
            case TokenTypes.CTOR_DEF:
            case TokenTypes.METHOD_DEF:
            case TokenTypes.PARAMETER_DEF:
            case TokenTypes.VARIABLE_DEF:
              targetAST = parentAST;
              break;

            default:
              // unexpected target type
            }
          }
          break;

        default:
          // unexpected container type
        }
      }
      if (targetAST == null) {
        log(ast, "suppress.generated.invalid.target");
        return;
      }

      // get range of target
      int firstLine = targetAST.getLineNo();
      int firstColumn = targetAST.getColumnNo();

      int lastLine;
      int lastColumn;

      DetailAST nextAST = targetAST.getNextSibling();

      if (nextAST != null) {
        lastLine = nextAST.getLineNo();
        lastColumn = nextAST.getColumnNo() - 1;
      }
      else {
        lastLine = Integer.MAX_VALUE;
        lastColumn = Integer.MAX_VALUE;
      }

      GENERATED_REGIONS.get().add(new Region(firstLine, firstColumn, lastLine, lastColumn));
    }

  }

}
