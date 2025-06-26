package dev.dannytaylor.soundsign.entity;

public interface MusicalEntity {
	int soundsign$getMaxDelayFront();
	void soundsign$setMaxDelayFront(int value);
	int soundsign$getMaxDelayBack();
	void soundsign$setMaxDelayBack(int value);
	void soundsign$setDelayFront(int value);
	int soundsign$getDelayFront();
	void soundsign$setDelayBack(int value);
	int soundsign$getDelayBack();
	void soundsign$setShouldUpdateDelay(boolean value);
	boolean soundsign$getShouldUpdateDelay();
	void soundsign$setReset(boolean value);
	boolean soundsign$getReset();
}
