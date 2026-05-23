package audio;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {

    public static int MENU_1 = 0;
    public static int LEVEL_1 = 1;
    public static int LEVEL_2 = 2;
    public static int LEVEL_3 = 3;
    public static int BOSS_1 = 4;
    public static int BOSS_2 = 5;
    public static int BOSS_3 = 6;
    public static int CREDITS_SONG = 7;

    public static int DIE = 0;
    public static int JUMP = 1;
    public static int GAMEOVER = 2;
    public static int ATTACK_ONE = 3;
    public static int ATTACK_TWO = 4;
    public static int ATTACK_THREE = 5;

    private Clip[] songs, effects;
    private int currentSongId;
    private int pausedSongId = -1;
    private long pausedSongPosition;
    private float volume = 1f;
    private boolean songMute, effectMute;
    private Random rand = new Random();

    public AudioPlayer() {
        loadSongs();
        loadEffects();
        playSong(MENU_1);
    }

    private void loadSongs() {
        String[] names = {"menu", "level1", "level2", "level3", "boss1", "boss2", "boss3", "credits"};
        songs = new Clip[names.length];
        for (int i = 0; i < songs.length; i++)
            songs[i] = getClip(names[i]);
    }

    private void loadEffects() {
        String[] effectNames = { "die", "jump", "gameover", "attack1", "attack2", "attack3" };
        effects = new Clip[effectNames.length];
        for (int i = 0; i < effects.length; i++)
            effects[i] = getClip(effectNames[i]);

        updateEffectsVolume();

    }

    private Clip getClip(String name) {
        URL url = getClass().getResource("/audio/" + name + ".wav");
        AudioInputStream audio;

        if (url == null) {
            System.err.println("Could not find file: /audio/" + name + ".wav");
            return null;
        }

        try {
            audio = AudioSystem.getAudioInputStream(url);
            Clip c = AudioSystem.getClip();
            c.open(audio);
            return c;

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | IllegalArgumentException e) {
            System.err.println("Error loading audio file: " + name + " (" + e.getMessage() + ")");
        }

        return null;

    }

    public void setVolume(float volume) {
        this.volume = volume;
        updateSongVolume();
        updateEffectsVolume();
    }

    public void stopSong() {
        if (!isValidClip(songs, currentSongId))
            return;
        if (songs[currentSongId].isActive())
            songs[currentSongId].stop();
    }

    public void pauseSongForCutscene() {
        if (pausedSongId != -1)
            return;
        if (!isValidClip(songs, currentSongId))
            return;

        pausedSongId = currentSongId;
        pausedSongPosition = songs[currentSongId].getMicrosecondPosition();
        songs[currentSongId].stop();
    }

    public void resumeSongAfterCutscene() {
        if (pausedSongId == -1)
            return;

        currentSongId = pausedSongId;
        pausedSongId = -1;
        updateSongVolume();
        if (!isValidClip(songs, currentSongId))
            return;
        songs[currentSongId].setMicrosecondPosition(pausedSongPosition);
        songs[currentSongId].loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void setLevelSong(int lvlIndex) {
        switch (lvlIndex) {
            case 0, 1 -> playSong(LEVEL_1);
            case 2 -> playSong(BOSS_1);
            case 3 -> playSong(LEVEL_2);
            case 4 -> playSong(BOSS_2);
            case 5 -> playSong(LEVEL_3);
            case 6 -> playSong(BOSS_3);
            default -> playSong(MENU_1);
        }
    }

    public void playCreditsSong() {
        playSong(CREDITS_SONG);
    }

    public void playAttackSound() {
        int start = ATTACK_ONE;
        start += rand.nextInt(3);
        playEffect(start);
    }

    public void playEffect(int effect) {
        if (!isValidClip(effects, effect))
            return;
        if (effects[effect].getMicrosecondPosition() > 0)
            effects[effect].setMicrosecondPosition(0);
        effects[effect].start();
    }

    public void playSong(int song) {
        stopSong();
        pausedSongId = -1;

        currentSongId = song;
        updateSongVolume();
        if (!isValidClip(songs, currentSongId))
            return;
        songs[currentSongId].setMicrosecondPosition(0);
        songs[currentSongId].loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void toggleSongMute() {
        this.songMute = !songMute;
        for (Clip c : songs) {
            if (c == null || !c.isControlSupported(BooleanControl.Type.MUTE))
                continue;
            BooleanControl booleanControl = (BooleanControl) c.getControl(BooleanControl.Type.MUTE);
            booleanControl.setValue(songMute);
        }
    }

    public void toggleEffectMute() {
        this.effectMute = !effectMute;
        for (Clip c : effects) {
            if (c == null || !c.isControlSupported(BooleanControl.Type.MUTE))
                continue;
            BooleanControl booleanControl = (BooleanControl) c.getControl(BooleanControl.Type.MUTE);
            booleanControl.setValue(effectMute);
        }
        if (!effectMute)
            playEffect(JUMP);
    }

    private void updateSongVolume() {
        if (!isValidClip(songs, currentSongId) ||
                !songs[currentSongId].isControlSupported(FloatControl.Type.MASTER_GAIN))
            return;

        FloatControl gainControl = (FloatControl) songs[currentSongId].getControl(FloatControl.Type.MASTER_GAIN);
        float range = gainControl.getMaximum() - gainControl.getMinimum();
        float gain = (range * volume) + gainControl.getMinimum();
        gainControl.setValue(gain);

    }

    private void updateEffectsVolume() {
        for (Clip c : effects) {
            if (c == null || !c.isControlSupported(FloatControl.Type.MASTER_GAIN))
                continue;
            FloatControl gainControl = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
            float range = gainControl.getMaximum() - gainControl.getMinimum();
            float gain = (range * volume) + gainControl.getMinimum();
            gainControl.setValue(gain);
        }
    }

    private boolean isValidClip(Clip[] clips, int index) {
        return clips != null && index >= 0 && index < clips.length && clips[index] != null;
    }

}
