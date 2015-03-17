package eaustria.net.findthebug;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int FROG_ID = 212121;
    private static final int ROUND_TIME = 10;
    private int points;
    private int round;
    private int countdown;
    private int highscore;
    private Random rnd = new Random();
    private Handler handler = new Handler();
    private Typeface ttf;
    private ImageView frog;
    private InterstitialAd interstitial;
    private boolean interstitialLoaded = false;
    private AdView mAdView;
    private double difficultyLevel = 1;
    private MediaPlayer[] squashSounds;
    private MediaPlayer[] backgroundMusic;
    private MediaPlayer[] gameOverSounds;
    private boolean playBackgroundMusic;
    private boolean playSounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ttf = Typeface.createFromAsset(getAssets(), "JandaManateeSolid.ttf");
        playBackgroundMusic = true;
        playSounds = true;
        ((TextView)findViewById(R.id.countdown)).setTypeface(ttf);
        ((TextView)findViewById(R.id.round)).setTypeface(ttf);
        ((TextView)findViewById(R.id.points)).setTypeface(ttf);
        ((TextView)findViewById(R.id.highscore)).setTypeface(ttf);
        ((TextView)findViewById(R.id.help)).setTypeface(ttf);
        findViewById(R.id.help).setOnClickListener(this);
        prepareSoundDatabase();
        if ( playBackgroundMusic ) playBackgroundMusic();
        showStartFragment();
    }

    private void prepareSoundDatabase() {
        prepareSoundEffects();
        prepareBackgroundMusic();
        prepareGameOverSounds();
    }

    private void playBackgroundMusic() {
        backgroundMusic[0].setLooping(false);
        backgroundMusic[0].start();
    }

    private void prepareBackgroundMusic() {
        backgroundMusic = new MediaPlayer[1];
        backgroundMusic[0] = new MediaPlayer().create(this, R.raw.frogs);
    }

    private void prepareSoundEffects() {
        squashSounds = new MediaPlayer[14];
        new Thread(new Runnable() {
            @Override
            public void run() {
                squashSounds[0] = new MediaPlayer().create(getBaseContext(), R.raw.blop2);
                squashSounds[1] = new MediaPlayer().create(getBaseContext(), R.raw.tick2);
                squashSounds[2] = new MediaPlayer().create(getBaseContext(), R.raw.woosh_mark);
                squashSounds[3] = new MediaPlayer().create(getBaseContext(), R.raw.blop2);
                squashSounds[4] = new MediaPlayer().create(getBaseContext(), R.raw.hole_punch);
                squashSounds[5] = new MediaPlayer().create(getBaseContext(), R.raw.plop2);
                squashSounds[6] = new MediaPlayer().create(getBaseContext(), R.raw.plop3);
                squashSounds[7] = new MediaPlayer().create(getBaseContext(), R.raw.plop);
                squashSounds[8] = new MediaPlayer().create(getBaseContext(), R.raw.punch);
                squashSounds[9] = new MediaPlayer().create(getBaseContext(), R.raw.slap);
                squashSounds[10] = new MediaPlayer().create(getBaseContext(), R.raw.smashing);
                squashSounds[11] = new MediaPlayer().create(getBaseContext(), R.raw.tick);
                squashSounds[12] = new MediaPlayer().create(getBaseContext(), R.raw.tick2);
                squashSounds[13] = new MediaPlayer().create(getBaseContext(), R.raw.woosh_mark);
            }
        }).start();
    }
    
    private void prepareGameOverSounds() {
        gameOverSounds = new MediaPlayer[3];
        new Thread(new Runnable() {
            @Override
            public void run() {
                gameOverSounds[0] = new MediaPlayer().create(getBaseContext(), R.raw.evil_laugh_male_6);
                gameOverSounds[1] = new MediaPlayer().create(getBaseContext(), R.raw.evil_laugh_male_9);
                gameOverSounds[2] = new MediaPlayer().create(getBaseContext(), R.raw.evil_laugh_male_9_1);
            }
        }).start();
    }

    private void createAndLoadInterstitial() {
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getString(R.string.myAdUnitId));
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                interstitialLoaded = true;
                Log.d(TAG, "interstitial ads loaded");
            }
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d(TAG, "Could not load interstitial ads");
            }
        });

        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                AdRequest.Builder builder = new AdRequest.Builder();
                builder.addTestDevice("F708B3D263B7BAED98419BB27EAB4F76");
                AdRequest adRequest = builder.build();
                interstitial.loadAd(adRequest);
                Log.d(TAG, "Loading Ads...");
            }
        }));
        if (interstitial == null) throw new AssertionError();
        if ( interstitialLoaded )
            interstitial.show();
        else
            Log.d(TAG, "Could not show interstitital as ad has not yet been loaded....");
    }

    private void loadHighscore() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        highscore = sp.getInt("highscore", 0);
    }

    private void saveHighscore(int points) {
        highscore=points;
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putInt("highscore", highscore);
        e.commit();
    }


    private void newGame() {
        points=0;
        round=1;
        createAndLoadInterstitial();
        initRound();
    }

    private void initRound() {
        Log.d(TAG, "enter initRound");
        countdown = ROUND_TIME;
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.removeAllViews();
        WimmelView wv = new WimmelView(this);
        container.addView(wv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        wv.setImageCount(8*(10+round)*difficultyLevel);
        frog = new ImageView(this);
        frog.setId(R.id.frog);
        frog.setImageResource(R.drawable.frog);
        frog.setScaleType(ImageView.ScaleType.CENTER);
        float scale = getResources().getDisplayMetrics().density;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Math.round(64*scale),Math.round(61*scale));
        lp.gravity = Gravity.TOP + Gravity.LEFT;
        lp.leftMargin = rnd.nextInt(container.getWidth()-64);
        lp.topMargin = rnd.nextInt(container.getHeight()-61);
        frog.setOnClickListener(this);
        container.addView(frog, lp);
        update();
        handler.postDelayed(runnable,1000-round*50);
    }

    private void update() {
        fillTextView(R.id.points, getString(R.string.points)+ ":" + Integer.toString(points));
        loadHighscore();
        fillTextView(R.id.highscore, getString(R.string.highscore) + ":" + Integer.toString(highscore));
        fillTextView(R.id.round, " "+ getString(R.string.level) + ":" + Integer.toString(round));
        fillTextView(R.id.countdown, getString(R.string.time) + ":" + Integer.toString(countdown*1000)+" ");
    }

    private void fillTextView(int id, String text) {
        TextView tv = (TextView) findViewById(id);
        tv.setText(text);
    }

    private void showStartFragment() {
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.removeAllViews();
        container.addView(getLayoutInflater().inflate(R.layout.fragment_start, null));
        container.findViewById(R.id.start_easy).setOnClickListener(this);
        container.findViewById(R.id.start_medium).setOnClickListener(this);
        container.findViewById(R.id.start_hard).setOnClickListener(this);

        ((TextView)findViewById(R.id.title)).setTypeface(ttf);
        ((TextView)findViewById(R.id.start_easy)).setTypeface(ttf);
        ((TextView)findViewById(R.id.start_medium)).setTypeface(ttf);
        ((TextView)findViewById(R.id.start_hard)).setTypeface(ttf);
    }

    private void showGameOverFragment() {
        playGameOverSound();
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.addView( getLayoutInflater().inflate(R.layout.fragment_gameover, null) );
        container.findViewById(R.id.play_again).setOnClickListener(this);
        ((TextView)findViewById(R.id.title)).setTypeface(ttf);
        ((TextView)findViewById(R.id.play_again)).setTypeface(ttf);
        mAdView = (AdView) container.findViewById(R.id.gameOverAdView);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //AdRequest adRequest = new AdRequest.Builder().build();
                AdRequest.Builder builder = new AdRequest.Builder();
                builder.addTestDevice("F708B3D263B7BAED98419BB27EAB4F76");
                AdRequest adRequest = builder.build();
                mAdView.loadAd(adRequest);
            }
        });
    }

    private void playGameOverSound() {
        if ( playSounds ) gameOverSounds[rnd.nextInt(gameOverSounds.length)].start();
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.start_easy) {
            startGame(Difficulty.easy);
        } else if(view.getId()==R.id.start_medium) {
            startGame(Difficulty.medium);
        } else if(view.getId()==R.id.start_hard) {
            startGame(Difficulty.hard);
        } else if(view.getId()==R.id.play_again) {
            showStartFragment();
        } else if(view.getId()==R.id.frog) {
            kissFrog();
        } else if(view.getId()==R.id.help) {
            showTutorial();
        }
    }

    private void kissFrog() {
        handler.removeCallbacks(runnable);
        playSquashSound();
        showToast(R.string.kissed);        
        points += countdown*1000;
        round++;
        initRound();
    }

    private void playSquashSound() {
        if ( playSounds ) squashSounds[rnd.nextInt(squashSounds.length)].start();
    }

    private void showToast(int stringResId) {
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.setDuration(Toast.LENGTH_SHORT);
        TextView textView = new TextView(this);
        textView.setText(stringResId);
        textView.setTextColor(getResources().getColor(R.color.points));
        textView.setTextSize(48f);
        textView.setTypeface(ttf);
        toast.setView(textView);
        toast.show();
    }

    private void showTutorial() {
        final Dialog dialog = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_tutorial);
        ((TextView)(dialog.findViewById(R.id.text))).setTypeface(ttf);
        ((TextView)(dialog.findViewById(R.id.start))).setTypeface(ttf);
        dialog.findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startGame(Difficulty.medium);
            }
        });
        dialog.show();
    }

    private void startGame(Difficulty difficulty) {
        switch (difficulty) {
            case easy:
                this.difficultyLevel = 0.75;
                break;
            case medium:
                this.difficultyLevel = 1;
                break;
            case hard:
                this.difficultyLevel = 1.75;
                break;
        }
        newGame();
    }

    private void countdown() {
        countdown--;
        update();
        if(countdown<=0) {
            frog.setOnClickListener(null);
            if(points>highscore) {
                saveHighscore(points);
                update();
            }
            showGameOverFragment();
        } else {
            handler.postDelayed(runnable, 1000 - round * 50);
        }
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            countdown();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}


