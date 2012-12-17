/**
 * Copyright (c) 2012 keyhom.c@gmail.com.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose
 * excluding commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *     1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *     2. Altered source versions must be plainly marked as such, and must not
 *     be misrepresented as being the original software.
 *
 *     3. This notice may not be removed or altered from any source
 *     distribution.
 */
package org.vimide.core.server.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol codec implementation of Vimide's server.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimideProtocolCodecFactory implements ProtocolCodecFactory {

    /**
     * Logger
     */
    static final Logger LOGGER = LoggerFactory
            .getLogger(VimideProtocolCodecFactory.class);

    /**
     * Encoder
     */
    private ProtocolEncoder encoder = new ProtocolEncoderAdapter() {

        @Override
        public void encode(IoSession session, Object message,
                ProtocolEncoderOutput out) throws Exception {
            // if the message wasn't the illegal writing data, ignore it.
            if (message instanceof Object) {

            }
        }
    };

    /**
     * Decoder
     */
    private ProtocolDecoder decoder = new CumulativeProtocolDecoder() {

        @SuppressWarnings("unused")
        @Override
        protected boolean doDecode(IoSession session, IoBuffer in,
                ProtocolDecoderOutput out) throws Exception {
            while (in.hasRemaining()
                    && in.remaining() >= in.getInt(in.position())) {
                // remaining data to decoder.
                int packetLength = in.getInt();
                final IoBuffer buf = in.slice().limit(packetLength)
                        .order(in.order());
                in.position(packetLength + 4);

                // process the buffer with the sliced buf.
            }
            return false;
        }
    };

    /**
     * Creates an new VimideProtocolCodecFactory instance.
     */
    public VimideProtocolCodecFactory() {
        super();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.mina.filter.codec.ProtocolCodecFactory#getEncoder(org.apache.mina.core.session.IoSession)
     */
    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return encoder;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.mina.filter.codec.ProtocolCodecFactory#getDecoder(org.apache.mina.core.session.IoSession)
     */
    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return decoder;
    }

}
