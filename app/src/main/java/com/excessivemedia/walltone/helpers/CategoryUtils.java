package com.excessivemedia.walltone.helpers;

import android.content.Context;
import android.util.Log;

import com.excessivemedia.walltone.widgets.Category.CategoryClass;
import com.google.common.collect.Sets;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryUtils {
    private static final String CATEGORY = "Category";
    private static CategoryUtils obj;

    private final CollectionReference db;
    private final HashMap<String, File> categoryFiles;
    private final Context mContext;
    private boolean hasUpdated;


    private CategoryUtils(Context mContext){
        this.mContext = mContext.getApplicationContext();
        db = FirebaseFirestore.getInstance().collection(CATEGORY);
        categoryFiles = new HashMap<>();
    }

    public static void init(Context Context){
        obj = new CategoryUtils(Context);
    }

    public static CategoryUtils getInstance() {
        return obj;
    }

    private void fetchLocal() {
        File file = new File(mContext.getFilesDir(),CATEGORY);
        File[] files = file.listFiles();
        if(files==null)return;
        for (File f:files) {
            if(f.isDirectory()) continue;
            String name = Utils.stripExtension(f.getName());
            categoryFiles.put(name,f);
        }
    }
    public void fetchCategory(){
        fetchLocal();
        Log.i(CATEGORY,"Fetching Categories");
        db.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Set<String> updatedList = new HashSet<>();
            for (DocumentSnapshot ds: queryDocumentSnapshots.getDocuments()) {
                updatedList.add(ds.getString(Consts.NAME));
                if(categoryFiles.containsKey(ds.getString( Consts.NAME ) ) ) continue;
                createFile(mContext,ds);
            }
            updateLocalFiles(updatedList);
            hasUpdated =true;
        });
    }


    private void updateLocalFiles(Set<String> updatedList) {
        Set<String> oldFiles = Sets.difference(categoryFiles.keySet(),updatedList);
        for (String s:oldFiles){
            File highlight = getCategory(s);
            if(highlight!=null)highlight.delete();
            highlight= categoryFiles.get(s);
            if(highlight!=null)highlight.delete();
            categoryFiles.remove(s);
        }
        fetchLocal();
    }
    public void createFile(Context mContext, DocumentSnapshot ds) {
        String relativeLocation = mContext.getFilesDir().getAbsolutePath() + "/" + CATEGORY;
        if(!new File(relativeLocation).exists()) new File(relativeLocation).mkdir();
        File file = new File(relativeLocation,ds.getString(Consts.NAME)+".txt");

        try {
            FileWriter fw = new FileWriter(file.getAbsolutePath());
            fw.append(ds.getId()).append("\n");
            fw.append(ds.getString(Consts.CATEGORY)).append("\n");
            fw.append(ds.getString(Consts.COLOR_CODE)).append("\n");
            fw.append(ds.getString(Consts.COLOR_NAME)).append("\n");
            List<String> list = (List<String>) ds.get(Consts.TAGS);
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

    public File getCategory(String name) {
        File file = new File(mContext.getFilesDir()+"/"+CATEGORY+"/Images",name+".jpg");
        if(file.exists()) return file;
        else return null;
    }

    public ArrayList<File> getCategoryFiles() {
        return new ArrayList<>(categoryFiles.values());
    }

    public File copyFile(String name, File source)  {
        String relativeLocation = mContext.getFilesDir() + "/" + CATEGORY + "/Images";
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

    public CategoryClass parseFile(File file) {
        if(!file.exists()) return null;
        CategoryClass category = new CategoryClass();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.readLine(); // this line is skiped because 1 and 2nd both line contains categoryName
            category.setCategoryName(br.readLine());
            category.setFileName(Utils.stripExtension(file.getName()));
            category.setColorCode(br.readLine());
            category.setColorName(br.readLine());
            String[] line = br.readLine().split(" ");
            ArrayList<String> tagList = new ArrayList<>();
            if(line.length!=0){
                tagList.addAll(Arrays.asList(line));
            }
            category.setTagsList(tagList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return category;
    }

    public boolean isUpdate() {
        return hasUpdated;
    }
}
