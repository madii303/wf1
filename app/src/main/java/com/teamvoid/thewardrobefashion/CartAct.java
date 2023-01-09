package com.teamvoid.thewardrobefashion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.style.UpdateLayout;
import android.view.LayoutInflater;
import android.view.View;
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
import java.util.zip.Inflater;

public class CartAct extends AppCompatActivity {
    LinearLayout list;
    LayoutInflater inflater;
    TextView TotalItems;
    int total = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        getSupportActionBar().hide();

        list = findViewById(R.id.list);
        inflater = LayoutInflater.from(this);
        TotalItems = findViewById(R.id.TotalItems);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference UsersRef = database.getReference("Users");

        UsersRef.child(MainActivity.User.get(4)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> userDetails = new ArrayList<String>();
                if (snapshot.getValue()!=null) {
                    for (DataSnapshot value : snapshot.getChildren()) {
                        if (value.getValue() != null)
                            userDetails.add(value.getValue().toString());
                    }
                        MainActivity.User = userDetails;
                }
                list.removeAllViews();
                UpdateList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        findViewById(R.id.clearBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsersRef.child(MainActivity.User.get(4)).child("CartItems").setValue("");
            }
        });

        findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (total>0 ){
                    MainActivity.loading.show();
                    String OrderNumber = ""+ new Date().getTime();
                    UsersRef.child(MainActivity.User.get(4)).child("Orders").setValue(MainActivity.User.get(2) +OrderNumber+","+total+";");
                    MainActivity.AppContext = "ConfirmOrder" + OrderNumber;
                    UsersRef.child(MainActivity.User.get(4)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (MainActivity.AppContext.equals("ConfirmOrder" + OrderNumber)) {
                                MainActivity.AppContext = "...";
                                List<String> userDetails = new ArrayList<String>();
                                if (snapshot.getValue() != null) {
                                    for (DataSnapshot value : snapshot.getChildren()) {
                                        if (value.getValue() != null)
                                            userDetails.add(value.getValue().toString());
                                    }
                                    MainActivity.User = userDetails;
                                    Toast.makeText(CartAct.this, "Order Confirmed\nOrder#"+OrderNumber, Toast.LENGTH_SHORT).show();
                                    UsersRef.child(MainActivity.User.get(4)).child("CartItems").setValue("");
                                    MainActivity.loading.dismiss();
                                    startActivity(new Intent(getApplicationContext(),OrdersAct.class));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else
                    Toast.makeText(CartAct.this, "No Item to Order", Toast.LENGTH_SHORT).show();

            }
        });

        /*
        Product Array
        0 Details
        1 Img
        2 Price
        3 Key
        4 Quantity
         */


    }

    private void UpdateList() {
        String str = MainActivity.User.get(0);
        String[] CartProducts = str.split(";");
        total = 0;
        for (int i = 0 ; i<CartProducts.length ; i++) {
            if (!CartProducts[i].trim().equals("")){
                View listView = inflater.inflate(R.layout.cart_item, list, false);
                ImageView img = listView.findViewById(R.id.img);
                TextView details = listView.findViewById(R.id.detail);
                TextView quantity = listView.findViewById(R.id.quantity);
                TextView actPrice = listView.findViewById(R.id.actPrice);
                TextView tPrice = listView.findViewById(R.id.tPrice);
                String[] Product = CartProducts[i].trim().split("`");
                Picasso.get().load(Product[1]).placeholder(android.R.drawable.ic_menu_gallery).into(img);
                details.setText(Product[0]);
                quantity.setText("Quantity: x"+Product[4]);
                actPrice.setText(Product[2]);
                int tp = Integer.parseInt(Product[2].trim().replace("Rs.","").trim());
                tp = tp * (Integer.parseInt(Product[4].trim()));
                tPrice.setText("Rs."+tp);
                total+=tp;
                list.addView(listView);
            }
        }
        TotalItems.setText("( Total: Rs."+total+" )");
    }
}