package com.example.asssignment_2;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    //variables for the unit converter
    private Spinner fromUnitSpinner;
    private Spinner toUnitSpinner;
    private EditText editTextNumber;
    private TextView answer;
    private Button convertButton;
    private Switch themeSwitch;

    //variables for the app bar with drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;



    //    private  LottieAnimationView lottie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //fetching the components from the activity main
        fromUnitSpinner = findViewById(R.id.fromUnit);
        toUnitSpinner = findViewById(R.id.to_Unit);
        editTextNumber = findViewById(R.id.editTextNumber);
        answer = findViewById(R.id.result);
        convertButton = findViewById(R.id.convert_button);
        convertButton.setOnClickListener(v -> convertUnits());

        //fetching components for app bar
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);

        // the drawer's open/close state
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);

        // Add the toggle as a listener to the DrawerLayout
        drawerLayout.addDrawerListener(toggle);

        // Synchronize the toggle's state with the linked DrawerLayout
        toggle.syncState();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle the selected item based on its I
                if (item.getItemId() == R.id.nav_settings) {
                    Intent intent = new Intent(MainActivity.this,Settings.class);
                    startActivity(intent);
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

    }


    public void convertUnits(){
        String from = fromUnitSpinner.getSelectedItem().toString();
        String to = toUnitSpinner.getSelectedItem().toString();
        String value = editTextNumber.getText().toString();

        if(value.isEmpty())
        {
            Toast.makeText(this, "Please input a number", Toast.LENGTH_LONG).show();
            return;
        }
        double valueInDouble = Double.parseDouble(value);
        double convertedValue = convert(from, to, valueInDouble);
        answer.setText(String.format(Locale.getDefault(), "%.2f %s", convertedValue, to));
    }

    public double convert(String fromUnit, String toUnit, double valueInDouble)
    {
        HashMap<String, Double> conversionMap = new HashMap<>();
        conversionMap.put("Foot", 0.3048);
        conversionMap.put("Inch", 0.0254);
        conversionMap.put("Centimetre", 0.01);
        conversionMap.put("Metre", 1.0);
        conversionMap.put("Yard", 0.9144);

        Double fromValue = conversionMap.getOrDefault(fromUnit, null);
        Double toValue = conversionMap.getOrDefault(toUnit, null);

        if(fromValue == null || toValue == null)
        {
            Toast.makeText(this, "Invalid unit selection", Toast.LENGTH_LONG).show();
            return 0;
        }

        double valueInMetre = valueInDouble * fromValue;
        return valueInMetre/toValue;
    }
}