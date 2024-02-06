package edu.ucsb.ece150.pickture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

/*
 * This is the main activity of Pickture. It will should display the user's profile picture
 * and the user's first/last name. An example ImageView and example picture is given.
 *
 * Remember to read through all available documentation (there are so many Android development
 * guides that can be found) and read through your error logs.
 */
public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final ImageView exampleImage = (ImageView) this.findViewById(R.id.exampleImageView);
        exampleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, GalleryActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        // [TODO] Hint: You will need this for implementing graceful app shutdown
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences("PICKTURE_PREF", MODE_PRIVATE);
        int selectedImage = sharedPreferences.getInt("selectedImage", 0);
        if (selectedImage != 0) {
            ImageView exampleImage = findViewById(R.id.exampleImageView);
            exampleImage.setImageResource(selectedImage);
        }
    }

    /*
     * You may or may not need this function depending on how you decide to pass messages
     * between your activities.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                int selectedImage = data.getIntExtra("selectedImage", 0);
                if (selectedImage != 0) {
                    ImageView exampleImage = findViewById(R.id.exampleImageView);
                    exampleImage.setImageResource(selectedImage);
                }
            }
        }
    }
}
