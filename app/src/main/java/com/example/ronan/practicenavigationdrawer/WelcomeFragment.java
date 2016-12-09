package com.example.ronan.practicenavigationdrawer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.animation;
import static android.R.attr.data;
import static android.R.attr.process;
import static com.example.ronan.practicenavigationdrawer.R.id.upload_image;
import static com.google.android.gms.fitness.data.zzs.Re;
import static com.google.android.gms.internal.zzax.getKey;


public class WelcomeFragment extends Fragment {

    TextView registered;
    TextView stolen;
    TextView systemStolen;
    TextView user;
    TextView reportedSigntings;
    CircleImageView profielPic;
    FloatingActionButton floatingEditProfile;
    String key_passed_fromList;
    String email = "";


    long countStolen;
    long countReg;
    long thisStolen = 0;

    ArrayList<String> registeredBikeKeys = new ArrayList<>();
    ArrayList<String> sightingBikeKeys = new ArrayList<>();
    ArrayList<BikeData> reportedSightingsList = new ArrayList<>();
    ArrayList<BikeData> registeredBikesList = new ArrayList<>();

    //Firebase variables
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private DatabaseReference stolenBikesDatabse;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference reportedStolen;
    private DatabaseReference readReportOfStolenQuery;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    String imageValue = "";


    ValueEventListener userDataListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {


            if (dataSnapshot.getValue(UserData.class) == null) {

                Log.v("Profile_fragment", "datasnap shot returned null in userDataListener");
                return;
            }

            user = dataSnapshot.getValue(UserData.class);
//            usernameET.setText(user.getUsername());
//            String email.setText(user.getEmail());
//            addressET.setText(user.getAddress());

            imageValue = user.getUser_image_In_Base64();


            if (!imageValue.equals("imageValue")) {
                getBitMapFromString(imageValue);
            }

        }

        UserData user = new UserData();

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //extract bitmap helper, this sets image view
    public void getBitMapFromString(String imageAsString) {
        if (imageAsString != null) {
            if (imageAsString.equals("No image") || imageAsString == null) {
                // bike_image.setImageResource(R.drawable.not_uploaded);
                Log.v("***", "No image Found");
            } else {
                byte[] decodedString = Base64.decode(imageAsString, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                profielPic.setImageBitmap(bitmap);
            }
        } else {
            Log.v("***", "Null paramater passed into getBitMapFromString");
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        final View loadingIndicator = rootView.findViewById(R.id.loading_indicator_edit);

        if (!imageValue.equals("")) {

        }


        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            email = mFirebaseUser.getEmail();
            email = email.split("@")[0];
        }


        Log.v("EMAIL", email);
        //seting up firebase DB refrences
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Bikes Registered By User").child(email);
        stolenBikesDatabse = FirebaseDatabase.getInstance().getReference().child("Stolen Bikes");
        readReportOfStolenQuery = FirebaseDatabase.getInstance().getReference().child("Viewing bikes Reported Stolen").child(email);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("User Profile Data");
        mDatabaseUsers.child(email).addValueEventListener(userDataListener);

        reportedStolen = FirebaseDatabase.getInstance().getReference().child("Reported Bikes");


        registered = (TextView) rootView.findViewById(R.id.bikesRegistered);
        stolen = (TextView) rootView.findViewById(R.id.personalStolen);
        systemStolen = (TextView) rootView.findViewById(R.id.totalStolen);
        reportedSigntings = (TextView) rootView.findViewById(R.id.reportedSigntings);
        user = (TextView) rootView.findViewById(R.id.userProfile);
        floatingEditProfile = (FloatingActionButton) rootView.findViewById(R.id.floatingConfirmEdit);
        profielPic = (CircleImageView) rootView.findViewById(R.id.profile_image);

        floatingEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //setFragment
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.fragment_container, new Profile_Fragment()).commit();

            }
        });

//        imageAnim.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FragmentManager fm = getFragmentManager();
//                fm.beginTransaction().replace(R.id.fragment_container, new ViewReportedSightingsFragment()).commit();
//            }
//        });


        //event listener for checking if bike is on stolen DB used to give correct user feedback
        ValueEventListener CountRegListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                countReg = dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // countReg++;
                    registeredBikeKeys.add(snapshot.getKey().toString());

                    BikeData bike = snapshot.getValue(BikeData.class);
                    registeredBikesList.add(bike);
                }
                registered.setText("Bikes registered to you: " + countReg);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        }; //end listener


        //event listener for checking if bike is on stolen DB used to give correct user feedback
        ValueEventListener reportedStolenListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                countReg = dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // countReg++;
                    sightingBikeKeys.add(snapshot.getKey().toString());

                    BikeData bike = snapshot.getValue(BikeData.class);

                    //get specic objects that were sighted
                    if (registeredBikeKeys.contains(snapshot.getKey().toString())) {
                        reportedSightingsList.add(bike);
                        Log.v("**rprint", Arrays.toString(reportedSightingsList.toArray()));
                        Log.v("**rprint make:", bike.getMake() + "Model: " + bike.getModel());

                        readReportOfStolenQuery.child(snapshot.getKey().toString()).setValue(bike);
                        reportedSigntings.setText("*Another User has reported a potental sighting your bikes, check mail");
                    }


                }


                reportedSightingsList.size();

                List<String> list3 = new ArrayList<>();


                for (String matches : registeredBikeKeys) {
                    if (sightingBikeKeys.contains(matches)) {
                        list3.add(matches);
                        Log.v("**size", "" + list3.size());
                    }
                }

                if (!list3.isEmpty()) {
                   // startAnim();
                }


                //   registered.setText("Bikes registered to you: " + countReg);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        }; //end listener


        //event listener for checking if bike is on stolen DB used to give correct user feedback
        ValueEventListener CountStolenListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loadingIndicator.setVisibility(View.GONE);
                countStolen = dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    BikeData bike = snapshot.getValue(BikeData.class);

                    //check field is not null
                    if (bike.getRegisteredBy() != null) {


                        if (bike.getRegisteredBy().equals(email)) {
                            thisStolen++;
                            Log.v("**reg", bike.getRegisteredBy());

                        } else {
                            Log.v("**reg", "no user");
                        }

                    }
                }

                stolen.setText("Bikes you've listed as stolen: " + thisStolen);

                systemStolen.setText("Total bikes stolen in system: " + countStolen);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        }; //end listener

        user.setText(email);

        mDatabase.addValueEventListener(CountRegListener);
        stolenBikesDatabse.addValueEventListener(CountStolenListener);
        reportedStolen.addValueEventListener(reportedStolenListener);


        return rootView;

    }



    //animation for notification of reported bike
//    public void startAnim() {
//        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
//        animation.setDuration(3500); // duration - half a second
//        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
//        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
//        animation.setRepeatMode(Animation.REVERSE);
//
//        imageAnim.setVisibility(View.VISIBLE);
//        imageAnim.startAnimation(animation);
//
//
//        // start the animation!
//        animation.start();
//    }
}




