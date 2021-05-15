package com.excessivemedia.walltone.widgets.ColorPicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.helpers.Consts;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CustomColorPicker extends CardView implements View.OnClickListener ,
                                ColorPickerAdapter.OnColorSelected{
    private static ArrayList<String> colorCodes;
    private ColorPickerAdapter pickerAdapter;
    private String selectedColor;
    private CardView picker_btn;
    private AlertDialog pickerDialog;
    private View view,dialog;
    private Context mContext;
    private ColorChangeListener listener;

    public CustomColorPicker(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CustomColorPicker(@NonNull Context context, @Nullable  AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public interface ColorChangeListener{
        void onColorChanged(String color);
    }

    public void setOnColorChangeListener(ColorChangeListener listener) {
        this.listener = listener;
    }

    private void init(Context mContext) {
        this.mContext = mContext;
        view = LayoutInflater.from(mContext).inflate(R.layout.ccp_wdget,this,true);
        picker_btn = view.findViewById(R.id.color_picker_btn);
        picker_btn.setOnClickListener(this);
        updateUI();
    }

    public void setSelectedColor(String selectedColor) {
        this.selectedColor = selectedColor;
        updateUI();
        updateUI();
    }

    @SuppressLint("InflateParams")
    private void initDialog() {
        dialog = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_color_dialog, null,false);
        updateDialog();
        RecyclerView colorRecycler = dialog.findViewById(R.id.colorRecycler);
        colorRecycler.setHasFixedSize(true);
        colorRecycler.setLayoutManager(new GridLayoutManager(mContext,3,RecyclerView.VERTICAL,false));

        pickerAdapter = new ColorPickerAdapter(colorCodes);
        pickerAdapter.setOnColorSelected(this);
        colorRecycler.setAdapter(pickerAdapter);

        pickerDialog = new MaterialAlertDialogBuilder(mContext, R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
                .setView(dialog)
                .setCancelable(true)
                .create();
        dialog.findViewById(R.id.color_wheel_icon).setOnClickListener(v->{
            selectedColor = null;
            updateDialog();
            updateUI();
        });

    }

    private void updateDialog() {
        if(selectedColor == null || selectedColor.isEmpty()){
            (dialog.findViewById(R.id.color_wheel_icon)).setVisibility(VISIBLE);
        }else{
            ((CardView)dialog.findViewById(R.id.color_picker_card)).setCardBackgroundColor(Color.parseColor(selectedColor));
            (dialog.findViewById(R.id.color_wheel_icon)).setVisibility(GONE);
        }
    }


    private void updateUI() {
        Log.wtf(CustomColorPicker.class.getSimpleName(),selectedColor);
        if(selectedColor == null || selectedColor.isEmpty()){
            (view.findViewById(R.id.color_wheel_icon)).setVisibility(VISIBLE);

        }else{
            picker_btn.setCardBackgroundColor(Color.parseColor(selectedColor));
            (view.findViewById(R.id.color_wheel_icon)).setVisibility(GONE);

        }

    }

    public static void extractColors(List<DocumentSnapshot> list) {
        HashSet<String> colorCode = new HashSet<>();
        for (DocumentSnapshot ds:list) {
            colorCode.add(ds.getString(Consts.COLOR_CODE));
        }
        if(colorCodes == null){
            colorCodes = new ArrayList<>();
        }
        colorCodes.clear();
        colorCode.add(null);
        colorCodes.addAll(colorCode);

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        super.setVisibility(enabled?VISIBLE:GONE);
    }

    @Override
    public void onClick(View v) {
        if(pickerDialog==null) initDialog();
        pickerDialog.show();
    }

    @Override
    public void onColorSelected(String color) {
        selectedColor = color;
        updateDialog();
        updateUI();
        if(listener!=null){
            listener.onColorChanged(selectedColor);
        }
    }

    public void closeDialog(){
        pickerDialog.dismiss();
    }
}
