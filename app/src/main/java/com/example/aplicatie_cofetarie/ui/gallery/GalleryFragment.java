package com.example.aplicatie_cofetarie.ui.gallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.aplicatie_cofetarie.R;
import com.example.aplicatie_cofetarie.databinding.FragmentGalleryBinding;
import com.example.aplicatie_cofetarie.db.AppDatabase;
import com.example.aplicatie_cofetarie.db.GalleryImage;
import com.example.aplicatie_cofetarie.db.GalleryImageDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GalleryFragment extends Fragment {

    private static final int REQUEST_CODE_GALLERY = 1;
    private Button buttonAddPhoto;
    private Button buttonDeletePhoto;
    private FragmentGalleryBinding binding;
    private RecyclerView list;
    private Uri photoUri;
    private GalleryImageDao galleryImageDao;
    private ListAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AppDatabase db = Room.databaseBuilder(
                requireContext().getApplicationContext(),
                AppDatabase.class,
                "app"
        ).build();
        galleryImageDao = db.galleryImageDao();

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        list = root.findViewById(R.id.list);
        buttonAddPhoto = root.findViewById(R.id.buttonAddPhoto);
        buttonDeletePhoto = root.findViewById(R.id.buttonDeletePhoto);

        buttonAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        buttonDeletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectedPhotos();
            }
        });


        adapter = new ListAdapter(inflater);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        loadImages();

        return root;
    }

    private void loadImages() {
        (new Thread() {
            @Override
            public void run() {
                List<GalleryImage> images = galleryImageDao.getAll();
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setImages(images);
                    }
                });
            }
        }).start();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedMediaUri = data.getData();
            saveImage(selectedMediaUri);
        }
    }

    private void daoInsert(GalleryImage img) {
        (new Thread() {
            @Override
            public void run() {
                galleryImageDao.insertAll(img);
                loadImages();
            }
        }).start();
    }

    private void saveImage(Uri imageUri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageUri));
            if (bitmap != null) {
                File imageFile = createImageFile();
                OutputStream outputStream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

                // save to db
                GalleryImage img = new GalleryImage();
                img.path = imageFile.getAbsolutePath();

                daoInsert(img);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        photoUri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".fileprovider", imageFile);
        return imageFile;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static class ListAdapter extends RecyclerView.Adapter<VH> {
        private LayoutInflater inflater;
        private List<GalleryImage> images = new ArrayList<>();

        public ListAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        public void setImages(List<GalleryImage> newList) {
            this.images.clear();
            this.images.addAll(newList);
            this.notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = inflater.inflate(R.layout.list_item_gallery_image, null, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            GalleryImage image = images.get(position);
            Bitmap bitmap = BitmapFactory.decodeFile(image.path);
            holder.img.setImageBitmap(bitmap);

            // Verifică dacă imaginea este selectată și setează starea de vizibilitate a unei imagini suplimentare
            if (selectedImages.contains(image)) {
                holder.img.setSelected(true);
            } else {
                holder.img.setSelected(false);
            }

            // Adaugă un ascultător de evenimente pentru imagine pentru a permite selecția/deselectarea acesteia
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSelection(holder.getAdapterPosition());
                }
            });
        }


        @Override
        public int getItemCount() {
            return images.size();
        }
        private List<GalleryImage> selectedImages = new ArrayList<>();

        public void toggleSelection(int position) {
            GalleryImage image = images.get(position);
            if (selectedImages.contains(image)) {
                selectedImages.remove(image);
            } else {
                selectedImages.add(image);
            }
            notifyDataSetChanged();
        }

        public void clearSelection() {
            selectedImages.clear();
            notifyDataSetChanged();
        }

        public List<GalleryImage> getSelectedImages() {
            return selectedImages;
        }

    }

    public static class VH extends RecyclerView.ViewHolder {
        public View itemView;
        public ImageView img;
        public VH(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.img = itemView.findViewById(R.id.image);
        }
    }
    private void deleteSelectedPhotos() {
        List<GalleryImage> selectedImages = adapter.getSelectedImages();
        if (selectedImages.isEmpty()) {
            // Nu sunt selectate poze pentru ștergere
            // Poți afișa un mesaj sau să nu faci nimic
            return;
        }

        // Șterge pozele selectate din baza de date și actualizează lista de imagini
        for (GalleryImage image : selectedImages) {
            deleteImage(image);
        }

        // Deselectează toate pozele selectate
        adapter.clearSelection();
    }
    private void deleteImage(GalleryImage image) {
        (new Thread() {
            @Override
            public void run() {
                galleryImageDao.delete(image);
                File imageFile = new File(image.path);
                imageFile.delete();
                loadImages();
            }
        }).start();
    }


}
