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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * The default implementation of the {@link CloseableCollectionType} interface.
 *
 * @param <E> On close failures
 */

@ThreadSafe
public final class CloseableCollection<E extends Exception>
  implements CloseableCollectionType<E>
{
  private final ConcurrentLinkedDeque<AutoCloseable> stack;
  private final Supplier<E> exceptions;
  private final AtomicBoolean closed;

  private CloseableCollection(final Supplier<E> in_exceptions)
  {
    this.exceptions =
      Objects.requireNonNull(in_exceptions, "exceptions");
    this.stack =
      new ConcurrentLinkedDeque<>();
    this.closed =
      new AtomicBoolean(false);
  }

  /**
   * Create a new closeable collection.
   *
   * @param exceptions A supplier of exceptions
   * @param <E>        The precise type of exceptions thrown on close failures
   *
   * @return A new collection
   */

  public static <E extends Exception> CloseableCollectionType<E> create(
    final Supplier<E> exceptions)
  {
    return new CloseableCollection<>(exceptions);
  }

  /**
   * Create a new closeable collection.
   *
   * @return A new collection
   */

  public static CloseableCollectionType<ClosingResourceFailedException> create()
  {
    return create(() -> new ClosingResourceFailedException(
      "One or more resources could not be closed."));
  }

  @Override
  public void close()
    throws E
  {
    if (this.closed.compareAndSet(false, true)) {
      E e = null;
      while (!this.stack.isEmpty()) {
        try {
          this.stack.pop().close();
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
  }

  @Override
  public int size()
  {
    return this.stack.size();
  }

  @Override
  public <T extends AutoCloseable> T add(final T resource)
  {
    if (!this.closed.get()) {
      this.stack.push(Objects.requireNonNull(resource, "resource"));
      return resource;
    }
    throw new IllegalStateException("Collection is closed.");
  }
}
