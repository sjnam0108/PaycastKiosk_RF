// Generated by view binder compiler. Do not edit!
package kr.co.bbmc.paycast.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import kr.co.bbmc.paycast.R;

public final class PrintSettingLayoutBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button optionCancelId;

  @NonNull
  public final Button optionSaveId;

  @NonNull
  public final EditText printIpId;

  @NonNull
  public final CheckBox setMasterPrtId;

  private PrintSettingLayoutBinding(@NonNull LinearLayout rootView, @NonNull Button optionCancelId,
      @NonNull Button optionSaveId, @NonNull EditText printIpId, @NonNull CheckBox setMasterPrtId) {
    this.rootView = rootView;
    this.optionCancelId = optionCancelId;
    this.optionSaveId = optionSaveId;
    this.printIpId = printIpId;
    this.setMasterPrtId = setMasterPrtId;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static PrintSettingLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static PrintSettingLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.print_setting_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static PrintSettingLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.option_cancel_id;
      Button optionCancelId = ViewBindings.findChildViewById(rootView, id);
      if (optionCancelId == null) {
        break missingId;
      }

      id = R.id.option_save_id;
      Button optionSaveId = ViewBindings.findChildViewById(rootView, id);
      if (optionSaveId == null) {
        break missingId;
      }

      id = R.id.print_ip_id;
      EditText printIpId = ViewBindings.findChildViewById(rootView, id);
      if (printIpId == null) {
        break missingId;
      }

      id = R.id.set_master_prt_id;
      CheckBox setMasterPrtId = ViewBindings.findChildViewById(rootView, id);
      if (setMasterPrtId == null) {
        break missingId;
      }

      return new PrintSettingLayoutBinding((LinearLayout) rootView, optionCancelId, optionSaveId,
          printIpId, setMasterPrtId);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
