package io.gomint.server.entity.passive;

import io.gomint.entity.EntityPlayer;
import io.gomint.math.Vector;
import io.gomint.server.entity.Attribute;
import io.gomint.server.entity.EntityAgeable;
import io.gomint.server.entity.EntityLiving;
import io.gomint.server.entity.EntityTags;
import io.gomint.server.entity.EntityType;
import io.gomint.server.entity.metadata.MetadataContainer;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.WorldAdapter;
import io.gomint.taglib.NBTTagCompound;

import java.util.Set;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:villager" )
public class EntityVillager extends EntityAgeable implements io.gomint.entity.passive.EntityVillager {

    /**
     * Constructs a new EntityLiving
     *
     * @param world The world in which this entity is in
     */
    public EntityVillager( WorldAdapter world ) {
        super( EntityType.VILLAGER, world );
        this.initEntity();
    }

    /**
     * Create new entity villager for API
     */
    public EntityVillager() {
        super( EntityType.VILLAGER, null );
        this.initEntity();
    }

    private void initEntity() {
        this.eyeHeight = 1.62f;
        this.addAttribute(Attribute.HEALTH);
        this.setMaxHealth(20);
        this.setHealth(20);
        this.setProfession(Profession.FARMER);
        if(this.isBaby()) {
            this.setSize(0.3f, 0.975f);
        }else{
            this.setSize(0.6f, 1.95f);
        }
    }

    @Override
    public void initFromNBT( NBTTagCompound compound ) {
        super.initFromNBT( compound );

        this.metadataContainer.putInt( MetadataContainer.DATA_VARIANT, compound.getInteger( "Variant", 0 ) );
    }

    @Override
    public NBTTagCompound persistToNBT() {
        return super.persistToNBT();
    }

    @Override
    public void update( long currentTimeMS, float dT ) {
        super.update( currentTimeMS, dT );
    }

    @Override
    public void interact( EntityPlayer player, Vector clickVector ) {
        // TODO: Adding the ability of open the villager shop inventory for the player
    }

    @Override
    public void setProfession( Profession profession ) {
        int variant = 0;
        switch ( profession ) {
            case BUTCHER:
                variant = 4;
                break;
            case BLACKSMITH:
                variant = 3;
                break;
            case PRIEST:
                variant = 2;
                break;
            case LIBRARIAN:
                variant = 1;
                break;
            case FARMER:
            default:
                variant = 0;
        }

        this.metadataContainer.putInt( MetadataContainer.DATA_VARIANT, variant );
    }

    @Override
    public Profession getProfession() {
        int variant = this.metadataContainer.getInt( MetadataContainer.DATA_VARIANT );
        switch ( variant ) {
            case 4:
                return Profession.BUTCHER;
            case 3:
                return Profession.BLACKSMITH;
            case 2:
                return Profession.PRIEST;
            case 1:
                return Profession.LIBRARIAN;
            case 0:
            default:
                return Profession.FARMER;
        }
    }

    @Override
    public Set<String> getTags() {
        return EntityTags.CREATURE;
    }

}
