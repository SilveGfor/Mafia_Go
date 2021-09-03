package com.mafiago.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.mafiago.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.mafiago.MainActivity;
import com.mafiago.classes.OnBackPressedListener;
import com.mafiago.pager_adapters.SettingsPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.socket.emitter.Emitter;

import static android.app.Activity.RESULT_OK;
import static com.mafiago.MainActivity.socket;
import static com.mafiago.fragments.MenuFragment.GALLERY_REQUEST;

public class SettingsFragment extends Fragment implements OnBackPressedListener {

    TabLayout tab;
    ViewPager viewPager;
    ImageView Menu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        tab = view.findViewById(R.id.fragmentSettings_TabLayout);
        viewPager = view.findViewById(R.id.fragmentSettings_ViewPager);
        Menu = view.findViewById(R.id.fragmentMenu_IV_menu);

        Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup_menu = new PopupMenu(getActivity(), Menu);
                popup_menu.inflate(R.menu.main_menu);
                popup_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mainMenu_play:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new GamesListFragment()).commit();
                                return true;
                            case R.id.mainMenu_shop:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new ShopFragment()).commit();
                                return true;
                            case R.id.mainMenu_friends:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new FriendsFragment()).commit();
                                return true;
                            case R.id.mainMenu_chats:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new PrivateChatsFragment()).commit();
                                return true;
                            case R.id.mainMenu_settings:
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new SettingsFragment()).commit();
                                return true;
                        }
                        return true;
                    }
                });
                popup_menu.show();
            }
        });

        // Получаем ViewPager и устанавливаем в него адаптер
        viewPager.setAdapter(
                new SettingsPagerAdapter(getActivity().getSupportFragmentManager(), getActivity()));

        // Передаём ViewPager в TabLayout
        tab.setupWithViewPager(viewPager);




        return  view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (imageReturnedIntent == null
                || imageReturnedIntent.getData() == null) {
            return;
        }
        // сжимает до ~500КБ максимум. Тогда как обычная картинка весит ~2МБ

        switch(requestCode) {
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK){
                    Uri uri = imageReturnedIntent.getData();

                    try {
                        /*
                        Bitmap bitmap2 = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        Log.d("kkk", String.valueOf(bitmap2.getByteCount() / 1048576));
                        byte[] bytes = stream.toByteArray();
                        String base642 = Base64.encodeToString(bytes, Base64.DEFAULT);
                        //Log.d("kkk", String.valueOf(base642.length()));
                         */

                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                        int scaleDivider = 5;
                        int max = 0;
                        if (bitmap.getWidth() > bitmap.getHeight()) {
                            max = bitmap.getWidth();
                        }
                        else
                        {
                            max = bitmap.getHeight();
                        }
                        if (max <= 300)
                        {
                            scaleDivider = 1;
                        }
                        else if (max <= 600)
                        {
                            scaleDivider = 2;
                        }
                        else if (max <= 900)
                        {
                            scaleDivider = 3;
                        }
                        else if (max <= 1200)
                        {
                            scaleDivider = 4;
                        }
                        else if (max <= 1500)
                        {
                            scaleDivider = 5;
                        }
                        else if (max <= 1800)
                        {
                            scaleDivider = 6;
                        }
                        else if (max <= 2100)
                        {
                            scaleDivider = 7;
                        }
                        else if (max <= 2400)
                        {
                            scaleDivider = 8;
                        }
                        else if (max <= 2700)
                        {
                            scaleDivider = 9;
                        }
                        else if (max <= 3000)
                        {
                            scaleDivider = 10;
                        }
                        else if (max <= 3300)
                        {
                            scaleDivider = 11;
                        }
                        else if (max <= 3600)
                        {
                            scaleDivider = 12;
                        }
                        else if (max <= 3900)
                        {
                            scaleDivider = 13;
                        }
                        else if (max <= 4200)
                        {
                            scaleDivider = 14;
                        }

                        int scaleWidth = bitmap.getWidth() / scaleDivider;
                        int scaleHeight = bitmap.getHeight() / scaleDivider;
                        Log.d("kkk", String.valueOf(scaleWidth));
                        Log.d("kkk", String.valueOf(scaleHeight));
                        byte[] downsizedImageBytes =
                                getDownsizedImageBytes(bitmap, scaleWidth, scaleHeight);
                        String base64 = Base64.encodeToString(downsizedImageBytes, Base64.DEFAULT);
                        Log.d("kkk", String.valueOf("Длина = " + base64.length()));
                        if (base64.length() <= 524288) {
                            final JSONObject json2 = new JSONObject();
                            try {
                                json2.put("nick", MainActivity.NickName);
                                json2.put("session_id", MainActivity.Session_id);
                                json2.put("avatar", base64);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("kkk", "Socket_отправка - edit_profile - " + json2.toString());
                            socket.emit("edit_profile", json2);

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Профиль успешно изменён!")
                                    .setMessage("")
                                    .setIcon(R.drawable.ic_ok)
                                    .setCancelable(false)
                                    .setNegativeButton("Ок",
                                            (dialog, id) -> dialog.cancel());
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                        else
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Слишком большое изображение!")
                                    .setMessage("")
                                    .setIcon(R.drawable.ic_error)
                                    .setCancelable(false)
                                    .setNegativeButton("Ок",
                                            (dialog, id) -> dialog.cancel());
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromBytes(byte[] bytes,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public byte[] getDownsizedImageBytes(Bitmap fullBitmap, int scaleWidth, int scaleHeight) throws IOException {

        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(fullBitmap, scaleWidth, scaleHeight, true);

        int width  = fullBitmap.getWidth();
        int height = fullBitmap.getHeight(); //высота
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int max = newHeight;
        int scaleDivider = 1;
        if (max <= 600)
        {
            scaleDivider = 1;
        }
        else if (max <= 1200)
        {
            scaleDivider = 2;
        }
        else if (max <= 1800)
        {
            scaleDivider = 3;
        }
        else if (max <= 2400)
        {
            scaleDivider = 4;
        }
        else if (max <= 3000)
        {
            scaleDivider = 5;
        }
        else if (max <= 3600)
        {
            scaleDivider = 6;
        }
        else if (max <= 4200)
        {
            scaleDivider = 7;
        }
        //newWidth = newWidth / scaleDivider;
        //newHeight = newHeight / scaleDivider;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(fullBitmap, cropW, cropH, newWidth, newHeight);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(cropImg, newWidth / scaleDivider, newHeight / scaleDivider, true);

        // 2. Instantiate the downsized image content as a byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] downsizedImageBytes = baos.toByteArray();

        return downsizedImageBytes;
    }

    @Override
    public void onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
    }


}