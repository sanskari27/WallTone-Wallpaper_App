package com.excessivemedia.walltone.helpers;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Sets;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HighlightUtils {
    private static final String HIGHLIGHT = "Highlights";
    private static HighlightUtils obj;
    private final CollectionReference db;
    private final HashMap<String,File> highlightFiles;
    private final Context mContext;
    private boolean hasUpdated;

    private HighlightUtils(Context mContext){
        this.mContext = mContext.getApplicationContext();
        db = FirebaseFirestore.getInstance().collection(HIGHLIGHT);
        highlightFiles = new HashMap<>();
    }

    public static void init(Context Context){
        obj = new HighlightUtils(Context);
    }

    public static HighlightUtils getInstance() {
        return obj;
    }

    private void fetchLocal() {
        File file = new File(mContext.getFilesDir(),HIGHLIGHT);
        File[] files = file.listFiles();
        if(files==null)return;
        for (File f:files) {
            if(f.isDirectory()) continue;
            String name = Utils.stripExtension(f.getName());
            highlightFiles.put(name,f);
        }
    }
    public void fetchHighlights(){
        fetchLocal();
        Log.i(HIGHLIGHT,"Fetching Highlights");
        db.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Set<String> updatedList = new HashSet<>();
            for (DocumentSnapshot ds: queryDocumentSnapshots.getDocuments()) {
                updatedList.add(ds.getId());
                if(highlightFiles.containsKey(ds.getId())) continue;
                createFile(mContext,ds);
            }
            hasUpdated = true;
            updateLocalFiles(updatedList);
        });
    }
    private void updateLocalFiles(Set<String> updatedList) {
        Set<String> oldFiles = Sets.difference(highlightFiles.keySet(),updatedList);
        for (String s:oldFiles){
            File highlight = getHighlight(s);
            if(highlight!=null)highlight.delete();
            highlight= highlightFiles.get(s);
            if(highlight!=null)highlight.delete();
            highlightFiles.remove(s);
        }
        fetchLocal();
    }

    public void createFile(Context mContext, DocumentSnapshot ds) {
        String relativeLocation = mContext.getFilesDir().getAbsolutePath() + "/" + HIGHLIGHT;
        if(!new File(relativeLocation).exists()) new File(relativeLocation).mkdir();
        File file = new File(relativeLocation,ds.getId()+".txt");

        try {
            FileWriter fw = new FileWriter(file.getAbsolutePath());
            fw.append(ds.getId()).append("\n");
            fw.append(ds.getString(Consts.CATEGORY)).append("\n");
            fw.append(ds.getString(Consts.COLOR_CODE)).append("\n");
            fw.append(ds.getString(Consts.COLOR_NAME)).append("\n");
            List<String> list = (List<String>) ds.get("Tags");
            if(list!=null) {
                for (int y = 0; y < list.size(); y++) {
                    fw.append(list.get(y).toLowerCase()).append(" ");
                }
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getHighlights(){
        return  highlightFiles.keySet();
    }

    public File getHighlight(String name) {
        File file = new File(mContext.getFilesDir()+"/"+HIGHLIGHT+"/Images",name+".jpg");
        if(file.exists()) return file;
        else return null;
    }

    public File copyFile(String name, File source)  {
        String relativeLocation = mContext.getFilesDir() + "/" + HIGHLIGHT + "/Images";
        if(!new File(relativeLocation).exists()) new File(relativeLocation).mkdir();
        File dest = new File(relativeLocation,name+".jpg");
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }catch (IOException e){
            Log.e(HighlightUtils.class.getSimpleName(),e.getMessage());
        }
        return dest;
    }

    public boolean isUpdate() {
        return hasUpdated;
    }
}
