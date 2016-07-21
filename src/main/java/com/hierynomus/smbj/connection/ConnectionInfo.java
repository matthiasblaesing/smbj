/*
 * Copyright (C)2016 - SMBJ Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hierynomus.smbj.connection;

import com.hierynomus.protocol.commons.EnumWithValue;
import com.hierynomus.smbj.smb2.SMB2Dialect;
import com.hierynomus.smbj.smb2.messages.SMB2NegotiateResponse;

import java.io.OutputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static com.hierynomus.protocol.commons.EnumWithValue.EnumUtils.toEnumSet;

public class ConnectionInfo {
    public byte[] getGssNegotiateToken() {
        return gssNegotiateToken;
    }

    public enum GlobalCapability implements EnumWithValue<GlobalCapability> {
        SMB2_GLOBAL_CAP_DFS(0x01L),
        SMB2_GLOBAL_CAP_LEASING(0x02L),
        SMB2_GLOBAL_CAP_LARGE_MTU(0x04L), // Multi-Credit support
        SMB2_GLOBAL_CAP_MULTI_CHANNEL(0x08L),
        SMB2_GLOBAL_CAP_PERSISTENT_HANDLES(0x10L),
        SMB2_GLOBAL_CAP_DIRECTORY_LEASING(0x20L),
        SMB2_GLOBAL_CAP_ENCRYPTION(0x40L);

        private long i;

        GlobalCapability(long i) {
            this.i = i;
        }

        public long getValue() {
            return i;
        }
    }

    // All SMB2 Dialect
    private List<Void> sessionTable;
    private List<Void> preauthSessionTable;
    private OutstandingRequests outstandingRequests = new OutstandingRequests();
    private SequenceWindow sequenceWindow;
    private byte[] gssNegotiateToken;
    private int maxTransactSize;
    private int maxReadSize;
    private int maxWriteSize;
    private UUID serverGuid;
    private String serverName;
    private SMB2Dialect dialect;
    // SMB 2.1+
    private UUID clientGuid = UUID.randomUUID();
    // For SMB 2.1+ only SMB2_GLOBAL_CAP_LEASING and SMB2_GLOBAL_CAP_LARGE_MTU
    // For SMB 3.x+ all capabilities supported
    private EnumSet<GlobalCapability> clientCapabilities;
    private EnumSet<GlobalCapability> serverCapabilities;
    // SMB 3.x+
    private int clientSecurityMode;
    private int serverSecurityMode;
    private String server; // Reference to the server connected to?
    // SMB 3.1.1
    private String preauthIntegrityHashId;
    private byte[] preauthIntegrityHashValue;
    private String cipherId;


    public ConnectionInfo(UUID clientGuid, String serverName) {
        // new SessionTable
        // new OutstandingRequests
        this.clientGuid = clientGuid;
        this.sequenceWindow = new SequenceWindow();
        this.gssNegotiateToken = new byte[0];
        this.dialect = SMB2Dialect.UNKNOWN;
        this.serverName = serverName;
    }

    void negotiated(SMB2NegotiateResponse response) {
        gssNegotiateToken = response.getGssToken();
        maxTransactSize = response.getMaxTransactSize();
        maxReadSize = response.getMaxReadSize();
        maxWriteSize = response.getMaxWriteSize();
        serverGuid = response.getServerGuid();
        dialect = response.getDialect();
        serverCapabilities = toEnumSet(response.getCapabilities(), GlobalCapability.class);
        serverSecurityMode = response.getSecurityMode();
    }

    public SequenceWindow getSequenceWindow() {
        return sequenceWindow;
    }

    public UUID getClientGuid() {
        return clientGuid;
    }

    public boolean isRequireSigning() {
        return (serverSecurityMode & 0x02) > 0;
    }

    public int getMaxTransactSize() {
        return maxTransactSize;
    }

    public int getMaxReadSize() {
        return maxReadSize;
    }

    public int getMaxWriteSize() {
        return maxWriteSize;
    }

    public UUID getServerGuid() {
        return serverGuid;
    }

    public String getServerName() {
        return serverName;
    }

    public SMB2Dialect getDialect() {
        return dialect;
    }

    public OutstandingRequests getOutstandingRequests() {
        return outstandingRequests;
    }

    public boolean supports(GlobalCapability capability) {
        return serverCapabilities.contains(capability);
    }


}
