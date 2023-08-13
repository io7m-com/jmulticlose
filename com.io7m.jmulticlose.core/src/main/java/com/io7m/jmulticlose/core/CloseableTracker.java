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

import net.jcip.annotations.ThreadSafe;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

/**
 * The default implementation of the {@link CloseableTrackerType} interface.
 *
 * @param <E> On close failures
 */

@ThreadSafe
public final class CloseableTracker<E extends Exception> implements CloseableTrackerType<E>
{
  private final ConcurrentLinkedDeque<CloseableType> stack;
  private final Supplier<E> exceptions;

  private CloseableTracker(final Supplier<E> in_exceptions)
  {
    this.exceptions =
      Objects.requireNonNull(in_exceptions, "exceptions");
    this.stack =
      new ConcurrentLinkedDeque<>();
  }

  /**
   * Create a new closeable tracker.
   *
   * @param exceptions A supplier of exceptions
   * @param <E>        The precise type of exceptions thrown on close failures
   *
   * @return A new collection
   */

  public static <E extends Exception> CloseableTrackerType<E> create(
    final Supplier<E> exceptions)
  {
    return new CloseableTracker<>(exceptions);
  }

  /**
   * Create a new closeable tracker.
   *
   * @return A new collection
   */

  public static CloseableTrackerType<ClosingResourceFailedException> create()
  {
    return create(() -> new ClosingResourceFailedException(
      "One or more resources could not be closed."));
  }

  @Override
  public void close()
    throws E
  {
    E e = null;

    while (!this.stack.isEmpty()) {
      final AutoCloseable resource = this.stack.pop();
      try {
        resource.close();
      } catch (final Exception re) {
        if (e == null) {
          e = this.exceptions.get();
        }
        e.addSuppressed(re);
      }
    }

    if (e != null) {
      throw e;
    }
  }

  @Override
  public <T extends CloseableType> T add(
    final T resource)
  {
    this.stack.push(resource);
    this.stack.removeIf(CloseableType::isClosed);
    return resource;
  }

  @Override
  public <T extends CloseableType> void remove(
    final T resource)
  {
    this.stack.remove(resource);
    this.stack.removeIf(CloseableType::isClosed);
  }
}
