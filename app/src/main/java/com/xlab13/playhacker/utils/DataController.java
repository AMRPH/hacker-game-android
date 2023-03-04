package com.xlab13.playhacker.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.GetCarsQuery;
import com.example.GetGirlItemsQuery;
import com.example.GetHardwareQuery;
import com.example.GetHealthQuery;
import com.example.GetHousingQuery;
import com.example.GetJobsQuery;
import com.example.GetLongtermJobsQuery;
import com.example.GetNewsQuery;
import com.example.GetSoftwareQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataController {
    private static DataController mDataController;
    private static MutableLiveData<String> jobLongTimer;

    private static List<GetHealthQuery.GetHealth> healthItems;
    private static List<GetHealthQuery.GetHealth> alcoItems;
    private static List<GetHealthQuery.GetHealth> moodItems;

    private static List<GetJobsQuery.GetJob> jobItems;
    private static List<GameItem> gameItems;
    private static List<GetLongtermJobsQuery.GetLongtermJob> jobLongtermItems;

    private static List<GetHardwareQuery.GetHardware> hardwareItems;
    private static List<GetSoftwareQuery.GetSoftware> softwareItems;
    //TODO
    //private static List<> perksItems;

    //statistic
    private static List<GetNewsQuery.GetNew> newItems;
    //TODO
    //private static List<> partnersItems;

    private static List<GetHousingQuery.GetHousing> houseItems;
    private static List<GetCarsQuery.GetCar> carItems;
    private static List<GetGirlItemsQuery.GetGirlItem> girlItems;


    public static DataController getDataController(){
        if (mDataController == null) {
            mDataController = new DataController();

            jobLongTimer = new MutableLiveData<>();

            healthItems = new ArrayList<>();
            alcoItems = new ArrayList<>();
            moodItems = new ArrayList<>();

            jobItems = new ArrayList<>();
            gameItems = new ArrayList<>();
            jobLongtermItems = new ArrayList<>();

            hardwareItems = new ArrayList<>();
            softwareItems = new ArrayList<>();
            //perksItems = new ArrayList<>();

            //statistic
            newItems = new ArrayList<>();
            //partnersItems = new ArrayList<>();

            houseItems = new ArrayList<>();
            carItems = new ArrayList<>();
            girlItems = new ArrayList<>();
        }
        return mDataController;
    }

    private DataController(){
    }

    public List<GetHealthQuery.GetHealth> getHealthItems() {
        return healthItems;
    }

    public void setHealthItems(List<GetHealthQuery.GetHealth> items) {
        healthItems = items;
    }

    public List<GetHealthQuery.GetHealth> getAlcoItems() {
        return alcoItems;
    }

    public void setAlcoItems(List<GetHealthQuery.GetHealth> items) {
        alcoItems = items;
    }

    public List<GetHealthQuery.GetHealth> getMoodItems() {
        return moodItems;
    }

    public void setMoodItems(List<GetHealthQuery.GetHealth> items) {
        moodItems = items;
    }

    public List<GetJobsQuery.GetJob> getJobItems() {
        return jobItems;
    }

    public void setJobItems(List<GetJobsQuery.GetJob> items) {
        jobItems = DataController.sortByLevel(items);
    }

    public List<GameItem> getGameItems() {
        return gameItems;
    }

    public void setGameItems(List<GameItem> items) {
        gameItems = items;
    }

    public List<GetLongtermJobsQuery.GetLongtermJob> getJobLongtermItems() {
        return jobLongtermItems;
    }

    public void setJobLongtermItems(List<GetLongtermJobsQuery.GetLongtermJob> items) {
        jobLongtermItems = items;
    }

    public List<GetHardwareQuery.GetHardware> getHardwareItems() {
        return hardwareItems;
    }

    public void setHardwareItems(List<GetHardwareQuery.GetHardware> items) {
        hardwareItems = items;
    }

    public List<GetSoftwareQuery.GetSoftware> getSoftwareItems() {
        return softwareItems;
    }

    public void setSoftwareItems(List<GetSoftwareQuery.GetSoftware> items) {
        softwareItems = items;
    }

    public List<GetNewsQuery.GetNew> getNewItems() {
        return newItems;
    }

    public void setNewItems(List<GetNewsQuery.GetNew> items) {
        newItems = items;
    }

    public List<GetHousingQuery.GetHousing> getHouseItems() {
        return houseItems;
    }

    public void setHouseItems(List<GetHousingQuery.GetHousing> items) {
        houseItems = items;
    }

    public List<GetCarsQuery.GetCar> getCarItems() {
        return carItems;
    }

    public void setCarItems(List<GetCarsQuery.GetCar> items) {
        carItems = items;
    }

    public List<GetGirlItemsQuery.GetGirlItem> getGirlItems() {
        return girlItems;
    }

    public void setGirlItems(List<GetGirlItemsQuery.GetGirlItem> items) {
        girlItems = items;
    }

    public void setJobTimer(String str){
        jobLongTimer.setValue(str);
    }

    public LiveData getJobTimer() {
        return jobLongTimer;
    }


    private static List<GetJobsQuery.GetJob> sortByLevel(List<GetJobsQuery.GetJob> list){
        Collections.sort(new ArrayList(list), new Comparator<GetJobsQuery.GetJob>() {
            @Override
            public int compare(GetJobsQuery.GetJob o1, GetJobsQuery.GetJob o2) {
                Integer p1 = o1.user_level();
                Integer p2 = o2.user_level();
                return Integer.compare(p1, p2);
            }
        });
        return list;
    }
}