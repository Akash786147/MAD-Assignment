package com.example.image_gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.GridView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private GridView gridView;
    private final ArrayList<Uri> imageUris = new ArrayList<>();
    private ImageUriAdapter imageAdapter;

    private Uri selectedSaveFolderUri = null;
    private Uri selectedLoadFolderUri = null;

    private ActivityResultLauncher<Intent> selectSaveFolderLauncher;
    private ActivityResultLauncher<Intent> selectLoadFolderLauncher;
    private ActivityResultLauncher<Intent> selectImagesLauncher;

    private static final int REQUEST_IMAGE_DETAILS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeLaunchers();
        setListeners();
    }

    private void initializeViews() {
        gridView = findViewById(R.id.gridView);
    }

    private void initializeLaunchers() {
        selectSaveFolderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        selectedSaveFolderUri = result.getData().getData();
                        grantUriPermissions(selectedSaveFolderUri);
                        takePhoto();
                    }
                });

        selectLoadFolderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        selectedLoadFolderUri = result.getData().getData();
                        grantUriPermissions(selectedLoadFolderUri);
                        loadImagesFromFolder();
                    }
                });

        selectImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUris.clear();
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                imageUris.add(imageUri);
                            }
                        } else if (result.getData().getData() != null) {
                            imageUris.add(result.getData().getData());
                        }
                        updateGridView();
                    }
                });
    }

    private void setListeners() {
        findViewById(R.id.TakePhoto).setOnClickListener(v -> openFolderPicker(true));
        findViewById(R.id.LoadImages).setOnClickListener(v -> openFolderPicker(false));
        findViewById(R.id.selectImages).setOnClickListener(v -> openImagePicker());

        gridView.setOnItemClickListener((adapterView, view, position, id) -> {
            Intent intent = new Intent(this, ImageDetailsActivity.class);
            intent.putExtra("imageUri", imageUris.get(position).toString());
            intent.putExtra("position", position);
            startActivityForResult(intent, REQUEST_IMAGE_DETAILS);
        });
    }

    private void openFolderPicker(boolean isSave) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (isSave) {
            selectSaveFolderLauncher.launch(intent);
        } else {
            selectLoadFolderLauncher.launch(intent);
        }
    }

    private void grantUriPermissions(Uri uri) {
        if (uri != null) {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );
        }
    }

    private void takePhoto() {
        if (selectedSaveFolderUri == null) {
            showToast("No save folder selected");
            return;
        }

        String fileName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        DocumentFile folder = DocumentFile.fromTreeUri(this, selectedSaveFolderUri);
        DocumentFile newFile = folder.createFile("image/jpeg", fileName);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, newFile.getUri());
        startActivity(intent);
    }

    private void loadImagesFromFolder() {
        imageUris.clear();

        if (selectedLoadFolderUri == null) {
            showToast("No folder selected");
            return;
        }

        DocumentFile folder = DocumentFile.fromTreeUri(this, selectedLoadFolderUri);
        for (DocumentFile file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".jpg")) {
                imageUris.add(file.getUri());
            }
        }
        updateGridView();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                Intent.FLAG_GRANT_READ_URI_PERMISSION);
        selectImagesLauncher.launch(intent);
    }

    private void updateGridView() {
        imageAdapter = new ImageUriAdapter(this, imageUris);
        gridView.setAdapter(imageAdapter);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_DETAILS && resultCode == RESULT_OK && data != null) {
            boolean deleted = data.getBooleanExtra("image_deleted", false);
            int position = data.getIntExtra("position", -1);
            if (deleted && position >= 0 && position < imageUris.size()) {
                imageUris.remove(position);
                imageAdapter.notifyDataSetChanged();
                showToast("Image removed from list");
            }
        }
    }
}
