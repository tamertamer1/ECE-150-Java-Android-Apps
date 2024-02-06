package edu.ucsb.ece150.pickture;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.GridLayout;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
public class GalleryActivity extends AppCompatActivity {

    // Image resource IDs
    private int[] imageResources = {
            R.drawable.connor,
            R.drawable.chloe,
            R.drawable.amanda,
            R.drawable.sylvie,
            R.drawable.alina,
            R.drawable.dusty
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        GridLayout gridLayout = findViewById(R.id.gridLayout);

        for (int imageRes : imageResources) {
            final int currentImageRes = imageRes;

            ImageView imageView = new ImageView(this);
            imageView.setImageResource(currentImageRes);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setAdjustViewBounds(true);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);

            imageView.setLayoutParams(params);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPreferences = getSharedPreferences("PICKTURE_PREF", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("selectedImage", currentImageRes); // Use the final local variable
                    editor.apply();

                    finish();
                }
            });

            gridLayout.addView(imageView);
        }
    }
}
