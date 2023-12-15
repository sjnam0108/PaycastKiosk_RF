// Generated by view binder compiler. Do not edit!
package kr.co.bbmc.paycast.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public final class SettingLayoutBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final TextView agentSettingId;

  @NonNull
  public final TextView chgLockId;

  @NonNull
  public final TextView closingSetId;

  @NonNull
  public final Button optionCancelId;

  @NonNull
  public final Button orderCancelId;

  @NonNull
  public final TextView printSettingId;

  @NonNull
  public final TextView quitAppId;

  @NonNull
  public final EditText receiptNumId;

  private SettingLayoutBinding(@NonNull LinearLayout rootView, @NonNull TextView agentSettingId,
      @NonNull TextView chgLockId, @NonNull TextView closingSetId, @NonNull Button optionCancelId,
      @NonNull Button orderCancelId, @NonNull TextView printSettingId, @NonNull TextView quitAppId,
      @NonNull EditText receiptNumId) {
    this.rootView = rootView;
    this.agentSettingId = agentSettingId;
    this.chgLockId = chgLockId;
    this.closingSetId = closingSetId;
    this.optionCancelId = optionCancelId;
    this.orderCancelId = orderCancelId;
    this.printSettingId = printSettingId;
    this.quitAppId = quitAppId;
    this.receiptNumId = receiptNumId;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static SettingLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static SettingLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.setting_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static SettingLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.agent_setting_id;
      TextView agentSettingId = ViewBindings.findChildViewById(rootView, id);
      if (agentSettingId == null) {
        break missingId;
      }

      id = R.id.chg_lock_id;
      TextView chgLockId = ViewBindings.findChildViewById(rootView, id);
      if (chgLockId == null) {
        break missingId;
      }

      id = R.id.closing_set_id;
      TextView closingSetId = ViewBindings.findChildViewById(rootView, id);
      if (closingSetId == null) {
        break missingId;
      }

      id = R.id.option_cancel_id;
      Button optionCancelId = ViewBindings.findChildViewById(rootView, id);
      if (optionCancelId == null) {
        break missingId;
      }

      id = R.id.order_cancel_id;
      Button orderCancelId = ViewBindings.findChildViewById(rootView, id);
      if (orderCancelId == null) {
        break missingId;
      }

      id = R.id.print_setting_id;
      TextView printSettingId = ViewBindings.findChildViewById(rootView, id);
      if (printSettingId == null) {
        break missingId;
      }

      id = R.id.quit_app_id;
      TextView quitAppId = ViewBindings.findChildViewById(rootView, id);
      if (quitAppId == null) {
        break missingId;
      }

      id = R.id.receipt_num_id;
      EditText receiptNumId = ViewBindings.findChildViewById(rootView, id);
      if (receiptNumId == null) {
        break missingId;
      }

      return new SettingLayoutBinding((LinearLayout) rootView, agentSettingId, chgLockId,
          closingSetId, optionCancelId, orderCancelId, printSettingId, quitAppId, receiptNumId);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
