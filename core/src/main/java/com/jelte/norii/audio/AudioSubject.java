package com.jelte.norii.audio;

public interface AudioSubject {
    public void addAudioObserver(AudioObserver audioObserver);
    public void removeAudioObserver(AudioObserver audioObserver);
    public void removeAllAudioObservers();
    public void notifyAudio(final AudioObserver.AudioCommand command, AudioObserver.AudioTypeEvent event);
}

