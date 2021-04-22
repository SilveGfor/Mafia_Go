package com.example.mafiago;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.example.mafiago.fragments.CreateRoomFragment;
import com.example.mafiago.fragments.GameFragment;
import com.example.mafiago.fragments.GamesListFragment;
import com.example.mafiago.fragments.MenuFragment;
import com.example.mafiago.fragments.StartFragment;

import java.net.URI;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {


    public static String NickName = "";
    public static String Session_id = "";
    public static int Game_id;
    public static String url = "1d19e08842f0d6.localhost.run";

    public static String password = "";
    public static String nick = "";
/*
    public static Socket socket;
    {
        try{
            IO.Options options = IO.Options.builder()
                    .setReconnection(false)
                    .build();
            socket = IO.socket(URI.create("https://" + MainActivity.url), options); // the main namespace
            //socket = IO.socket("https://" + MainActivity.url);

        }catch (URISyntaxException e){
            throw new RuntimeException();
        }
    }

 */
public static Socket socket;
    {
        IO.Options options = IO.Options.builder()
                .setReconnectionDelay(0)
                .build();
        socket = IO.socket(URI.create("https://" + MainActivity.url), options); // the main namespace
        //socket = IO.socket("https://" + MainActivity.url);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        socket.connect();

        getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, new MenuFragment()).commit();
    }
}