/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package internal.io.netty.handler.codec.compression;

import internal.io.netty.buffer.ByteBuf;
import internal.io.netty.handler.codec.ByteToMessageDecoder;

/**
 * Decompresses a {@link ByteBuf} using the deflate algorithm.
 */
public abstract class ZlibDecoder extends ByteToMessageDecoder {

    /**
     * Returns {@code true} if and only if the end of the compressed stream
     * has been reached.
     */
    public abstract boolean isClosed();
}
