package org.apache.tuweni.ssz;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.apache.tuweni.units.bigints.UInt64;

import java.util.List;
import java.util.Optional;

public class ItemizedReceipt extends Receipt{
    private List<ReceiptLog> receiptLogs;

    public ItemizedReceipt() {
    }

    public ItemizedReceipt(UInt256 status, UInt256 cumulativeGasUsed, List<Bytes> logsBloom, int transactionType, Optional<UInt64> cumulativeDataGasUsed, List<ReceiptLog> receiptLogs) {
        super(status, cumulativeGasUsed, logsBloom, transactionType, cumulativeDataGasUsed);
        this.receiptLogs = receiptLogs;
    }

    public List<ReceiptLog> getReceiptLogs() {
        return receiptLogs;
    }

    public void setReceiptLogs(List<ReceiptLog> receiptLogs) {
        this.receiptLogs = receiptLogs;
    }

    @Override
    public void populateFromReader(SSZReader reader) {
        super.populateFromReader(reader);
        this.receiptLogs = reader.readVariableSizeTypeList(ReceiptLog::new);
    }

    @Override
    public void writeTo(SSZWriter writer) {
        super.writeTo(writer);
        writer.writeVariableSizeTypeList(receiptLogs);
    }

    public class ReceiptLog implements SSZReadable, SSZWritable{
        private List<Bytes> topics;
        private Bytes data;

        public ReceiptLog() {
        }

        public ReceiptLog(List<Bytes> topics, Bytes data) {
            this.topics = topics;
            this.data = data;
        }

        public List<Bytes> getTopics() {
            return topics;
        }

        public void setTopics(List<Bytes> topics) {
            this.topics = topics;
        }

        public Bytes getData() {
            return data;
        }

        public void setData(Bytes data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ReceiptLog that = (ReceiptLog) o;

            if (!topics.equals(that.topics)) return false;
            return data.equals(that.data);
        }

        @Override
        public int hashCode() {
            int result = topics.hashCode();
            result = 31 * result + data.hashCode();
            return result;
        }

        @Override
        public void populateFromReader(SSZReader reader) {
            this.topics = reader.readFixedBytesVector(32, 1);
            this.data = reader.readBytes();
        }

        @Override
        public boolean isFixed() {
            return SSZReadable.super.isFixed();
        }

        @Override
        public void writeTo(SSZWriter writer) {

        }
    }
}
