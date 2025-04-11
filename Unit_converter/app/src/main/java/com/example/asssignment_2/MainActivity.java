package com.example.asssignment_2;


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

public class MainActivity extends AppCompatActivity {
    private Spinner fromUnitSpinner;
    private Spinner toUnitSpinner;
    private EditText editTextNumber;
    private TextView answer;
    private Button convertButton;
    private Switch themeSwitch;
//    private  LottieAnimationView lottie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        fromUnitSpinner = findViewById(R.id.fromUnit);
        toUnitSpinner = findViewById(R.id.to_Unit);
        editTextNumber = findViewById(R.id.editTextNumber);
        answer = findViewById(R.id.result);
        convertButton = findViewById(R.id.convert_button);


        convertButton.setOnClickListener(v -> convertUnits());



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
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