package org.pdnk.ufeed.util;

/**
 * Extended version of Runnable which allows passing a single generalised parameter
 */
public interface ParametricRunnable<T>
{
    void run(T param);
}
