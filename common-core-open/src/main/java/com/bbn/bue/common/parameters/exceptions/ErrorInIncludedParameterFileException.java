package com.bbn.bue.common.parameters.exceptions;

import java.io.File;
import java.util.Stack;

public class ErrorInIncludedParameterFileException extends ParameterFileException {

  private static final long serialVersionUID = 1L;

  private ErrorInIncludedParameterFileException(
      Stack<ErrorInIncludedParameterFileException.ErrorLocation> includeStack,
      Exception rootCause) {
    super(makeMessage(includeStack, rootCause));
    this.includeStack = includeStack;
    this.rootCause = rootCause;
  }

  public static ErrorInIncludedParameterFileException fromException(final File filename,
      final int lineNumber,
      Exception rootCause) {
    Stack<ErrorInIncludedParameterFileException.ErrorLocation> includeStack =
        new Stack<ErrorInIncludedParameterFileException.ErrorLocation>();
    includeStack.push(new ErrorLocation(filename, lineNumber));
    return new ErrorInIncludedParameterFileException(includeStack, rootCause);
  }

  public static ErrorInIncludedParameterFileException fromNextLevel(final File filename,
      final int lineNumber,
      ErrorInIncludedParameterFileException nextLevel) {
    final Stack<ErrorInIncludedParameterFileException.ErrorLocation> includeStack =
        new Stack<ErrorInIncludedParameterFileException.ErrorLocation>();
    includeStack.addAll(nextLevel.includeStack);
    includeStack.push(new ErrorLocation(filename, lineNumber));
    return new ErrorInIncludedParameterFileException(includeStack, nextLevel.rootCause);
  }

  private static String makeMessage(
      Stack<ErrorInIncludedParameterFileException.ErrorLocation> includeStack,
      Exception rootCause) {
    final StringBuilder ret = new StringBuilder();

    while (!includeStack.empty()) {
      final ErrorInIncludedParameterFileException.ErrorLocation errorLocation = includeStack.pop();
      ret.append(String.format("Included from %s:%d\n",
          errorLocation.file.getAbsolutePath(), errorLocation.lineNumber));
    }
    ret.append("Root cause: ").append(rootCause.toString());

    return ret.toString();
  }

  private static final class ErrorLocation {

    public ErrorLocation(File file, int lineNumber) {
      this.file = file;
      this.lineNumber = lineNumber;
    }

    public final File file;
    public final int lineNumber;
  }

  final Exception rootCause;
  final Stack<ErrorInIncludedParameterFileException.ErrorLocation> includeStack;
}
