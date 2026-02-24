package com.smartpdfsuite.fragments;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
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

    private void loadBitmap() {
        try {
            bitmap = ImageUtils.loadBitmapFromUri(
                    requireContext().getContentResolver(),
                    imageUri,
                    2048
            );

            if (bitmap == null) {
                Toast.makeText(getContext(), "Invalid image", Toast.LENGTH_SHORT).show();
                return;
            }

            imageView.setImageBitmap(bitmap);

// Overlay now auto-initializes itself
            imageView.post(() -> cropOverlayView.initDefaultCorners());
/*

            imageView.post(() -> {
                cropOverlayView.setBitmapSize(
                        imageView.getWidth(),
                        imageView.getHeight()
                );
                cropOverlayView.initDefaultCorners();
            });
*/

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void cropAndContinue() {

        PointF[] screenPoints = cropOverlayView.getOrderedPoints();
        if (screenPoints == null || screenPoints.length != 4) {
            Toast.makeText(getContext(), "Invalid crop points", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            PointF[] bitmapPoints = mapScreenPointsToBitmap(screenPoints);

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

        float[] values = new float[9];
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
        return mapped;
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(v, max));
    }
}

/*public class EdgeCropFragment extends Fragment {

    private ImageView imageView;
    private CropOverlayView cropOverlayView;

    private Uri imageUri;
    private Bitmap bitmap;

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

        // ✅ SAFE ARGUMENT READ
        if (getArguments() != null) {
            String uriString = getArguments().getString("imageUri");
            if (uriString != null) {
                imageUri = Uri.parse(uriString);
            }
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

    private void loadBitmap() {
        try {
            bitmap = ImageUtils.loadBitmapFromUri(
                    requireContext().getContentResolver(),
                    imageUri,
                    2048
            );

            if (bitmap == null) {
                Toast.makeText(getContext(), "Invalid image", Toast.LENGTH_SHORT).show();
                return;
            }

            imageView.setImageBitmap(bitmap);

            imageView.post(() -> {

                // Get displayed bitmap bounds inside ImageView
                float[] values = new float[9];
                imageView.getImageMatrix().getValues(values);

                float scaleX = values[Matrix.MSCALE_X];
                float scaleY = values[Matrix.MSCALE_Y];

                float displayedWidth = bitmap.getWidth() * scaleX;
                float displayedHeight = bitmap.getHeight() * scaleY;

                cropOverlayView.setBitmapSize(
                        (int) displayedWidth,
                        (int) displayedHeight
                );

                cropOverlayView.initDefaultCorners();
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }
    *//*private void loadBitmap() {
        try {
            bitmap = ImageUtils.loadBitmapFromUri(
                    requireContext().getContentResolver(),
                    imageUri,
                    2048
            );

            if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                Toast.makeText(getContext(), "Invalid image", Toast.LENGTH_SHORT).show();
                return;
            }

            imageView.setImageBitmap(bitmap);

            // ✅ Initialize crop points
            cropOverlayView.setBitmapSize(
                    bitmap.getWidth(),
                    bitmap.getHeight()
            );
            cropOverlayView.initDefaultCorners();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }*//*

    private void cropAndContinue() {

        if (bitmap == null) {
            Toast.makeText(getContext(), "Image not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        PointF[] screenPoints = cropOverlayView.getOrderedPoints();

        if (screenPoints == null || screenPoints.length != 4) {
            Toast.makeText(getContext(), "Invalid crop points", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            // Convert screen points → bitmap points
            PointF[] bitmapPoints = mapScreenPointsToBitmap(screenPoints);

            Bitmap cropped = DocumentScanner.fourPointTransform(
                    bitmap,
                    bitmapPoints[0],
                    bitmapPoints[1],
                    bitmapPoints[2],
                    bitmapPoints[3]
            );

            if (cropped == null || cropped.getWidth() <= 0 || cropped.getHeight() <= 0) {
                Toast.makeText(getContext(), "Crop failed", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri croppedUri = ImageUtils.saveBitmapToCache(requireContext(), cropped);

            if (croppedUri == null) {
                Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("imageUri", croppedUri.toString());

            Navigation.findNavController(requireView())
                    .navigate(R.id.action_edgeCrop_to_imageProcessor, bundle);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(),
                    "Crop error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ✅ PUT IT HERE (OR ABOVE cropAndContinue)
    private PointF[] mapScreenPointsToBitmap(PointF[] screenPoints) {

        float[] values = new float[9];
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
                    Math.max(0, Math.min(x, bitmap.getWidth())),
                    Math.max(0, Math.min(y, bitmap.getHeight()))
            );
        }
        return mapped;
    }
   *//* private PointF[] mapScreenPointsToBitmap(PointF[] screenPoints) {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) return screenPoints;

        float imageViewWidth = imageView.getWidth();
        float imageViewHeight = imageView.getHeight();

        float bitmapWidth = bitmap.getWidth();
        float bitmapHeight = bitmap.getHeight();

        float scaleX = bitmapWidth / imageViewWidth;
        float scaleY = bitmapHeight / imageViewHeight;

        PointF[] mapped = new PointF[4];
        for (int i = 0; i < 4; i++) {
            mapped[i] = new PointF(
                    screenPoints[i].x * scaleX,
                    screenPoints[i].y * scaleY
            );
        }
        return mapped;
    }*//*
}*/
    /*private void cropAndContinue() {
        PointF[] points = cropOverlayView.getOrderedPoints();

        if (points == null || points.length != 4) {
            Toast.makeText(getContext(), "Invalid crop points", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap cropped = DocumentScanner.fourPointTransform(
                bitmap,
                points[0], points[1], points[2], points[3]
        );

        if (cropped == null ||
                cropped.getWidth() <= 0 ||
                cropped.getHeight() <= 0) {

            Toast.makeText(getContext(), "Crop failed", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri croppedUri = ImageUtils.saveBitmapToCache(
                requireContext(),
                cropped
        );

        if (croppedUri == null) {
            Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("imageUri", croppedUri.toString());

        NavController navController =
                Navigation.findNavController(requireView());

        navController.navigate(
                R.id.action_edgeCrop_to_imageProcessor,
                bundle
        );
    }
}*/



/*
package com.smartpdfsuite.fragments;

        import android.graphics.Bitmap;
        import android.graphics.PointF;
        import android.net.Uri;
        import android.os.Bundle;
        import android.view.*;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.Toast;

        import androidx.annotation.*;
        import androidx.fragment.app.Fragment;
        import androidx.navigation.*;

        import com.smartpdfsuite.R;
        import com.smartpdfsuite.utils.DocumentScanner;
        import com.smartpdfsuite.utils.ImageUtils;

public class EdgeCropFragment extends Fragment {

    private ImageView imageView;
    private com.smartpdfsuite.fragments.CropOverlayView cropOverlayView;
    private Uri imageUri;
    private Bitmap bitmap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edge_crop, container, false);
        imageView = view.findViewById(R.id.crop_image_view);
        cropOverlayView = view.findViewById(R.id.crop_overlay);
        Button doneButton = view.findViewById(R.id.btn_crop_done);

        imageUri = getArguments().getParcelable("imageUri");

        loadBitmap();

        doneButton.setOnClickListener(v -> cropAndContinue());

        return view;
    }

    private void loadBitmap() {
        try {
            bitmap = ImageUtils.loadBitmapFromUri(
                    requireContext().getContentResolver(),
                    imageUri,
                    2048
            );

            imageView.setImageBitmap(bitmap);

            // 🔥 Auto initialize corners (basic auto-detect fallback)
            cropOverlayView.initDefaultCorners(bitmap.getWidth(), bitmap.getHeight());

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void cropAndContinue() {

        PointF[] points = cropOverlayView.getOrderedPoints();

        Bitmap cropped = DocumentScanner.fourPointTransform(
                bitmap,
                points[0], points[1], points[2], points[3]
        );

        if (cropped == null) {
            Toast.makeText(getContext(), "Invalid crop area", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri croppedUri = ImageUtils.saveBitmapToCache(requireContext(), cropped);

        Bundle bundle = new Bundle();
        bundle.putParcelable("imageUri", croppedUri);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(
                R.id.action_edgeCropFragment_to_imageProcessorFragment,
                bundle
        );
    }
}
*/
