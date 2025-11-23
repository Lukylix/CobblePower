/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.lukylix.cobble_power.block.blockentity.cable;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import team.reborn.energy.api.EnergyStorage;

import java.util.*;

class CableTickManager {
    private static final List<EnergyCableBlockEntity> cableList = new ArrayList<>();
    private static final List<OfferedEnergyStorage> targetStorages = new ArrayList<>();
    private static final Deque<EnergyCableBlockEntity> bfsQueue = new ArrayDeque<>();
    private static long tickCounter = 0;

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> tickCounter++);
    }

    static void handleCableTick(EnergyCableBlockEntity startingCable) {
        if (!(startingCable.getLevel() instanceof ServerLevel)) throw new IllegalStateException();

        try {
            gatherCables(startingCable);
            if (cableList.isEmpty()) return;

            long networkCapacity = 0;
            long networkAmount = 0;

            // Gather energy and targets
            for (EnergyCableBlockEntity cable : cableList) {
                networkAmount += cable.getAmount();
                networkCapacity += cable.getCapacity();

                cable.collectTargets(targetStorages);
                cable.setIoBlocked(true);
            }

            if (networkAmount > networkCapacity) {
                networkAmount = networkCapacity;
            }

            networkAmount += dispatchTransfer(startingCable.getCableType(), EnergyStorage::extract, networkCapacity - networkAmount);
            // Push energy into storages
            networkAmount -= dispatchTransfer(startingCable.getCableType(), EnergyStorage::insert, networkAmount);

            int cableCount = cableList.size();
            for (EnergyCableBlockEntity cable : cableList) {
                long assign = networkAmount / cableCount;
                cable.setAmount(assign);
                networkAmount -= assign;
                cableCount--;
                cable.setChanged();
                cable.setIoBlocked(false);
            }


        } finally {
            cableList.clear();
            targetStorages.clear();
            bfsQueue.clear();
        }
    }

    private static boolean shouldTickCable(EnergyCableBlockEntity current) {
        if (current.lastTick == tickCounter) return false;
        if (!(current.getLevel() instanceof ServerLevel serverLevel)) return false;

        // Check if the chunk containing this block is loaded
        int chunkX = current.getBlockPos().getX() >> 4;
        int chunkZ = current.getBlockPos().getZ() >> 4;
        return serverLevel.getChunkSource().hasChunk(chunkX, chunkZ);
    }


    private static void gatherCables(EnergyCableBlockEntity start) {
        if (!shouldTickCable(start)) return;

        bfsQueue.add(start);
        start.lastTick = tickCounter;
        cableList.add(start);

        while (!bfsQueue.isEmpty()) {
            EnergyCableBlockEntity current = bfsQueue.removeFirst();

            for (Direction direction : Direction.values()) {
                if (current.getAdjacentBlockEntity(direction) instanceof EnergyCableBlockEntity adjCable &&
                        current.getCableType().getTransferRate() == adjCable.getCableType().getTransferRate()) {

                    if (shouldTickCable(adjCable)) {
                        bfsQueue.add(adjCable);
                        adjCable.lastTick = tickCounter;
                        cableList.add(adjCable);
                    }
                }
            }
        }
    }

    private static long dispatchTransfer(CableType cableType, TransferOperation operation, long maxAmount) {
        List<SortableStorage> sortedTargets = new ArrayList<>();
        for (var storage : targetStorages) {
            sortedTargets.add(new SortableStorage(operation, storage));
        }

        Collections.shuffle(sortedTargets);
        sortedTargets.sort(Comparator.comparingLong(s -> s.simulationResult));

        try (Transaction transaction = Transaction.openOuter()) {
            long transferredAmount = 0;

            for (int i = 0; i < sortedTargets.size(); i++) {
                SortableStorage target = sortedTargets.get(i);
                int remainingTargets = sortedTargets.size() - i;
                long remainingAmount = maxAmount - transferredAmount;

                long targetMaxAmount = Math.min(remainingAmount / remainingTargets, cableType.getTransferRate());

                long localTransferred = operation.transfer(target.storage.storage(), targetMaxAmount, transaction);
                if (localTransferred > 0) {
                    transferredAmount += localTransferred;
                    target.storage.afterTransfer();
                }
            }

            transaction.commit();
            return transferredAmount;
        }
    }

    private interface TransferOperation {
        long transfer(EnergyStorage storage, long maxAmount, Transaction transaction);
    }

    private static class SortableStorage {
        private final OfferedEnergyStorage storage;
        private final long simulationResult;

        SortableStorage(TransferOperation operation, OfferedEnergyStorage storage) {
            this.storage = storage;
            try (Transaction tx = Transaction.openOuter()) {
                this.simulationResult = operation.transfer(storage.storage(), Long.MAX_VALUE, tx);
            }
        }
    }

    /**
     * Simple wrapper for a target EnergyStorage.
     */
    public static class OfferedEnergyStorage {
        private final EnergyCableBlockEntity source;
        private final Direction direction;
        private final EnergyStorage storage;

        public OfferedEnergyStorage(EnergyCableBlockEntity source, Direction direction, EnergyStorage storage) {
            this.source = source;
            this.direction = direction;
            this.storage = storage;
        }

        public EnergyStorage storage() {
            return storage;
        }

        public void afterTransfer() {
            // Mark this side as blocked for this tick
            source.blockSide(direction);
        }
    }
}
