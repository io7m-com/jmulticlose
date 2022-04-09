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

package com.io7m.jmulticlose.core;

/**
 * A collection of resources that can be closed.
 *
 * Resources are added to the collection with {@link #add(AutoCloseable)}, and are closed in the
 * reverse of the order in which they were added to the collection when {@link #close()} is called
 * (typically via a {@code try-with-resources} statement).
 *
 * @param <E> The precise type of exceptions thrown on close failures
 */

public interface CloseableCollectionType<E extends Exception> extends AutoCloseable
{
  /**
   * Close this collection. If any of the resources within the collection raise an exception upon
   * being closed, add the exception as a <i>suppressed exception</i> to an exception {@code e},
   * continue closing resources, and at the end of the method, throw {@code e}.
   *
   * @throws E If required
   */

  @Override
  void close()
    throws E;

  /**
   * Add a resource to be closed when this collection is closed.
   *
   * @param resource The resource
   * @param <T>      The precise type of resource
   *
   * @return {@code resource}
   */

  <T extends AutoCloseable> T add(T resource);
}
