package com.excessivemedia.walltone.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.helpers.Utils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {
    private File[] files;
    private File[] details;
    private int i;
    private ImageView imageView;
    private RadioGroup radioGroup;
    private Spinner spinner;
    private String[] categories;
    private ArrayList<String> tagList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        i= 0;
        getFiles();
        imageView = findViewById(R.id.imageView);
        radioGroup = findViewById(R.id.radioGrp);
        findViewById(R.id.openFolder).setOnClickListener(v->{
                openFolder();
        });
        findViewById(R.id.openLast).setOnClickListener(v->{
                updateDetails();
            String name = details[details.length - 1].getName();
            name = name.substring(0,name.indexOf("_"));
            i=Integer.parseInt(name);
                loadImage();
        });
        findViewById(R.id.next).setOnClickListener(v->{
            i++;
            if(i>=files.length)i=files.length-1;
            loadImage();
        });
        findViewById(R.id.prev).setOnClickListener(v->{
            i--;
            if(i<0)i=0;
            loadImage();
        });
        findViewById(R.id.saveData).setOnClickListener(v->{
            saveData();
        });
        loadImage();
        findViewById(R.id.editTags).setOnClickListener(v->{
            editTags();
        });
        spinner=(Spinner)findViewById(R.id.categoriesSpinner);

       categories=getResources().getStringArray(R.array.categories_list );

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, categories);

        spinner.setAdapter(adapter);
        findViewById(R.id.upload).setOnClickListener(v->{
            upload();
        });
    }

    private void upload()  {
        updateDetails();
        int i=0;
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading Files");
        pd.setCancelable(false);
        pd.show();
        File f = details[i];
        pd.setMessage("Uploading Files "+f.getName());
        String a= f.getName().substring(0,f.getName().indexOf('_'));
        File file = getFile(a,files);
        if(file==null){
            upload();
            return;
        }
        Bitmap compressBitmap = null;
        try {
            compressBitmap = SiliCompressor.with(this).getCompressBitmap(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            upload();
            return;
        }
        if(compressBitmap==null){
            upload();
            return;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        compressBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
        final String imageName = getAlphaNumericString();
        StorageReference reference = FirebaseStorage.getInstance().getReference("Walls/Thumbnail/"+imageName+".jpg");
        reference.putBytes(out.toByteArray()).addOnSuccessListener(taskSnapshot -> {
            StorageReference reference1 = FirebaseStorage.getInstance().getReference("Walls/"+imageName+".jpg");

            Bitmap bitmap= BitmapFactory.decodeFile(file.getAbsolutePath());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            byte[] byteArray = stream.toByteArray();

            bitmap.recycle();
            reference1.putBytes(stream.toByteArray()).addOnSuccessListener(taskSnapshot1 -> {
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String[] tags = br.readLine().split(" ");
                    String[] colors = br.readLine().split(" ");
                    String colorName =colors[0];
                    String colorCode =colors[1];
                    String category = br.readLine();
                    Map<String, Object> map = new HashMap<>();
                    List<String> tag = Arrays.asList(tags);
                    tag.add(category);
                    map.put("Tags",tag);
                    map.put("colorName",colorName);
                    map.put("colorCode",colorCode);
                    map.put("category",category);
                    map.put("name",imageName);
                    FirebaseFirestore.getInstance()
                            .collection("Walls")
                            .document(imageName).set(map).addOnSuccessListener(aVoid -> {
                        f.delete();
                        file.delete();
                        pd.dismiss();
                        upload();
                    }).addOnFailureListener(e -> {
                        reference.delete();
                        reference1.delete();
                        Log.wtf("UploadActivity",e.getMessage());
                        upload();
                    });
                }catch (IOException e){
                    reference.delete();
                    reference1.delete();
                    Log.wtf("UploadActivity",e.getMessage());
                    upload();
                }
            });
        });
    }

    private String getAlphaNumericString() {
        int n=30;

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    private File getFile(String a, File[] files) {
        for (File f : files){
            if(stripExtension(f.getName()).equalsIgnoreCase(a)) return f;
        }
        return null;
    }

    private void editTags() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.upload_activity_dialog);
        RecyclerView recyclerView = dialog.findViewById(R.id.addTagRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        DialogAdapter adapter = new DialogAdapter(tagList);
        recyclerView.setAdapter(adapter);
        EditText text = dialog.findViewById(R.id.editTagsET);

        Button addTag = dialog.findViewById(R.id.addTag);
        addTag.setOnClickListener(v -> {
            if(!text.getText().toString().isEmpty()){
                tagList.add(text.getText().toString());
                adapter.notifyDataSetChanged();
            }
            text.setText("");
        });
        dialog.findViewById(R.id.saveTags).setOnClickListener(v->{
            dialog.dismiss();
            tagList = adapter.getList();
        });

        dialog.show();

    }

    private void loadImage() {
        if(files == null )return;
        if(i<0 || i>=files.length){
            return;
        }
        else if(!files[i].exists()) return;

        Bitmap bmp = BitmapFactory.decodeFile(files[i].getAbsolutePath());
        Picasso.get().load(files[i]).into(imageView);
        radioGroup.setBackgroundColor(Utils.getDominantColor(bmp));
        radioGroup.clearCheck();
        tagList = new ArrayList<>();
        File file = new File(getExternalFilesDir("details").getPath(),stripExtension(files[i].getName())+"_Details.txt");
        if(file.exists()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String[] line = br.readLine().split(" ");
                if(line.length!=0){
                    tagList.addAll(Arrays.asList(line));
                    line = br.readLine().split(" ");
                    ((RadioButton)radioGroup.findViewWithTag(line[1])).setChecked(true);
                    line = br.readLine().split(" ");
                    spinner.setSelection(indexOf(line[0],categories));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int indexOf(String s, String[] categories) {
        for(int i=0;i<categories.length;i++){
            if(categories[i].equalsIgnoreCase(s))return i;
        }
        return 0;
    }

    private void saveData() {
        if(tagList==null || tagList.isEmpty()){
            Toast.makeText(this,"TAgs Missing",Toast.LENGTH_SHORT).show();
            return;
        }
        int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        if(checkedRadioButtonId == -1){
            Toast.makeText(this,"Color Selection Missing",Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rb = findViewById(checkedRadioButtonId);
        String color = (String) rb.getTag();
        String colorName = rb.getText().toString();
        File file = new File(getExternalFilesDir("details").getPath(),stripExtension(files[i].getName())+"_Details.txt");

        try {
            FileWriter fw = new FileWriter(file.getAbsolutePath());
            for (int y=0;y<tagList.size();y++){
                fw.append(tagList.get(y).toLowerCase()).append(" ");
            }
            fw.append("\n").append(colorName).append(" ").append(color);
            fw.append("\n").append(spinner.getSelectedItem().toString());
            fw.close();
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        radioGroup.clearCheck();
        tagList = null;
        updateDetails();

        i++;
        if(i>=files.length)i=files.length-1;
        loadImage();
    }
    static String stripExtension (String str) {
        // Handle null case specially.

        if (str == null) return null;

        // Get position of last '.'.

        int pos = str.lastIndexOf(".");

        // If there wasn't any '.' just return the string as is.

        if (pos == -1) return str;

        // Otherwise return the string, up to the dot.

        return str.substring(0, pos);
    }
    private void getFiles(){
        String location = getExternalFilesDir("uploads").getPath();
        File file = new File(location);
        files = file.listFiles();
        if(files == null) return;
        Arrays.sort(files, (o1, o2) -> {
            if(o1.getName().length()!=o2.getName().length()){
                return Integer.compare(o1.getName().length(),o2.getName().length());
            }
            return o1.getName().compareTo(o2.getName());
        });
        updateDetails();

    }

    private void updateDetails() {

        File file = new File(getExternalFilesDir("details").getPath());
        details = file.listFiles();
        if(details == null) return;
        Arrays.sort(details,(o1, o2) -> {
            if(o1.getName().length()!=o2.getName().length()){
                return Integer.compare(o1.getName().length(),o2.getName().length());
            }
            return o1.getName().compareTo(o2.getName());
        });
    }

    private void openFolder() {
        String location = getExternalFilesDir("uploads").getPath();
        File file = new File(location);
        if(!file.exists()){
            file.mkdir();
        }
        Toast.makeText(this,"Store your all images to : \n" +
                "Internal Strorage -> Android -> data -> com.excessivemedia.walltone -> files -> uploads",Toast.LENGTH_LONG).show();

    }
}

class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.DialogVH>{
    private final ArrayList<String> list;

    DialogAdapter(ArrayList<String> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public DialogVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DialogVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_activity_card,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull DialogVH holder, int position) {
        holder.cb.setTag(position);
        holder.cb.setChecked(true);
        holder.cb.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class DialogVH extends RecyclerView.ViewHolder{
        private CheckBox cb;
        public DialogVH(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.checkBox);
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(!isChecked){
                    list.remove((int)cb.getTag());
                    notifyDataSetChanged();
                }
            });
        }
    }

    public ArrayList<String> getList() {
        return list;
    }

}