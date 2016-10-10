package org.pdnk.ufeed.api;

import android.support.annotation.NonNull;

import org.pdnk.ufeed.util.ParametricRunnable;

import java.lang.reflect.Type;


/**
 * Helper interface to wrap Network requests for REST API calls, such as GET, POST, UPDATE, etc.
 */
@SuppressWarnings("SameParameterValue")
public interface ApiHelper
{
    /**
     * Issue GET request. The call is asynchronous.
     * @param query fully formed request URL including query string
     * @param responseType type of object to convert the response to
     * @param onSuccessF action handler upon successful execution or null to ignore
     * @param onFailureF action handler upon failed execution or null to ignore
     * @param <T> type ot response object. This must match the type of object passed in responseType param
     */
    <T extends BaseResponse> void requestGET(@NonNull String query,
                        Type responseType,
                        ParametricRunnable<T> onSuccessF,
                        ParametricRunnable<RequestRetryHandle> onFailureF);


    /**
     * Install general error action handler for failed requests. The action will be performed upon any failure except
     * network related. The action is executed on the UI thread.<br/>
     * {@link ParametricRunnable} param passed to the action contains response information enclosed in
     * {@link RequestRetryHandle}, such as last associated exception and retry action.
     * @param genericErrorHandler instance of action handler or null to ignore
     */
    void setGenericErrorHandler(ParametricRunnable<RequestRetryHandle> genericErrorHandler);

    /**
     * Install network error action handler for failed requests. The action will be performed upon any network related
     * failure, such as lack of internet connection. The action is executed on the UI thread.<br/>
     * {@link ParametricRunnable} param passed to the action contains response information enclosed in
     * {@link RequestRetryHandle}, such as last associated exception and retry action.
     * @param networkErrorHandler instance of action handler or null to ignore
     */
    void setNetworkErrorHandler(ParametricRunnable<RequestRetryHandle> networkErrorHandler);

    /**
     * Cancel all active requests. Cancelled requests will not execute any associated actions, such as onSuccess or onFailure.
     * This call is asynchronous.
     * @param remove clear requests queue. Normally must be set to true.
     */
    void cancelAllRequests(boolean remove);

    /**
     * @return underlying network client object
     */
    Object getApiClient();
}
