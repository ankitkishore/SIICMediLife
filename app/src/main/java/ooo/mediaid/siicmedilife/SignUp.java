package ooo.mediaid.siicmedilife;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SignUp extends AppCompatActivity {



    private static final int REQUEST_CAMERA = 3;
    private static final int SELECT_FILE = 2;

    EditText email_id;
    EditText store_name,password,phone_no,name,rePassword;
    LinearLayout store_location;
    ImageView userImageProfileView;
    Button b_Signup;


    DatabaseReference db;
    StorageReference mStorageRef;

    Spinner user_type;

    Uri imageHoldUri = null;

    private TextView textViewSignin;

    private ProgressDialog progressDialog;

    final int[] c = {0};
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //if getCurrentUser does not returns null
        if(firebaseAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }




        //initializing views

        userImageProfileView = findViewById(R.id.ProfilePic);
        user_type = findViewById(R.id.user_types);
        name = findViewById(R.id.name);
        store_name = findViewById(R.id.store_name);
        phone_no = findViewById(R.id.phone_no);
        email_id = findViewById(R.id.Email_id);
        password = findViewById(R.id.Password);
        rePassword = findViewById(R.id.RePassword);
        b_Signup = findViewById(R.id.b_Signup);
        textViewSignin = findViewById(R.id.textViewSignin);




        user_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String med = String.valueOf(parent.getItemAtPosition(position));
                if(med.equals("Medical Store"))
                {
                    store_name.setVisibility(View.VISIBLE);
                    store_location.setVisibility(View.VISIBLE);
                }
                else
                {
                    store_name.setVisibility(View.GONE);
                    store_location.setVisibility(View.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        email_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String e = editable.toString();
                email_id.getBackground().setColorFilter(getResources().getColor(R.color.red),
                        PorterDuff.Mode.SRC_ATOP);
                c[0] =1;
                if(e.contains("@") && e.contains("."))
                {email_id.getBackground().setColorFilter(getResources().getColor(R.color.green),
                        PorterDuff.Mode.SRC_ATOP);
                    c[0] =0;}

            }
        });

        rePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String pass = password.getText().toString();
                String repass = editable.toString();
                rePassword.getBackground().setColorFilter(getResources().getColor(R.color.red),
                        PorterDuff.Mode.SRC_ATOP);
                c[0] =1;
                if(repass.equals(pass))
                {
                    rePassword.getBackground().setColorFilter(getResources().getColor(R.color.green),
                            PorterDuff.Mode.SRC_ATOP);
                    c[0] =0;
                }
            }
        });

        mStorageRef = FirebaseStorage.getInstance().getReference();

        progressDialog = new ProgressDialog(this);

        //attaching listener to button
        userImageProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LOGIC FOR PROFILE PICTURE
                profilePicSelection();
            }
        });
        b_Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
        textViewSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUp.this, LoginActivity.class));
            }
        });




    }

    private void registerUser(){


        //getting email and password from edit texts
        final String email = email_id.getText().toString().trim();
        String pass =  password.getText().toString().trim();
        //checking if email and passwords are empty
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email",Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(pass)){
            Toast.makeText(this,"Please enter password",Toast.LENGTH_LONG).show();
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog



        //creating a new user
        if(c[0]==0) {
            progressDialog.setMessage("Registering Please Wait...");
            progressDialog.show();
            firebaseAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //checking if success
                            if (task.isSuccessful()) {
                                final User d = new User();
                                final FirebaseUser user = firebaseAuth.getCurrentUser();
                                db = FirebaseDatabase.getInstance().getReference("user");
                                StorageReference mChildStorage = mStorageRef.child("User_Profile").child(imageHoldUri.getLastPathSegment());
                                String profilePicUrl = imageHoldUri.getLastPathSegment();
                                mChildStorage.putFile(imageHoldUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        final Uri imageUrl = taskSnapshot.getUploadSessionUri();
                                        d.setImageurl(imageUrl.toString());
                                        d.setEmail(email);
                                        d.setPhone(phone_no.getText().toString());
                                        d.setName(name.getText().toString());
                                        if (!store_name.getText().toString().isEmpty()) {
                                            d.setStore_name(store_name.getText().toString());
                                        }
                                        db.child(firebaseAuth.getCurrentUser().getUid()).setValue(d, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                progressDialog.dismiss();
                                                finish();
                                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                            }
                                        });
                                    }
                                });


                            } else {
                                //display some message here
                                Toast.makeText(SignUp.this, "Registration Error", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
        }else{
            Toast.makeText(this,"PLs Check the Detailes Entered",Toast.LENGTH_LONG).show();
        }
    }



    private void profilePicSelection() {
        //DISPLAY DIALOG TO CHOOSE CAMERA OR GALLERY
        final CharSequence[] items = {/*"Take Photo", */"Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
        builder.setTitle("Add Photo!");
        //SET ITEMS AND THERE LISTENERS
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                /*if (items[item].equals("Take Photo")) {
                    cameraIntent();
                } else*/ if (items[item].equals("Choose from Library")) {
                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }



    /*private void cameraIntent() {
        //CHOOSE CAMERA
        Log.d("gola", "entered here");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setType("image*//*");
        startActivityForResult(intent, REQUEST_CAMERA);

    }*/
    private void galleryIntent() {
        //CHOOSE IMAGE FROM GALLERY
        Log.d("gola", "entered here");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //SAVE URI FROM GALLERY
        if(requestCode == SELECT_FILE && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(SignUp.this);
        }else /*if ( requestCode == REQUEST_CAMERA && resultCode == RESULT_OK ){
            //SAVE URI FROM CAMERA
            Uri imageUri = data.getData();

            Log.d("yo", " "+imageUri);
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(Sign_up.this);
        }*/
            //image crop library code
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    imageHoldUri = result.getUri();

                    userImageProfileView.setImageURI(imageHoldUri);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
    }




}
