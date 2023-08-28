package de.dafuqs.spectrum.entity.entity;

import de.dafuqs.spectrum.entity.*;
import de.dafuqs.spectrum.entity.variants.*;
import de.dafuqs.spectrum.registries.*;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.*;
import net.minecraft.entity.data.*;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.server.world.*;
import net.minecraft.sound.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.*;
import net.minecraft.world.*;
import net.minecraft.world.poi.*;
import org.jetbrains.annotations.*;

// TODO - review port
// funny little creatures
// always out for trouble
public class LizardEntity extends TameableEntity implements PackEntity<LizardEntity>, POIMemorized {

	protected static final TrackedData<LizardScaleVariant> SCALE_VARIANT = DataTracker.registerData(LizardEntity.class, SpectrumTrackedDataHandlerRegistry.LIZARD_SCALE_VARIANT);
	protected static final TrackedData<LizardFrillVariant> FRILL_VARIANT = DataTracker.registerData(LizardEntity.class, SpectrumTrackedDataHandlerRegistry.LIZARD_FRILL_VARIANT);
	protected static final TrackedData<LizardHornVariant> HORN_VARIANT = DataTracker.registerData(LizardEntity.class, SpectrumTrackedDataHandlerRegistry.LIZARD_HORN_VARIANT);
	
	protected @Nullable LizardEntity leader;
	protected int groupSize = 1;

	protected int ticksLeftToFindPOI;
	protected @Nullable BlockPos poiPos;
	
	public LizardEntity(EntityType<? extends LizardEntity> entityType, World world) {
		super(entityType, world);
		this.experiencePoints = 4;
	}
	
