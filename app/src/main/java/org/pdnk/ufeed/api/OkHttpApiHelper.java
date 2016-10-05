package org.pdnk.ufeed.api;

import android.accounts.NetworkErrorException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.pdnk.ufeed.util.ParametricRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp client implementation wrapped in REST API Helper interface<br/>
 * OkHttp provides convenient and flexible way of configuring http requests and interacting with requests/responses.
 *
 */
@SuppressWarnings("WeakerAccess")
public class OkHttpApiHelper implements ApiHelper
{
    private final OkHttpClient client;

    private final ConnectivityManager connectivityManager;
    private final ResponseParser parser;
    private final Object requestListLock = new Object();
    private final LinkedList<Call> storedRequests = new LinkedList<>();
    private final Handler uiHandler;

    private ParametricRunnable<RequestRetryHandle> genericErrorHandler;
    private ParametricRunnable<RequestRetryHandle> networkErrorHandler;

    /**
     * Default constructor. Uses global looper's handler for actions invocation on the main thread. For custom handler use
     * the alternative constructor
     * @param connectivityManager instance of the manager for network status polling
     * @param cacheDir Application's cache or files system directory to enable disk caching or null to disable
     * @param parser instance of parser for conversion between API responses and desired data model used by specific
     *               data provider.
     */
    public OkHttpApiHelper(@NonNull ConnectivityManager connectivityManager, File cacheDir, @NonNull ResponseParser parser)
    {
        this(connectivityManager, cacheDir, parser, new Handler(Looper.getMainLooper()));
    }

    /**
     * Alternative constructor which supports custom handler for actions invocation.
     * @param connectivityManager instance of the manager for network status polling
     * @param cacheDir Application's cache or files system directory to enable disk caching or null to disable
     * @param parser instance of parser for conversion between API responses and desired data model used by specific
     *               data provider.
     * @param handler instance of custom handler
     */
    public OkHttpApiHelper(@NonNull ConnectivityManager connectivityManager, File cacheDir, @NonNull ResponseParser parser, @NonNull Handler handler)
    {
        /*
         * Current settings include disk cache set at 10 Mb (granted cacheDir is provided), follow 3xx redirects and cookies
         */
        uiHandler = handler;
        this.connectivityManager = connectivityManager;
        this.parser = parser;
        client = new OkHttpClient().newBuilder()
                .cache(cacheDir != null ? new Cache(cacheDir, 10 * 1024 * 1024) : null)
                .cookieJar(new MyCookieJar())
                .followRedirects(true)
                .build();

    }

    @Override
    public Object getApiClient()
    {
        return client;
    }



    @Override
    public void cancelAllRequests(boolean remove)
    {
        synchronized (requestListLock)
        {
            for (Call request : storedRequests)
            {
                if (request.isExecuted() && !request.isCanceled())
                    request.cancel();
            }

            if (remove)
                storedRequests.clear();
        }
    }


    @Override
    public <T extends BaseResponse> void requestGET(@NonNull String query,
                               final Type responseType,
                               final ParametricRunnable<T> onSuccessF,
                               final ParametricRunnable<RequestRetryHandle> onFailureF)
    {
        try
        {
            Request req = new Request.Builder()
                    .get()
                    .url(query)
                    .build();

            request(req, responseType, onSuccessF, onFailureF);

        } catch (final Exception e)
        {
            handleError(e, onFailureF, null);
        }
    }


