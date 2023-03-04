package com.xlab13.playhacker.games.game2048;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.MoveMutation;
import com.example.type.Direction;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.alerts.MyAlertDialog;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class View2048 extends View {
    Context context;

    private Grid2048 grid = null;
    private AnimationGrid2048 aGrid;

    private Integer score;

    private static final int SPAWN_ANIMATION = -1;
    private static final int MOVE_ANIMATION = 0;
    private static final int MERGE_ANIMATION = 1;

    private static final long MOVE_ANIMATION_TIME = View2048.BASE_ANIMATION_TIME;
    private static final long SPAWN_ANIMATION_TIME = View2048.BASE_ANIMATION_TIME;

    private final int numSquaresX = 4;
    private final int numSquaresY = 4;

    //Internal Constants
    static final int BASE_ANIMATION_TIME = 100000000;
    private static final String TAG = View2048.class.getSimpleName();
    private static final float MERGING_ACCELERATION = (float) -0.5;
    private static final float INITIAL_VELOCITY = (1 - MERGING_ACCELERATION) / 4;
    public final int numCellTypes = 21;
    private final BitmapDrawable[] bitmapCell = new BitmapDrawable[numCellTypes];
    //Internal variables
    private final Paint paint = new Paint();
    public boolean hasSaveState = false;
    public boolean continueButtonEnabled = false;
    public int startingX;
    public int startingY;
    public int endingX;
    public int endingY;
    //Icon variables
    public int sYIcons;
    public int sXUndo;
    public int iconSize;
    //Misc
    boolean refreshLastTime = true;
    boolean showHelp;
    //Timing
    private long lastFPSTime = System.nanoTime();
    //Text
    private float titleTextSize;
    private float bodyTextSize;
    private float headerTextSize;
    private float instructionsTextSize;
    private float gameOverTextSize;
    //Layout variables
    private int cellSize = 0;
    private float textSize = 0;
    private float cellTextSize = 0;
    private int gridWidth = 0;
    private int textPaddingSize;
    private int iconPaddingSize;
    //Assets
    private Drawable backgroundRectangle;
    private Drawable fadeRectangle;
    private Bitmap background = null;
    private BitmapDrawable loseGameOverlay;
    private BitmapDrawable winGameContinueOverlay;
    private BitmapDrawable winGameFinalOverlay;
    //Text variables
    private int sYAll;
    private int titleStartYAll;
    private int bodyStartYAll;
    private int eYAll;
    private int titleWidthScore;

    public View2048(Context context) {
        super(context);
        this.context = context;

        Resources resources = context.getResources();
        //Loading resources
        try {
            //Getting assets
            backgroundRectangle = resources.getDrawable(R.drawable.background_rectangle_2048);
            this.setBackgroundColor(resources.getColor(R.color.background2048));
            Typeface font = Typeface.createFromAsset(resources.getAssets(), "retro.ttf");
            paint.setTypeface(font);
            paint.setAntiAlias(true);
        } catch (Exception e) {
            Log.e(TAG, "Error getting assets?", e);
        }

        setOnTouchListener(new InputListener2048(this));
    }

    public void newGame(List<List<Integer>> serverTiles, Integer score, String status){
        if (grid == null) {
            grid = new Grid2048(numSquaresX, numSquaresY);
        } else {
            grid.clearGrid();
        }
        aGrid = new AnimationGrid2048(numSquaresX, numSquaresY);

        this.score = score;
        addTiles(serverTiles);

        resyncTime();
        invalidate();
    }

    public void addTiles(List<List<Integer>> serverTiles) {
        for (int y = 0; y < numSquaresY; y++){
            for (int x = 0; x < numSquaresX; x++){
                int value = serverTiles.get(y).get(x);

                if (value > 0){
                    Tile2048 tile = new Tile2048(x, y, value);

                    grid.insertTile(tile);
                    aGrid.startAnimation(tile.getX(), tile.getY(), SPAWN_ANIMATION,
                            SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null); //Direction: -1 = EXPANDING
                }
            }
        }
    }

    public void setTiles(List<List<Integer>> serverTiles) {
        for (int y = 0; y < numSquaresY; y++){
            for (int x = 0; x < numSquaresX; x++){
                int value = serverTiles.get(y).get(x);

                if (value > 0){
                    Tile2048 tile = new Tile2048(x, y, value);

                    grid.insertTile(tile);
                    if (grid.undoField[x][y] == null){
                        aGrid.startAnimation(tile.getX(), tile.getY(), SPAWN_ANIMATION,
                                SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null);

                    } else if (grid.undoField[x][y].getValue() != grid.field[x][y].getValue()){
                        aGrid.startAnimation(tile.getX(), tile.getY(), SPAWN_ANIMATION,
                                SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null);
                    }
                }
            }
        }
    }

    public void updateData(List<List<Integer>> serverTiles, Integer score){
        this.score = score;
        grid.saveTiles();
        grid.clearGrid();
        setTiles(serverTiles);

        resyncTime();
        invalidate();
    }

    public void move(Direction direction){
        MoveMutation.Builder builder = MoveMutation.builder().sessionId(((Activity2048) context).sessionId).direction(direction);

        ((Activity2048) context).client.mutate(builder.build()).enqueue(new ApolloCall.Callback<MoveMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<MoveMutation.Data> response) {
                ((Activity2048) context).loadDialog.dismissDialog();
                if (!response.hasErrors()){
                    ((Activity2048) context).runOnUiThread(() -> {
                        MoveMutation.Move game = response.getData().move();

                        if (game.status().equals("Game Over")){
                            endGame(game.win(), game.money_rub());
                        } else {
                            updateData(game.field(), game.scores());
                        }
                    });

                } else {
                    ((Activity2048) context).runOnUiThread(() -> {
                        String error = response.getErrors().get(0).getMessage();

                        mTools.showErrorDialog(context, error);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    public void openMenu() {
        MyAlertDialog gameDialog = new MyAlertDialog(context);
        gameDialog.setCancelable(false);
        gameDialog.setText("Вы действительно хотите закончить игру?");
        gameDialog.addButton(context.getString(R.string.yes), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endGame(false, score/4);
                gameDialog.dismissDialog();
            }
        });

        gameDialog.addButton(context.getString(R.string.no), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameDialog.dismissDialog();
            }
        });
        gameDialog.showDialog();
    }

    public void endGame(boolean isWin, int bonus){
        String message;
        if (isWin){ message = "Вы победили. Вы получили " + bonus + " RUB.\n Начать заново?";
        } else message = "Вы Проиграли. Вы получили " + bonus + " RUB.\n Начать заново?";

        MyAlertDialog gameDialog = new MyAlertDialog(context);
        gameDialog.setCancelable(false);
        gameDialog.setText(message);
        gameDialog.addButton(context.getString(R.string.yes), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameDialog.dismissDialog();
                ((Activity2048)context).newGame();
            }
        });

        gameDialog.addButton(context.getString(R.string.no), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameDialog.dismissDialog();
                ((Activity)context).finish();
            }
        });
        gameDialog.showDialog();
    }

    private static int log2(int n) {
        if (n <= 0) throw new IllegalArgumentException();
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    @Override
    public void onDraw(Canvas canvas) {
        //Reset the transparency of the screen

        canvas.drawBitmap(background, 0, 0, paint);

        drawScoreText(canvas);


        drawCells(canvas);

        if (false) {
            drawEndGameState(canvas);
        }

        //Refresh the screen if there is still an animation running
        if (aGrid.isAnimationActive()) {
            invalidate(startingX, startingY, endingX, endingY);
            tick();
            //Refresh one last time on game end.
        } else if (false && refreshLastTime) {
            invalidate();
            refreshLastTime = false;
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldW, int oldH) {
        super.onSizeChanged(width, height, oldW, oldH);
        getLayout(width, height);
        createBitmapCells();
        createBackgroundBitmap(width, height);
    }

    private void drawDrawable(Canvas canvas, Drawable draw, int startingX, int startingY, int endingX, int endingY) {
        draw.setBounds(startingX, startingY, endingX, endingY);
        draw.draw(canvas);
    }

    private void drawCellText(Canvas canvas, int value) {
        int textShiftY = centerText();
        if (value >= 8) {
            paint.setColor(getResources().getColor(R.color.textWhite2048));
        } else {
            paint.setColor(getResources().getColor(R.color.textBlack2048));
        }
        canvas.drawText("" + value, cellSize / 2, cellSize / 2 - textShiftY, paint);
    }

    private void drawScoreText(Canvas canvas) {
        //Drawing the score text: Ver 2
        paint.setTextSize(bodyTextSize);
        paint.setTextAlign(Paint.Align.CENTER);

        int bodyWidthScore = (int) (paint.measureText(String.valueOf(score)));
        int textWidthScore = Math.max(titleWidthScore, bodyWidthScore) + textPaddingSize * 2;
        int textMiddleScore = textWidthScore / 2;

        int eXScore = endingX;
        int sXScore = eXScore - textWidthScore;

        //Outputting scores box
        backgroundRectangle.setBounds(sXScore, sYAll, eXScore, eYAll);
        backgroundRectangle.draw(canvas);
        paint.setTextSize(titleTextSize);
        paint.setColor(getResources().getColor(R.color.textBrown2048));
        canvas.drawText(getResources().getString(R.string.score), sXScore + textMiddleScore, titleStartYAll, paint);
        paint.setTextSize(bodyTextSize);
        paint.setColor(getResources().getColor(R.color.textWhite2048));
        canvas.drawText(String.valueOf(score), sXScore + textMiddleScore, bodyStartYAll, paint);
    }



    private void drawUndoButton(Canvas canvas) {
        drawDrawable(canvas,
                backgroundRectangle,
                sXUndo,
                sYIcons, sXUndo + iconSize,
                sYIcons + iconSize
        );

        drawDrawable(canvas,
                getResources().getDrawable(R.drawable.icon_undo_2048),
                sXUndo + iconPaddingSize,
                sYIcons + iconPaddingSize,
                sXUndo + iconSize - iconPaddingSize,
                sYIcons + iconSize - iconPaddingSize
        );
    }

    private void drawHeader(Canvas canvas) {
        paint.setTextSize(headerTextSize);
        paint.setColor(getResources().getColor(R.color.textBlack2048));
        paint.setTextAlign(Paint.Align.LEFT);
        int textShiftY = centerText() * 2;
        int headerStartY = sYAll - textShiftY;
        canvas.drawText(getResources().getString(R.string.header), startingX, headerStartY, paint);
    }


    private void drawBackground(Canvas canvas) {
        drawDrawable(canvas, backgroundRectangle, startingX, startingY, endingX, endingY);
    }

    //Renders the set of 16 background squares.
    private void drawBackgroundGrid(Canvas canvas) {
        Resources resources = getResources();
        Drawable backgroundCell = resources.getDrawable(R.drawable.cell_rectangle);
        // Outputting the game grid
        for (int xx = 0; xx < numSquaresX; xx++) {
            for (int yy = 0; yy < numSquaresY; yy++) {
                int sX = startingX + gridWidth + (cellSize + gridWidth) * xx;
                int eX = sX + cellSize;
                int sY = startingY + gridWidth + (cellSize + gridWidth) * yy;
                int eY = sY + cellSize;

                drawDrawable(canvas, backgroundCell, sX, sY, eX, eY);
            }
        }
    }

    private void drawCells(Canvas canvas) {
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        // Outputting the individual cells
        for (int xx = 0; xx < numSquaresX; xx++) {
            for (int yy = 0; yy < numSquaresY; yy++) {
                int sX = startingX + gridWidth + (cellSize + gridWidth) * xx;
                int eX = sX + cellSize;
                int sY = startingY + gridWidth + (cellSize + gridWidth) * yy;
                int eY = sY + cellSize;

                Tile2048 currentTile = grid.getCellContent(xx, yy);
                if (currentTile != null) {
                    int value = currentTile.getValue();
                    int index = log2(value);

                    //Check for any active animations
                    ArrayList<AnimationCell2048> aArray = aGrid.getAnimationCell(xx, yy);
                    boolean animated = false;
                    for (int i = aArray.size() - 1; i >= 0; i--) {
                        AnimationCell2048 aCell = aArray.get(i);
                        //If this animation is not active, skip it
                        if (aCell.getAnimationType() == SPAWN_ANIMATION) {
                            animated = true;
                        }
                        if (!aCell.isActive()) {
                            continue;
                        }

                        if (aCell.getAnimationType() == SPAWN_ANIMATION) { // Spawning animation
                            double percentDone = aCell.getPercentageDone();
                            float textScaleSize = (float) (percentDone);
                            paint.setTextSize(textSize * textScaleSize);

                            float cellScaleSize = cellSize / 2 * (1 - textScaleSize);
                            bitmapCell[index].setBounds((int) (sX + cellScaleSize), (int) (sY + cellScaleSize), (int) (eX - cellScaleSize), (int) (eY - cellScaleSize));
                            bitmapCell[index].draw(canvas);
                        } else if (aCell.getAnimationType() == MERGE_ANIMATION) { // Merging Animation
                            double percentDone = aCell.getPercentageDone();
                            float textScaleSize = (float) (1 + INITIAL_VELOCITY * percentDone
                                    + MERGING_ACCELERATION * percentDone * percentDone / 2);
                            paint.setTextSize(textSize * textScaleSize);

                            float cellScaleSize = cellSize / 2 * (1 - textScaleSize);
                            bitmapCell[index].setBounds((int) (sX + cellScaleSize), (int) (sY + cellScaleSize), (int) (eX - cellScaleSize), (int) (eY - cellScaleSize));
                            bitmapCell[index].draw(canvas);
                        } else if (aCell.getAnimationType() == MOVE_ANIMATION) {  // Moving animation
                            double percentDone = aCell.getPercentageDone();
                            int tempIndex = index;
                            if (aArray.size() >= 2) {
                                tempIndex = tempIndex - 1;
                            }
                            int previousX = aCell.extras[0];
                            int previousY = aCell.extras[1];
                            int currentX = currentTile.getX();
                            int currentY = currentTile.getY();
                            int dX = (int) ((currentX - previousX) * (cellSize + gridWidth) * (percentDone - 1) * 1.0);
                            int dY = (int) ((currentY - previousY) * (cellSize + gridWidth) * (percentDone - 1) * 1.0);
                            bitmapCell[tempIndex].setBounds(sX + dX, sY + dY, eX + dX, eY + dY);
                            bitmapCell[tempIndex].draw(canvas);
                        }
                        animated = true;
                    }

                    //No active animations? Just draw the cell
                    if (!animated) {
                        bitmapCell[index].setBounds(sX, sY, eX, eY);
                        bitmapCell[index].draw(canvas);
                    }
                }
            }
        }
    }

    private void drawEndGameState(Canvas canvas) {
        double alphaChange = 1;
        continueButtonEnabled = false;
        for (AnimationCell2048 animation : aGrid.globalAnimation) {
            if (animation.getAnimationType() == 0) {
                alphaChange = animation.getPercentageDone();
            }
        }
        BitmapDrawable displayOverlay = null;
        if (false) {
            displayOverlay = winGameFinalOverlay;
        } else if (false) {
            displayOverlay = loseGameOverlay;
        }

        if (displayOverlay != null) {
            displayOverlay.setBounds(startingX, startingY, endingX, endingY);
            displayOverlay.setAlpha((int) (255 * alphaChange));
            displayOverlay.draw(canvas);
        }
    }

    private void createBackgroundBitmap(int width, int height) {
        background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        drawHeader(canvas);
        drawUndoButton(canvas);
        drawBackground(canvas);
        drawBackgroundGrid(canvas);
    }

    private void createBitmapCells() {
        Resources resources = getResources();
        int[] cellRectangleIds = getCellRectangleIds();
        paint.setTextAlign(Paint.Align.CENTER);
        for (int xx = 1; xx < bitmapCell.length; xx++) {
            int value = (int) Math.pow(2, xx);
            paint.setTextSize(cellTextSize);
            float tempTextSize = cellTextSize * cellSize * 0.9f / Math.max(cellSize * 0.9f, paint.measureText(String.valueOf(value)));
            paint.setTextSize(tempTextSize);
            Bitmap bitmap = Bitmap.createBitmap(cellSize, cellSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawDrawable(canvas, resources.getDrawable(cellRectangleIds[xx]), 0, 0, cellSize, cellSize);
            drawCellText(canvas, value);
            bitmapCell[xx] = new BitmapDrawable(resources, bitmap);
        }
    }

    private int[] getCellRectangleIds() {
        int[] cellRectangleIds = new int[numCellTypes];

        cellRectangleIds[0] = R.drawable.cell_rectangle_2048;
        cellRectangleIds[1] = R.drawable.cell_rectangle_2;
        cellRectangleIds[2] = R.drawable.cell_rectangle_4;
        cellRectangleIds[3] = R.drawable.cell_rectangle_8;
        cellRectangleIds[4] = R.drawable.cell_rectangle_16;
        cellRectangleIds[5] = R.drawable.cell_rectangle_32;
        cellRectangleIds[6] = R.drawable.cell_rectangle_64;
        cellRectangleIds[7] = R.drawable.cell_rectangle_128;
        cellRectangleIds[8] = R.drawable.cell_rectangle_256;
        cellRectangleIds[9] = R.drawable.cell_rectangle_512;
        cellRectangleIds[10] = R.drawable.cell_rectangle_1024;
        cellRectangleIds[11] = R.drawable.cell_rectangle_2048;
        for (int xx = 12; xx < cellRectangleIds.length; xx++) {
            cellRectangleIds[xx] = R.drawable.cell_rectangle_4096;
        }
        return cellRectangleIds;
    }

    private void tick() {
        long currentTime = System.nanoTime();
        aGrid.tickAll(currentTime - lastFPSTime);
        lastFPSTime = currentTime;
    }

    public void resyncTime() {
        lastFPSTime = System.nanoTime();
    }

    private void getLayout(int width, int height) {
        cellSize = Math.min(width / (numSquaresX + 1), height / (numSquaresY + 3));
        gridWidth = cellSize / 7;
        int screenMiddleX = width / 2;
        int screenMiddleY = height / 2;
        int boardMiddleY = screenMiddleY + cellSize / 2;
        iconSize = cellSize / 2;

        //Grid Dimensions
        double halfNumSquaresX = numSquaresX / 2d;
        double halfNumSquaresY = numSquaresY / 2d;
        startingX = (int) (screenMiddleX - (cellSize + gridWidth) * halfNumSquaresX - gridWidth / 2);
        endingX = (int) (screenMiddleX + (cellSize + gridWidth) * halfNumSquaresX + gridWidth / 2);
        startingY = (int) (boardMiddleY - (cellSize + gridWidth) * halfNumSquaresY - gridWidth / 2);
        endingY = (int) (boardMiddleY + (cellSize + gridWidth) * halfNumSquaresY + gridWidth / 2);

        float widthWithPadding = endingX - startingX;

        // Text Dimensions
        paint.setTextSize(cellSize);
        textSize = cellSize * cellSize / Math.max(cellSize, paint.measureText("0000"));

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(1000);
        gameOverTextSize = Math.min(
            Math.min(
                1000f * ((widthWithPadding - gridWidth * 2) / (paint.measureText(getResources().getString(R.string.game_over)))),
                textSize * 2
            ),
            1000f * ((widthWithPadding - gridWidth * 2) / (paint.measureText(getResources().getString(R.string.you_win))))
        );

        paint.setTextSize(cellSize);
        cellTextSize = textSize;
        titleTextSize = textSize / 3;
        bodyTextSize = (int) (textSize / 1.5);
        headerTextSize = textSize * 2;
        textPaddingSize = (int) (textSize / 3);
        iconPaddingSize = (int) (textSize / 5);

        paint.setTextSize(titleTextSize);

        int textShiftYAll = centerText();
        //static variables
        sYAll = (int) (startingY - cellSize * 1.5);
        titleStartYAll = (int) (sYAll + textPaddingSize + titleTextSize / 2 - textShiftYAll);
        bodyStartYAll = (int) (titleStartYAll + textPaddingSize + titleTextSize / 2 + bodyTextSize / 2);

        titleWidthScore = (int) (paint.measureText(getResources().getString(R.string.score)));
        paint.setTextSize(bodyTextSize);
        textShiftYAll = centerText();
        eYAll = (int) (bodyStartYAll + textShiftYAll + bodyTextSize / 2 + textPaddingSize);

        sYIcons = (startingY + eYAll) / 2 - iconSize / 2;
        sXUndo = (endingX - iconSize);
        resyncTime();
    }

    private int centerText() {
        return (int) ((paint.descent() + paint.ascent()) / 2);
    }

}