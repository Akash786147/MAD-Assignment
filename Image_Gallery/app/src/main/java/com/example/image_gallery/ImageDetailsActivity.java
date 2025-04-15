package com.example.image_gallery;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

public class ImageDetailsActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textDetails;
    Button delete;
    String imagePath;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        imageView = findViewById(R.id.imageView);
        textDetails = findViewById(R.id.textDetails);
        delete = findViewById(R.id.delete);

        // Get the image URI or path from the Intent
        String uriString = getIntent().getStringExtra("imageUri");
        imagePath = getIntent().getStringExtra("imagePath");

        if (uriString == null && imagePath == null) {
            Toast.makeText(this, "Invalid image", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // If URI is passed, use it
        if (uriString != null) {
            imageUri = Uri.parse(uriString);
            loadImageFromUri(imageUri);
        }

        // If path is passed, use it
        else if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(bitmap);

                String info = "Name: " + imgFile.getName() +
                        "\nPath: " + imgFile.getAbsolutePath() +
                        "\nSize: " + (imgFile.length() / 1024) + " KB" +
                        "\nLast Modified: " + new Date(imgFile.lastModified());
                textDetails.setText(info);
            }
        }

        delete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        try {
                            if (imageUri != null) {
                                // Deleting the image using URI
                                boolean deleted = DocumentsContract.deleteDocument(getContentResolver(), imageUri);
                                if (deleted) {
                                    Toast.makeText(this, "Image Deleted", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                                }
                            } else if (imagePath != null) {
                                // Deleting the image using file path
                                File imgFile = new File(imagePath);
                                if (imgFile.delete()) {
                                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void loadImageFromUri(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = resolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imageView.setImageBitmap(bitmap);

            // Use ContentResolver to query file metadata
            String name = "Unknown";
            String size = "Unknown";
            long lastModified = -1;

            String[] projection = {
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.SIZE,
                    MediaStore.MediaColumns.DATE_MODIFIED
            };

            try (Cursor cursor = resolver.query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    int sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
                    int dateIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED);

                    if (nameIndex != -1) name = cursor.getString(nameIndex);
                    if (sizeIndex != -1) size = (cursor.getLong(sizeIndex) / 1024) + " KB";
                    if (dateIndex != -1) lastModified = cursor.getLong(dateIndex) * 1000;
                }
            }

            String info = "Name: " + name +
                    "\nPath: " + uri.toString() +
                    "\nSize: " + size +
                    (lastModified > 0 ? "\nLast Modified: " + new Date(lastModified) : "");
            textDetails.setText(info);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }
}
