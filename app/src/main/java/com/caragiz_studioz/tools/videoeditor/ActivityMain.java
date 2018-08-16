package com.caragiz_studioz.tools.videoeditor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class ActivityMain extends AppCompatActivity {
    private static boolean supported = false;
    private String[] cmd = new String[1];
    private TextView output;
    private static final int RC_FILE_ACCESS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        output = findViewById(R.id.sample_text);
        initialize(this);
        cmd[0] = "";
        if (supported)
            exexuteCommand(null);

        getFileLocation();
    }

    private void exexuteCommand(@Nullable String command) {
        if (command != null)
            cmd[0] = command;
        if (!cmd[0].isEmpty()) {
            FFmpeg ffMpeg = FFmpeg.getInstance(this);
            try {
                ffMpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
                    @Override
                    public void onSuccess(String message) {
                        output.setText(message);
                    }

                    @Override
                    public void onProgress(String message) {

                    }

                    @Override
                    public void onFailure(String message) {
                        Log.i("Error", message);
                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onFinish() {

                    }
                });
            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }
        }
    }

    public void initialize(final Context context) {
        final FFmpeg ffmpeg = FFmpeg.getInstance(context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onSuccess() {
                    super.onSuccess();
                    Toast.makeText(context, "FFMpeg Supported", Toast.LENGTH_SHORT).show();
                    supported = true;
                    exexuteCommand(null);
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
            Toast.makeText(context, "FFMpeg not supported", Toast.LENGTH_SHORT).show();
        }
    }

    private void getFileLocation() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, RC_FILE_ACCESS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RC_FILE_ACCESS) {

                if (data != null) {
                    Uri uri = data.getData();
                    InputStream in = getContentResolver().openInputStream(uri);
                    File newFile = new File(getExternalFilesDir("temp"), "temp");

                    if (!newFile.exists())
                        newFile.createNewFile();
                    OutputStream out = new FileOutputStream(newFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0)
                        out.write(buf, 0, len);
                    out.close();
                    in.close();

                    if (newFile.exists())
                        Toast.makeText(this, newFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                    exexuteCommand("-i " + newFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }

}
