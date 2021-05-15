package com.excessivemedia.walltone.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Set;


public class LikeManager {
    private final SharedPreferences likePref ;

    public LikeManager(Context context){
        likePref = context.getSharedPreferences(Consts.LIKE,0);
    }

    public boolean isliked(String id){
        return likePref.getBoolean(id,false);
    }
    public boolean toggleLiked(String id){
        boolean b = likePref.getBoolean(id, false);
        b=!b;
        likePref.edit().putBoolean(id,b).apply();
        FirebaseDatabase.getInstance().getReference(Consts.USERS).child(Consts.LIKE).child(id).setValue(b);
        return b;
    }
    public void setLiked(String id){
        likePref.edit().putBoolean(id,true).apply();
    }

    public void update() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null || user.isAnonymous()) return;
        FirebaseDatabase.getInstance().getReference(Consts.USERS).child(user.getUid())
                .child(Consts.LIKE).get().addOnSuccessListener(dataSnapshot -> {
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        setLiked(ds.getKey());
                    }
        });
    }
    public ArrayList<String> getLikedImagesId(){
        Set<String> strings = likePref.getAll().keySet();
        for (String s: strings) {

            if(!likePref.getBoolean(s,false)){
                likePref.edit().remove(s).apply();
                strings.remove(s);
            }
        }
        return new ArrayList<>(strings);
    }

}
