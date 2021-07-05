package me.ahoo.cosid.snowflake;

import me.ahoo.cosid.snowflake.exception.ClockBackwardsException;
import me.ahoo.cosid.snowflake.exception.TimestampOverflowException;

/**
 * @author ahoo wang
 * Creation time: 2019/11/26 16:14
 */
public abstract class AbstractSnowflakeId implements SnowflakeId {

    protected final long epoch;
    protected final int timestampBit;
    protected final int machineBit;
    protected final int sequenceBit;

    protected final long maxTimestamp;
    protected final long maxSequence;
    protected final long maxMachine;

    protected final long machineLeft;
    protected final long timestampLeft;

    protected final int machineId;
    protected long sequence = 0L;
    protected long lastTimestamp = -1L;

    public AbstractSnowflakeId(long epoch, int timestampBit, int machineBit, int sequenceBit, int machineId) {
        if ((timestampBit + machineBit + sequenceBit) > TOTAL_BIT) {
            throw new IllegalArgumentException("total bit can't be greater than TOTAL_BIT[63] .");
        }
        this.epoch = epoch;
        this.timestampBit = timestampBit;
        this.machineBit = machineBit;
        this.sequenceBit = sequenceBit;
        this.maxTimestamp = -1L ^ (-1L << timestampBit);
        this.maxSequence = -1L ^ (-1L << sequenceBit);
        this.maxMachine = -1L ^ (-1L << machineBit);
        this.machineLeft = sequenceBit;
        this.timestampLeft = this.machineLeft + machineBit;
        if (machineId > this.maxMachine || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than maxMachine or less than 0 .");
        }
        this.machineId = machineId;
    }

    protected long nextTime() {
        long time = getCurrentTime();
        while (time <= lastTimestamp) {
            time = getCurrentTime();
        }
        return time;
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    protected abstract long getCurrentTime();

    @Override
    public synchronized long generate() {
        long currentTimestamp = getCurrentTime();
        if (currentTimestamp < lastTimestamp) {
            throw new ClockBackwardsException(lastTimestamp, currentTimestamp);
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0L) {
                currentTimestamp = nextTime();
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;
        long diffTimestamp = (currentTimestamp - epoch);
        if (diffTimestamp > maxTimestamp) {
            throw new TimestampOverflowException(epoch, diffTimestamp, maxTimestamp);
        }
        return diffTimestamp << timestampLeft
                | machineId << machineLeft
                | sequence;
    }

    @Override
    public long getEpoch() {
        return epoch;
    }

    @Override
    public int getTimestampBit() {
        return timestampBit;
    }

    @Override
    public int getMachineBit() {
        return machineBit;
    }

    @Override
    public int getSequenceBit() {
        return sequenceBit;
    }

    @Override
    public long getMaxTimestamp() {
        return maxTimestamp;
    }

    @Override
    public long getMaxMachine() {
        return maxMachine;
    }

    @Override
    public long getMaxSequence() {
        return maxSequence;
    }

    @Override
    public long getLastTimestamp() {
        return lastTimestamp;
    }

    @Override
    public int getMachineId() {
        return machineId;
    }

}