package com.teamvoid.thewardrobefashion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    AlertDialog.Builder alert;
    AlertDialog alertDialog;
    EditText search;
    public static String Category = "";
    public static String logInStatus = "login";
    public static ProgressDialog loading;
    public static String AppContext = "";
    public static List<String> User = null;
    public static String searchedKeyword = "";

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference UsersRef = database.getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        loading = new ProgressDialog(MainActivity.this);
        loading.setCancelable(false);
        loading.setMessage("Please Wait.");
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        search = findViewById(R.id.search);

        findViewById(R.id.user).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (logInStatus.equalsIgnoreCase("login") || logInStatus.equalsIgnoreCase("signup") )
                    userButton();
                else {
                    new android.app.AlertDialog.Builder(MainActivity.this)
                            .setTitle("Welcome "+User.get(1)+",")
                            .setMessage("You are logged in with "+User.get(4)+".com")
                            .setPositiveButton("Cancel", null)
                            .setNegativeButton("Orders", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(getApplicationContext(),OrdersAct.class));
                                }
                            })
                            .setNeutralButton("Logout", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    logInStatus = "login";
                                    User = null;
                                    Toast.makeText(getApplicationContext(), "User Logged Out", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setIcon(R.drawable.user)
                            .show();
                }

            }
        });


        findViewById(R.id.searchBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchedKeyword = search.getText().toString().trim();
                if (!searchedKeyword.equals(""))
                    GotoPage(new Intent(getApplicationContext(),HomeAct.class));
                else {
                    Toast.makeText(MainActivity.this, "Enter Search Word", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GotoPage(new Intent(getApplicationContext(),CartAct.class));
            }
        });


        findViewById(R.id.MensBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Category = "MEN";
                 GotoPage(new Intent(getApplicationContext(),HomeAct.class));
            }
        });
        findViewById(R.id.WomensBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Category = "WOMEN";
                GotoPage(new Intent(getApplicationContext(),HomeAct.class));
            }
        });
        findViewById(R.id.KidsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Category = "KIDS";
                GotoPage(new Intent(getApplicationContext(),HomeAct.class));
            }
        });

    }

    private void GotoPage(Intent i) {
        if (User != null){
            startActivity(i);
        }else
        {
            new android.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("No User Found")
                    .setMessage("Please Login your Account to Continue Shopping.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            userButton();
                        }
                    })
                    .setIcon(R.drawable.user)
                    .show();
        }
    }

    void userButton (){

        alert = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.user,null);
        EditText name = (EditText)mView.findViewById(R.id.Name);
        EditText username = (EditText)mView.findViewById(R.id.uName);
        EditText pass = (EditText)mView.findViewById(R.id.pass);
        TextView heading = (TextView)mView.findViewById(R.id.Heading);
        TextView option = (TextView)mView.findViewById(R.id.opt);
        Button goBtn = (Button)mView.findViewById(R.id.goBtn);
        alert.setView(mView);
        alertDialog = alert.create();

        if (logInStatus.equalsIgnoreCase("login")){
            name.setVisibility(View.GONE);
            username.setText("");
            pass.setText("");
            heading.setText("USER LOGIN");
            option.setText("Create New Account?");
            option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logInStatus = "signup";
                    alertDialog.dismiss();
                    userButton();
                }
            });
            goBtn.setText("GO");
            goBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loading.show();
                    if (validateEmail(username.getText().toString().trim())){
                        AppContext = "MainLogin"+username.getText().toString();
                        UsersRef.child(getUsername(username.getText().toString())).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (AppContext.equals("MainLogin"+username.getText().toString())) {
                                    AppContext = "...";
                                    List<String> userDetails = new ArrayList<String>();
                                    if (snapshot.getValue()!=null) {
                                        for (DataSnapshot value : snapshot.getChildren()) {
                                            if (value.getValue() != null)
                                                userDetails.add(value.getValue().toString());
                                        }
                                        if (pass.getText().toString().trim().equals(userDetails.get(3))) {
                                            User = userDetails;
                                            Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                            loading.dismiss();
                                            alertDialog.dismiss();
                                            logInStatus = "loggedin";
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Incorrect Password", Toast.LENGTH_SHORT).show();
                                            loading.dismiss();
                                        }
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "No User Found", Toast.LENGTH_SHORT).show();
                                        loading.dismiss();
                                    }
                                }
                                else loading.dismiss();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                loading.dismiss();
                            }
                        });
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Invalid Email Format\nabc@xyz.com", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }
            });
        }
        else if (logInStatus.equalsIgnoreCase("signup"))
        {
            name.setVisibility(View.VISIBLE);
            name.setText("");
            username.setText("");
            pass.setText("");
            heading.setText("USER SIGN UP");
            option.setText("Already Have Account?");
            option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logInStatus = "login";
                    alertDialog.dismiss();
                    userButton();
                }
            });
            goBtn.setText("Submit");
            goBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loading.show();

                    if (validateEmail(username.getText().toString())) {
                        Map<String, String> Data = new HashMap<String, String>();
                        Data.put("Name", getUsername(name.getText().toString().trim()));
                        Data.put("Username", getUsername(username.getText().toString().trim()));
                        Data.put("Pass", pass.getText().toString().trim());
                        Data.put("CartItems", "");
                        Data.put("Orders", "");
                        UsersRef.child(getUsername(username.getText().toString().trim())).setValue(Data);
                        AppContext = "MainSignUp"+username.getText().toString();
                        UsersRef.child(getUsername(username.getText().toString().trim())).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (AppContext.equals("MainSignUp"+username.getText().toString())) {
                                    AppContext = "...";
                                    User = new ArrayList<>();
                                    for (DataSnapshot detail: snapshot.getChildren()) {
                                        User.add(detail.getValue().toString().trim());
                                    }
                                    Toast.makeText(getApplicationContext(), "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                    loading.dismiss();
                                    alertDialog.dismiss();
                                    logInStatus = "loggedin";
                                    new android.app.AlertDialog.Builder(MainActivity.this)
                                            .setTitle("Welcome "+User.get(1)+",")
                                            .setMessage("You can use "+User.get(4)+".com email to login next time.")
                                            .setPositiveButton("Start Exploring", null)
                                            .setIcon(R.drawable.user)
                                            .show();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                loading.dismiss();
                            }
                        });


                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid Email Format\nabc@xyz.com", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                    loading.dismiss();
                }
            });
        }
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    static String getUsername (String email){
        String[] splitter = email.trim().split(Pattern.quote("."));
        return splitter[0];
    }

    boolean validateEmail (String email){
        if (email.trim().isEmpty())
            return false;
        else if (email.trim().contains("@") && email.trim().contains(".com"))
            return true;
        else return false;
    }



}