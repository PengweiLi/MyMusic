package com.lipengwei.music;


interface IMusic {
    String getFileList(int postion);
    void play(int current);
    int getDuration();
    int getTime();
    void seekBarTo(int progress);
    int getCurrentItem();
    void next();
    void last();
    void pause();
    boolean isPlaying();
    void deleteItem(int postion);
    String getMode();
    void setMode(String mode);
    int getSongId();
    int getAlbumId();
}