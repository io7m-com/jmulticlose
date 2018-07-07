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

package com.io7m.jmulticlose.core;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * The default implementation of the {@link CloseableCollectionType} interface.
 *
 * @param <E> On close failures
 */

public final class CloseableCollection<E extends Exception> implements CloseableCollectionType<E>
{
  private final ArrayDeque<AutoCloseable> stack;
  private final Supplier<E> exceptions;

  private CloseableCollection(final Supplier<E> in_exceptions)
  {
    this.exceptions = Objects.requireNonNull(in_exceptions, "exceptions");
    this.stack = new ArrayDeque<>(16);
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
  public <T extends AutoCloseable> T add(final T resource)
  {
    this.stack.push(Objects.requireNonNull(resource, "resource"));
    return resource;
  }
}
