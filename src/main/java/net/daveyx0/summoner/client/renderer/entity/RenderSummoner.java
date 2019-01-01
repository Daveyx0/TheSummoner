package net.daveyx0.summoner.client.renderer.entity;

import net.daveyx0.summoner.client.renderer.entity.layers.LayerSummonerAura;
import net.daveyx0.summoner.core.TheSummonerReference;
import net.daveyx0.summoner.entity.EntitySummoner;
import net.daveyx0.summoner.entity.EntitySummoningIllager;
import net.minecraft.client.model.ModelIllager;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpellcasterIllager;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;

public class RenderSummoner<T extends EntityLiving> extends RenderLiving<EntitySummoner> {

	private static final ResourceLocation SUMMONER_TEXTURES = new ResourceLocation(TheSummonerReference.MODID + ":" + "textures/entity/summoner/summoner.png");
	private static final ResourceLocation BOSS_SUMMONER_TEXTURES = new ResourceLocation(TheSummonerReference.MODID + ":" + "textures/entity/summoner/summoner_boss.png");

    public RenderSummoner(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelIllager(0.0F, 0.0F, 64, 64), 0.5F);
        this.addLayer(new LayerHeldItem(this)
        {
            public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
            {
                if (((EntitySummoningIllager)entitylivingbaseIn).isSpellcasting())
                {
                    super.doRenderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
                }
            }
            protected void translateToHand(EnumHandSide p_191361_1_)
            {
                ((ModelIllager)this.livingEntityRenderer.getMainModel()).getArm(p_191361_1_).postRender(0.0625F);
            }
        });
        this.addLayer(new LayerSummonerAura(this));
    }
    
    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntitySummoner entity)
    {
    	if(entity.isBoss()){return BOSS_SUMMONER_TEXTURES;}
        return SUMMONER_TEXTURES;
    }
    

    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void preRenderCallback(EntityMob entitylivingbaseIn, float partialTickTime)
    {
        float f = 0.9375F;
        GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
