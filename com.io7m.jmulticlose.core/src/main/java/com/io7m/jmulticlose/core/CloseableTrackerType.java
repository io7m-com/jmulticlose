/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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
 * <p>A tracker of resources that can be closed.</p>
 *
 * <p>Resources are added to the tracker with {@link #add(CloseableType)},
 * and are closed in the reverse of the order in which they were added to the
 * collection when {@link #close()} is called (typically via a
 * {@code try-with-resources} statement). Additionally, resources are removed
 * from the tracker if, at any point, it is determined that they have already
 * been closed.</p>
 *
 * <p>The purpose of this tracker is to provide a means to guarantee that
 * all resources have been closed when the tracker is closed, but to also
 * ensure that this tracker is not responsible for keeping strong references
 * to resources that have been closed.</p>
 *
 * @param <E> The precise type of exceptions thrown on close failures
 */

public interface CloseableTrackerType<E extends Exception> extends AutoCloseable
{
  /**
   * Close this tracker. If any of the resources within the tracker raise an
   * exception upon being closed, add the exception as a
   * <i>suppressed exception</i> to an exception {@code e},
   * continue closing resources, and at the end of the method, throw {@code e}.
   *
   * @throws E If required
   */

  @Override
  void close()
    throws E;

  /**
   * @return The number of items in the tracker
   */

  int size();

  /**
   * Add a resource to be closed when this tracker is closed.
   *
   * @param resource The resource
   * @param <T>      The precise type of resource
   *
   * @return {@code resource}
   */

  <T extends CloseableType> T add(T resource);

  /**
   * Add a plain {@link AutoCloseable} resource to be closed when this tracker
   * is closed.
   *
   * @param resource The resource
   * @param <T>      The precise type of resource
   *
   * @return {@code resource}
   */

  <T extends AutoCloseable> T addAuto(T resource);

  /**
   * Remove resource from this tracker.
   *
   * @param resource The resource
   * @param <T>      The precise type of resource
   */

  <T extends CloseableType> void remove(T resource);
}
