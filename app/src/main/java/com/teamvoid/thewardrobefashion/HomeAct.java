package com.teamvoid.thewardrobefashion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.collection.ArraySet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeAct extends AppCompatActivity {

    LinearLayout L1,L2;
    TextView Heading,bigImgDetails;
    EditText Quantity;
    LinearLayout bigImgCard;
    ImageView bigImg;
    public static int liCount = 1;
    List<String> SelectedItem = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        L1 = findViewById(R.id.L1);
        L2 = findViewById(R.id.L2);
        bigImgCard = findViewById(R.id.bigImgCard);
        bigImgDetails = findViewById(R.id.bigImgDetail);
        bigImg = findViewById(R.id.bigImg);
        Quantity = findViewById(R.id.quantity);
        Heading = findViewById(R.id.Heading);

        LayoutInflater inflater = LayoutInflater.from(this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ClothesRef = database.getReference("Clothes");
        DatabaseReference UsersRef = database.getReference("Users");

        if (!MainActivity.searchedKeyword.equals("")){
            Heading.setText("Search Results For: "+MainActivity.searchedKeyword);
            L1.removeAllViews();
            L2.removeAllViews();
            ClothesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot categories: snapshot.getChildren()) {
                        for (DataSnapshot products: categories.getChildren()) {
                            List<String> ProductDetails = new ArrayList<String>();
                            for (DataSnapshot product: products.getChildren()) {
                                if (product.getValue() != null)
                                    ProductDetails.add(product.getValue().toString());
                            }
                            ProductDetails.add(products.getKey());
                            if (ProductDetails.get(0).toLowerCase().trim().contains(MainActivity.searchedKeyword.toLowerCase())){
                                if (liCount++ <= 1){
                                    View listView = inflater.inflate(R.layout.item, L1, false);
                                    L1.addView(AddProduct(listView,ProductDetails ));
                                }
                                else {
                                    View listView = inflater.inflate(R.layout.item, L2, false);
                                    L2.addView(AddProduct(listView, ProductDetails));
                                    liCount = 1;
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        else {
            Heading.setText(MainActivity.Category+" CLOTHING");
            ClothesRef.child(MainActivity.Category).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    L1.removeAllViews();
                    L2.removeAllViews();
                    liCount=1;
                    Iterable<DataSnapshot> items = snapshot.getChildren();
                    for (DataSnapshot itemValues: items) {
                        List<String> item = new ArrayList<String>();
                        for (DataSnapshot value : itemValues.getChildren()) {
                            if (value.getValue() != null)
                                item.add(value.getValue().toString());
                        }
                        item.add(itemValues.getKey());
                        if (liCount++ <= 1){
                            View listView = inflater.inflate(R.layout.item, L1, false);
                            L1.addView(AddProduct(listView, item));
                        }
                        else {
                            View listView = inflater.inflate(R.layout.item, L2, false);
                            L2.addView(AddProduct(listView, item));
                            liCount = 1;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        findViewById(R.id.addToCart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Quantity.getText().toString().isEmpty()){
                    Toast.makeText(HomeAct.this, "Please Enter Quantity", Toast.LENGTH_SHORT).show();
                }
                else {
                    MainActivity.loading.show();
                    SelectedItem.add(Quantity.getText().toString());
                    /*
                    0 Details
                    1 Price
                    2 Img
                    3 Key
                    4 Quantity
                     */
                    UsersRef.child(MainActivity.User.get(4)).child("CartItems").setValue(MainActivity.User.get(0)+SelectedItem.get(0)+"`"+SelectedItem.get(1)+"`"+SelectedItem.get(2)+"`"+SelectedItem.get(3)+"`"+SelectedItem.get(4)+";");
                    MainActivity.AppContext = "AddCart"+SelectedItem.get(3);
                    UsersRef.child(MainActivity.User.get(4)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(MainActivity.AppContext.equals("AddCart"+SelectedItem.get(3))){
                                MainActivity.AppContext = "...";
                                List<String> userDetails = new ArrayList<String>();
                                if (snapshot.getValue()!=null) {
                                    for (DataSnapshot value : snapshot.getChildren()) {
                                        if (value.getValue() != null)
                                            userDetails.add(value.getValue().toString());
                                    }
                                    MainActivity.User = userDetails;
                                    MainActivity.loading.dismiss();
                                    Toast.makeText(HomeAct.this, "Added Successfully", Toast.LENGTH_SHORT).show();
                                    Quantity.setText("");
                                    onBackPressed();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
        });

    }
    View AddProduct (View listView, List<String> item){
        ImageView img = listView.findViewById(R.id.image);
        TextView detail = listView.findViewById(R.id.Detail);
        Picasso.get().load(item.get(1)).placeholder(android.R.drawable.ic_menu_gallery).into(img);
        detail.setText(item.get(2)+"\n"+item.get(0));
        listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Picasso.get().load(item.get(1)).placeholder(android.R.drawable.ic_menu_gallery).into(bigImg);
                bigImgDetails.setText(item.get(2)+"\n"+item.get(0));
                SelectedItem = item;
                bigImgCard.setVisibility(View.VISIBLE);
            }
        });
        return listView;
    }

    @Override
    public void onBackPressed() {
        MainActivity.searchedKeyword = "";
        if (bigImgCard.getVisibility() == View.VISIBLE){
            bigImgCard.setVisibility(View.GONE);
            Picasso.get().load(android.R.drawable.ic_menu_gallery).into(bigImg);
            bigImgDetails.setText("Details");
        }
        else
            super.onBackPressed();
    }
}