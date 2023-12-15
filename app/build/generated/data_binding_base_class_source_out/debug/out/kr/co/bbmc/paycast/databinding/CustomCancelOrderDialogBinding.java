// Generated by view binder compiler. Do not edit!
package kr.co.bbmc.paycast.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import kr.co.bbmc.paycast.R;

public final class CustomCancelOrderDialogBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final TextView approvalNumberId;

  @NonNull
  public final Button lockCancelId;

  @NonNull
  public final Button lockConfirmId;

  @NonNull
  public final TextView orderNumberId;

  @NonNull
  public final TextView paymentDateId;

  @NonNull
  public final TextView paymentMoneyId;

  private CustomCancelOrderDialogBinding(@NonNull LinearLayout rootView,
      @NonNull TextView approvalNumberId, @NonNull Button lockCancelId,
      @NonNull Button lockConfirmId, @NonNull TextView orderNumberId,
      @NonNull TextView paymentDateId, @NonNull TextView paymentMoneyId) {
    this.rootView = rootView;
    this.approvalNumberId = approvalNumberId;
    this.lockCancelId = lockCancelId;
    this.lockConfirmId = lockConfirmId;
    this.orderNumberId = orderNumberId;
    this.paymentDateId = paymentDateId;
    this.paymentMoneyId = paymentMoneyId;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static CustomCancelOrderDialogBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static CustomCancelOrderDialogBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.custom_cancel_order_dialog, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static CustomCancelOrderDialogBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.approval_number_id;
      TextView approvalNumberId = ViewBindings.findChildViewById(rootView, id);
      if (approvalNumberId == null) {
        break missingId;
      }

      id = R.id.lock_cancel_id;
      Button lockCancelId = ViewBindings.findChildViewById(rootView, id);
      if (lockCancelId == null) {
        break missingId;
      }

      id = R.id.lock_confirm_id;
      Button lockConfirmId = ViewBindings.findChildViewById(rootView, id);
      if (lockConfirmId == null) {
        break missingId;
      }

      id = R.id.order_number_id;
      TextView orderNumberId = ViewBindings.findChildViewById(rootView, id);
      if (orderNumberId == null) {
        break missingId;
      }

      id = R.id.payment_date_id;
      TextView paymentDateId = ViewBindings.findChildViewById(rootView, id);
      if (paymentDateId == null) {
        break missingId;
      }

      id = R.id.payment_money_id;
      TextView paymentMoneyId = ViewBindings.findChildViewById(rootView, id);
      if (paymentMoneyId == null) {
        break missingId;
      }

      return new CustomCancelOrderDialogBinding((LinearLayout) rootView, approvalNumberId,
          lockCancelId, lockConfirmId, orderNumberId, paymentDateId, paymentMoneyId);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
