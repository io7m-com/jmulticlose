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

import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link CloseableCollection}.
 */

public final class CloseableCollectionTest
{
  private static final Logger LOG = LoggerFactory.getLogger(
    CloseableCollectionTest.class);

  /**
   * An empty collection raises no exceptions.
   *
   * @throws ClosingResourceFailedException On errors
   */

  @Test
  public void testEmpty0()
    throws ClosingResourceFailedException
  {
    final CloseableCollectionType<ClosingResourceFailedException> collection =
      CloseableCollection.create();
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
    final CloseableCollectionType<IOException> collection =
      CloseableCollection.create(IOException::new);

    assertEquals(0, collection.size());
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

    try (CloseableCollectionType<ClosingResourceFailedException> c = CloseableCollection.create()) {
      r0 = c.add(new Resource(0));
      assertEquals(1, c.size());
      r1 = c.add(new Resource(1));
      assertEquals(2, c.size());
      r2 = c.add(new Resource(2));
      assertEquals(3, c.size());
    }

    Assertions.assertTrue(r0.closed, "r0 closed");
    Assertions.assertTrue(r1.closed, "r1 closed");
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
      try (CloseableCollectionType<ClosingResourceFailedException> c = CloseableCollection.create()) {
        resources.r0 = c.add(new Resource(0));
        assertEquals(1, c.size());
        resources.r1 = c.add(new ResourceCrasher(1));
        assertEquals(2, c.size());
        resources.r2 = c.add(new Resource(2));
        assertEquals(3, c.size());
        resources.r3 = c.add(new ResourceCrasher(3));
        assertEquals(4, c.size());
      }
    });

    Assertions.assertTrue(resources.r0.closed, "r0 closed");
    Assertions.assertTrue(resources.r1.closed, "r1 closed");
    Assertions.assertTrue(resources.r2.closed, "r2 closed");
    Assertions.assertTrue(resources.r3.closed, "r3 closed");
  }

  /**
   * Multithreaded closing works.
   */

  @Test
  @RepeatedTest(value = 100, failureThreshold = 1)
  public void testThreadSafety()
    throws InterruptedException
  {
    final var resources = new ArrayList<Resource>();
    for (int index = 0; index < 1000; ++index) {
      resources.add(new Resource(index));
    }

    final var collection = CloseableCollection.create();
    for (final var resource : resources) {
      collection.add(resource);
    }

    final var executor = Executors.newFixedThreadPool(20);
    try {
      for (int index = 0; index < 20; ++index) {
        executor.execute(() -> {
          try {
            collection.close();
          } catch (final Throwable e) {
            throw new RuntimeException(e);
          }
        });
      }
    } finally {
      executor.shutdown();
      executor.awaitTermination(5L, TimeUnit.SECONDS);
    }

    Assertions.assertAll(
      resources.stream()
        .map(x -> (Executable) () -> {
          Assertions.assertTrue(x.closed);
          assertEquals(1, x.attempts.get());
        })
        .collect(Collectors.toList())
    );
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

  private final class Resource implements Closeable
  {
    private final int x;
    private volatile boolean closed;
    private final AtomicInteger attempts;

    Resource(final int in_x)
    {
      this.x = in_x;
      this.attempts = new AtomicInteger();
    }

    @Override
    public void close()
    {
      LOG.debug("Resource close " + this.x);
      this.closed = true;
      this.attempts.incrementAndGet();
    }
  }

  private final class ResourceCrasher implements Closeable
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
  }
}
