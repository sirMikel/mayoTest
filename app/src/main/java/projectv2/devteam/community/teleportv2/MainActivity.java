package projectv2.devteam.community.teleportv2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import projectv2.devteam.community.teleportv2.Common.Common;
import projectv2.devteam.community.teleportv2.Model.User;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {


    Button btnSignIn, btnRegister;
    RelativeLayout rootLayout;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    TextView txt_forgot_pwd;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                        .setDefaultFontPath("fonts/Arkhip_font.ttf")
                                        .setFontAttrId(R.attr.fontPath)
                                        .build());
        setContentView(R.layout.activity_main);

        //init paperDB
        Paper.init(this);

        //Init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_driver_tbl_);

        //View
        btnRegister =(Button)findViewById(R.id.btnRegister);
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        txt_forgot_pwd = (TextView)findViewById(R.id.txt_forgot_pwd);


        //Event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://communitydevteam.formstack.com/forms/biker_registration_form"));
                startActivity(intent);

                /*showRegisterDialog();*/
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });
        txt_forgot_pwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showDialogForgotPwd();
                return false;
            }
        });

        //autologin--rememberme
        String user = Paper.book().read(Common.user_field);
        String pwd = Paper.book().read(Common.pwd_field);
        if (user != null && pwd != null)
        {
            if (!TextUtils.isEmpty(user)&&
                    !TextUtils.isEmpty(pwd))
            {
                autoLogin(user,pwd);
            }
        }
    }

    private void autoLogin(String user, String pwd) {
        final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
        waitingDialog.show();

        //Login
        auth.signInWithEmailAndPassword(user, pwd)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        waitingDialog.dismiss();

                        FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //after assigned value
                                        Common.currentUser = dataSnapshot.getValue(User.class);
                                        //start new activity
                                        startActivity(new Intent(MainActivity.this, DriverHome.class));
                                        waitingDialog.dismiss();
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                waitingDialog.dismiss();
                Snackbar.make(rootLayout, "Log-In Failed" + e.getMessage(), Snackbar.LENGTH_LONG)
                        .show();
                Log.e("ERROR",e.getMessage());

                //activate signin button
                btnSignIn.setEnabled(true);
            }
        });
    }

    private void showDialogForgotPwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("FORGOT PASSWORD");
        alertDialog.setMessage("Please enter valid e-mail address");

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View forgot_pwd_layout = inflater.inflate(R.layout.layout_forgot_pwd,null);

        final MaterialEditText edtEmail = (MaterialEditText)forgot_pwd_layout.findViewById(R.id.edtEmail);
        alertDialog.setView(forgot_pwd_layout);

        //set button
        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int i) {
                final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
                waitingDialog.show();

                auth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();
                                waitingDialog.dismiss();

                                Snackbar.make(rootLayout,"Reset password link has been sent", Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(rootLayout,""+e.getMessage(), Snackbar.LENGTH_LONG)
                                .show();
                    }
                });
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }

    private void showLoginDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN-IN");
        dialog.setMessage("Please use email to sign-in");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login,null);

        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);


        dialog.setView(login_layout);




        //Set Button
        dialog.setPositiveButton("SIGN-IN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        //Set disable button Sign in If Processing
                        btnSignIn.setEnabled(false);
                        //Check Validation
                        if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                            Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_LONG)
                                    .show();
                            return;
                        }
                        if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                            Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_LONG)
                                    .show();
                            return;
                        }
                        if (edtPassword.getText().toString().length() < 6) {
                            Snackbar.make(rootLayout, "Password too short!", Snackbar.LENGTH_LONG)
                                    .show();
                            return;
                        }
                        final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
                        waitingDialog.show();

                        //Login
                        auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        waitingDialog.dismiss();

                                        FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl_)
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            //save value
                                                            Paper.book().write(Common.user_field, edtEmail.getText().toString());
                                                            Paper.book().write(Common.pwd_field, edtPassword.getText().toString());
                                                            //after assigned value
                                                            Common.currentUser = dataSnapshot.getValue(User.class);
                                                            //start new activity
                                                            startActivity(new Intent(MainActivity.this, DriverHome.class));
                                                            finish();
                                                        }else {
                                                            removePaper();
                                                            FirebaseAuth.getInstance().signOut();
                                                            Toast madaya = Toast.makeText(MainActivity.this, "Sign In Failed!!\nPlease Check if you are using\nthe correct App", Toast.LENGTH_LONG);
                                                            madaya.setGravity(Gravity.CENTER,0,0);
                                                            madaya.show();
                                                            btnSignIn.setEnabled(true);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Snackbar.make(rootLayout, "Log-In Failed" + e.getMessage(), Snackbar.LENGTH_LONG)
                                        .show();

                                //activate signin button
                                btnSignIn.setEnabled(true);
                            }
                        });


                    }
                });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });



        dialog.show();
    }

    private void removePaper() {
        Paper.init(this);
        Paper.book().destroy();
    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_registration,null);

        final MaterialEditText edtEmail = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName = register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = register_layout.findViewById(R.id.edtPhone);
        final MaterialEditText edtMC = register_layout.findViewById(R.id.edtMC);
        final MaterialEditText edtPlateNum = register_layout.findViewById(R.id.edtPlateNum);
        final MaterialEditText edtTeamName = register_layout.findViewById(R.id.edtTeamName);
        final MaterialEditText edtRefCode = register_layout.findViewById(R.id.edtRefCode);

        dialog.setView(register_layout);




        //Set Button
        dialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                //Check Validation
                if (TextUtils.isEmpty(edtEmail.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please enter email address",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (TextUtils.isEmpty(edtPassword.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please enter email password",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (edtPassword.getText().toString().length()<6)
                {
                    Snackbar.make(rootLayout,"Password too short!",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (TextUtils.isEmpty(edtName.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please enter name",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (TextUtils.isEmpty(edtPhone.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please enter Phone Number",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (TextUtils.isEmpty(edtMC.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please Motorcycle Info",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (TextUtils.isEmpty(edtPlateNum.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please enter Plate Number",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (TextUtils.isEmpty(edtTeamName.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please enter Team Name",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (TextUtils.isEmpty(edtRefCode.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please enter ReferralCode",Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                //Register New User
                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //Save user to db
                                User user = new User();
                                user.setName(edtName.getText().toString());
                                user.setPhone(edtPhone.getText().toString());
                                user.setMotorcycle(edtMC.getText().toString());
                                user.setPlateNumber(edtPlateNum.getText().toString());
                                user.setTeamName(edtTeamName.getText().toString());
                                user.setEmail(edtEmail.getText().toString());
                                user.setPassword(edtPassword.getText().toString());
                                user.setAvatarUrl("");
                                user.setRefCode(edtRefCode.getText().toString());
                                user.setRates("0");

                                //Use email to Key
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout,"Registered Successfully",Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootLayout,"Registration Failed"+e.getMessage(),Snackbar.LENGTH_SHORT)
                                                .show();
                                    }
                                });



                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT)
                                .show();
                    }
                });
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();



    }
}
