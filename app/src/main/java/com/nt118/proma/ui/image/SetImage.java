package com.nt118.proma.ui.image;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
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
    private MutableLiveData<Integer> selectedImage = new MutableLiveData<>();
    private TextView title_set_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_image);

        InitUi();

        onClickBack();

//        putSquareImageToGrid();
        putCircleImageToGrid();
    }


    private void putCircleImageToGrid() {

        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.avatar1);
        images.add(R.drawable.avatar2);
        images.add(R.drawable.avatar3);
        images.add(R.drawable.avatar4);
        images.add(R.drawable.avatar5);
        images.add(R.drawable.avatar6);
        images.add(R.drawable.avatar7);
        images.add(R.drawable.avatar8);
        images.add(R.drawable.avatar9);
        images.add(R.drawable.avatar10);
        images.add(R.drawable.avatar11);
        images.add(R.drawable.avatar12);
        images.add(R.drawable.avatar13);
        images.add(R.drawable.avatar14);
        images.add(R.drawable.avatar15);
        images.add(R.drawable.avatar16);
        images.add(R.drawable.avatar17);
        images.add(R.drawable.avatar18);
        images.add(R.drawable.avatar19);
        images.add(R.drawable.avatar20);
        images.add(R.drawable.avatar21);
        images.add(R.drawable.avatar22);
        images.add(R.drawable.avatar23);
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
                selectedImage.setValue(image);
            });
            selectedImage.observe(this, integer -> {
                if (Objects.equals(integer, image)) {
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
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.cover1);
        images.add(R.drawable.cover2);
        images.add(R.drawable.cover3);
        images.add(R.drawable.cover4);
        images.add(R.drawable.cover5);
        images.add(R.drawable.cover6);
        images.add(R.drawable.cover7);
        images.add(R.drawable.cover8);
        images.add(R.drawable.cover9);
        images.add(R.drawable.cover10);
        images.add(R.drawable.cover11);
        images.add(R.drawable.cover12);
        images.add(R.drawable.cover13);
        images.add(R.drawable.cover14);
        images.add(R.drawable.cover15);
        images.add(R.drawable.cover16);
        images.add(R.drawable.cover17);
        images.add(R.drawable.cover18);
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
                selectedImage.setValue(image);
                });
            selectedImage.observe(this, integer -> {
                if (Objects.equals(integer, image)) {
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
        imgBack = findViewById(R.id.img_Back);
        grid = findViewById(R.id.grid);
        title_set_image = findViewById(R.id.title_set_image);
        title_set_image.setText("Set avatar");
    }
}