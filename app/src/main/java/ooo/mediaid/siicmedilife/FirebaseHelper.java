package ooo.mediaid.siicmedilife;


import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;


public class FirebaseHelper {
    DatabaseReference db;
    Boolean saved;
    ArrayList<String> med=new ArrayList<>();
    /*
 PASS DATABASE REFRENCE
  */
    public FirebaseHelper(DatabaseReference db) {
        this.db = db;
    }
    //WRITE IF NOT NULL
    public Boolean save(Meds med, Quantity q)
    {
        if(med==null && q==null)
        {
            saved=false;
        }else
        {
            try
            {
                String key = db.getDatabase().getReference("meds").push().getKey();
                med.setMed_id(key);
                db.child("meds").child(key).setValue(med);
                String user_id = FirebaseApp.getInstance().getUid();
                db.child("inventory").child(key).child(user_id).setValue(q);
                db.child("store_inventory").child(user_id).push().setValue(med.getMed_id());
                saved=true;
            }catch (DatabaseException e)
            {
                e.printStackTrace();
                saved=false;
            } catch (FirebaseApiNotAvailableException e) {
                e.printStackTrace();
            }
        }
        return saved;
    }
}