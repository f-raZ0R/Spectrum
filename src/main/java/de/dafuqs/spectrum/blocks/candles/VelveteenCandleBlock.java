package de.dafuqs.spectrum.blocks.candles;

import de.dafuqs.spectrum.particle.*;
import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.fluid.*;
import net.minecraft.item.*;
import net.minecraft.particle.*;
import net.minecraft.sound.*;
import net.minecraft.state.*;
import net.minecraft.state.property.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.*;
import net.minecraft.world.*;
import net.minecraft.world.event.*;
import org.jetbrains.annotations.*;
import org.joml.*;

import java.util.function.*;

public class VelveteenCandleBlock extends Block implements Waterloggable{
	public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE = (state) -> (Boolean)state.get(LIT) ? 15 : 0;
	private static final VoxelShape AABB = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 14.0, 11.0);
    public VelveteenCandleBlock(Settings settings) {
        super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(LIT, true).with(WATERLOGGED, false));
    }
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(LIT, WATERLOGGED);
	}
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		boolean bl = fluidState.getFluid() == Fluids.WATER;
		return super.getPlacementState(ctx).with(LIT, !bl).with(WATERLOGGED,bl);
	}
	public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
		if (!(Boolean)state.get(WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
			BlockState blockState = state.with(WATERLOGGED, true);
			if (state.get(LIT)) {
				extinguish(null, blockState, world, pos);
			} else {
				world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
			}
			
			world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
			return true;
		} else {
			return false;
		}
	}
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (player.getAbilities().allowModifyWorld && player.getStackInHand(hand).isEmpty() && state.get(LIT)) {
			extinguish(player, state, world, pos);
			return ActionResult.success(world.isClient);
		} else {
			return ActionResult.PASS;
		}
	}
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
	private static void setLit(WorldAccess world, BlockState state, BlockPos pos, boolean lit) {
		world.setBlockState(pos, state.with(LIT, lit), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
	}
	
	protected boolean isNotLit(BlockState state) {
		return !(Boolean)state.get(WATERLOGGED) && !(Boolean)state.get(LIT);
	}
	
	public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
		if (!world.isClient && projectile.isOnFire() && this.isNotLit(state)) {
			setLit(world, state, hit.getBlockPos(), true);
		}
		
	}
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return Block.sideCoversSmallSquare(world, pos.down(), Direction.UP);
	}
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (state.get(LIT)) {
			float f = random.nextFloat();
			Vector3d vec3d = new Vector3d((double)pos.getX()+0.5, (double)pos.getY()+0.5,(double)pos.getZ()+0.5);
			if (f < 0.3F) {
				world.addParticle(ParticleTypes.SMOKE, vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 0.0);
				if (f < 0.17F) {
					world.playSound(vec3d.x + 0.5, vec3d.y + 0.5, vec3d.z + 0.5, SoundEvents.BLOCK_CANDLE_AMBIENT, SoundCategory.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
				}
			}
			world.addParticle(SpectrumParticleTypes.PRIMORDIAL_FLAME_SMALL, vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 0.0);
		}
	}
	
	
	public static void extinguish(@Nullable PlayerEntity player, BlockState state, WorldAccess world, BlockPos pos) {
		setLit(world, state, pos, false);
		if (state.getBlock() instanceof VelveteenCandleBlock) {
				world.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 0.0, 0.1, 0.0);
		}
		world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
		world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return AABB;
	}

}
