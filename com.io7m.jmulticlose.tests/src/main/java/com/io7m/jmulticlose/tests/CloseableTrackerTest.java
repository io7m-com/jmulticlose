/*
 * Copyright © 2018 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.jmulticlose.core.CloseableTracker;
import com.io7m.jmulticlose.core.CloseableTrackerType;
import com.io7m.jmulticlose.core.CloseableType;
import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * Tests for {@link CloseableTracker}.
 */

public final class CloseableTrackerTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CloseableTrackerTest.class);

  /**
   * An empty collection raises no exceptions.
   *
   * @throws ClosingResourceFailedException On errors
   */

  @Test
  public void testEmpty0()
    throws ClosingResourceFailedException
  {
    final CloseableTrackerType<ClosingResourceFailedException> collection =
      CloseableTracker.create();
    collection.close();
  }

  /**
   * An empty collection raises no exceptions.
   *
   * @throws IOException On errors
   */

  @Test
  public void testEmpty1()
    throws IOException
  {
    final CloseableTrackerType<IOException> collection =
      CloseableTracker.create(IOException::new);
    collection.close();
  }

  /**
   * Resources are closed.
   *
   * @throws Exception On errors
   */

  @Test
  public void testSimple()
    throws Exception
  {
    final Resource r0;
    final Resource r1;
    final Resource r2;

    try (CloseableTrackerType<ClosingResourceFailedException> c = CloseableTracker.create()) {
      r0 = c.add(new Resource(0));
      r1 = c.add(new Resource(1));
      r2 = c.add(new Resource(2));
    }

    Assertions.assertTrue(r0.closed, "r0 closed");
    Assertions.assertTrue(r1.closed, "r1 closed");
    Assertions.assertTrue(r2.closed, "r2 closed");
  }

  /**
   * Removed resources aren't closed.
   *
   * @throws Exception On errors
   */

  @Test
  public void testRemove()
    throws Exception
  {
    final Resource r0;
    final Resource r1;
    final Resource r2;

    try (CloseableTrackerType<ClosingResourceFailedException> c = CloseableTracker.create()) {
      r0 = c.add(new Resource(0));
      r1 = c.add(new Resource(1));
      r2 = c.add(new Resource(2));

      c.remove(r0);
      c.remove(r1);
    }

    Assertions.assertFalse(r0.closed, "r0 closed");
    Assertions.assertFalse(r1.closed, "r1 closed");
    Assertions.assertTrue(r2.closed, "r2 closed");
  }

  /**
   * Crashing resources don't prevent closing.
   */

  @Test
  public void testFailure()
  {
    final Resources resources = new Resources();
    Assertions.assertThrows(ClosingResourceFailedException.class, () -> {
      try (CloseableTrackerType<ClosingResourceFailedException> c = CloseableTracker.create()) {
        resources.r0 = c.add(new Resource(0));
        resources.r1 = c.add(new ResourceCrasher(1));
        resources.r2 = c.add(new Resource(2));
        resources.r3 = c.add(new ResourceCrasher(3));
      }
    });

    Assertions.assertTrue(resources.r0.closed, "r0 closed");
    Assertions.assertTrue(resources.r1.closed, "r1 closed");
    Assertions.assertTrue(resources.r2.closed, "r2 closed");
    Assertions.assertTrue(resources.r3.closed, "r3 closed");
  }

  /**
   * Resources are closed.
   *
   * @throws Exception On errors
   */

  @Test
  public void testWrapped()
    throws Exception
  {
    final ResourceAuto r0;
    final ResourceAuto r1;
    final ResourceAuto r2;

    try (CloseableTrackerType<ClosingResourceFailedException> c = CloseableTracker.create()) {
      r0 = c.addAuto(new ResourceAuto(0));
      r1 = c.addAuto(new ResourceAuto(1));
      r2 = c.addAuto(new ResourceAuto(2));
    }

    Assertions.assertTrue(r0.closed, "r0 closed");
    Assertions.assertTrue(r1.closed, "r1 closed");
    Assertions.assertTrue(r2.closed, "r2 closed");
  }

  private static final class Resources
  {
    Resource r0;
    ResourceCrasher r1;
    Resource r2;
    ResourceCrasher r3;

    Resources()
    {

    }
  }

  private final class Resource implements CloseableType
  {
    private final int x;
    private boolean closed;

    Resource(final int in_x)
    {
      this.x = in_x;
    }

    @Override
    public void close()
    {
      LOG.debug("Resource close " + this.x);
      this.closed = true;
    }

    @Override
    public boolean isClosed()
    {
      return this.closed;
    }
  }

  private final class ResourceCrasher implements CloseableType
  {
    private final int x;
    private boolean closed;

    ResourceCrasher(final int in_x)
    {
      this.x = in_x;
    }

    @Override
    public void close()
      throws IOException
    {
      LOG.debug("ResourceCrasher close " + this.x);
      this.closed = true;
      throw new IOException("Failed " + this.x);
    }

    @Override
    public boolean isClosed()
    {
      return this.closed;
    }
  }

  private final class ResourceAuto implements Closeable
  {
    private final int x;
    private boolean closed;

    ResourceAuto(final int in_x)
    {
      this.x = in_x;
    }

    @Override
    public void close()
    {
      LOG.debug("Resource close " + this.x);
      this.closed = true;
    }
  }
}
