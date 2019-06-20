package com.cianciaruso_cataldo.cnn.image_analyzer.activity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.AsyncLayoutInflater;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.cianciaruso_cataldo.cnn.image_analyzer.R;
import com.cianciaruso_cataldo.cnn.image_analyzer.utils.FileUtil;
import com.cianciaruso_cataldo.cnn.image_analyzer.utils.GlideApp;
import com.cianciaruso_cataldo.cnn.image_analyzer.utils.HTMLFormatter;
import com.cianciaruso_cataldo.cnn.image_analyzer.utils.http.RetrofitInterface;
import com.cianciaruso_cataldo.cnn.image_analyzer.widget.AdvancedArrayAdapter;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.preference.PowerPreference;
import com.preference.Preference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    public static String server_address = "http:/192.168.1.15";
    public static int port = 8081;
    private static final int GALLERY = 100;
    private static final int CAMERA = 200;

    private ProgressBar loading;
    private Bitmap img;
    private ImageView image_display;
    private LinearLayout parent;
    private DrawerLayout drawer;
    private String pictureImagePath = "";
    AlertDialog.Builder pictureDialog;


    private static HashMap<String, String> types = new HashMap<String, String>() {{
        put("w", "woman");
        put("m", "man");
        put("u", "undefined");
    }};


    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_layout);

        new AsyncLayoutInflater(this).inflate(R.layout.activity_main, null, (view, resid, parent) ->
                AsyncTask.execute(() -> {
                    Preference settings = PowerPreference.getDefaultFile();
                    SettingsActivity.is_animations_enabled = settings.getBoolean("animations", true);
                    server_address = settings.getString("address", "https://nn-image-analyzer.herokuapp.com/");
                    port = settings.getInt("port", 8081);
                    Log.i("ADDRESS", server_address + ":" + port);

                    final Integer[] icons = new Integer[]{android.R.drawable.ic_menu_gallery, android.R.drawable.ic_menu_camera, android.R.drawable.ic_menu_close_clear_cancel};
                    String[] items = {
                            getString(R.string.gallery_option),
                            getString(R.string.camera_option),
                            getString(R.string.cancel)
                    };

                    ListAdapter adapter = new AdvancedArrayAdapter(this, items, icons);

                    pictureDialog = new AlertDialog.Builder(this, R.style.DialogTheme);
                    pictureDialog.setTitle(getString(R.string.action_title));


                    pictureDialog.setAdapter(adapter,
                            (DialogInterface dialog, int which) -> {
                                switch (which) {
                                    case 0: {
                                        galleryIntent();
                                    }
                                    break;

                                    case 1: {
                                        cameraIntent();
                                    }
                                    break;

                                    case 2:
                                        dialog.dismiss();
                                        break;
                                }
                            });

                    initViews(view);
                }));
    }


    private void initViews(View view) {

        drawer = view.findViewById(R.id.drawer_layout);
        image_display = drawer.findViewById(R.id.image_display);
        loading = drawer.findViewById(R.id.progress);
        loading.setVisibility(View.GONE);
        parent = drawer.findViewById(R.id.result_container);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.arrow_down_float);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText(R.string.toolbar_title);
        drawer.findViewById(R.id.btn_select_image).setOnClickListener((View v) -> pictureDialog.show());

        runOnUiThread(() -> {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            setContentView(view);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (loading != null) {
            loading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
        } else
            super.onBackPressed();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GALLERY) {

            if (resultCode == RESULT_OK && data.getData() != null) {

                runOnUiThread(() -> parent.removeAllViews());

                AsyncTask.execute(() -> {
                    try {
                        byte[] bytes = getBytes(getContentResolver().openInputStream(data.getData()));
                        img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        uploadImage(bytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } else if (requestCode == CAMERA && resultCode == RESULT_OK) {
            File imgFile = new File(pictureImagePath);
            if (imgFile.exists()) {
                AsyncTask.execute(() -> {
                    try {
                        byte[] bytes = getBytes(getContentResolver().openInputStream(Uri.fromFile(imgFile)));
                        img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        uploadImage(bytes);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }
    }


    public void onClickMenu(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_settings: {
                Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
                SettingsActivity.pause = true;
                drawer.closeDrawers();
                startActivity(intentSettings);
            }
            break;

            case R.id.nav_about_us: {

            }
            break;

            default:
                break;
        }
    }


    public byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();

        int buffSize = 1024;
        byte[] buff = new byte[buffSize];

        int len;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }

        return byteBuff.toByteArray();
    }


    private void uploadImage(byte[] imageBytes) {
        runOnUiThread(() -> loading.setVisibility(View.VISIBLE));

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(server_address + ":" + port)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(okHttpClient)
                .build();

        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);
        Call<String> call = retrofitInterface.uploadImage(body);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {

                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        parent.removeAllViews();
                        loading.setVisibility(View.GONE);
                        GlideApp.with(MainActivity.this)
                                .asBitmap()
                                .load(img)
                                .into(image_display);

                        if (SettingsActivity.is_animations_enabled) {
                            YoYo.with(Techniques.SlideInLeft)
                                    .duration(400)
                                    .onEnd((Animator animator) -> setViews(response.body()))
                                    .playOn(image_display);

                        } else {
                            setViews(response.body());
                        }

                    });
                } else {
                    //response.raw().close();
                    Toasty.error(MainActivity.this, "Server connection error. Try again", Toast.LENGTH_SHORT, true).show();
                    loading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                runOnUiThread(() -> {
                            loading.setVisibility(View.GONE);
                            Toasty.error(MainActivity.this, "Server connection error. Try again", Toast.LENGTH_SHORT, true).show();
                        }
                );
            }
        });
    }

    public void setViews(String txt) {
        Point screen_size = new Point();
        getWindowManager().getDefaultDisplay().getSize(screen_size);
        String[] split = txt.split("FACE");
        boolean noFace = false, noObjects = false;

        String[] faces = new String[0], objects = new String[0];

        try {
            faces = split[1].split(",");
        } catch (ArrayIndexOutOfBoundsException ex) {
            noFace = true;
        }

        try {
            objects = split[0].replace("OBJ", "").split(",");
        } catch (ArrayIndexOutOfBoundsException ex) {
            noObjects = true;
        }
        String result;
        int i;
        int[] coordinates = new int[4];

        //Parse Faces
        if (!noFace) {
            addSeparator("Faces");
            for (i = 0; i < faces.length; i++) {
                String[] psplit = faces[i].split("-");
                String[] coords = psplit[1].split(" ");
                coordinates[0] = Integer.parseInt(coords[0]);
                coordinates[1] = Integer.parseInt(coords[1]);
                coordinates[2] = Integer.parseInt(coords[2]);
                coordinates[3] = Integer.parseInt(coords[3]);
                String[] temp = psplit[3].split(" ");

                if(psplit[2].contains("undefined")){
                    psplit[2]=psplit[2].replace("undefined","");
                }

                result = "<br><p>" + HTMLFormatter.getWhiteSpaces(1) + HTMLFormatter.getColoredText("Person", HTMLFormatter.HTMLColor.red) + "</p><br>" +
                        "<p>" + HTMLFormatter.getWhiteSpaces(5) + HTMLFormatter.getColoredText("Gender", HTMLFormatter.HTMLColor.yellow) + " : " + types.get(psplit[0]) + "</p>" +
                        "<p>" + HTMLFormatter.getWhiteSpaces(8) + HTMLFormatter.getColoredText("Age", HTMLFormatter.HTMLColor.yellow) + " : " + psplit[2] + "</p><br>" +
                        "<p>" + HTMLFormatter.getWhiteSpaces(3) + HTMLFormatter.getColoredText("Emotions", HTMLFormatter.HTMLColor.blue) + "</p><br>" +
                        "<p>" + HTMLFormatter.getWhiteSpaces(6) + HTMLFormatter.getColoredText("Angry", HTMLFormatter.HTMLColor.yellow) + " : " + temp[0] + "</p>" +
                        "<p>" + HTMLFormatter.getWhiteSpaces(4) + HTMLFormatter.getColoredText(" Disgust", HTMLFormatter.HTMLColor.yellow) + " : " + temp[1] + "</p>" +
                        "<p>" + HTMLFormatter.getWhiteSpaces(7) + HTMLFormatter.getColoredText(" Fear", HTMLFormatter.HTMLColor.yellow) + " : " + temp[2] + "</p>" +
                        "<p>" + HTMLFormatter.getWhiteSpaces(6) + HTMLFormatter.getColoredText("Happy", HTMLFormatter.HTMLColor.yellow) + " : " + temp[3] + "</p>" +
                        "<p>" + HTMLFormatter.getWhiteSpaces(8) + HTMLFormatter.getColoredText("Sad", HTMLFormatter.HTMLColor.yellow) + " : " + temp[4] + "</p>" +
                        "<p>" + HTMLFormatter.getWhiteSpaces(4) + HTMLFormatter.getColoredText("Surprise", HTMLFormatter.HTMLColor.yellow) + " : " + temp[5] + "</p>" +
                        "<p>" + HTMLFormatter.getWhiteSpaces(5) + HTMLFormatter.getColoredText("Neutral", HTMLFormatter.HTMLColor.yellow) + " : " + temp[6] + "</p>";
                addTile(result, coordinates, screen_size);
            }
        }

        //Parse Objects
        if (!noObjects && !objects[0].equals("")) {
            addSeparator("Objects");
            for (i = 0; i < objects.length; i++) {
                String[] splitOb = objects[i].split("-");
                String[] tcoord = splitOb[1].split(" ");
                coordinates[0] = Integer.parseInt(tcoord[0]);
                coordinates[1] = Integer.parseInt(tcoord[1]);
                coordinates[2] = Integer.parseInt(tcoord[2]);
                coordinates[3] = Integer.parseInt(tcoord[3]);

                result = "<br><p>" + HTMLFormatter.getWhiteSpaces(4) + HTMLFormatter.getColoredText("Object", HTMLFormatter.HTMLColor.red) + "</p><br>" +
                        "<p>" + HTMLFormatter.getWhiteSpaces(4) + HTMLFormatter.getColoredText("Type", HTMLFormatter.HTMLColor.yellow) + " : " + splitOb[0] + "</p><br><br><br><br><br>";
                addTile(result, coordinates, screen_size);
            }
        }
    }


    public void addTile(String description, int[] crop, Point screen_size) {
        int new_width = crop[2] - crop[0];
        int new_height = crop[3] - crop[1];

        if (new_width + crop[0] > img.getWidth()) {
            new_width = img.getWidth() - crop[0];
        }

        if (new_height + crop[1] > img.getHeight()) {
            new_height = img.getHeight() - crop[1];
        }

        Bitmap cropped_bitmap = Bitmap.createBitmap(img, crop[0], crop[1], new_width, new_height);
        cropped_bitmap = Bitmap.createScaledBitmap(cropped_bitmap, screen_size.x / 3, 470, false);
        LinearLayout image_container = new LinearLayout(this);
        LinearLayout result_container = new LinearLayout(this);

        result_container.setOrientation(LinearLayout.HORIZONTAL);

        image_container.setBackground(ContextCompat.getDrawable(this, R.drawable.tile));
        image_container.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams display_params = new LinearLayout.LayoutParams(
                screen_size.x / 3, LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout.LayoutParams image_container_params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TableRow.LayoutParams text_params = new TableRow.LayoutParams(20);

        LinearLayout.LayoutParams container_params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        ImageView display = new ImageView(this);
        TextView info_box = new TextView(this);

        text_params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        info_box.setVerticalScrollBarEnabled(true);
        info_box.setMovementMethod(new ScrollingMovementMethod());
        info_box.setTextSize(21);
        info_box.setTypeface(info_box.getTypeface(), Typeface.BOLD);

        info_box.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT));
        info_box.setGravity(Gravity.START);
        info_box.setTextColor(Color.WHITE);
        info_box.setLayoutParams(text_params);

        image_container_params.bottomMargin = 100;
        display_params.leftMargin = 50;

        GlideApp.with(this)
                .asBitmap()
                .load(cropped_bitmap)
                .into(display);
        display.setLayoutParams(display_params);


        Button share = new Button(this);
        share.setCompoundDrawablesWithIntrinsicBounds(R.drawable.share, 0, 0, 0);
        share.setBackgroundColor(Color.TRANSPARENT);
        share.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams share_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        share.setOnClickListener((View view) -> AsyncTask.execute(() ->
                FileUtil.shareView(result_container))
        );
        share_params.gravity=Gravity.CENTER;
        share.setLayoutParams(share_params);

        result_container.addView(display);
        result_container.addView(info_box);
        result_container.setLayoutParams(container_params);

        image_container.setLayoutParams(image_container_params);
        image_container.addView(result_container);
        image_container.addView(share);

        parent.addView(image_container);

        if (SettingsActivity.is_animations_enabled) {
            YoYo.with(Techniques.SlideInLeft)
                    .duration(400)
                    .playOn(result_container);
        }
    }

    public void addSeparator(String title) {
        LinearLayout result_container = new LinearLayout(this);
        result_container.setOrientation(LinearLayout.VERTICAL);
        TableRow.LayoutParams text_params = new TableRow.LayoutParams(20);

        LinearLayout.LayoutParams container_params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView info_box = new TextView(this);

        text_params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        info_box.setTextSize(42);
        info_box.setTypeface(info_box.getTypeface(), Typeface.BOLD);

        info_box.setText(Html.fromHtml(HTMLFormatter.getColoredText("<br>" + title, HTMLFormatter.HTMLColor.white), Html.FROM_HTML_MODE_COMPACT));
        info_box.setGravity(Gravity.CENTER);
        info_box.setTextColor(Color.WHITE);
        info_box.setLayoutParams(text_params);

        container_params.bottomMargin = 40;

        result_container.addView(info_box);
        result_container.setLayoutParams(container_params);

        parent.addView(result_container);

        if (SettingsActivity.is_animations_enabled) {
            YoYo.with(Techniques.SlideInLeft)
                    .duration(400)
                    .playOn(result_container);
        }
    }

    /**
     * Capture image from default camera app on device
     */
    private void cameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                String timeStamp = SimpleDateFormat.getDateInstance().format(new Date());
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File photoFile = File.createTempFile(
                        "IMAGE_" + timeStamp,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );

                pictureImagePath = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.cianciaruso_cataldo.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA);
            } catch (IOException ex) {
                runOnUiThread(() -> Toasty.error(this, "Can't capture image from Camera app !"));
            }
        }
    }

    /**
     * Get image from default gallery app
     */
    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");

        try {
            startActivityForResult(intent, GALLERY);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}


