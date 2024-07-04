package com.cyanogen.experienceobelisk.block_entities;

import com.cyanogen.experienceobelisk.ExperienceObelisk;
import com.cyanogen.experienceobelisk.recipe.LaserTransfiguratorRecipe;
import com.cyanogen.experienceobelisk.registries.RegisterBlockEntities;
import com.cyanogen.experienceobelisk.utils.MiscUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LaserTransfiguratorEntity extends ExperienceReceivingEntity implements GeoBlockEntity {

    public LaserTransfiguratorEntity(BlockPos pos, BlockState state) {
        super(RegisterBlockEntities.LASER_TRANSFIGURATOR_BE.get(), pos, state);
    }

    boolean isProcessing = false;
    int processTime = 0;
    int processProgress = 0;

    NonNullList<ItemStack> remainderItems = NonNullList.withSize(4, ItemStack.EMPTY);
    ResourceLocation recipeId;

    //-----------ANIMATIONS-----------//

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    protected static final RawAnimation ACTIVE = RawAnimation.begin().thenPlay("active");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, this::controller));
    }

    protected <E extends LaserTransfiguratorEntity> PlayState controller(final AnimationState<E> state){

        LaserTransfiguratorEntity entity = state.getAnimatable();
        AnimationController<E> controller = state.getController();
        RawAnimation animation = controller.getCurrentRawAnimation();
        controller.transitionLength(20);

        if(animation == null){
            controller.setAnimation(IDLE);
        }
        else{
            if(entity.isProcessing && animation.equals(IDLE)){
                controller.setAnimation(ACTIVE);
            }
            else if(!entity.isProcessing && animation.equals(ACTIVE)){
                controller.setAnimation(IDLE);
            }
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    //-----------BEHAVIOR-----------//

    public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {

        if(blockEntity instanceof LaserTransfiguratorEntity transfigurator){

            boolean active = !transfigurator.redstoneEnabled || level.hasNeighborSignal(pos);

            if(transfigurator.getBoundObelisk() != null){
                transfigurator.sendObeliskInfoToScreen(transfigurator.getBoundObelisk());
            }

            if(transfigurator.isProcessing){

                if(transfigurator.processProgress >= transfigurator.processTime){
                    transfigurator.dispenseResult();
                }
                else{
                    if(transfigurator.validateRecipe()){
                        transfigurator.incrementProcessProgress();
                    }
                }
            }
            else if(active && transfigurator.hasContents()){
                if(!transfigurator.handleJsonRecipes()){
                    transfigurator.handleNameFormattingRecipes();
                }
            }

        }

    }

    public boolean hasContents(){

        boolean hasContents = false;

        for(int i = 0; i < 3; i++){
            ItemStack stack = itemHandler.getStackInSlot(i);
            if(!stack.isEmpty() && !stack.getItem().equals(Items.AIR)){
                hasContents = true;
                break;
            }
        }
        return hasContents;
    }

    //-----------ITEM HANDLER-----------//

    protected ItemStackHandler itemHandler = itemHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);

    public ItemStackHandler itemHandler() {

        return new ItemStackHandler(4){
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return slot <= 2;
            }
        };
    }

    public ItemStackHandler getItemHandler(){
        return itemHandler;
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if(capability == ForgeCapabilities.ITEM_HANDLER && facing != Direction.UP){
            return handler.cast();
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        handler.invalidate();
        super.invalidateCaps();
    }

    //-----------RECIPE HANDLER-----------//

    public boolean handleJsonRecipes(){

        if(getRecipe().isPresent()){
            LaserTransfiguratorRecipe recipe = getRecipe().get();
            ItemStack output = recipe.getResultItem(null);
            int cost = recipe.getCost();

            if(canPerformRecipe(output, cost)){
                initiateRecipe(recipe);
                return true;
            }

        }
        return false;

    }

    public boolean canPerformRecipe(ItemStack output, int cost){

        //here we check for criteria that are independent of recipe parameters

        return getBoundObelisk() != null //has been bound to a valid obelisk
                && getBoundObelisk().getFluidAmount() >= cost * 20 //obelisk has enough Cognitium
                && (ItemStack.isSameItemSameTags(itemHandler.getStackInSlot(3), output)
                || itemHandler.getStackInSlot(3).isEmpty() || itemHandler.getStackInSlot(3).is(Items.AIR)) //results slot empty or same as output
                && itemHandler.getStackInSlot(3).getCount() <= output.getMaxStackSize() - output.getCount(); //results slot can accommodate output
    }

    public void initiateRecipe(LaserTransfiguratorRecipe recipe){

        this.setProcessing(true);
        this.setRecipeId(recipe);
        this.setOutputItem(recipe.getResultItem(null));
        this.setRemainderItems(deplete(recipe));
        this.setProcessProgress(0);
        this.setProcessTime(recipe.getProcessTime());
        this.getBoundObelisk().drain(recipe.getCost() * 20);
    }

    public boolean validateRecipe(){

        boolean hasValidRecipe = getRecipe().isPresent() && getRecipe().get().getId().equals(recipeId);

        if(hasValidRecipe){
            setRemainderItems(deplete(getRecipe().get()));
        }
        else if(hasNameFormattingRecipe()){
            setRemainderItems(deplete(getNameFormattingRecipe()));
        }
        else{
            setProcessing(false);
            resetAll();
            return false;
        }

        return true;
    }

    public SimpleContainer deplete(LaserTransfiguratorRecipe recipe){

        SimpleContainer container = getSimpleContainer();

        Map<Ingredient, Tuple<Integer, Integer>> ingredientMap = recipe.getIngredientMapNoFiller();

        for(Map.Entry<Ingredient, Tuple<Integer, Integer>> entry : ingredientMap.entrySet()){

            Ingredient ingredient = entry.getKey();
            int count = entry.getValue().getB();

            for(int i = 0; i < 3; i++){
                ItemStack stack = container.getItem(i);

                if(ingredient.test(stack)){
                    stack.shrink(count);
                    break;
                }
            }
        }

        return container;
    }

    public void dispenseResult(){

        setProcessing(false);

        ItemStack result = remainderItems.get(3);
        ItemStack existingStack = itemHandler.getStackInSlot(3).copy();

        if(existingStack.getItem().equals(result.getItem())){
            existingStack.grow(1);
            itemHandler.setStackInSlot(3, existingStack);
        }
        else{
            itemHandler.setStackInSlot(3, result);
        }

        for(int i = 0; i < 3; i++){
            itemHandler.setStackInSlot(i, remainderItems.get(i));
        }

        resetAll();
    }

    //-----------NON-JSON RECIPES-----------//

    public void handleNameFormattingRecipes(){

        if(hasNameFormattingRecipe()){

            LaserTransfiguratorRecipe recipe = getNameFormattingRecipe();
            ItemStack output = recipe.getResultItem(null);
            int cost = recipe.getCost();

            if(canPerformRecipe(output, cost)){
                initiateRecipe(recipe);
            }
        }
    }

    public boolean hasNameFormattingRecipe(){
        Item formatItem = itemHandler.getStackInSlot(2).getItem();

        return !itemHandler.getStackInSlot(0).isEmpty() //any item
                && itemHandler.getStackInSlot(1).is(Items.NAME_TAG) //a name tag
                && (formatItem instanceof DyeItem || MiscUtils.getValidFormattingItems().contains(formatItem)); //a valid formatting item
    }

    public LaserTransfiguratorRecipe getNameFormattingRecipe(){

        ItemStack inputItem = itemHandler.getStackInSlot(0);
        MutableComponent name = inputItem.getHoverName().copy();
        Item formatItem = itemHandler.getStackInSlot(2).getItem();

        if(formatItem instanceof DyeItem dye){
            int dyeColor = dye.getDyeColor().getId();
            ChatFormatting format = ChatFormatting.getById(MiscUtils.dyeColorToTextColor(dyeColor));

            if (format != null) {
                name = name.withStyle(format);
            }
        }
        else if(MiscUtils.getValidFormattingItems().contains(formatItem)){
            int index = MiscUtils.getValidFormattingItems().indexOf(formatItem);
            char code = MiscUtils.itemToFormat(index);
            ChatFormatting format = ChatFormatting.getByCode(code);

            if (format != null) {
                name = name.withStyle(format);
            }
        }

        Map<Ingredient, Tuple<Integer, Integer>> ingredientMap = new HashMap<>();
        ingredientMap.put(Ingredient.of(inputItem.copy()), new Tuple<>(1, inputItem.getCount()));
        ingredientMap.put(Ingredient.of(itemHandler.getStackInSlot(1).copy()), new Tuple<>(2, itemHandler.getStackInSlot(1).getCount()));
        ingredientMap.put(Ingredient.of(itemHandler.getStackInSlot(2).copy()), new Tuple<>(3, itemHandler.getStackInSlot(2).getCount()));

        ItemStack output = inputItem.copy().setHoverName(name);
        int cost = 55;
        int processTime = 40;

        return new LaserTransfiguratorRecipe(ImmutableMap.copyOf(ingredientMap), output, cost, processTime,
                new ResourceLocation(ExperienceObelisk.MOD_ID, "item_name_formatting"));
    }

    //-----------UTILITY METHODS-----------//

    public Optional<LaserTransfiguratorRecipe> getRecipe(){
        return this.level.getRecipeManager().getRecipeFor(LaserTransfiguratorRecipe.Type.INSTANCE, getSimpleContainer(), level);
    }

    public SimpleContainer getSimpleContainer(){
        SimpleContainer container = new SimpleContainer(3);
        container.setItem(0, itemHandler.getStackInSlot(0).copy());
        container.setItem(1, itemHandler.getStackInSlot(1).copy());
        container.setItem(2, itemHandler.getStackInSlot(2).copy());

        return container;
    }

    public void setProcessing(boolean isProcessing){
        this.isProcessing = isProcessing;
        setChanged();
    }

    public int getProcessTime(){
        return processTime;
    }

    public void setProcessTime(int time){
        this.processTime = time;
        setChanged();
    }

    public int getProcessProgress(){
        return processProgress;
    }

    public void setProcessProgress(int progress){
        this.processProgress = progress;
        setChanged();
    }

    public void incrementProcessProgress(){
        this.processProgress += 1;
        setChanged();
    }

    public void setRemainderItems(SimpleContainer container){
        for(int i = 0; i < 3; i++){
            remainderItems.set(i, container.getItem(i));
        }
        setChanged();
    }

    public void setOutputItem(ItemStack stack){
        remainderItems.set(3, stack);
        setChanged();
    }

    public void setRecipeId(LaserTransfiguratorRecipe recipe){
        this.recipeId = recipe.getId();
        setChanged();
    }

    public void resetAll(){

        processProgress = 0;
        processTime = 0;
        this.remainderItems = NonNullList.withSize(4, ItemStack.EMPTY);
        recipeId = null;
        setChanged();
    }

    //-----------NBT-----------//

    @Override
    public void setChanged() {
        if(this.level != null){
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
            //do this if live block entity data is needed in the GUI
        }
        super.setChanged();
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);

        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        this.remainderItems = NonNullList.withSize(4, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, remainderItems);

        this.isProcessing = tag.getBoolean("IsProcessing");
        this.processTime = tag.getInt("ProcessTime");
        this.processProgress = tag.getInt("ProcessProgress");
        this.recipeId = new ResourceLocation(tag.getString("RecipeID"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);

        tag.put("Inventory", itemHandler.serializeNBT());
        ContainerHelper.saveAllItems(tag, remainderItems);

        tag.putBoolean("IsProcessing", isProcessing);
        tag.putInt("ProcessTime", processTime);
        tag.putInt("ProcessProgress", processProgress);

        if(recipeId != null){
            tag.putString("RecipeID", recipeId.toString());
        }
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag tag = super.getUpdateTag();

        tag.put("Inventory", itemHandler.serializeNBT());
        ContainerHelper.saveAllItems(tag, remainderItems);

        tag.putBoolean("IsProcessing", isProcessing);
        tag.putInt("ProcessTime", processTime);
        tag.putInt("ProcessProgress", processProgress);

        if(recipeId != null){
            tag.putString("RecipeID", recipeId.toString());
        }

        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
