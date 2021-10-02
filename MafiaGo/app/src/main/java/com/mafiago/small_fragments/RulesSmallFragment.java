package com.mafiago.small_fragments;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mafiago.R;
import com.mafiago.adapters.RulesAdapter;
import com.mafiago.fragments.MenuFragment;
import com.mafiago.models.RuleModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.mafiago.MainActivity.Theme;

public class RulesSmallFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    ViewPager viewPager;

    ////////////////

    List<RuleModel> models;
    Integer[] colors = null;

    RulesAdapter rules_adapter;

    public static final String APP_PREFERENCES = "user";
    public static final String APP_PREFERENCES_EMAIL = "email";
    public static final String APP_PREFERENCES_PASSWORD = "password";
    public static final String APP_PREFERENCES_LAST_ROLE = "role";
    public static final String APP_PREFERENCES_SHOW_ROLE= "show_role";

    private SharedPreferences mSettings;

    public static RulesSmallFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        RulesSmallFragment fragment = new RulesSmallFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rules_small, container, false);

        viewPager = view.findViewById(R.id.fragmentRulesSmall_ViewPager);

        mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);


        models = new ArrayList<>();

        if (mPage == 1)
        {
            if (Theme.equals("dark")) {
                models.add(new RuleModel(R.drawable.game_goal_round, "Цель игры", "В городе присутствуют две группы людей: мирные и мафия. Хоть мафия и в меньшинстве, но она знает друг друга, в отличие от мирных, которые в большинстве. Мафия жаждет завладеть городом, и ее цель - перебить всех мирных жителей. Цель мирных жителей - скооперироваться и дать отпор мафии, методично выслеживая и убивая ее членов. Игра заканчивается, когда в городе не остается членов мафии или мирных жителей."));
                models.add(new RuleModel(R.drawable.game_progress, "Ход игры:", "До начала игры игроки ожидают в лобби комнаты, где можно свободно общаться. Создатель комнаты является ее хостом. Если хост вышел из комнаты, то у комнаты нет хоста, если создатель комнаты вернется в нее, то он снова станет хостом. Игроки смогут увидеть профиль тех, кто с ними в одной комнате, нажав на аватарку рядом с сообщением игрока. Просматривая профиль игрока, можно будет написать ему личное сообщение или отправить запрос дружбы. Хост игры также сможет заблокировать до 5 человек в данной комнате во время лобби через профиль игрока. В игре есть день и ночь."));
                models.add(new RuleModel(R.drawable.game_progress, "Ход игры:", "Ночь делится на две фазы. Во время первой фазы ночи мафия может переписываться в чате, любовница и доктор легкого поведения могут выбрать свою цель. Во время второй фазы ночи мафия и мирные жители, которые обладают ночными способностями, могут выбрать свою цель." +
                        "\n" +
                        "День также делится на две фазы. Во время первой фазы можно обсуждать в чате произошедшие события и делиться своими предложениями, кого убить во время голосования."));
                models.add(new RuleModel(R.drawable.game_progress, "Ход игры:", "Каждый игрок может отправить не более 10 сообщений днем. Если игрокам уже нечего обсуждать, они могут нажать на кнопку пропуска дня. Если на нее нажмут 75 и более процентов живых игроков, то день пропустится. Мирные жители, которые обладают дневными способностями, могут выбрать свою цель. Во время второй фазы наступает голосование, где жители могут проголосовать за того, кого хотят убить. В это время террорист может попытаться взорвать свою цель." +
                        "\n" +
                        "После смерти какого-либо игрока, он имеет права на одно прощальное сообщение, после чего он может наблюдать за игрой, не принимая в ней участия и общаясь в чате для мертвых."));
                models.add(new RuleModel(R.drawable.experience_round, "Начисление опыта и монет", "По окончанию игры каждому игроку начислятся монеты и опыт. На количество монет и опыта влияют следующие параметры: использование своей роли, голосование на дневном голосовании, количество полностью прожитых дней, количество игроков в комнате и результат игры команды. Позитивное влияние на количество опыта и монет окажут игра в пользу своей команды, длинная жизнь, большое количество игроков в комнате и победа команды."));
                models.add(new RuleModel(R.drawable.skip_day_round, "Кнопка пропуска дня", "Во время дня каждый живой игрок может проголосовать за пропуск времени, чтобы сразу наступило голосование. Красная кнопка расположена в правом нижнем углу. Как только за пропуск проголосуют 75% игроков - день пропустится."));
            } else {
                models.add(new RuleModel(R.drawable.game_goal_round_light, "Цель игры", "В городе присутствуют две группы людей: мирные и мафия. Хоть мафия и в меньшинстве, но она знает друг друга, в отличие от мирных, которые в большинстве. Мафия жаждет завладеть городом, и ее цель - перебить всех мирных жителей. Цель мирных жителей - скооперироваться и дать отпор мафии, методично выслеживая и убивая ее членов. Игра заканчивается, когда в городе не остается членов мафии или мирных жителей."));
                models.add(new RuleModel(R.drawable.game_progress_round_light, "Ход игры:", "До начала игры игроки ожидают в лобби комнаты, где можно свободно общаться. Создатель комнаты является ее хостом. Если хост вышел из комнаты, то у комнаты нет хоста, если создатель комнаты вернется в нее, то он снова станет хостом. Игроки смогут увидеть профиль тех, кто с ними в одной комнате, нажав на аватарку рядом с сообщением игрока. Просматривая профиль игрока, можно будет написать ему личное сообщение или отправить запрос дружбы. Хост игры также сможет заблокировать до 5 человек в данной комнате во время лобби через профиль игрока. В игре есть день и ночь."));
                models.add(new RuleModel(R.drawable.game_progress_round_light, "Ход игры:", "Ночь делится на две фазы. Во время первой фазы ночи мафия может переписываться в чате, любовница и доктор легкого поведения могут выбрать свою цель. Во время второй фазы ночи мафия и мирные жители, которые обладают ночными способностями, могут выбрать свою цель." +
                        "\n" +
                        "День также делится на две фазы. Во время первой фазы можно обсуждать в чате произошедшие события и делиться своими предложениями, кого убить во время голосования."));
                models.add(new RuleModel(R.drawable.game_progress_round_light, "Ход игры:", "Каждый игрок может отправить не более 10 сообщений днем. Если игрокам уже нечего обсуждать, они могут нажать на кнопку пропуска дня. Если на нее нажмут 75 и более процентов живых игроков, то день пропустится. Мирные жители, которые обладают дневными способностями, могут выбрать свою цель. Во время второй фазы наступает голосование, где жители могут проголосовать за того, кого хотят убить. В это время террорист может попытаться взорвать свою цель." +
                        "\n" +
                        "После смерти какого-либо игрока, он имеет права на одно прощальное сообщение, после чего он может наблюдать за игрой, не принимая в ней участия и общаясь в чате для мертвых."));
                models.add(new RuleModel(R.drawable.experience_round_light, "Начисление опыта и монет", "По окончанию игры каждому игроку начислятся монеты и опыт. На количество монет и опыта влияют следующие параметры: использование своей роли, голосование на дневном голосовании, количество полностью прожитых дней, количество игроков в комнате и результат игры команды. Позитивное влияние на количество опыта и монет окажут игра в пользу своей команды, длинная жизнь, большое количество игроков в комнате и победа команды."));
                models.add(new RuleModel(R.drawable.skip_day_round_light, "Кнопка пропуска дня", "Во время дня каждый живой игрок может проголосовать за пропуск времени, чтобы сразу наступило голосование. Красная кнопка расположена в правом нижнем углу. Как только за пропуск проголосуют 75% игроков - день пропустится."));
            }
        }
        else
        {
            models.add(new RuleModel(R.drawable.mafia_round, "Мафия", "Мафия — неотъемлемый персонаж игры. Мафия образует опасную группировку из своих членов, и ее цель - уничтожить все мирное население города. Каждый член мафии знает всех остальных мафиози. Ночью мафия может обсуждать в отдельном чате свои мысли и выбирать свою новую жертву. Присутствует во всех играх."));
            models.add(new RuleModel(R.drawable.citizen_round, "Мирный житель", "Мирный житель — он живет в городе, где никого не знает. Его задача выяснить, кто из жителей стоит на стороне мафии, а кто - нет, и уничтожить всех мафиози, пока они не уничтожили весь город. Каждый раз по окончанию дня мирные жители могут голосовать за того, кого они считают мафией."));
            models.add(new RuleModel(R.drawable.sheriff_round, "Шериф", "Шериф — играет на стороне мирных жителей. Задача шерифа - упростить мирным жителям задачу поиска мафии. Ночью шериф может проверить любого игрока и узнать его роль. Присутствует во всех играх."));
            models.add(new RuleModel(R.drawable.doctor_round, "Доктор", "Доктор — играет на стороне мирных. Доктор может защитить любого игрока от смерти ночью, тем самым увеличивая шансы мирных жителей на победу. Также доктор сможет защитить от смерти игрока, к которому пришел отравитель. Один раз в игру доктор может вылечить себя."));
            models.add(new RuleModel(R.drawable.lover_round, "Любовница", "Любовница — на стороне мирных жителей. Никто не способен противостоять ее красоте, даже члены мафии, чем любовница и пользуется, чтобы помочь мирным. Ночью любовница самая первая может использовать свои чары против любого игрока, тем самым лишая его способностей роли и возможности голосовать днем."));
            models.add(new RuleModel(R.drawable.mafia_don_round, "Дон мафии", "Дон мафии — лидер мафиозной группировки. Дон обладает теми же способностями, что и обычная мафия, но его голос на ночном выборе жертвы считается за два."));
            models.add(new RuleModel(R.drawable.journalist_round, "Агент СМИ", "Агент СМИ — проводит расследования на стороне мирных жителей. Ночью он может проверить любых двух игроков и выяснить, играют ли они в одной команде или нет."));
            models.add(new RuleModel(R.drawable.terrorist_round, "Террорист", "Террорист — играет на стороне мафии. Он не знает, кто член мафиозной группировки, однако мафия знает, кто террорист, и не может его убить ночью. Задача террориста - во время дневного голосования выгодно подорвать свою жизнь вместе с жизнью мирного игрока, обладающего значительными способностями, например, шерифа или любовницу."));
            models.add(new RuleModel(R.drawable.bodyguard_round, "Телохранитель", "Телохранитель — играет на стороне мирных. Телохранитель может прийти к любому игроку днем и защищать его от смерти до конца следующей ночи. Если террорист попробует взорвать игрока, который под защитой телохранителя, то умрет только террорист, но игрок уже останется без защиты телохранителя. Защита телохранителя от смерти не действует для смерти по результатам дневного голосования."));
            models.add(new RuleModel(R.drawable.doctor_of_easy_virtue_round, "Доктор лёгкого поведения", "Доктор легкого поведения — на стороне мирных. Имеет опыт любовницы, однако его призвание - врачевание, поэтому ночью доктор легкого поведения может решить, воспользоваться ему способностями любовницы или доктора."));
            models.add(new RuleModel(R.drawable.poisoner_round, "Отравитель", "Отравитель — играет за мафию. Отравитель не знает, кто мафия, а мафия не знает, кто отравитель, поэтому при неосторожной игре отравитель может быть убит мафией ночью. Отравитель ночью может отравить любого игрока, который умрет на следующий день после голосования, если к нему не придет доктор или телохранитель. Отравленный человек настолько обессилен, что может написать только одно сообщение следующим днем."));
            models.add(new RuleModel(R.drawable.maniac_round, "Маньяк", "Маньяк — играет за мирных. Маньяк примкнул к стороне мирных, потому что у него появился ночной конкурент - мафия. Маньяк не желает, чтобы его цели убивал кто-то еще, поэтому он старается каждую ночь убить кого-то из клана мафии."));
        }

        rules_adapter = new RulesAdapter(models, getActivity());

        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();

        viewPager.setAdapter(rules_adapter);
        //viewPager.setPadding(displaymetrics.widthPixels / 10,0,displaymetrics.widthPixels / 10,30);

        Integer[] colors_temp = {
                getResources().getColor(R.color.color_mafia),
                getResources().getColor(R.color.color_citizen),
                getResources().getColor(R.color.color_sheriff),
                getResources().getColor(R.color.color_doctor),
                getResources().getColor(R.color.color_lover),
                getResources().getColor(R.color.color_mafia_don),
                getResources().getColor(R.color.color_journalist),
                getResources().getColor(R.color.color_terrorist),
                getResources().getColor(R.color.color_bodyguard),
                getResources().getColor(R.color.color_doctor_of_easy_virtue),
                getResources().getColor(R.color.color_poisoner),
                getResources().getColor(R.color.color_maniac),
                getResources().getColor(R.color.color_mafia)
        };

        colors = colors_temp;

        /*
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position < (rules_adapter.getCount() - 1) && position < (colors.length - 1))
                {
                    btn_exitRules.setTextColor((Integer) argbEvaluator.evaluate(
                            positionOffset,
                            colors[position],
                            colors[position + 1]
                    ));
                }
                else
                {
                    btn_exitRules.setTextColor(colors[colors.length - 1]);
                }
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
         */
        return view;
    }
}