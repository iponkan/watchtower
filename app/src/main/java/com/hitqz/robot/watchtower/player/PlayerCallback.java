package com.hitqz.robot.watchtower.player;

public interface PlayerCallback {
    void onPlayStart();

    void onPlayPause();

    void onPlayStop();

    void onPlaying(int progress);
}
