package dev.dannytaylor.soundsign.mixin;

import dev.dannytaylor.soundsign.entity.MusicalEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(priority = 100, value = ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin extends AbstractDecorationEntity implements MusicalEntity {
	@Shadow public abstract ItemStack getHeldItemStack();

	protected ItemFrameEntityMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public void tick() {
		super.tick();
		World world = this.getWorld();
		BlockPos pos = this.getBlockPos();
		if (!this.getHeldItemStack().isEmpty()) {
			BlockState attachedState = world.getBlockState(pos.offset(this.getFacing().getOpposite()));
			if (attachedState.isOf(Blocks.NOTE_BLOCK)) {
				if (attachedState.get(NoteBlock.POWERED)) {
					soundsign$processSound(world, this.getHeldItemStack());
					if (this.soundsign$getReset()) {
						this.soundsign$setReset(false);
					}
				} else {
					if (!this.soundsign$getReset()) {
						this.soundsign$setReset(true);
					}
				}
				if (this.soundsign$getReset()) {
					this.soundsign$setShouldUpdateDelay(true);
					this.soundsign$setReset(false);
				}
			}
		} else {
			this.soundsign$setShouldUpdateDelay(true);
		}
		if (this.soundsign$getShouldUpdateDelay()) {
			soundsign$updateDelay(world, this.getHeldItemStack());
		}
	}
	@Unique
	private void soundsign$processSound(World world, ItemStack stack) {
		try {
			Optional<String> oSound = idFromStack(stack);
			if (oSound.isPresent()) {
				String sound = oSound.get().toLowerCase();
				if (!sound.isEmpty()) {
					Identifier soundId = Identifier.of(sound);
					int current = this.soundsign$getDelayFront();
					if (current == -3) {
						this.soundsign$setReset(true);
						return;
					}
					if (current == -1 || current == 0) {
						world.playSound(null, this.getBlockPos(), SoundEvent.of(soundId), SoundCategory.RECORDS);
						if (current == -1) {
							this.soundsign$setDelayFront(-2);
							return;
						}
					}
					if (current >= 0) {
						if (current < this.soundsign$getMaxDelayFront()) {
							this.soundsign$setDelayFront(current + 1);
						} else {
							this.soundsign$setReset(true);
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
	private void soundsign$updateDelay(World world, ItemStack stack) {
		int delay = 20;
		try {
			if (stack.getCustomName() != null) {
				String rawDelay = stack.getCustomName().getString();
				if (rawDelay.startsWith("r")) {
					try {
						delay = Integer.parseInt(rawDelay.substring(1));
					} catch (NumberFormatException error) {
					}
					delay = world.random.nextInt(delay);
				} else {
					delay = Integer.parseInt(rawDelay);
				}
			}
		} catch (NumberFormatException error) {
		}
			this.soundsign$setMaxDelayFront(delay);
			this.soundsign$setDelayFront(Math.min(0, delay));
		this.soundsign$setShouldUpdateDelay(false);
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
	@Unique
	private Optional<String> idFromStack(ItemStack stack) {
		if (stack.getComponents().contains(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
			WrittenBookContentComponent content = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
			if (content != null) {
				String id = content.pages().stream().map(page -> page.get(false).getString()).collect(Collectors.joining());
				return Optional.of(id);
			}
		}
		return Optional.empty();
	}
}