	public static DefaultAttributeContainer.Builder createLizardAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0D)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 16.0D)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D)
				.add(EntityAttributes.GENERIC_ARMOR, 6.0D)
				.add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 1.0D)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2D)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 12.0D);
	}
	
	@Override
	protected void initGoals() {
		super.initGoals();
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, new AnimalMateGoal(this, 1.0D));
		this.goalSelector.add(3, new AttackGoal(this));
		this.goalSelector.add(4, new FollowParentGoal(this, 1.2D));
		this.goalSelector.add(4, new FollowClanLeaderGoal<>(this));
		this.goalSelector.add(5, new FindPOIGoal(PointOfInterestTypes.LODESTONE, 32));
		this.goalSelector.add(6, new ClanLeaderWanderAroundGoal(this, 0.8, 20, 8, 4));
		this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(8, new LookAroundGoal(this));
		
		this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true, target -> !LizardEntity.this.isOwner(target)));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, LivingEntity.class, true, // different clans attacking each other
				target -> {
					if (target instanceof LizardEntity other) {
						return isDifferentPack(other);
					}
					return !target.isBaby();
				}));
	}

	@Override
	public float getBrightnessAtEyes() {
		return 1.0F;
	}

	@Override
	public boolean isOwner(LivingEntity entity) {
		return entity == this.getOwner() || this.leader != null && entity == this.leader.getOwner();
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (this.age % 1200 == 0) {
			this.heal(1.0F);
		}
	}
	
	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(SCALE_VARIANT, LizardScaleVariant.CYAN);
		this.dataTracker.startTracking(FRILL_VARIANT, LizardFrillVariant.SIMPLE);
		this.dataTracker.startTracking(HORN_VARIANT, LizardHornVariant.HORNY);
	}
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putString("scales", SpectrumRegistries.LIZARD_SCALE_VARIANT.getId(this.getScales()).toString());
		nbt.putString("frills", SpectrumRegistries.LIZARD_FRILL_VARIANT.getId(this.getFrills()).toString());
		nbt.putString("horns", SpectrumRegistries.LIZARD_HORN_VARIANT.getId(this.getHorns()).toString());
		writePOIPosToNbt(nbt);
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		
		LizardScaleVariant scales = SpectrumRegistries.LIZARD_SCALE_VARIANT.get(Identifier.tryParse(nbt.getString("scales")));
		this.setScales(scales == null ? SpectrumRegistries.getRandomTagEntry(SpectrumRegistries.LIZARD_SCALE_VARIANT, LizardScaleVariant.NATURAL_VARIANT, this.getWorld().random, LizardScaleVariant.CYAN) : scales);
		
		LizardFrillVariant frills = SpectrumRegistries.LIZARD_FRILL_VARIANT.get(Identifier.tryParse(nbt.getString("frills")));
		this.setFrills(frills == null ? SpectrumRegistries.getRandomTagEntry(SpectrumRegistries.LIZARD_FRILL_VARIANT, LizardFrillVariant.NATURAL_VARIANT, this.getWorld().random, LizardFrillVariant.SIMPLE) : frills);
		
		LizardHornVariant horns = SpectrumRegistries.LIZARD_HORN_VARIANT.get(Identifier.tryParse(nbt.getString("horns")));
		this.setHorns(horns == null ? SpectrumRegistries.getRandomTagEntry(SpectrumRegistries.LIZARD_HORN_VARIANT, LizardHornVariant.NATURAL_VARIANT, this.getWorld().random, LizardHornVariant.HORNY) : horns);
		
		readPOIPosFromNbt(nbt);
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (!this.getWorld().isClient && this.ticksLeftToFindPOI > 0) {
			--this.ticksLeftToFindPOI;
		}
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);
		if (this.isBreedingItem(itemStack)) {
			int i = this.getBreedingAge();
			if (!this.getWorld().isClient && i == 0 && this.canEat() && this.random.nextInt(5) == 0) {
				// yes, this also overrides the existing owner
				// there is no god besides the new god
				this.eat(player, hand, itemStack);
				this.setOwner(player);
				this.lovePlayer(player);
				return ActionResult.SUCCESS;
			}

			if (this.isBaby()) {
				this.eat(player, hand, itemStack);
				this.growUp(toGrowUpAge(-i), true);
				return ActionResult.success(this.getWorld().isClient);
			}

			if (this.getWorld().isClient) {
				return ActionResult.CONSUME;
			}
		}

		return ActionResult.PASS;
	}

	@Override
	public boolean canEat() {
		return super.canEat() || getOwner() != null;
	}
	
	public LizardScaleVariant getScales() {
		return this.dataTracker.get(SCALE_VARIANT);
	}
	
	public void setScales(LizardScaleVariant variant) {
		this.dataTracker.set(SCALE_VARIANT, variant);
	}
	
	public LizardFrillVariant getFrills() {
		return this.dataTracker.get(FRILL_VARIANT);
	}
	
	public void setFrills(LizardFrillVariant variant) {
		this.dataTracker.set(FRILL_VARIANT, variant);
	}
	
	public LizardHornVariant getHorns() {
		return this.dataTracker.get(HORN_VARIANT);
	}
	
	public void setHorns(LizardHornVariant variant) {
		this.dataTracker.set(HORN_VARIANT, variant);
	}
	
	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_PIG_AMBIENT;
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_PIG_HURT;
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_PIG_DEATH;
	}
	
	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(SoundEvents.ENTITY_PIG_STEP, 0.15F, 1.0F);
	}
	
	@Override
	protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
		return 0.5F * dimensions.height;
	}

	// Breeding

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		if (stack.isOf(SpectrumItems.LIZARD_MEAT)) {
			return false;
		}
		FoodComponent food = stack.getItem().getFoodComponent();
		return food != null && food.isMeat();
	}

	@Override
	public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
		LizardEntity other = (LizardEntity) entity;
		LizardEntity child = SpectrumEntityTypes.LIZARD.create(world);
		if (child != null) {
			child.setScales(getChildScales(this, other));
			child.setFrills(getChildFrills(this, other));
			child.setHorns(getChildHorns(this, other));
		}
		return child;
	}

	private LizardFrillVariant getChildFrills(LizardEntity firstParent, LizardEntity secondParent) {
		return this.getWorld().random.nextBoolean() ? firstParent.getFrills() : secondParent.getFrills();
	}

	private LizardScaleVariant getChildScales(LizardEntity firstParent, LizardEntity secondParent) {
		return this.getWorld().random.nextBoolean() ? firstParent.getScales() : secondParent.getScales();
	}

	private LizardHornVariant getChildHorns(LizardEntity firstParent, LizardEntity secondParent) {
		return this.getWorld().random.nextBoolean() ? firstParent.getHorns() : secondParent.getHorns();
	}

	// PackEntity

	@Override
	public boolean hasOthersInGroup() {
		return this.groupSize > 1;
	}

	@Override
	public @Nullable LizardEntity getLeader() {
		return this.leader;
	}

	@Override
	public boolean isCloseEnoughToLeader() {
		return this.squaredDistanceTo(this.leader) <= 121.0;
	}

	@Override
	public void leaveGroup() {
		this.leader.decreaseGroupSize();
		this.leader = null;
	}

	@Override
	public void moveTowardLeader() {
		if (this.hasLeader()) {
			this.getNavigation().startMovingTo(this.leader, 1.0);
		}
	}

	@Override
	public int getMaxGroupSize() {
		return super.getLimitPerChunk();
	}

	@Override
	public void joinGroupOf(LizardEntity groupLeader) {
		this.leader = groupLeader;
		groupLeader.increaseGroupSize();
	}

	@Override
	public int getGroupSize() {
		return this.groupSize;
	}

	protected void increaseGroupSize() {
		++this.groupSize;
	}

	protected void decreaseGroupSize() {
		--this.groupSize;
	}

	// POIMemorized
	@Override
	public TagKey<PointOfInterestType> getPOITag() {
		return SpectrumPointOfInterestTypeTags.LIZARD_DENS;
	}

	@Override
	public @Nullable BlockPos getPOIPos() {
		return this.poiPos;
	}

	@Override
	public void setPOIPos(@Nullable BlockPos blockPos) {
		this.poiPos = blockPos;
	}

	// Goals
	protected class ClanLeaderWanderAroundGoal extends WanderAroundGoal {

		int chanceToNavigateToPOI;
		int maxDistanceFromPOI;

		public ClanLeaderWanderAroundGoal(PathAwareEntity mob, double speed, int chance, int chanceToNavigateToPOI, int maxDistanceFromPOI) {
			super(mob, speed, chance);
			this.chanceToNavigateToPOI = chanceToNavigateToPOI;
			this.maxDistanceFromPOI = maxDistanceFromPOI;
		}

		@Override
		public boolean canStart() {
			return !LizardEntity.this.hasLeader() && super.canStart();
		}

		@Override
		protected @Nullable Vec3d getWanderTarget() {
			// when we are away from our poi (their den) there is a chance they navigate back to it, so they always stay near
			if (random.nextFloat() < this.chanceToNavigateToPOI
					&& LizardEntity.this.isPOIValid((ServerWorld) LizardEntity.this.getWorld())
					&& !LizardEntity.this.getBlockPos().isWithinDistance(LizardEntity.this.poiPos, this.maxDistanceFromPOI)) {

				return Vec3d.ofCenter(LizardEntity.this.poiPos);
			}

			return NoPenaltyTargeting.find(LizardEntity.this, 8, 7);
		}

	}

	private class FindPOIGoal extends Goal {

		RegistryKey<PointOfInterestType> poiType;
		int maxDistance;

		FindPOIGoal(RegistryKey<PointOfInterestType> poiType, int maxDistance) {
			super();
			this.poiType = poiType;
			this.maxDistance = maxDistance;
		}

		@Override
		public boolean canStart() {
			return LizardEntity.this.hasOthersInGroup()
					&& LizardEntity.this.ticksLeftToFindPOI == 0
					&& !LizardEntity.this.isPOIValid((ServerWorld) LizardEntity.this.getWorld());
		}

		@Override
		public void start() {
			LizardEntity.this.ticksLeftToFindPOI = 200;
			LizardEntity.this.poiPos = LizardEntity.this.findNearestPOI((ServerWorld) LizardEntity.this.getWorld(), LizardEntity.this.getBlockPos(), 40);
		}

	}


	@Override
	public EntityView method_48926() {
		return this.getWorld();
	}
}
