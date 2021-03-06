/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.crypto.transformer.bytes;

import org.apache.cayenne.crypto.unit.SwapBytesTransformer;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since 4.0
 */
public class HmacEncryptorTest {

    @Test
    public void encrypt() throws Exception {
        HmacEncryptor encryptor = mock(HmacEncryptor.class);
        encryptor.delegate = SwapBytesTransformer.encryptor();
        when(encryptor.createHmac(any(byte[].class))).thenReturn(new byte[]{0, 1, 2, 3, 4, 5, 6, 7});
        when(encryptor.encrypt(any(byte[].class), anyInt(), any(byte[].class))).thenCallRealMethod();

        byte[] input = {-1, -2, -3};

        byte[] result1 = encryptor.encrypt(input, 0, new byte[1]);
        assertArrayEquals(new byte[]{8, 0, 1, 2, 3, 4, 5, 6, 7, -3, -2, -1}, result1);

        byte[] result2 = encryptor.encrypt(input, 5, new byte[1]);
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 8, 0, 1, 2, 3, 4, 5, 6, 7, -3, -2, -1}, result2);

    }

}