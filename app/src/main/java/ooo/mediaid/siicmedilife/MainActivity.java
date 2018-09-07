package ooo.mediaid.siicmedilife;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ooo.mediaid.siicmedilife.Fragment.Med_List;
import ooo.mediaid.siicmedilife.Fragment.Med_billing;
import ooo.mediaid.siicmedilife.Fragment.My_meds;
import ooo.mediaid.siicmedilife.Fragment.Search_med;
import ooo.mediaid.siicmedilife.Fragment.Store_Med_Billing;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    FirebaseAuth mAuth;
    DatabaseReference db;
    private GoogleSignInClient mGoogleSignInClient;

    LocationManager locationManager;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    FirebaseHelper helper;
    ListView lv;
    ConstraintLayout c;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();


        Log.i("yo6","qwe1");
        final FloatingActionButton fab =  findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDialog();
            }
        });
        c= findViewById(R.id.c);

        locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        getLocation();



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        final Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();

        db = FirebaseDatabase.getInstance().getReference();
        helper = new FirebaseHelper(db);

        if(mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));

        }



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(MainActivity.this);
        final View headerView = navigationView.getHeaderView(0);
        final ImageView imageView = headerView.findViewById(R.id.imageView);
        final TextView name =headerView.findViewById(R.id.name);
        final TextView email_id = headerView.findViewById(R.id.email_id);

        db.child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("yo6",FirebaseAuth.getInstance().getCurrentUser().getUid());
                User u = dataSnapshot.getValue(User.class);
                if(u!=null) {
                   /* if(u.getEmail() != null){}else
                    {
                        Log.i("y06", "Ankit");
                        startActivity(new Intent(MainActivity.this,GoogleSignUp.class));
                    }*/
                    if (u.getStore_name() != null) {
                        Log.i("ankuryo","1");
                        tabLayout = findViewById(R.id.tabLayout);
                        tabLayout.addTab(tabLayout.newTab().setText("Med Billing"));
                        tabLayout.addTab(tabLayout.newTab().setText("My Meds"));
                        //Initializing viewPager
                        viewPager = findViewById(R.id.containers);
                        //Creating our pager adapter
                        Pager2 adapter = new Pager2(getSupportFragmentManager(), tabLayout.getTabCount());
                        //Adding adapter to pager
                        viewPager.setAdapter(adapter);
                        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
                        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                        navigationView.getMenu().clear();
                        navigationView.inflateMenu(R.menu.store_menu);
                        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                            @Override
                            public void onTabSelected(TabLayout.Tab tab) {
                                if(tab.getText().equals("Med Billing"))
                                    fab.hide();
                                else
                                    fab.show();
                            }
                            @Override
                            public void onTabUnselected(TabLayout.Tab tab) {
                            }
                            @Override
                            public void onTabReselected(TabLayout.Tab tab) {
                            }
                        });
                        progressDialog.hide();
                    } else {
                        tabLayout = findViewById(R.id.tabLayout);

                        tabLayout.addTab(tabLayout.newTab().setText("Meds List"));
                        tabLayout.addTab(tabLayout.newTab().setText("Search Meds"));
                        tabLayout.addTab(tabLayout.newTab().setText("Meds Billing"));
                        fab.setVisibility(View.GONE);
                        //Initializing viewPager
                        viewPager = findViewById(R.id.containers);
                        //Creating our pager adapter
                        Pager adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount());
                        //Adding adapter to pager
                        viewPager.setAdapter(adapter);
                        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
                        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                        navigationView.getMenu().clear();
                        navigationView.inflateMenu(R.menu.user_menu);
                        fab.hide();
                        progressDialog.hide();
                    }
                    name.setText(u.getName().toString());
                    email_id.setText(u.getEmail().toString());
                    Glide.with(MainActivity.this).load(u.getImageurl()).into(imageView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.i("yo6","qwe");

            }
        });

    }

    void getLocation() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 255);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null){
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                Log.i("yo"," "+ latti);
                Log.i("yo"," "+ longi);
            } else {

                Log.i("yo","Unable to find correct location.");
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 255:
                getLocation();
                break;
        }
    }





    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.Post) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }else if(id == R.id.sign_out){
            sign_out();
        }else if(id == R.id.profile){
            // startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sign_out() {
        // Firebase sign out
        mAuth.signOut();
        // Google sign out
        mGoogleSignInClient.signOut();
        finish();
        startActivity(new Intent(getBaseContext(), LoginActivity.class));
    }

    private void displayDialog()
    {
        final Dialog d= new Dialog(MainActivity.this);
        d.setTitle("Save Data in firebase");
        d.setContentView(R.layout.customdialog_layout);
        final EditText nameeditText= d.findViewById(R.id.name);
        final EditText urleditText= d.findViewById(R.id.desc);
        final EditText price = d.findViewById(R.id.price);
        final EditText quantity = d.findViewById(R.id.quantity);
        Button btnsave= d.findViewById(R.id.b1);
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { String word = nameeditText.getText().toString();
                String meaning = urleditText.getText().toString();
                Meds s = new Meds();
                s.setName(word);
                s.setDescrition(meaning);
                Quantity q = new Quantity();
                q.setPrice(price.getText().toString());
                q.setQuantity(quantity.getText().toString());
                //SIMPLE VALIDATION
                if (word!= null && word.length() > 0 && meaning!= null && meaning.length() > 0) {
                    //THEN SAVE
                    if (helper.save(s,q)) {
                        //IF SAVED CLEAR EDITXT
                        nameeditText.setText("");
                        urleditText.setText("");
                        price.setText("");
                        quantity.setText("");
                    } else {
                        Toast.makeText(MainActivity.this, "Name Must Not Be Empty", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        d.show();
    }


    public class Pager extends FragmentStatePagerAdapter {

        //integer to count number of tabs
        int tabCount;

        //Constructor to the class
        public Pager(FragmentManager fm, int tabCount) {
            super(fm);
            //Initializing tab count
            this.tabCount= tabCount;
        }

        //Overriding method getItem
        @Override
        public Fragment getItem(int position) {
            //Returning the current tabs
            switch (position) {
                case 0:
                    Med_List med_list = new Med_List();
                    return med_list;
                case 1:
                    Search_med search_med = new Search_med();
                    return search_med;
                case 2:
                    Med_billing med_billing= new Med_billing();
                    return med_billing;
                default:
                    return null;
            }
        }


        @Override
        public int getCount() {
            return tabCount;
        }
    }
    public class Pager2 extends FragmentStatePagerAdapter {

        //integer to count number of tabs
        int tabCount;

        //Constructor to the class
        public Pager2(FragmentManager fm, int tabCount) {
            super(fm);
            //Initializing tab count
            this.tabCount= tabCount;
        }

        //Overriding method getItem
        @Override
        public Fragment getItem(int position) {
            //Returning the current tabs
            switch (position) {
                case 0:
                    Store_Med_Billing med_list = new Store_Med_Billing();
                    return med_list;
                case 1:
                    My_meds search_med = new My_meds();
                    return search_med;
                default:
                    return null;
            }
        }


        @Override
        public int getCount() {
            return tabCount;
        }
    }


    @SuppressLint("NewApi")
    private Bitmap blurRenderScript(Bitmap smallBitmap, int radius) {

        try {
            smallBitmap = RGB565toARGB888(smallBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Bitmap bitmap = Bitmap.createBitmap(
                smallBitmap.getWidth(), smallBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(MainActivity.this);

        Allocation blurInput = Allocation.createFromBitmap(renderScript, smallBitmap);
        Allocation blurOutput = Allocation.createFromBitmap(renderScript, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript,
                Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(radius); // radius must be 0 < r <= 25
        blur.forEach(blurOutput);

        blurOutput.copyTo(bitmap);
        renderScript.destroy();

        return bitmap;

    }

    private Bitmap RGB565toARGB888(Bitmap img) throws Exception {
        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];

        //Get JPEG pixels.  Each int is the color values for one pixel.
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

        //Create a Bitmap of the appropriate format.
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        //Set RGB pixels.
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cart:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));

        }
    }
}
