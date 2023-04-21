/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */


package org.apache.tuweni.ssz;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.apache.tuweni.units.bigints.UInt64;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Receipt implements SSZReadable, SSZWritable{

    private UInt256 status;
    private UInt256 cumulativeGasUsed;
    //is this a 256 long list of 8bit Bytes, or a Bytes of length 256?
    private List<Bytes> logsBloom;
    private int transactionType;
    private Optional<UInt64> cumulativeDataGasUsed;

    public Receipt() {
    }

    public Receipt(UInt256 status, UInt256 cumulativeGasUsed, List<Bytes> logsBloom, int transactionType, Optional<UInt64> cumulativeDataGasUsed) {
        this.status = status;
        this.cumulativeGasUsed = cumulativeGasUsed;
        if(logsBloom.size() != 256)
            throw new IllegalArgumentException("logsBloom must be 256 bytes long");
        this.logsBloom = logsBloom;
        //this.receiptLogs = receiptLogs;
        this.transactionType = transactionType;
        this.cumulativeDataGasUsed = cumulativeDataGasUsed;
    }


    @Override
    public void populateFromReader(SSZReader reader) {
        this.status = reader.readUInt256();
        this.cumulativeGasUsed = reader.readUInt256();
        this.logsBloom = reader.readFixedBytesVector(256,1);
        //this.receiptLogs = reader.readVariableSizeTypeList(ReceiptLogs.supplier());
        this.transactionType = reader.readInt8();
        this.cumulativeDataGasUsed = reader.readUInt64Optional();

    }

    @Override
    public boolean isFixed() {
        return false;
    }

    @Override
    public void writeTo(SSZWriter writer) {
        writer.writeUInt256(status);
        writer.writeUInt256(cumulativeGasUsed);
        writer.writeFixedBytesVector(logsBloom);
        //writer.writeVariableSizeTypeList(receiptLogs);
        writer.writeUInt8(this.transactionType);
        writer.writeOptionalUint64(this.cumulativeDataGasUsed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ! (o instanceof Receipt) ) return false;
        Receipt receipt = (Receipt) o;
        return transactionType == receipt.transactionType && Objects.equals(status, receipt.status) && Objects.equals(cumulativeGasUsed, receipt.cumulativeGasUsed) && Objects.equals(logsBloom, receipt.logsBloom) && Objects.equals(cumulativeDataGasUsed, receipt.cumulativeDataGasUsed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, cumulativeGasUsed, logsBloom, transactionType, cumulativeDataGasUsed);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "status=" + status +
                ", cumulativeGasUsed=" + cumulativeGasUsed +
                ", logsBloom=" + logsBloom +
                ", transactionType=" + transactionType +
                ", cumulativeDataGasUsed=" + cumulativeDataGasUsed +
                '}';
    }
}
