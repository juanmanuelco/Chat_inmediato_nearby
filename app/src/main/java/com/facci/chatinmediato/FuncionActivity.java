package com.facci.chatinmediato;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.facci.chatinmediato.DB.DB_SOSCHAT;
import com.facci.chatinmediato.Entities.Mensaje;
import com.facci.chatinmediato.Fragments.FM_encontrados;
import com.facci.chatinmediato.Fragments.FM_historico;
import com.facci.chatinmediato.Fragments.FM_mensajes;
import com.facci.chatinmediato.InitThreads.ServerInit;
import com.facci.chatinmediato.NEGOCIO.Validaciones;
import com.facci.chatinmediato.Servicios.Nerby;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesClient;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FuncionActivity extends AppCompatActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    public static ServerInit server;
    public static String chatName="";
    public static FloatingActionButton fab;
    Fragment fm= null;

    MessageListener mMessageListener;
    ArrayList<Message> mMessages;
    static DB_SOSCHAT db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DB_SOSCHAT(this);
        mMessages = new ArrayList<Message>();
        setContentView(R.layout.activity_funcion);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        fab = (FloatingActionButton) findViewById(R.id.fab_recargar);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if(position == 0) fab.show(); else  fab.hide();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                byte[] mensaje= message.getContent();
                try {
                    Mensaje msg = (Mensaje) Validaciones.NearbyDeserialize(mensaje);
                    db.guardarRegistro(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLost(Message message) {

            }
        };

        List<Mensaje> listado_bloque = db.todos_mensajes();
        for (final Mensaje mensaje: listado_bloque) {
            try {
                byte[] msg = Validaciones.NearbySerialize(mensaje);
                mMessages.add(new Message(msg));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    



    @Override
    protected void onStart() {
        super.onStart();
        for (Message mMessage: mMessages ) {
            MessagesClient mMessagesClient= Nearby.getMessagesClient(this);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMessagesClient = Nearby.getMessagesClient(this, new MessagesOptions.Builder()
                        .setPermissions(NearbyPermissions.BLE)
                        .build());
            }
            mMessagesClient.publish(mMessage);
        }
        mMessages.clear();
        Nearby.getMessagesClient(this).subscribe(mMessageListener);
    }

    @Override
    protected void onStop() {
        for (Message mMessage: mMessages ) {
            Nearby.getMessagesClient(this).unpublish(mMessage);
        }
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        super.onStop();
    }


    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }


        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_fm_encontrados, container, false);
            return rootView;
        }
    }



    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {


            switch (position){
                case 0:
                    fm=new FM_encontrados();
                    invalidateOptionsMenu();
                    break;
                case 1:
                    fm= new FM_mensajes();
                    invalidateOptionsMenu();
                    break;
                case 2:
                    fm=new FM_historico();
                    invalidateOptionsMenu();
                    break;
            }

            return fm;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}