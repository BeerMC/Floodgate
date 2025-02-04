/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Floodgate
 */

package org.geysermc.floodgate.addon.debug;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.geysermc.floodgate.api.logger.FloodgateLogger;

@Sharable
public final class ChannelInDebugHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final String direction;
    private final FloodgateLogger logger;

    private final boolean toServer;
    private final StateChangeDetector changeDetector;

    public ChannelInDebugHandler(
            String implementationType,
            boolean toServer,
            StateChangeDetector changeDetector,
            FloodgateLogger logger) {
        this.direction = (toServer ? "Server -> " : "Player -> ") + implementationType;
        this.logger = logger;
        this.toServer = toServer;
        this.changeDetector = changeDetector;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        try {
            int index = msg.readerIndex();

            if (changeDetector.shouldPrintPacket(msg, !toServer)) {
                logger.info("{} {}:\n{}",
                        direction,
                        changeDetector.getCurrentState(),
                        ByteBufUtil.prettyHexDump(msg)
                );

                changeDetector.checkPacket(msg, !toServer);
            }

            // reset index
            msg.readerIndex(index);

            ctx.fireChannelRead(msg.retain());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
