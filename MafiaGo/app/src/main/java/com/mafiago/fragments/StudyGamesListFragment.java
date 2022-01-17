package com.mafiago.fragments;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.mafiago.R;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.util.ArrayList;

public class StudyGamesListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_study_games_list, container, false);

        TextView TV_name = view.findViewById(R.id.fragmentStudyGamesList_TV_name);

        RelativeLayout RL_info = view.findViewById(R.id.itemGame_RL_info1);
        ImageView IV_doctor1 = view.findViewById(R.id.itemGame_doctor1);
        ImageView IV_lover1 = view.findViewById(R.id.itemGame_lover1);
        ImageView IV_mafia_don1 = view.findViewById(R.id.itemGame_mafia_don1);
        ImageView IV_journalist1 = view.findViewById(R.id.itemGame_journalist1);
        TextView TV_roomName1 = view.findViewById(R.id.itemGame_TV_roomName1);
        TextView TV_roomState1 = view.findViewById(R.id.itemGame_TV_roomState1);
        TextView TV_minMaxPlayers1 = view.findViewById(R.id.itemGame_TV_minMaxPlayers1);
        TextView TV_playersInRoom1 = view.findViewById(R.id.itemGame_TV_playersInRoom1);

        RelativeLayout RL_info2 = view.findViewById(R.id.itemGame_RL_info);
        ImageView IV_doctor2 = view.findViewById(R.id.itemGame_doctor);
        ImageView IV_lover2 = view.findViewById(R.id.itemGame_lover);
        ImageView IV_mafia_don2 = view.findViewById(R.id.itemGame_mafia_don);
        ImageView IV_journalist2 = view.findViewById(R.id.itemGame_journalist);
        ImageView IV_lock2 = view.findViewById(R.id.itemGame_lock);
        TextView TV_roomName2 = view.findViewById(R.id.itemGame_TV_roomName);
        TextView TV_roomState2 = view.findViewById(R.id.itemGame_TV_roomState);
        TextView TV_minMaxPlayers2 = view.findViewById(R.id.itemGame_TV_minMaxPlayers);
        TextView TV_playersInRoom2 = view.findViewById(R.id.itemGame_TV_playersInRoom);
        TextView TV_customRoom2 = view.findViewById(R.id.itemGame_TV_customRoom);
        HorizontalScrollView SV1 = view.findViewById(R.id.itemGame_SV1);

        CheckBox CB_deletePlayingRoom = view.findViewById(R.id.fragmentStudyGamesList_CB_playingRoom);
        CheckBox CB_deleteNormalRoom = view.findViewById(R.id.fragmentStudyGamesList_CB_normalRoom);
        CheckBox CB_deletePasswordRoom = view.findViewById(R.id.fragmentStudyGamesList_CB_passwordRoom);
        CheckBox CB_deleteCustomRoom = view.findViewById(R.id.fragmentStudyGamesList_CB_customRoom);
        RelativeLayout RL_filter = view.findViewById(R.id.fragmentStudyGamesList_RL_filter);
        RangeSeekBar RSB_num_users = view.findViewById(R.id.fragmentStudyGamesList_RSB);
        ImageView IV_filter = view.findViewById(R.id.fragmentStudyGamesList_IV_filter);
        EditText ET_search = view.findViewById(R.id.fragmentStudyGamesList_ET_search);
        HorizontalScrollView SV2 = view.findViewById(R.id.itemGame_SV);


        new TapTargetSequence(getActivity())
                .targets(
                        TapTarget.forView(TV_name,"У каждой комнаты есть своё название и свои настройки игры","")
                                .outerCircleColor(R.color.orange)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(20)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(10)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(80),
                        TapTarget.forView(TV_roomName1,"<<Комната 1>> - название этой комнаты","")
                                .outerCircleColor(R.color.orange)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(20)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(10)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(80),
                        TapTarget.forView(TV_playersInRoom1,"Количество игроков в комнате","")
                                .outerCircleColor(R.color.orange)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(20)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(10)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(60),
                        TapTarget.forView(TV_minMaxPlayers1,"Минимальное и максимальное количество людей для игры","")
                                .outerCircleColor(R.color.orange)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(20)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(10)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)

                                .targetRadius(60),
                        TapTarget.forView(SV1,"Доступные роли в комнате","")
                                .outerCircleColor(R.color.orange)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(20)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(10)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(90),
                        TapTarget.forView(TV_roomState1,"Статус комнаты. Если идёт набор - в комнату можно войти и играть в ней, если игра уже началась - играть в комнате уже нельзя, но можно за ней наблюдать","")
                                .outerCircleColor(R.color.orange)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(20)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(10)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(60),
                        TapTarget.forView(IV_lock2,"Значок блокироваки обозначает комнату с паролем","")
                                .outerCircleColor(R.color.orange)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(20)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(10)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(60),
                        TapTarget.forView(TV_customRoom2,"Значок кастомной комнаты. В кастомной комнате создатель может сам выбирать роли для игры. Например, 10 шерифов против 10 мафий","")
                                .outerCircleColor(R.color.orange)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(20)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(10)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(60),
                        TapTarget.forView(RL_info2,"Дополнительная информация о комнате","")
                                .outerCircleColor(R.color.orange)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(20)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(10)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(60),
                        TapTarget.forView(TV_roomName1,"А теперь зайди в 1 комнату. Для этого надо нажать на эту комнату","")
                                .outerCircleColor(R.color.notActiveText)
                                .outerCircleAlpha(0.96f)
                                .targetCircleColor(R.color.white)
                                .titleTextSize(20)
                                .titleTextColor(R.color.white)
                                .descriptionTextSize(10)
                                .descriptionTextColor(R.color.black)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .dimColor(R.color.black)
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .targetRadius(120)).listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {
                StudyFragment studyFragment = StudyFragment.newInstance("mafia");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.MainActivity, studyFragment).commit();
            }

            @Override
            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
            }

            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {
            }
        }).start();


        return view;
    }
}