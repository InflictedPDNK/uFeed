package org.pdnk.ufeed.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;

import org.pdnk.ufeed.R;

import java.lang.ref.WeakReference;

/**
 * Simple helper class encapsulating Dialogs creation and setup
 */
public class DialogManager
{

    private final WeakReference<Context> contextRef;

    public DialogManager(@NonNull Context context)
    {
        this.contextRef = new WeakReference<>(context);
    }

    /**
     * Pre-defined dialog for Network errors. Allows retry functionality
     * @param onRetryF instance of Runnable action to be performed upon Retry click or null to disable retries
     */
    public void buildNoNetworkDialog(final Runnable onRetryF)
    {
        final Context context = contextRef.get();
        if(context != null)
        {

            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.no_network_access))
                    .setMessage(context.getString(R.string.please_connect))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(true)
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener()
                                                {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    context.startActivity(new Intent(Settings.ACTION_SETTINGS));
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton(onRetryF == null ? context.getString(R.string.cancel_button) : context.getString(R.string.retry),
                                 new DialogInterface.OnClickListener()
                                 {
                                     public void onClick(DialogInterface dialog, int which)
                                     {
                                         if (onRetryF != null)
                                             onRetryF.run();

                                         dialog.dismiss();
                                     }
                                 })
                    .show();

        }

    }

    /**
     * Dialog with retry functionality
     * @param title title of the dialog
     * @param message dialog message
     * @param onRetryF instance of Runnable action to be performed upon Retry click
     * @param blocking true to prevent cancellation (force only Retry button)
     */
    public void buildMessageRetryDialog(String title, String message, final Runnable onRetryF, boolean blocking)
    {
        final Context context = contextRef.get();
        if(context != null)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(!blocking);
            if(onRetryF != null)
            {
                builder.setPositiveButton(context.getString(R.string.retry), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        onRetryF.run();
                        dialog.dismiss();
                    }
                });
            }

            if (!blocking || onRetryF == null)
            {
                builder.setNegativeButton(context.getString(R.string.Ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
            }
            builder.show();
        }
    }

}
