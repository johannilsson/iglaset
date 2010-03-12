/**
 * 
 */
package com.markupartist.iglaset.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import com.markupartist.iglaset.R;

/**
 * @author marco
 *
 */
public class DialogFactory {

	public static Dialog createNetworkProblemDialog(
			Activity activity,
			OnClickListener onClickListener) {
        return new AlertDialog.Builder(activity)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("Ett fel inträffade")
        .setMessage("Kunde inte ansluta till servern. Försök igen, eller Cancel för att gå tillbaka till föregående vy.")
        .setPositiveButton(R.string.retry, onClickListener)
        .setNegativeButton(activity.getText(android.R.string.cancel), onClickListener)
        .create();
	}
}
