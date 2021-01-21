package com.github.alexthe666.rats.server.entity;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class EntityPlagueShot extends EntityArrow {

    public EntityPlagueShot(World worldIn) {
        super(worldIn);
        this.setDamage(6F);
    }

    public EntityPlagueShot(World worldIn, double x, double y, double z, float r, float g, float b) {
        this(worldIn);
        this.setPosition(x, y, z);
        this.setDamage(6F);
    }

    public EntityPlagueShot(World worldIn, EntityLivingBase shooter, double dmg) {
        super(worldIn, shooter);
        this.setDamage(dmg);
    }

    public boolean isInWater() {
        return false;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    public void onUpdate() {
        float sqrt = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        if ((sqrt < 0.1F || this.inGround || this.collidedHorizontally) && this.ticksExisted > 5) {
            this.setDead();
        }
        double d0 = 0;
        double d1 = 0.01D;
        double d2 = 0D;
        double x = this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width;
        double y = this.posY + (double) (this.rand.nextFloat() * this.height) - (double) this.height;
        double z = this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width;
        float f = (this.width + this.height + this.width) * 0.333F + 0.5F;
        if (particleDistSq(x, y, z) < f * f) {
            if (rand.nextBoolean()) {
                RatsMod.PROXY.spawnParticle("black_death", x, y + 0.5D, z, d0, d1, d2);
            } else {
                this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, x, y + 0.5D, z, d0, d1, d2);

            }
        }
        super.onUpdate();
    }

    public double particleDistSq(double toX, double toY, double toZ) {
        double d0 = posX - toX;
        double d1 = posY - toY;
        double d2 = posZ - toZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }


    public void playSound(@Nonnull SoundEvent soundIn, float volume, float pitch) {
        if (!this.isSilent() && soundIn != SoundEvents.ENTITY_ARROW_HIT && soundIn != SoundEvents.ENTITY_ARROW_HIT_PLAYER) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, soundIn, this.getSoundCategory(), volume, pitch);
        }
    }

    protected void arrowHit(@Nonnull EntityLivingBase living) {
        super.arrowHit(living);
        if (this.shootingEntity == null || !living.isEntityEqual(this.shootingEntity)){
            //15 minutes of plague (to allow the player to go through the first level of plague [18000])
            living.addPotionEffect(new PotionEffect(RatsMod.PLAGUE_POTION, RatsMod.CONFIG_OPTIONS.plagueEffectDuration * 20, 0));

            if (living instanceof EntityPlayer) {
                this.damageShield((EntityPlayer) living, (float) this.getDamage());
            }
        }
    }

    protected void damageShield(EntityPlayer player, float damage) {
        if (damage >= 3.0F && player.getActiveItemStack().getItem().isShield(player.getActiveItemStack(), player)) {
            ItemStack copyBeforeUse = player.getActiveItemStack().copy();
            int i = 1 + MathHelper.floor(damage);
            player.getActiveItemStack().damageItem(i, player);

            if (player.getActiveItemStack().isEmpty()) {
                EnumHand enumhand = player.getActiveHand();
                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyBeforeUse, enumhand);

                if (enumhand == EnumHand.MAIN_HAND) {
                    this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
                } else {
                    this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
                player.resetActiveHand();
                this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + this.world.rand.nextFloat() * 0.4F);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }

    public float getBrightness() {
        return 1.0F;
    }

    public boolean hasNoGravity() {
        return true;
    }

    @Override
    protected ItemStack getArrowStack() {
        return ItemStack.EMPTY;
    }
}
