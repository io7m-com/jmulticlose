/*
 * Copyright © 2018 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jmulticlose.tests;

import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Exception tests.
 */

public final class ClosingResourceFailedExceptionTest
{
  /**
   * Test constructors.
   */

  @Test
  public void testConstructors()
  {
    final Exception e = new Exception();

    final ClosingResourceFailedException e0 = new ClosingResourceFailedException();
    final ClosingResourceFailedException e1 = new ClosingResourceFailedException("x", e);
    final ClosingResourceFailedException e2 = new ClosingResourceFailedException("x");
    final ClosingResourceFailedException e3 = new ClosingResourceFailedException(e);

    Assertions.assertEquals(null, e0.getMessage());
    Assertions.assertEquals(null, e0.getCause());

    Assertions.assertEquals("x", e1.getMessage());
    Assertions.assertEquals(e, e1.getCause());

    Assertions.assertEquals("x", e2.getMessage());
    Assertions.assertEquals(null, e2.getCause());

    Assertions.assertEquals(e, e3.getCause());
  }
}
