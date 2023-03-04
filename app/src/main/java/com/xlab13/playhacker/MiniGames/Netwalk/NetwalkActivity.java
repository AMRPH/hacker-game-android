package com.xlab13.playhacker.MiniGames.Netwalk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

public class NetwalkActivity extends AppCompatActivity {


    private TableLayout tlGridTable;
    private MyAlertDialog gridWinDialog, gridLossDialog;
    private TextView tvTimeRemaining, tvMoves;


    private SparseIntArray gridImageMap;
    private TileEventHandler tileEventHandler;
    private NetwalkGrid netwalkGrid;
    private CountDownTimer timer;

    private int gridColumns, gridRows, imageRatio;
    private long timeTimer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netwalk);
        timeTimer = 1000* 60;
        mTools.startMusic(R.raw.game_ost);

        initializeGridMap();
        initializeViews();
        restartGame();
    }

    private void initializeViews() {
        tlGridTable = (TableLayout) findViewById(R.id.tlGridTable);
        tvTimeRemaining = (TextView) findViewById(R.id.tvTimeRemaining);


        initializeAlertDialog();
    }

    private void initializeAlertDialog() {
        gridWinDialog = new MyAlertDialog(this);
        gridWinDialog.setText(getString(R.string.you_win)+ "\n" + getString(R.string.play_again));
        gridWinDialog.setCancelable(false);
        gridWinDialog.addButton(getString(R.string.yes), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame();
            }
        });
        gridWinDialog.addButton(getString(R.string.no), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        gridLossDialog = new MyAlertDialog(this);
        gridLossDialog.setText(getString(R.string.you_lose)+ "\n" + getString(R.string.play_again));
        gridLossDialog.setCancelable(false);
        gridLossDialog.addButton(getString(R.string.yes), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame();
            }
        });
        gridLossDialog.addButton(getString(R.string.no), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Initializes the instance members of this activity
     */
    private void initializeComponents() {
        initializeDifficulty();
        tileEventHandler = new TileEventHandler();
        netwalkGrid = new NetwalkGrid(gridColumns, gridRows);
    }

    private void initializeDifficulty() {
        gridColumns = 5;
        gridRows = 5;
        imageRatio = getIconSize(gridColumns);
    }

    private int getIconSize(int column){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width/(column+2);
    }

    private void initializeGridMap() {
        gridImageMap = new SparseIntArray();

        gridImageMap.put(5, R.drawable.idle_pipe_ns);
        gridImageMap.put(133, R.drawable.active_pipe_ns);

        gridImageMap.put(10, R.drawable.idle_pipe_ew);
        gridImageMap.put(138, R.drawable.active_pipe_ew);

        gridImageMap.put(6, R.drawable.idle_pipe_ne);
        gridImageMap.put(134, R.drawable.active_pipe_ne);

        gridImageMap.put(3, R.drawable.idle_pipe_es);
        gridImageMap.put(131, R.drawable.active_pipe_es);

        gridImageMap.put(9, R.drawable.idle_pipe_sw);
        gridImageMap.put(137, R.drawable.active_pipe_sw);

        gridImageMap.put(12, R.drawable.idle_pipe_wn);
        gridImageMap.put(140, R.drawable.active_pipe_wn);

        gridImageMap.put(14, R.drawable.idle_pipe_new);
        gridImageMap.put(142, R.drawable.active_pipe_new);

        gridImageMap.put(7, R.drawable.idle_pipe_nes);
        gridImageMap.put(135, R.drawable.active_pipe_nes);

        gridImageMap.put(11, R.drawable.idle_pipe_esw);
        gridImageMap.put(139, R.drawable.active_pipe_esw);

        gridImageMap.put(13, R.drawable.idle_pipe_nsw);
        gridImageMap.put(141, R.drawable.active_pipe_nsw);

        gridImageMap.put(36, R.drawable.idle_terminal_n);
        gridImageMap.put(164, R.drawable.active_terminal_n);

        gridImageMap.put(34, R.drawable.idle_terminal_e);
        gridImageMap.put(162, R.drawable.active_terminal_e);

        gridImageMap.put(33, R.drawable.idle_terminal_s);
        gridImageMap.put(161, R.drawable.active_terminal_s);

        gridImageMap.put(40, R.drawable.idle_terminal_w);
        gridImageMap.put(168, R.drawable.active_terminal_w);

        gridImageMap.put(68, R.drawable.idle_evil_terminal_n);
        gridImageMap.put(196, R.drawable.active_evil_terminal_n);

        gridImageMap.put(66, R.drawable.idle_evil_terminal_e);
        gridImageMap.put(194, R.drawable.active_evil_terminal_e);

        gridImageMap.put(65, R.drawable.idle_evil_terminal_s);
        gridImageMap.put(193, R.drawable.active_evil_terminal_s);

        gridImageMap.put(72, R.drawable.idle_evil_terminal_w);
        gridImageMap.put(200, R.drawable.active_evil_terminal_w);


        gridImageMap.put(148, R.drawable.server_n);
        gridImageMap.put(146, R.drawable.server_e);
        gridImageMap.put(145, R.drawable.server_s);
        gridImageMap.put(152, R.drawable.server_w);

        gridImageMap.put(150, R.drawable.server_ne);
        gridImageMap.put(147, R.drawable.server_es);
        gridImageMap.put(153, R.drawable.server_sw);
        gridImageMap.put(156, R.drawable.server_wn);
        gridImageMap.put(149, R.drawable.server_ns);
        gridImageMap.put(154, R.drawable.server_ew);

        gridImageMap.put(158, R.drawable.server_new);
        gridImageMap.put(151, R.drawable.server_nes);
        gridImageMap.put(155, R.drawable.server_esw);
        gridImageMap.put(157, R.drawable.server_swn);

        gridImageMap.put(159, R.drawable.server_nesw);
    }

    /**
     * Randomizes game grid
     */
    private void randomizeGrid() {
        Random rand = new Random();

        //This section of the loop is to randomly rotate each grid element a random amount of times
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridColumns; col++) {
                int randomRotateAmount = rand.nextInt(5) + 1;
                for (int i = 0; i < randomRotateAmount; i++) {
                    netwalkGrid.rotateRight(col, row);
                }
            }
        }

        //Looping through each grid element again and making sure that they're not connected
        //This loop in not inside the previous loop to ensure a certain degree of randomness
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridColumns; col++) {
                if (! netwalkGrid.isServer(col, row)) {
                    int maxAttempts = 0;
                    while (maxAttempts < 4 && netwalkGrid.isConnected(col, row)) {
                        maxAttempts++;
                        netwalkGrid.rotateRight(col, row);
                    }
                }
            }
        }
    }

    private void restartGame() {
        if(timer != null){
            timer.cancel();
        }

        startTimer(timeTimer);
        initializeComponents();
        randomizeGrid();
        displayNetwalkGrid();
        netwalkGrid.findVirus();
        gridWinDialog.dismissDialog();
        gridLossDialog.dismissDialog();
        initializeAlertDialog();
    }


    private Bitmap getImage(int imageId) {
        return BitmapFactory.decodeResource(getResources(), imageId);
    }

    /**
     * Displays each grid element to the screen
     */
    private void displayNetwalkGrid() {
        tlGridTable.removeAllViews();

        for (int row = 0; row < gridRows; row++) {
            TableRow tableRow = new TableRow(this);

            for (int col = 0; col < gridColumns; col++) {
                int element = netwalkGrid.getGridElem(col, row);

                int key = gridImageMap.get(element, -1);
                if (key != -1) {
                    Bitmap image = getImage(key);

                    NetwalkGridTile tile = new NetwalkGridTile(this, new NetwalkGridLocation(col, row));
                    Bitmap scaledImage = Bitmap.createScaledBitmap(image, imageRatio, imageRatio, true);
                    tile.setImageBitmap(scaledImage);
                    tile.setPadding(2, 2, 2, 2);
                    tile.setOnClickListener(tileEventHandler);
                    tableRow.addView(tile);
                }
            }

            tlGridTable.addView(tableRow);
        }
    }

    public void displayNetwalkGridIcon(int col, int row, Handler h) {
        int element = netwalkGrid.getGridElem(col, row);
        int key = gridImageMap.get(element, -1);
        if (key != -1) {
            Bitmap image = getImage(key);

            Bitmap scaledImage = Bitmap.createScaledBitmap(image, imageRatio, imageRatio, true);
            ViewGroup gridChild = (ViewGroup) tlGridTable.getChildAt(row);
            NetwalkGridTile tile = (NetwalkGridTile) gridChild.getChildAt(col);
            h.post(new Runnable() {
                @Override
                public void run() {
                    tile.setImageBitmap(scaledImage);
                    tile.setPadding(1, 1, 1, 1);
                    tile.setOnClickListener(tileEventHandler);
                    tile.setClickable(false);
                }
            });
        }
    }

    private void updateTimerDisplay(long time) {
        long minutes = (time % 3600) / 60;
        long seconds = time % 60;

        String timeElapsed;
        if (seconds <10) timeElapsed = String.format(Locale.getDefault(), "%d:0%d", minutes, seconds);
        else timeElapsed = String.format(Locale.getDefault(), "%d:%d", minutes, seconds);
        
        tvTimeRemaining.setText(timeElapsed);
    }

    @Override
    protected void onResume() {
        mTools.playMusic();
        super.onResume();
    }


    @Override
    protected void onPause() {
        mTools.stopMusic();
        gridLossDialog.dismissDialog();
        gridWinDialog.dismissDialog();
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(timer != null) {
            timer.cancel();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("netwalk_grid", netwalkGrid);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        netwalkGrid = savedInstanceState.getParcelable("netwalk_grid");
        displayNetwalkGrid();
    }



    /**
     * A class that handles the event handlers for when a image tile is clicked
     */
    private class TileEventHandler implements View.OnClickListener {
        public void onClick(View view) {
            NetwalkGridTile clickedTile = (NetwalkGridTile) view;
            clickedTile.setClickable(false);
            NetwalkGridLocation tileLocation = clickedTile.getGridLocation();
            Handler h = new Handler();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Integer> iconPosArray = netwalkGrid.rotateRight(tileLocation.getColumn(), tileLocation.getRow());

                    if (netwalkGrid.isVirusConnected()) {
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                timer.cancel();
                                gridLossDialog.showDialog();
                            }
                        });
                    }

                    if (netwalkGrid.isGridSolved()) {
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                timer.cancel();
                                gridWinDialog.showDialog();
                            }
                        });
                    }

                    for (int gridPos : iconPosArray) {
                        int x = gridPos % gridColumns;
                        int y = gridPos / gridColumns;
                        displayNetwalkGridIcon(x, y, h);
                    }

                    for (int gridPos : iconPosArray) {
                        ViewGroup gridChild = (ViewGroup) tlGridTable.getChildAt(gridPos / gridColumns);
                        NetwalkGridTile tile = (NetwalkGridTile) gridChild.getChildAt(gridPos % gridColumns);
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                tile.setClickable(true);
                            }
                        });
                    }
                }
            });
            t.start();
        }
    }


    private void startTimer(long time){
        timer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimerDisplay(millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {
                if (gridWinDialog != null || gridLossDialog != null) {
                    gridLossDialog.showDialog();
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
    }
}