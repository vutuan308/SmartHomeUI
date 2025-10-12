package com.example.smarthomeui.smarthome.ai_speech_reg;

import com.example.smarthomeui.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.List;
import static com.example.smarthomeui.smarthome.ai_speech_reg.DeviceModels.*;

public class DeviceConfirmSheet {

    public interface Callback {
        void onConfirmed(DeviceModels.Device device, ParseResult planned);
        void onCanceled();
    }

    private final BottomSheetDialog dialog;
    private final ImageView imgDevice;
    private final TextView tvDeviceName, tvRoomType, tvPlannedAction;
    private final Button btnCancel, btnNext, btnConfirm;

    private final List<DeviceRegistry.CandidateResult> candidates;
    private int index = 0;
    private final ParseResult planned;
    private final Context ctx;

    public DeviceConfirmSheet(Context ctx,
                              List<DeviceRegistry.CandidateResult> candidates,
                              ParseResult planned,
                              Callback cb) {
        this.ctx = ctx;
        this.candidates = candidates;
        this.planned = planned;
        this.dialog = new BottomSheetDialog(ctx);
        View v = LayoutInflater.from(ctx).inflate(R.layout.sheet_device_confirm, null, false);
        dialog.setContentView(v);

        imgDevice = v.findViewById(R.id.imgDevice);
        tvDeviceName = v.findViewById(R.id.tvDeviceName);
        tvRoomType = v.findViewById(R.id.tvRoomType);
        tvPlannedAction = v.findViewById(R.id.tvPlannedAction);
        btnCancel = v.findViewById(R.id.btnCancel);
        btnNext = v.findViewById(R.id.btnNext);
        btnConfirm = v.findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(view -> { dialog.dismiss(); cb.onCanceled(); });
        btnNext.setOnClickListener(view -> { nextCandidate(); });
        btnConfirm.setOnClickListener(view -> {
            DeviceModels.Device d = candidates.get(index).device;
            dialog.dismiss();
            cb.onConfirmed(d, planned);
        });

        bindCandidate();
    }

    private void bindCandidate() {
        DeviceModels.Device d = candidates.get(index).device;
        tvDeviceName.setText(d.name);
        tvRoomType.setText(d.room + " • " + d.type);

        String actionStr;
        switch (planned.action) {
            case TURN_ON: actionStr = "Hành động: BẬT"; break;
            case TURN_OFF: actionStr = "Hành động: TẮT"; break;
            case INCREASE: actionStr = "Hành động: TĂNG" + (planned.value!=null?(" lên "+planned.value):""); break;
            case DECREASE: actionStr = "Hành động: GIẢM" + (planned.value!=null?(" xuống "+planned.value):""); break;
            case SET: actionStr = "Hành động: ĐẶT" + (planned.value!=null?(" = "+planned.value):""); break;
            default: actionStr = "Hành động: (không rõ)";
        }
        tvPlannedAction.setText(actionStr);

        // Icon gợi ý (đơn giản)
        if ("fan".equalsIgnoreCase(d.type)) {
            imgDevice.setImageResource(R.drawable.ic_fan); // thêm icon của bạn
        } else {
            imgDevice.setImageResource(R.drawable.ic_light);
        }

        // Cập nhật label nút "Xem tiếp"
        if (candidates.size() <= 1) btnNext.setVisibility(View.GONE);
        else btnNext.setText("Không phải, xem tiếp (" + (index+1) + "/" + candidates.size() + ")");
    }

    private void nextCandidate() {
        index = (index + 1) % candidates.size();
        bindCandidate();
    }

    public void show() { dialog.show(); }
}

