package com.smartpdfsuite.fragments;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.smartpdfsuite.R;
import com.smartpdfsuite.utils.DocumentScanner;
import com.smartpdfsuite.utils.ImageUtils;
import com.smartpdfsuite.views.CropOverlayView;

public class EdgeCropFragment extends Fragment {

    private ImageView imageView;
    private CropOverlayView cropOverlayView;
    private Bitmap bitmap;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edge_crop, container, false);

        imageView = view.findViewById(R.id.crop_image_view);
        cropOverlayView = view.findViewById(R.id.crop_overlay);
        Button doneButton = view.findViewById(R.id.btn_crop_done);

        Button rotateLeft = view.findViewById(R.id.btn_rotate_left);
        Button rotateRight = view.findViewById(R.id.btn_rotate_right);


        rotateLeft.setOnClickListener(v -> rotateImage(-90));
        rotateRight.setOnClickListener(v -> rotateImage(90));

        if (getArguments() != null) {
            String uri = getArguments().getString("imageUri");
            if (uri != null) imageUri = Uri.parse(uri);
        }

        if (imageUri == null) {
            Toast.makeText(getContext(), "Image not found", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return view;
        }

        loadBitmap();

        doneButton.setOnClickListener(v -> cropAndContinue());

        return view;
    }


    private void rotateImage(int angle) {
        if (bitmap == null) return;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
        );

        imageView.setImageBitmap(bitmap);

        // 🔥 Reset overlay after rotation
        imageView.post(() -> {
            RectF rect = getBitmapRect();
            cropOverlayView.setImageRect(rect);
        });
    }

    private void loadBitmap() {
        try {
            Bitmap original = ImageUtils.loadBitmapFromUri(
                    requireContext().getContentResolver(),
                    imageUri,
                    2048
            );
            if (original == null) {
                Toast.makeText(getContext(), "Invalid image", Toast.LENGTH_SHORT).show();
                return;
            }
            // 🔥 FIX ROTATION HERE
            bitmap = ImageUtils.rotateBitmapIfRequired(requireContext(), original, imageUri);

            imageView.setImageBitmap(bitmap);

// Overlay now auto-initializes itself
            imageView.post(() -> {
                RectF rect = getBitmapRect();
                cropOverlayView.setImageRect(rect);
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private RectF getBitmapRect() {
        Matrix m = imageView.getImageMatrix();
        float[] v = new float[9];
        m.getValues(v);

        float scaleX = v[Matrix.MSCALE_X];
        float scaleY = v[Matrix.MSCALE_Y];
        float transX = v[Matrix.MTRANS_X];
        float transY = v[Matrix.MTRANS_Y];

        float w = bitmap.getWidth() * scaleX;
        float h = bitmap.getHeight() * scaleY;

        return new RectF(
                transX,
                transY,
                transX + w,
                transY + h
        );
    }

    private void cropAndContinue() {

        PointF[] screen = cropOverlayView.getCornerPoints();
        if (screen == null || screen.length != 4) {
            Toast.makeText(getContext(), "Invalid crop points", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            PointF[] bitmapPoints = mapScreenPointsToBitmap(screen);

            Bitmap cropped = DocumentScanner.fourPointTransform(
                    bitmap,
                    bitmapPoints[0],
                    bitmapPoints[1],
                    bitmapPoints[2],
                    bitmapPoints[3]
            );

            Uri croppedUri = ImageUtils.saveBitmapToCache(requireContext(), cropped);

            Bundle bundle = new Bundle();
            bundle.putString("imageUri", croppedUri.toString());

            Navigation.findNavController(requireView())
                    .navigate(R.id.action_edgeCrop_to_imageProcessor, bundle);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Crop failed", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ CORRECT MAPPING (NO DISTORTION)
    private PointF[] mapScreenPointsToBitmap(PointF[] screenPoints) {
        float[] v = new float[9];
        imageView.getImageMatrix().getValues(v);

        float sx = v[Matrix.MSCALE_X];
        float sy = v[Matrix.MSCALE_Y];
        float tx = v[Matrix.MTRANS_X];
        float ty = v[Matrix.MTRANS_Y];

        PointF[] out = new PointF[4];
        for (int i = 0; i < 4; i++) {
            out[i] = new PointF(
                    (screenPoints[i].x - tx) / sx,
                    (screenPoints[i].y - ty) / sy
            );
        }
        return out;
       /* float[] values = new float[9];
        imageView.getImageMatrix().getValues(values);

        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];

        PointF[] mapped = new PointF[4];

        for (int i = 0; i < 4; i++) {
            float x = (screenPoints[i].x - transX) / scaleX;
            float y = (screenPoints[i].y - transY) / scaleY;

            mapped[i] = new PointF(
                    clamp(x, 0, bitmap.getWidth()),
                    clamp(y, 0, bitmap.getHeight())
            );
        }
        return mapped;*/
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(v, max));
    }


}



