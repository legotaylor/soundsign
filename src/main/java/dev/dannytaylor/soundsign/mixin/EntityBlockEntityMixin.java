package dev.dannytaylor.soundsign.mixin;

import dev.dannytaylor.soundsign.entity.MusicalEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(priority = 100, value = SignBlockEntity.class)
public abstract class EntityBlockEntityMixin implements MusicalEntity {
	@Unique
	private boolean soundsign$wasPowered;
	@Override
	public void soundsign$setWasPowered(boolean value) {
		this.soundsign$wasPowered = value;
	}
	@Override
	public boolean soundsign$getWasPowered() {
		return this.soundsign$wasPowered;
	}
	@Inject(method = "tick", at = @At("TAIL"))
	private static void soundsign$tick(World world, BlockPos pos, BlockState state, SignBlockEntity sign, CallbackInfo ci) {
		if (sign.getEditor() == null) {
			if (world.getBlockState(pos).isIn(BlockTags.WALL_SIGNS)) {
				BlockState attachedState = world.getBlockState(pos.offset(state.get(WallSignBlock.FACING).getOpposite()));
				if (attachedState.isOf(Blocks.NOTE_BLOCK)) {
					if (attachedState.get(NoteBlock.POWERED)) {
						soundsign$processSound(true, world, sign);
						soundsign$processSound(false, world, sign);
						if (!((MusicalEntity)sign).soundsign$getWasPowered()) {
							((MusicalEntity)sign).soundsign$setWasPowered(true);
						}
					} else {
						if (((MusicalEntity)sign).soundsign$getWasPowered()) {
							((MusicalEntity)sign).soundsign$setReset(true);
							((MusicalEntity)sign).soundsign$setWasPowered(false);
						}
					}
					if (((MusicalEntity)sign).soundsign$getReset()) {
						((MusicalEntity)sign).soundsign$setShouldUpdateDelay(true);
						((MusicalEntity)sign).soundsign$setReset(false);
					}
				}
			}
		} else {
			((MusicalEntity)sign).soundsign$setShouldUpdateDelay(true);
		}
		if (((MusicalEntity)sign).soundsign$getShouldUpdateDelay()) {
			soundsign$updateDelay(true, world, sign, sign.getFrontText().getMessages(false));
			soundsign$updateDelay(false, world, sign, sign.getBackText().getMessages(false));
		}
	}
	@Unique
	private static void soundsign$processSound(boolean front, World world, SignBlockEntity sign) {
		try {
			SignText text = front ? sign.getFrontText() : sign.getBackText();
			Text[] messages = text.getMessages(false);
			String sound = (messages[0].getString() + messages[1].getString() + messages[2].getString()).toLowerCase();
			if (!sound.isEmpty()) {
				Identifier soundId = Identifier.of(sound);
				if (front) {
					int current = ((MusicalEntity) sign).soundsign$getDelayFront();
					if (current == -3) {
						((MusicalEntity) sign).soundsign$setReset(true);
						return;
					}
					if (current == -1 || current == 0) {
						world.playSound(null, sign.getPos(), SoundEvent.of(soundId), SoundCategory.RECORDS);
						if (current == -1) {
							((MusicalEntity) sign).soundsign$setDelayFront(-2);
							return;
						}
					}
					if (current >= 0) {
						if (current < ((MusicalEntity) sign).soundsign$getMaxDelayFront()) {
							((MusicalEntity) sign).soundsign$setDelayFront(current + 1);
						} else {
							((MusicalEntity) sign).soundsign$setReset(true);
						}
					}
				} else {
					int current = ((MusicalEntity) sign).soundsign$getDelayBack();
					if (current == -3) {
						((MusicalEntity) sign).soundsign$setReset(true);
						return;
					}
					if (current == -1 || current == 0) {
						world.playSound(null, sign.getPos(), SoundEvent.of(soundId), SoundCategory.RECORDS);
						if (current == -1) {
							((MusicalEntity) sign).soundsign$setDelayBack(-2);
							return;
						}
					}
					if (current >= 0) {
						if (current < ((MusicalEntity) sign).soundsign$getMaxDelayBack()) {
							((MusicalEntity) sign).soundsign$setDelayBack(current + 1);
						} else {
							((MusicalEntity) sign).soundsign$setReset(true);
						}
					}
				}
			}
		} catch (Exception error) {
		}
	}
	@Unique
	private int soundsign$delayFront = -3;
	@Unique
	private int soundsign$delayBack = -3;
	@Unique
	private int soundsign$maxDelayFront;
	@Unique
	private int soundsign$maxDelayBack;
	@Override
	public void soundsign$setDelayFront(int value) {
		this.soundsign$delayFront = value;
	}
	@Override
	public void soundsign$setDelayBack(int value) {
		this.soundsign$delayBack = value;
	}
	@Override
	public int soundsign$getDelayFront() {
		return this.soundsign$delayFront;
	}
	@Override
	public int soundsign$getDelayBack() {
		return this.soundsign$delayBack;
	}
	@Override
	public void soundsign$setMaxDelayFront(int value) {
		this.soundsign$maxDelayFront = value;
	}
	@Override
	public void soundsign$setMaxDelayBack(int value) {
		this.soundsign$maxDelayBack = value;
	}
	@Override
	public int soundsign$getMaxDelayFront() {
		return this.soundsign$maxDelayFront;
	}
	@Override
	public int soundsign$getMaxDelayBack() {
		return this.soundsign$maxDelayBack;
	}
	@Unique
	private static void soundsign$updateDelay(boolean front, World world, SignBlockEntity sign, Text[] messages) {
		int delay = 20;
		try {
			String rawDelay = messages[3].getString();
			if (rawDelay.startsWith("r")) {
				try {
					delay = Integer.parseInt(rawDelay.substring(1));
				} catch (NumberFormatException error) {
				}
				delay = world.random.nextInt(delay);
			} else {
				delay = Integer.parseInt(rawDelay);
			}
		} catch (NumberFormatException error) {
		}
		if (front) {
			((MusicalEntity)sign).soundsign$setMaxDelayFront(delay);
			((MusicalEntity)sign).soundsign$setDelayFront(Math.min(0, delay));
		}
		else {
			((MusicalEntity)sign).soundsign$setMaxDelayBack(delay);
			((MusicalEntity)sign).soundsign$setDelayBack(Math.min(0, delay));
		}
		((MusicalEntity)sign).soundsign$setShouldUpdateDelay(false);
	}
	@Unique
	private boolean soundsign$shouldUpdateDelay;
	@Override
	public void soundsign$setShouldUpdateDelay(boolean value) {
		this.soundsign$shouldUpdateDelay = value;
	}
	@Override
	public boolean soundsign$getShouldUpdateDelay() {
		return this.soundsign$shouldUpdateDelay;
	}
	@Unique
	private boolean soundsign$reset;
	@Override
	public void soundsign$setReset(boolean value) {
		this.soundsign$reset = value;
	}
	@Override
	public boolean soundsign$getReset() {
		return this.soundsign$reset;
	}
}