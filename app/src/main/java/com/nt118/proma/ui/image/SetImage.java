package com.nt118.proma.ui.image;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.gridlayout.widget.GridLayout;
import androidx.lifecycle.MutableLiveData;

import com.nt118.proma.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetImage extends AppCompatActivity {
    private ImageView imgBack;
    private GridLayout grid;
    private final MutableLiveData<Integer> selectedImage = new MutableLiveData<>();
    private TextView title_set_image;
    private List<Integer> images = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_image);

        InitUi();

        onClickBack();

    }


    private void putCircleImageToGrid() {
        selectedImage.setValue(-1);
        for (Integer image : images) {
            CircleImageView imageView = new CircleImageView(this);
            imageView.setImageResource(image);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(images.indexOf(image) / 3), GridLayout.spec(images.indexOf(image) % 3));
            float pxWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
            float pxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
            params.width = (int) pxWidth;
            params.height = (int) pxHeight;
            float pxMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
            params.setMargins((int) pxMargin, (int) pxMargin, (int) pxMargin, (int) pxMargin);
            imageView.setLayoutParams(params);
            grid.addView(imageView);
            imageView.setOnClickListener(v -> {
                if (Objects.equals(selectedImage.getValue(), image)) {
                    selectedImage.setValue(-1);
                    return;
                }
                selectedImage.setValue(images.indexOf(image));
            });
            selectedImage.observe(this, integer -> {
                if (Objects.equals(integer, images.indexOf(image))) {
                    imageView.setBorderColor(Color.parseColor("#007AFF"));
                    float pxBorderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
                    imageView.setBorderWidth((int) pxBorderWidth);
                } else {
                    imageView.setBorderWidth(0);
                }
            });
        }
    }

    private void putSquareImageToGrid() {
        selectedImage.setValue(-1);
        for (Integer image : images) {
            CardView img_card = (CardView) getLayoutInflater().inflate(R.layout.img_card, null);
            ImageView img_view = img_card.findViewById(R.id.img_view);
            FrameLayout border = img_card.findViewById(R.id.border);
            img_view.setImageResource(image);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(images.indexOf(image) / 3), GridLayout.spec(images.indexOf(image) % 3));
            float pxWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
            float pxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
            params.width = (int) pxWidth;
            params.height = (int) pxHeight;
            float pxMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
            params.setMargins((int) pxMargin, (int) pxMargin, (int) pxMargin, (int) pxMargin);
            img_card.setLayoutParams(params);
            grid.addView(img_card);
            img_card.setOnClickListener(v -> {
                if (Objects.equals(selectedImage.getValue(), image)) {
                    selectedImage.setValue(-1);
                    return;
                }
                selectedImage.setValue(images.indexOf(image));
                });
            selectedImage.observe(this, integer -> {
                if (Objects.equals(integer, images.indexOf(image))) {
                    border.setVisibility(View.VISIBLE);
                } else {
                    border.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private void onClickBack() {
        imgBack.setOnClickListener(v -> finish());
    }

    private void InitUi() {
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        images = intent.getIntegerArrayListExtra("images");
        imgBack = findViewById(R.id.img_Back);
        grid = findViewById(R.id.grid);
        title_set_image = findViewById(R.id.title_set_image);
        if (type.equals("avatar")) {
            title_set_image.setText("Set avatar");
            putCircleImageToGrid();
        } else {
            title_set_image.setText("Set cover");
            putSquareImageToGrid();
        }
        Button apply = findViewById(R.id.applybtn);
        apply.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("image", selectedImage.getValue());
            setResult(RESULT_OK, returnIntent);
            finish();
        });
    }
}