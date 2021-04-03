package com.example.mafiago;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.example.mafiago.fragments.GameFragment;
import com.example.mafiago.fragments.MenuFragment;
import com.example.mafiago.fragments.StartFragment;

public class MainActivity extends AppCompatActivity {

    //Всё ли хорошо в коде
    //какие элементы UI можно добавить

    public static String NickName = "";
    public static String Session_id = "";
    public static int Game_id;
    public static String url = "ae60e8f8ba9d66.localhost.run";

    public static String password = "";
    public static String nick = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
    }
}