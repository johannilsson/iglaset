/**
 * 
 */
package com.markupartist.iglaset.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import com.markupartist.iglaset.R;

/**
 * @author marco
 *
 */
public class DialogFactory {

	public static Dialog createNetworkProblemDialog(
			Context context,
			OnClickListener onClickListener) {
        return new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("Ett fel inträffade")
        .setMessage("Kunde inte ansluta till servern. Försök igen, eller Cancel för att gå tillbaka till föregående vy.")
        .setPositiveButton(R.string.retry, onClickListener)
        .setNegativeButton(android.R.string.cancel, onClickListener)
        .create();
	}
}
