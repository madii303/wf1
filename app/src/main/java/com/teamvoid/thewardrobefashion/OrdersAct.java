package com.teamvoid.thewardrobefashion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class OrdersAct extends AppCompatActivity {


    LinearLayout list;
    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        getSupportActionBar().hide();

        list = findViewById(R.id.list);
        inflater = LayoutInflater.from(this);

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


        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsersRef.child(MainActivity.User.get(4)).child("Orders").setValue("");
            }
        });

    }

    private void UpdateList() {
        String str = MainActivity.User.get(2);
        String[] orders = str.split(";");
        for (int i = 0 ; i<orders.length ; i++) {
            if (!orders[i].trim().equals("")){
                View listView = inflater.inflate(R.layout.order_item, list, false);
                TextView orderNumber = listView.findViewById(R.id.orderNumber);
                TextView amount = listView.findViewById(R.id.amount);
                String[] order = orders[i].trim().split(",");
                orderNumber.setText("Order Number: "+order[0]);
                amount.setText("Amount: Rs."+order[1]);
                list.addView(listView);
            }
        }
    }
}