package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class TickListServer<T> implements TickList<T> {

    protected final Predicate<T> a;
    private final Function<T, MinecraftKey> b;
    private final Function<MinecraftKey, T> c;
    private final Set<NextTickListEntry<T>> nextTickListHash = Sets.newHashSet();
    // MCMT: Made this set synchronyzed so it doesn't creash the processing thread anymore
    // TODO: find a better data structure, or find a way to minimize time spent locking.
    private final SortedSet<NextTickListEntry<T>> nextTickList = new ConcurrentSkipListSet<>(NextTickListEntry.a());
    private final WorldServer f;
    private final Queue<NextTickListEntry<T>> g = Queues.newArrayDeque();
    private final List<NextTickListEntry<T>> h = Lists.newArrayList();
    private final Consumer<NextTickListEntry<T>> i;

    public TickListServer(WorldServer worldserver, Predicate<T> predicate, Function<T, MinecraftKey> function, Function<MinecraftKey, T> function1, Consumer<NextTickListEntry<T>> consumer, String timingsType) { // Paper
        this.a = predicate;
        this.b = function;
        this.c = function1;
        this.f = worldserver;
        this.i = consumer;
        this.timingCleanup = co.aikar.timings.WorldTimingsHandler.getTickList(worldserver, timingsType + " - Cleanup");
        this.timingTicking = co.aikar.timings.WorldTimingsHandler.getTickList(worldserver, timingsType + " - Ticking");
    }
    private final co.aikar.timings.Timing timingCleanup; // Paper
    private final co.aikar.timings.Timing timingTicking; // Paper
    // Paper end

    public Set<NextTickListEntry<T>> getNextTickList() {
        return this.nextTickListHash;
    }

    public void doTick() {
        int i = this.nextTickList.size();

        if (false) { // CraftBukkit
            throw new IllegalStateException("TickNextTick list out of synch");
        } else {
            if (i > 65536) {
                // CraftBukkit start - If the server has too much to process over time, try to alleviate that
                if (i > 20 * 65536) {
                    i = i / 20;
                } else {
                    i = 65536;
                }
                // CraftBukkit end
            }

            ChunkProviderServer chunkproviderserver = this.f.getChunkProvider();
            Iterator<NextTickListEntry<T>> iterator = this.nextTickList.iterator();

            this.f.getMethodProfiler().enter("cleaning");

            this.timingCleanup.startTiming(); // Paper
            NextTickListEntry nextticklistentry;

            while (i > 0 && iterator.hasNext()) {
                nextticklistentry = (NextTickListEntry) iterator.next();
                if (nextticklistentry.b > this.f.getTime()) {
                    break;
                }

                if (chunkproviderserver.a(nextticklistentry.a)) {
                    iterator.remove();
                    this.nextTickListHash.remove(nextticklistentry);
                    this.g.add(nextticklistentry);
                    --i;
                }
            }
            this.timingCleanup.stopTiming(); // Paper

            this.timingTicking.startTiming(); // Paper
            this.f.getMethodProfiler().exitEnter("ticking");

            while ((nextticklistentry = (NextTickListEntry) this.g.poll()) != null) {
                if (chunkproviderserver.a(nextticklistentry.a)) {
                    try {
                        this.h.add(nextticklistentry);
                        this.i.accept(nextticklistentry);
                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.a(throwable, "Exception while ticking");
                        CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Block being ticked");

                        CrashReportSystemDetails.a(crashreportsystemdetails, nextticklistentry.a, (IBlockData) null);
                        throw new ReportedException(crashreport);
                    }
                } else {
                    this.a(nextticklistentry.a, (T) nextticklistentry.b(), 0); // CraftBukkit - decompile error
                }
            }

            this.f.getMethodProfiler().exit();
            this.timingTicking.stopTiming(); // Paper
            this.h.clear();
            this.g.clear();
        }
    }

    @Override
    public boolean b(BlockPosition blockposition, T t0) {
        return this.g.contains(new NextTickListEntry<>(blockposition, t0));
    }

    @Override
    public void a(Stream<NextTickListEntry<T>> stream) {
        stream.forEach(this::add);
    }

    public List<NextTickListEntry<T>> a(ChunkCoordIntPair chunkcoordintpair, boolean flag, boolean flag1) {
        int i = (chunkcoordintpair.x << 4) - 2;
        int j = i + 16 + 2;
        int k = (chunkcoordintpair.z << 4) - 2;
        int l = k + 16 + 2;

        return this.a(new StructureBoundingBox(i, 0, k, j, 256, l), flag, flag1);
    }

    public List<NextTickListEntry<T>> a(StructureBoundingBox structureboundingbox, boolean flag, boolean flag1) {
        List<NextTickListEntry<T>> list = this.a((List) null, this.nextTickList, structureboundingbox, flag);

        if (flag && list != null) {
            this.nextTickListHash.removeAll(list);
        }

        list = this.a(list, this.g, structureboundingbox, flag);
        if (!flag1) {
            list = this.a(list, this.h, structureboundingbox, flag);
        }

        return list == null ? Collections.emptyList() : list;
    }

    @Nullable
    private List<NextTickListEntry<T>> a(@Nullable List<NextTickListEntry<T>> list, Collection<NextTickListEntry<T>> collection, StructureBoundingBox structureboundingbox, boolean flag) {
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            NextTickListEntry<T> nextticklistentry = (NextTickListEntry) iterator.next();
            BlockPosition blockposition = nextticklistentry.a;

            if (blockposition.getX() >= structureboundingbox.a && blockposition.getX() < structureboundingbox.d && blockposition.getZ() >= structureboundingbox.c && blockposition.getZ() < structureboundingbox.f) {
                if (flag) {
                    iterator.remove();
                }

                if (list == null) {
                    list = Lists.newArrayList();
                }

                ((List) list).add(nextticklistentry);
            }
        }

        return (List) list;
    }

    public void a(StructureBoundingBox structureboundingbox, BlockPosition blockposition) {
        List<NextTickListEntry<T>> list = this.a(structureboundingbox, false, false);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            NextTickListEntry<T> nextticklistentry = (NextTickListEntry) iterator.next();

            if (structureboundingbox.b((BaseBlockPosition) nextticklistentry.a)) {
                BlockPosition blockposition1 = nextticklistentry.a.a((BaseBlockPosition) blockposition);
                T t0 = nextticklistentry.b();

                this.add(new NextTickListEntry<>(blockposition1, t0, nextticklistentry.b, nextticklistentry.c));
            }
        }

    }

    public NBTTagList a(ChunkCoordIntPair chunkcoordintpair) {
        List<NextTickListEntry<T>> list = this.a(chunkcoordintpair, false, true);

        return a(this.b, list, this.f.getTime());
    }

    public static <T> NBTTagList a(Function<T, MinecraftKey> function, Iterable<NextTickListEntry<T>> iterable, long i) {
        NBTTagList nbttaglist = new NBTTagList();
        Iterator iterator = iterable.iterator();

        while (iterator.hasNext()) {
            NextTickListEntry<T> nextticklistentry = (NextTickListEntry) iterator.next();
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            nbttagcompound.setString("i", ((MinecraftKey) function.apply(nextticklistentry.b())).toString());
            nbttagcompound.setInt("x", nextticklistentry.a.getX());
            nbttagcompound.setInt("y", nextticklistentry.a.getY());
            nbttagcompound.setInt("z", nextticklistentry.a.getZ());
            nbttagcompound.setInt("t", (int) (nextticklistentry.b - i));
            nbttagcompound.setInt("p", nextticklistentry.c.a());
            nbttaglist.add(nbttagcompound);
        }

        return nbttaglist;
    }

    @Override
    public boolean a(BlockPosition blockposition, T t0) {
        return this.nextTickListHash.contains(new NextTickListEntry<>(blockposition, t0));
    }

    @Override
    public void a(BlockPosition blockposition, T t0, int i, TickListPriority ticklistpriority) {
        if(t0 instanceof BlockBamboo)
        {
            System.out.println("Bamboo Update!");
        }

        if (!this.a.test(t0)) {
            this.add(new NextTickListEntry<>(blockposition, t0, (long) i + this.f.getTime(), ticklistpriority));
        }

    }

    public void add(NextTickListEntry<T> nextticklistentry) {
        if (!this.nextTickListHash.contains(nextticklistentry)) {
            this.nextTickListHash.add(nextticklistentry);
            this.nextTickList.add(nextticklistentry);
        }
    }
}
