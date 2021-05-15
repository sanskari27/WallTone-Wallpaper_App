package com.excessivemedia.walltone.helpers;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.Log;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    public static int getDominantColor(Bitmap bitmap) {
        List<Palette.Swatch> swatchesTemp = Palette.from(bitmap).generate().getSwatches();
        List<Palette.Swatch> swatches = new ArrayList<>(swatchesTemp);
        Collections.sort(swatches, (swatch1, swatch2) -> swatch2.getPopulation() - swatch1.getPopulation());
        if(swatches.size()>0){
            Palette.Swatch swatch = swatches.get(0);
            return ColorUtils.HSLToColor(swatch.getHsl());
        }return Color.WHITE;
    }

    public static void gradientTextView(TextView textView, int startColor, int endColor){
        try{
            TextPaint paint = textView.getPaint();
            float width = paint.measureText("Ready to Redesign");
            Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                    startColor,endColor, Shader.TileMode.MIRROR);
            textView.getPaint().setShader(textShader);
        }catch (Exception e){
            Log.e(TAG,e.getMessage(),e);
        }
    }

    public static String stripExtension (String str) {
        if (str == null) return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) return str;
        return str.substring(0, pos);
    }
}
