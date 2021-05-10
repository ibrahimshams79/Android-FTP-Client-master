package com.tv9.ftp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.os.Environment.getDataDirectory;

public class LocalHomeActivity extends AppCompatActivity {
    private Toolbar toolbar;

    private com.baoyz.swipemenulistview.SwipeMenuListView listview;
    private List<HashMap<String,Object>> simpleAdptList;;
    private SimpleAdapter simpleAdapter;

    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_home);

        Intent intent = getIntent();
        String uploadMessage = intent.getStringExtra("title");
        if (uploadMessage!= null && uploadMessage.contentEquals("Please select the file to upload")) {
            isUploading = true;
        }

        initViews();

        setListeners();
    }

//    Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void initViews() {
        verifyStoragePermissions(this);
        toolbar = findViewById(R.id.home_toolbar);
        toolbar.setTitle("My local file");
        if (isUploading) {
            toolbar.setTitle("Please select the file to upload");
        }
        setSupportActionBar(toolbar);

        listview = findViewById(R.id.names_list_view);

        String[] from = {"icon", "name"};
        int[] to = {R.id.cell_image, R.id.cell_name};

        simpleAdptList = new ArrayList<>();
        File file2 = new File("/data" + getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "ftpdownload");

        File file1 = new File(getExternalFilesDir("/"), "ftpdownload");

//        Toast.makeText(LocalHomeActivity.this, file1.toString(), Toast.LENGTH_LONG).show();

        if (!file1.exists()) {
            file1.mkdirs();
        }

        //
        //Traverse the local file array, judge the file format by the suffix name, and display different file format icons
        for(File file: file1.listFiles()) {
            HashMap<String, Object> hashMap
                    = new HashMap<>();
            hashMap.put("icon",R.drawable.other);
            if (file.isDirectory()) {
                hashMap.put("icon",R.drawable.folder);
            } else if (file.getName().endsWith(".txt")) {
                hashMap.put("icon",R.drawable.txt);
            } else if (file.getName().endsWith(".pdf")) {
                hashMap.put("icon",R.drawable.pdf);
            } else if (file.getName().endsWith(".img")) {
                hashMap.put("icon",R.drawable.swift);
            } else if (file.getName().endsWith(".jpg")) {
                hashMap.put("icon",R.drawable.html);
            } else if (file.getName().endsWith(".java")) {
                hashMap.put("icon",R.drawable.java);
            } else if (file.getName().endsWith(".py")) {
                hashMap.put("icon",R.drawable.py);
            } else if (file.getName().endsWith(".zip")) {
                hashMap.put("icon",R.drawable.zip);
            } else if (file.getName().endsWith(".rar")) {
                hashMap.put("icon",R.drawable.rar);
            }

            hashMap.put("name",file.getName());
            hashMap.put("file",file);
            simpleAdptList.add(hashMap);
        }

        simpleAdapter = new SimpleAdapter(getApplicationContext(), simpleAdptList, R.layout.cell, from, to);
        listview.setAdapter(simpleAdapter);
    }

    private void setListeners() {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private static final String TAG = "";

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filename = (String)simpleAdptList.get(position).get("name");
//                String filePath = "/data" + Environment.getDataDirectory().getAbsolutePath()
//                        + File.separator + getPackageName()
//                        + File.separator + "ftpdownload" + File.separator + filename;

                File file1 = new File(getExternalFilesDir("/"), "ftpdownload");
                String filePath = file1.getPath();

//                Toast.makeText(LocalHomeActivity.this, filePath, Toast.LENGTH_LONG).show();
                if (isUploading) {
                    Intent intent = new Intent(LocalHomeActivity.this, FolderActivity.class);
                    intent.putExtra("filepath", filePath);
                    intent.putExtra("filename", filename);
                    setResult(1, intent);
                    finish();

                } else {
                    Intent intent = new Intent(LocalHomeActivity.this, TxtActivity.class);
                    if (filename.endsWith(".pdf")) {
                        intent = new Intent(LocalHomeActivity.this, PDFActivity.class);
                    }
                    intent.putExtra("filepath", filePath);
                    intent.putExtra("filename", filename);
                    startActivity(intent);
                }

            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                deleteItem.setWidth(200);
                deleteItem.setIcon(R.drawable.delete);
                menu.addMenuItem(deleteItem);
            }
        };
        listview.setMenuCreator(creator);
        listview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        File file = (File)simpleAdptList.get(position).get("file");
                        file.delete();
                        simpleAdptList.remove(position);
                        simpleAdapter.notifyDataSetChanged();
                    default:
                        break;
                }

                // true : close the menu; false : not close the menu
                return true;
            }
        });
        listview.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
    }
}