    private <T extends BaseResponse> void request(Request request, final Type responseType,
                             final ParametricRunnable<T> onSuccessF,
                             final ParametricRunnable<RequestRetryHandle> onFailureF)
    {
        final RequestRetryHandle handle = new RequestRetryHandle();
        handle.call = client.newCall(request);
        handle.callback = new Callback()
        {
            @Override
            public void onFailure(Call call, final IOException e)
            {
                removeRequest(call);
                handleError(e, onFailureF, handle);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                removeRequest(call);

                //responses with codes lower than 400 normally can be treated as successful
                if(response.code() >= 400)
                {
                    handleError(new NetworkErrorException(String.format("%s\nRequest: %s\nResponse: %s",
                                                                        response.message(), URLDecoder.decode(call.request().url().toString(), "UTF-8"),
                                                                        response.body().string())), onFailureF, handle);
                    response.body().close();
                }
                else
                {
                    try
                    {
                        final T data;

                        /*
                        if requested type is String, simply returned body of the response in String form. It is unlikely
                        that parsing is needed when String is requested. Alternatively, String type must be encapsulated
                        to support parsing.

                        The unchecked cast is normal. It is caller's responsibility to match parametrised type and type
                        of response. RuntimeException produced in case of violated cast should alert developer about the
                        mismatch
                        */
                        if(responseType == RawResponse.class)
                        {
                            data = (T) new RawResponse(response.body().string());
                        }
                        else
                        {
                            data = (T) parser.parse(response.body().charStream(), responseType);
                        }

                        data.setContentType(response.header("content-type"));

                        //TODO: this is not reliable at the moment
                        data.setCached(response.cacheResponse() != null);

                        if (onSuccessF != null)
                        {
                            uiHandler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    onSuccessF.run(data);
                                }
                            });
                        }
                    } catch (IOException e)
                    {
                        handleError(e, onFailureF, handle);
                    } finally
                    {
                        response.body().close();
                    }
                }

            }
        };

        storeRequest(handle.call);
        handle.call.enqueue(handle.callback);
    }

    @Override
    public void setGenericErrorHandler(ParametricRunnable<RequestRetryHandle> genericErrorHandler)
    {
        this.genericErrorHandler = genericErrorHandler;
    }

    @Override
    public void setNetworkErrorHandler(ParametricRunnable<RequestRetryHandle> networkErrorHandler)
    {
        this.networkErrorHandler = networkErrorHandler;
    }

    private void storeRequest(Call request)
    {
        if (request != null)
        {
            synchronized (requestListLock)
            {
                storedRequests.add(request);
            }
        }
    }

    private void removeRequest(Call request)
    {
        synchronized (requestListLock)
        {
            storedRequests.remove(request);
        }
    }

    private void handleError(final Exception e,
                             final ParametricRunnable<RequestRetryHandle> onFailureF,
                             RequestRetryHandle handle)
    {
        //silently bail out if request was cancelled
        if (handle != null && handle.call.isCanceled())
        {
            return;
        }

        Runnable action;

        //update request information depending on the type of error. If handle was missing, it is likely that error
        //happened prior to actual request.
        final RequestRetryHandle retryHandle;
        if (handle == null)
        {
            retryHandle = new RequestRetryHandle();
            retryHandle.retryAvailable = false;
        }
        else
        {
            retryHandle = handle;
            retryHandle.retryAvailable = true;
        }

        retryHandle.lastAssociatedException = e;

        retryHandle.retryProc = retryHandle.retryAvailable ? new Runnable()
        {
            @Override
            public void run()
            {
                ++retryHandle.retries;

                Call retriedCall = client.newCall(retryHandle.call.request());
                storeRequest(retriedCall);
                retriedCall.enqueue(retryHandle.callback);
            }
        } : null;

        //determine if it's a network error then if there is no external network handler, proceed to generic error
        if (e instanceof IOException && !isNetworkAvailable() && networkErrorHandler != null)
        {
            action = new Runnable()
            {
                @Override
                public void run()
                {
                    networkErrorHandler.run(retryHandle);
                }
            };

        }
        else
        {

            action = new Runnable()
            {
                @Override
                public void run()
                {
                    if (onFailureF != null)
                        onFailureF.run(retryHandle);
                    else if (genericErrorHandler != null)
                        genericErrorHandler.run(retryHandle);
                }
            };
        }

        //pass action for the invocation
        uiHandler.post(action);
    }

    private boolean isNetworkAvailable()
    {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    /**
     * Crude implementation of CookieJar. Not persistent.
     */
    private static class MyCookieJar implements CookieJar
    {
        final HashMap<String, List<Cookie>> jar = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies)
        {
            jar.put(url.host(), cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url)
        {
            List<Cookie> cookies = jar.get(url.host());
            return cookies != null ? cookies : new ArrayList<Cookie>();
        }
    }


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
}
