package com.bbn.bue.common.parameters;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import java.util.regex.Pattern;

import static junit.framework.TestCase.assertTrue;

public class ParameterAccessListenerTest {

  @Test
  public void testListener() {
    final Parameters params = Parameters.fromMap(
        ImmutableMap.of("com.bbn.foo.a", "meep",
            "com.bbn.foo.b", "foo",
            "com.bbn.serif.c", "bar"));

    final ParameterAccessListener listener = ParameterAccessListener.create();
    params.registerListener(listener);

    final Parameters fooParams = params.copyNamespace("com.bbn.foo");
    fooParams.getString("b");
    params.getStringSet("com.bbn.serif.c");
    fooParams.getString("a");

    final String msg = listener.constructLogMsg();

    // note the line numbers in the references need to be changed if this class is altered
    final Pattern refB = Pattern.compile("Parameter com.bbn.foo.b accessed at \n"
            + "com.bbn.bue.common.parameters.ParameterAccessListenerTest.testListener\\(ParameterAccessListenerTest.java:\\d+\\)",
        Pattern.MULTILINE);
    final Pattern refC = Pattern.compile("Parameter com.bbn.serif.c accessed at \n"
            + "com.bbn.bue.common.parameters.ParameterAccessListenerTest.testListener\\(ParameterAccessListenerTest.java:\\d+\\)",
        Pattern.MULTILINE);
    final Pattern refA = Pattern.compile("Parameter com.bbn.foo.a accessed at \n"
            + "com.bbn.bue.common.parameters.ParameterAccessListenerTest.testListener\\(ParameterAccessListenerTest.java:\\d+\\)",
        Pattern.MULTILINE);

    assertTrue(refA.matcher(msg).find());
    assertTrue(refB.matcher(msg).find());
    assertTrue(refC.matcher(msg).find());
  }
}
