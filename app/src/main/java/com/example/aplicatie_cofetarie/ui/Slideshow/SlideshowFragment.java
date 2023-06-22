package com.example.aplicatie_cofetarie.ui.Slideshow;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.aplicatie_cofetarie.R;
import com.example.aplicatie_cofetarie.databinding.FragmentSlideshowBinding;

public class SlideshowFragment extends Fragment {

    private VideoView videoView;
    private FragmentSlideshowBinding binding;

    @SuppressLint("MissingInflatedId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        videoView = root.findViewById(R.id.videoView);
        int videoRawId = R.raw.video;
        MediaController mediaController = new MediaController(getActivity());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        Uri videoUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + videoRawId);
        videoView.setVideoURI(videoUri);
        videoView.start();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
