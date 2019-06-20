package com.cianciaruso_cataldo.cnn.image_analyzer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import es.dmoral.toasty.Toasty;

public class FileUtil {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private static void share_image(Bitmap image, Context context) {
        try {
            File imagesFolder = new File(context.getCacheDir(), "images");
            Uri uri;
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.cianciaruso_cataldo.fileprovider", file);
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/png");
            context.startActivity(intent);
        } catch (Exception e) {
            Toasty.error(context, "Error sharing result", Toast.LENGTH_SHORT);
        }

    }

    private static Bitmap getBitmapFromView(View view){
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static void shareView(View view){
        share_image(getBitmapFromView(view),view.getContext());
    }
}
