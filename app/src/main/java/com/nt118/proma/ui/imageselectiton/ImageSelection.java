package com.nt118.proma.ui.imageselectiton;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.nt118.proma.R;

import java.util.ArrayList;
import java.util.List;

public class ImageSelection extends AppCompatActivity {
    private ImageView imgBack;
    private SelectionAdapter myAdapter;
    private GridView gridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_cover);

        InitUi();

        onClickBack();
        //nem anh vao grid
        putImageToGrid();
    }

    private void putImageToGrid() {
        List<Integer> image= new ArrayList<>();
        image.add(R.drawable.avatar);
        image.add(R.drawable.avatar1);
        image.add(R.drawable.avatar2);
        image.add(R.drawable.avatar3);

        myAdapter =new SelectionAdapter(this,image);
        gridView.setAdapter(myAdapter);

    }

    private void onClickBack() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void InitUi(){
        imgBack=findViewById(R.id.img_Back);
        gridView=findViewById(R.id.gv_cover);
    }
}