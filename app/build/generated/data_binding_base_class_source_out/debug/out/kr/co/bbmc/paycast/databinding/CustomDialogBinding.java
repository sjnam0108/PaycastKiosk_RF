// Generated by view binder compiler. Do not edit!
package kr.co.bbmc.paycast.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import kr.co.bbmc.paycast.R;

public final class CustomDialogBinding implements ViewBinding {
  @NonNull
  private final CoordinatorLayout rootView;

  @NonNull
  public final Button dialogBtn;

  @NonNull
  public final TextView dialogText;

  @NonNull
  public final TextView dialogTitle;

  private CustomDialogBinding(@NonNull CoordinatorLayout rootView, @NonNull Button dialogBtn,
      @NonNull TextView dialogText, @NonNull TextView dialogTitle) {
    this.rootView = rootView;
    this.dialogBtn = dialogBtn;
    this.dialogText = dialogText;
    this.dialogTitle = dialogTitle;
  }

  @Override
  @NonNull
  public CoordinatorLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static CustomDialogBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static CustomDialogBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.custom_dialog, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static CustomDialogBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.dialog_btn;
      Button dialogBtn = ViewBindings.findChildViewById(rootView, id);
      if (dialogBtn == null) {
        break missingId;
      }

      id = R.id.dialog_text;
      TextView dialogText = ViewBindings.findChildViewById(rootView, id);
      if (dialogText == null) {
        break missingId;
      }

      id = R.id.dialog_title;
      TextView dialogTitle = ViewBindings.findChildViewById(rootView, id);
      if (dialogTitle == null) {
        break missingId;
      }

      return new CustomDialogBinding((CoordinatorLayout) rootView, dialogBtn, dialogText,
          dialogTitle);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
