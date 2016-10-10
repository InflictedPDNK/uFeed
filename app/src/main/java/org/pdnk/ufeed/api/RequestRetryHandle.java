package org.pdnk.ufeed.api;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Error handler data descriptor. Contains associated error information and support retry procedure.
 */
public class RequestRetryHandle
{
    Call call;
    Callback callback;
    Runnable retryProc;
    boolean retryAvailable;
    Exception lastAssociatedException;
    int retries = 0;

    /**
     * @return true if retry is available for this request
     */
    public boolean isRetryAvailable()
    {
        return retryAvailable;
    }

    /**
     *
     * @return instance of last exception associated with the failure
     */
    public Exception getLastAssociatedException()
    {
        return lastAssociatedException;
    }

    /**
     *
     * @return number of retries performed so far
     */
    public int getRetries()
    {
        return retries;
    }

    /**
     * Invokable action which should be executed if user desires to retry. Has no effect if {@link #isRetryAvailable()}
     * is false
     */
    public void executeRetry()
    {
        if (retryProc != null)
            retryProc.run();
    }
}
