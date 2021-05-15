package com.excessivemedia.walltone.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.service.WallpaperChanger;
import com.excessivemedia.walltone.widgets.ColorPicker.CustomColorPicker;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class BottomNavigation extends BottomSheetDialogFragment implements CustomColorPicker.ColorChangeListener {
    private SwitchCompat autoWall;
    private CustomColorPicker colorPicker;

    public BottomNavigation() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomNavigationTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_bottom_navigation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cacheSizeCheck(view);
        Context context = requireContext();

        view.findViewById(R.id.clearCache).setOnClickListener(v -> {
            deleteAllFiles(v.getContext().getCacheDir());
            cacheSizeCheck(view);
        });
        autoWall = (view.findViewById(R.id.switchAutoWallpaper));
        colorPicker = view.findViewById(R.id.colorPicker);
        String selectedColor = context.getSharedPreferences("ColorPref",0)
                .getString("selectedColor",null);
        colorPicker.setSelectedColor(selectedColor);
        colorPicker.setOnColorChangeListener(this);

        boolean isEnabled = (PendingIntent.getBroadcast(context, 0,
                new Intent(context,WallpaperChanger.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        colorPicker.setEnabled(isEnabled);
        autoWall.setChecked(isEnabled);
        autoWall.setOnCheckedChangeListener((buttonView, isChecked) -> {
            colorPicker.setEnabled(isChecked);
            WallpaperChanger.toggleAutoWallpaper(requireContext(), isChecked);
        });
    }

    @Override
    public void onColorChanged(String color) {
        if(getContext() == null) return;
            getContext().getSharedPreferences("ColorPref",0)
                    .edit()
                    .putString("selectedColor",color)
                    .apply();
    }

    private void cacheSizeCheck(@NonNull View view) {
        long fileSize = getFileSize(requireContext().getCacheDir());
        TextView cacheTV = view.findViewById(R.id.cacheSize);
        if(fileSize>1024){
            double size = Math.round((fileSize/1024f)*100)/100.0;
            cacheTV.setText(String.format("%s MB", size));
        }else
            cacheTV.setText(String.format("%s KB", fileSize));
    }

    public static long getFileSize(final File file) {
        if (file == null || !file.exists())
            return 0;
        if (!file.isDirectory())
            return file.length();
        final List<File> dirs = new LinkedList<>();
        dirs.add(file);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists())
                continue;
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0)
                continue;
            for (final File child : listFiles) {
                result += child.length();
                if (child.isDirectory())
                    dirs.add(child);
            }
        }
        return result/1000;
    }
    public static void deleteAllFiles(final File file) {
        if (file == null || !file.exists())
            return;
        if (!file.isDirectory()){
            file.delete();
            return ;
        }
        final List<File> dirs = new LinkedList<>();
        dirs.add(file);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists())
                continue;
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0)
                continue;
            for (final File child : listFiles) {
                result += child.length();
                if (child.isDirectory())
                    dirs.add(child);
                else child.delete();
            }
        }
    }

}